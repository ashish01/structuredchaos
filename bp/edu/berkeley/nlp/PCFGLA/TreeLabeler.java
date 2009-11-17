/**
 * 
 */
package edu.berkeley.nlp.PCFGLA;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.syntax.StateSet;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;
import edu.berkeley.nlp.util.Numberer;

/**
 * @author petrov
 *
 */
public class TreeLabeler {

	public static class Options {

		@Option(name = "-gr", usage = "Input File for Grammar (Required)\n")
		public String inFileName;

		@Option(name = "-labelLevel", usage = "Parse with projected grammar from this level (yielding 2^level substates) (Default: -1 = input grammar)")
		public int labelLevel = -1;

		@Option(name = "-scores", usage = "Output inside scores. (Default: false)")
		public boolean scores;

		@Option(name = "-getyield", usage = "Get the sentences only")
		public boolean getyield;
		
		@Option(name = "-maxLength", usage = "Remove sentences that are longer than this (doesn't print an empty line)")
		public int maxLength = 1000;		

		@Option(name = "-inputFile", usage = "Read input from this file instead of reading it from STDIN.")
		public String inputFile;

		@Option(name = "-outputFile", usage = "Store output in this file instead of printing it to STDOUT.")
		public String outputFile;
	}
	
	
	/**
	 * @param grammar
	 * @param lexicon
	 * @param labelLevel
	 */
	Grammar grammar;
	SophisticatedLexicon lexicon;
	ArrayParser labeler;
	CoarseToFineMaxRuleParser parser;
	Numberer tagNumberer;
	Binarization binarization;
	
	public TreeLabeler(Grammar grammar, SophisticatedLexicon lexicon, int labelLevel, Binarization bin) {
		if (labelLevel==-1){
			this.grammar = grammar.copyGrammar(false);
			this.lexicon = lexicon.copyLexicon();
		} else { // need to project
			int[][] fromMapping = grammar.computeMapping(1);
	    int[][] toSubstateMapping = grammar.computeSubstateMapping(labelLevel);
	    int[][] toMapping = grammar.computeToMapping(labelLevel,toSubstateMapping);
    	double[] condProbs = grammar.computeConditionalProbabilities(fromMapping,toMapping);
    	
    	this.grammar = grammar.projectGrammar(condProbs,fromMapping,toSubstateMapping);
    	this.lexicon = lexicon.projectLexicon(condProbs,fromMapping,toSubstateMapping);
    	this.grammar.splitRules();
    	double filter = 1.0e-10;
  		this.grammar.removeUnlikelyRules(filter,1.0);
  		this.lexicon.removeUnlikelyTags(filter,1.0);
		}
		this.grammar.logarithmMode();
		this.lexicon.logarithmMode();
		this.labeler = new ArrayParser(this.grammar, this.lexicon);
		this.parser = new CoarseToFineMaxRuleParser(grammar, lexicon, 
    		1,-1,true,false, false, false, false, false, true);      
    this.tagNumberer = Numberer.getGlobalNumberer("tags");
    this.binarization = bin;
	}


	public static void main(String[] args) {
		OptionParser optParser = new OptionParser(Options.class);
		Options opts = (Options) optParser.parse(args, true);
		// provide feedback on command-line arguments
		System.err.println("Calling with " + optParser.getPassedInOptions());

    
    String inFileName = opts.inFileName;
    Grammar grammar = null;
    SophisticatedLexicon lexicon = null;
    TreeLabeler treeLabeler = null;
    boolean labelTree = false;
    ParserData pData = null;
    short[] numSubstates = null;
    if (inFileName==null) {
    	System.err.println("Did not provide a grammar.");
    }
    else {
    	labelTree = true;
    	System.err.println("Loading grammar from "+inFileName+".");

	    pData = ParserData.Load(inFileName);
	    if (pData==null) {
	      System.out.println("Failed to load grammar from file"+inFileName+".");
	      System.exit(1);
	    }
	    grammar = pData.getGrammar();
	    grammar.splitRules();
	    lexicon = (SophisticatedLexicon)pData.getLexicon();
	    
	    Numberer.setNumberers(pData.getNumbs());
	    
	    int labelLevel = opts.labelLevel;
	    if (labelLevel!=-1) System.err.println("Labeling with projected grammar from level "+labelLevel+".");
	    treeLabeler = new TreeLabeler(grammar, lexicon, labelLevel, pData.bin);
	    numSubstates = treeLabeler.grammar.numSubStates;

    }
    Numberer tagNumberer =  Numberer.getGlobalNumberer("tags");
    
    Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
    try{
    	InputStreamReader inputData = (opts.inputFile==null) ? new InputStreamReader(System.in) : new InputStreamReader(new FileInputStream(opts.inputFile), "UTF-8");
    	PennTreeReader treeReader = new PennTreeReader(inputData);
    	PrintWriter outputData = (opts.outputFile==null) ? new PrintWriter(new OutputStreamWriter(System.out)) : new PrintWriter(new OutputStreamWriter(new FileOutputStream(opts.outputFile), "UTF-8"), true);

    	Tree<String> tree = null;
    	while(treeReader.hasNext()){
    		tree = treeReader.next(); 
    		if (tree.getYield().get(0).equals("")){ // empty tree -> parse failure
    			outputData.write("()\n");
    			continue;
    		}
    		if (tree.getYield().size() > opts.maxLength) continue;
    		
    		if (!labelTree){
    			if (opts.getyield){
    				List<String> words = tree.getYield();
    				for (String word : words){
    					outputData.write(word+" ");
    				}
    				outputData.write("\n");
    			}
    			else {
    				Tree<String> normalizedTree = treeTransformer.transformTree(tree);
            outputData.write(normalizedTree+"\n");
    			}
          continue;
        }

    		
    		tree = TreeAnnotations.processTree(tree,pData.v_markov, pData.h_markov,pData.bin,false);
    		List<String> sentence = tree.getYield();
    		Tree<StateSet> stateSetTree = StateSetTreeList.stringTreeToStatesetTree(tree, numSubstates, false, tagNumberer);
    		allocate(stateSetTree);
    		Tree<String> labeledTree = treeLabeler.label(stateSetTree, sentence, opts.scores);
    		if (labeledTree!=null && labeledTree.getChildren().size()>0) outputData.write(labeledTree.getChildren().get(0)+"\n");
    		else outputData.write("()\n");
    		outputData.flush();
    	 }
    	outputData.close();
    }catch (Exception ex) {
      ex.printStackTrace();
    }
    System.exit(0);
	}


	/**
	 * @param stateSetTree
	 * @return
	 */
	private Tree<String> label(Tree<StateSet> stateSetTree, List<String> sentence, boolean outputScores) {
		Tree<String> tree = labeler.getBestViterbiDerivation(stateSetTree,outputScores);
//		if (tree==null){ // max-rule tree had no viterbi derivation
//			tree = parser.getBestConstrainedParse(sentence, null);
//			tree = TreeAnnotations.processTree(tree,1, 0, binarization,false);
////			System.out.println(tree);
//			stateSetTree = StateSetTreeList.stringTreeToStatesetTree(tree, this.grammar.numSubStates, false, tagNumberer);
//			allocate(stateSetTree);
//			tree = labeler.getBestViterbiDerivation(stateSetTree,outputScores);
//		}
		return tree;
	}

	/*
   * Allocate the inside and outside score arrays for the whole tree
   */
  static void allocate(Tree<StateSet> tree) {
    tree.getLabel().allocate();
    for (Tree<StateSet> child : tree.getChildren()) {
      allocate(child);
    }
  }
	
}
