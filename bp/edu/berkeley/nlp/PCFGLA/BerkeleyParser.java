package edu.berkeley.nlp.PCFGLA;

import edu.berkeley.nlp.PCFGLA.GrammarTrainer.Options;
import edu.berkeley.nlp.PCFGLA.smoothing.SmoothAcrossParentBits;
import edu.berkeley.nlp.PCFGLA.smoothing.SmoothAcrossParentSubstate;
import edu.berkeley.nlp.PCFGLA.smoothing.Smoother;
import edu.berkeley.nlp.io.PTBLineLexer;
import edu.berkeley.nlp.io.PTBTokenizer;
import edu.berkeley.nlp.io.PTBLexer;
import edu.berkeley.nlp.syntax.StateSet;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.TreeJPanel;
import edu.berkeley.nlp.util.CommandLineUtils;
import edu.berkeley.nlp.util.Numberer;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.*;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * Reads in the Penn Treebank and generates N_GRAMMARS different grammars.
 *
 * @author Slav Petrov
 */
public class BerkeleyParser  {
	static TreeJPanel tjp;
	static JFrame frame;
	
	public static class Options {

		@Option(name = "-gr", required = true, usage = "Grammarfile (Required)\n")
		public String grFileName;

		@Option(name = "-tokenize", usage = "Tokenize input first. (Default: false=text is already tokenized)")
		public boolean tokenize;
		
		@Option(name = "-viterbi", usage = "Compute viterbi derivation instead of max-rule tree (Default: max-rule)")
		public boolean viterbi;

		@Option(name = "-binarize", usage = "Output binarized trees. (Default: false)")
		public boolean binarize;

		@Option(name = "-scores", usage = "Output inside scores (only for binarized viterbi trees). (Default: false)")
		public boolean scores;

		@Option(name = "-substates", usage = "Output subcategories (only for binarized viterbi trees). (Default: false)")
		public boolean substates;

		@Option(name = "-accurate", usage = "Set thresholds for accuracy. (Default: set thresholds for efficiency)")
		public boolean accurate;

		@Option(name = "-confidence", usage = "Output confidence measure, i.e. tree likelihood: P(T|w) (Default: false)")
		public boolean confidence;

		@Option(name = "-likelihood", usage = "Output sentence likelihood, i.e. summing out all parse trees: P(w) (Default: false)")
		public boolean likelihood;

		@Option(name = "-render", usage = "Write rendered tree to image file. (Default: false)")
		public boolean render;
		
		@Option(name = "-chinese", usage = "Enable some Chinese specific features in the lexicon.")
		public boolean chinese;

		@Option(name = "-inputFile", usage = "Read input from this file instead of reading it from STDIN.")
		public String inputFile;

		@Option(name = "-maxLength", usage = "Maximum sentence length (Default = 200).")
		public int maxLength = 200;

		@Option(name = "-nThreads", usage = "Parse in parallel using n threads (Default: 1).")
		public int nThreads = 1;

		@Option(name = "-kbest", usage = "Output the k best parse max-rule trees (Default: 1).")
		public int kbest = 1;

		@Option(name = "-outputFile", usage = "Store output in this file instead of printing it to STDOUT.")
		public String outputFile;
	
		@Option(name = "-useGoldPOS", usage = "Read data in CoNLL format, including gold part of speech tags.")
		public boolean goldPOS;
	}
	
