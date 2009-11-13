package edu.jhu.cs.asharm24;

import java.io.File;

import opennlp.tools.tokenize.SimpleTokenizer;

import org.w3c.dom.*;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class HTML2Treebank {
	public static void main(String[] args) throws Exception {
		if (args.length > 0)
			walk(args[0]);
	}
	
	public static void walk(String root) {
		File file = new File(root);
		if (file.isFile())
			try {
				processFile(file.getAbsolutePath());
			} catch (Exception ex) {
				System.err.println("Skipping file " + file.getAbsolutePath());
			}
		else {
			File[] files = file.listFiles();
			for (File file2 : files)
				walk(file2.getAbsolutePath());
		}
	}

	public static void processFile(String filename) throws Exception {
		System.err.println("Processing file " + filename);
		DOMParser parser = new DOMParser();
		parser.parse(filename);
		Document doc = parser.getDocument();
		System.out.println(visit(doc.getFirstChild(), 0));
	}

	public static String ptext(String text) {
		SimpleTokenizer tokenizer = new SimpleTokenizer();

		String tokens[] = tokenizer.tokenize(text);
		StringBuilder builder = new StringBuilder();
		for (String str : tokens) {
			if (str.equals("("))
				str = "RIGHT_ROUND";
			else if (str.equals(")"))
				str = "LEFT_ROUND";
			if (str.length() == 0)
				builder.append("(WSEQ " + "EMPTY" + ")");
			else if (str.trim().length() == 0)
				builder.append("(WSEQ " + "SPACE" + ")");
			else
				builder.append("(WSEQ " + str + ")");
		}

		return builder.toString();
	}

	public static String visit(Node node, int level) {
		// Process node
		if (node.getNodeType() == Node.TEXT_NODE)
			if (node.getNodeValue().trim().length() > 0)
				return ptext(node.getNodeValue().trim());
			else
				return "";
		else {
			// If there are any children, visit each one
			NodeList list = node.getChildNodes();
			StringBuilder builder = new StringBuilder();
			builder.append("(" + node.getNodeName().trim().toUpperCase());
			for (int i = 0; i < list.getLength(); i++) {
				// Get child node
				Node childNode = list.item(i);

				// Visit child node
				builder.append(visit(childNode, level + 1).trim());
			}
			builder.append(")");
			return builder.toString();
		}
	}
}