  @SuppressWarnings("unchecked")
	public static void main(String[] args) {
		OptionParser optParser = new OptionParser(Options.class);
		Options opts = (Options) optParser.parse(args, true);

    double threshold = 1.0;
    
    String inFileName = opts.grFileName;
    ParserData pData = ParserData.Load(inFileName);
    if (pData==null) {
      System.out.println("Failed to load grammar from file"+inFileName+".");
      System.exit(1);
    }
    Grammar grammar = pData.getGrammar();
    Lexicon lexicon = pData.getLexicon();
    Numberer.setNumberers(pData.getNumbs());
    
    if (opts.chinese) Corpus.myTreebank = Corpus.TreeBankType.CHINESE;
    
    CoarseToFineMaxRuleParser parser = null;
    if (opts.kbest==1) parser = new CoarseToFineMaxRuleParser(grammar, lexicon, threshold,-1,opts.viterbi,opts.substates,opts.scores, opts.accurate, false, true, true);
    else parser = new CoarseToFineNBestParser(grammar, lexicon, opts.kbest,threshold,-1,opts.viterbi,opts.substates,opts.scores, opts.accurate, false, false, true);
    parser.binarization = pData.getBinarization();
    
    if (opts.render) tjp = new TreeJPanel();
    
    MultiThreadedParserWrapper m_parser = null;
    if (opts.nThreads > 1){
	  	System.err.println("Parsing with "+opts.nThreads+" threads in parallel.");
	  	m_parser = new MultiThreadedParserWrapper(parser, opts.nThreads);
		}
    
    try{
    	BufferedReader inputData = (opts.inputFile==null) ? new BufferedReader(new InputStreamReader(System.in)) : new BufferedReader(new InputStreamReader(new FileInputStream(opts.inputFile), "UTF-8"));
    	PrintWriter outputData = (opts.outputFile==null) ? new PrintWriter(new OutputStreamWriter(System.out)) : new PrintWriter(new OutputStreamWriter(new FileOutputStream(opts.outputFile), "UTF-8"), true);
    	PTBLineLexer tokenizer = null;
    	if (opts.tokenize) tokenizer = new PTBLineLexer();

    	String line = "";
    	while((line=inputData.readLine()) != null){
      	List<String> sentence = null;
      	List<String> posTags = null;
    		
    		if (opts.goldPOS){
    			sentence = new ArrayList<String>();
    			posTags = new ArrayList<String>();
  				List<String> tmp = Arrays.asList(line.split("\t"));
  				if (tmp.size()==0) continue;
//  				System.out.println(line+tmp);
  				sentence.add(tmp.get(0));
  				String[] tags = tmp.get(1).split("-");
  				posTags.add(tags[0]);
    			while(!(line=inputData.readLine()).equals("")){
    				tmp = Arrays.asList(line.split("\t"));
    				if (tmp.size()==0) break;
//    				System.out.println(line+tmp);
    				sentence.add(tmp.get(0));
    				tags = tmp.get(1).split("-");
    				posTags.add(tags[0]);
    			}
    		} else {
	    		if (!opts.tokenize) sentence = Arrays.asList(line.split(" "));
	    		else sentence = tokenizer.tokenizeLine(line);
    		}
    		
//    		if (sentence.size()==0) { outputData.write("\n"); continue; }//break;
    		if (sentence.size()>=opts.maxLength) { 
    			outputData.write("(())\n");
    			if (opts.kbest > 1){ outputData.write("\n"); }
    			System.err.println("Skipping sentence with "+sentence.size()+" words since it is too long.");
    			continue; 
    		}
    		
    		if (opts.nThreads > 1){
          m_parser.parseThisSentence(sentence);
          while (m_parser.hasNext()){
          	List<Tree<String>> parsedTrees = m_parser.getNext();
      			outputTrees(parsedTrees, outputData, parser, opts);
          }
    		} else {
    			List<Tree<String>> parsedTrees = null;
    			if (opts.kbest > 1){
    				parsedTrees = parser.getKBestConstrainedParses(sentence, posTags, opts.kbest);
    			} else {
    	  		parsedTrees = new ArrayList<Tree<String>>();
    	  		Tree<String> parsedTree = parser.getBestConstrainedParse(sentence,posTags,null);
    	  		if (opts.goldPOS && parsedTree.getChildren().isEmpty()){ // parse error when using goldPOS, try without
  	    			parsedTree = parser.getBestConstrainedParse(sentence,null,null);
  	    		}

    	  		parsedTrees.add(parsedTree);
    			}
    			outputTrees(parsedTrees, outputData, parser, opts);
    			if (opts.render)		writeTreeToImage(parsedTrees.get(0),line.replaceAll("[^a-zA-Z]", "")+".png");
    		}
    	}
  		if (opts.nThreads > 1){
  			while(!m_parser.isDone()) {
	  			while (m_parser.hasNext()){
	        	List<Tree<String>> parsedTrees = m_parser.getNext();
	    			outputTrees(parsedTrees, outputData, parser, opts);
	        }
  			}
  		}
  		outputData.flush();
  		outputData.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.exit(0);
  }

  
  
  /**
	 * @param parsedTree
	 * @param outputData
	 * @param opts
	 */
	private static void outputTrees(List<Tree<String>> parseTrees, PrintWriter outputData, 
			CoarseToFineMaxRuleParser parser, edu.berkeley.nlp.PCFGLA.BerkeleyParser.Options opts) {
		for (Tree<String> parsedTree : parseTrees){
			if (opts.likelihood){
				double allLL = (parsedTree.getChildren().isEmpty()) ? Double.NEGATIVE_INFINITY : parser.getLogLikelihood();
				outputData.write(allLL+"\n");
//				continue;
			}
			if (!opts.binarize) parsedTree = TreeAnnotations.unAnnotateTree(parsedTree);
			if (opts.confidence) {
				double treeLL = (parsedTree.getChildren().isEmpty()) ? Double.NEGATIVE_INFINITY : parser.getLogLikelihood(parsedTree);
				outputData.write(treeLL+"\t");
			}
			if (!parsedTree.getChildren().isEmpty()) { 
	       			if (true) outputData.write("( "+parsedTree.getChildren().get(0)+" )\n");
//	       			else outputData.write(parsedTree.getChildren().get(0)+"\n\n");
	    } else {
	    	if (true) outputData.write("(())\n");
//	    	else outputData.write("()\n\n");
	    }
		}
		if (opts.kbest > 1) outputData.write("\n");
	}



	public static void writeTreeToImage(Tree<String> tree, String fileName) throws IOException{
  	tjp.setTree(tree);
    
    BufferedImage bi =new BufferedImage(tjp.width(),tjp.height(),BufferedImage.TYPE_INT_ARGB);
    int t=tjp.height();
    Graphics2D g2 = bi.createGraphics();
    
    
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));
    Rectangle2D.Double rect = new Rectangle2D.Double(0,0,tjp.width(),tjp.height()); 
    g2.fill(rect);
    
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    
    tjp.paintComponent(g2); //paint the graphic to the offscreen image
    g2.dispose();
    
    ImageIO.write(bi,"png",new File(fileName)); //save as png format DONE!
  }

}

