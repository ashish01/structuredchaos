package edu.jhu.cs.asharm24;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees.PennTreeReader;

public class RenderHTML {
	static Map<String, String> colormap = new HashMap<String, String>(); 
	static Random random = new Random();

	public static void main(String[] args) {
		StringReader reader = new StringReader("(HTML-0 (HEAD-0 (TITLE-0 (WSEQ-0 Harlem) (WSEQ-0 Nights))) (BODY-0 (H1-0 (WSEQ-0 Harlem) (WSEQ-0 Nights)) (TABLE-0 (@TABLE-0 (@TABLE-1 (@TABLE-2 (TR-1 (TD-3 (WSEQ-3 Release) (WSEQ-3 Year)) (TD-2 (WSEQ-2 1989))) (TR-1 (TD-3 (WSEQ-3 Play) (WSEQ-3 Duration)) (TD-2 (WSEQ-2 6900)))) (TR-0 (TD-2 (WSEQ-2 Genres)) (TD-1 (@TD-3 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-3 (WSEQ-2 Action) (WSEQ-1 &)) (WSEQ-1 Adventure)) (WSEQ-1 ,)) (WSEQ-1 Action)) (WSEQ-1 Comedies)) (WSEQ-1 ,)) (WSEQ-1 Crime)) (WSEQ-1 Action)) (WSEQ-1 ,)) (WSEQ-1 Period)) (WSEQ-1 Pieces)) (WSEQ-1 ,)) (WSEQ-1 20)) (WSEQ-1 th)) (WSEQ-1 Century)) (WSEQ-1 Period)) (WSEQ-1 Pieces)) (WSEQ-1 ,)) (WSEQ-1 Paramount)) (WSEQ-1 Home)) (WSEQ-0 Entertainment)))) (TR-0 (TD-2 (WSEQ-2 Cast)) (TD-1 (@TD-3 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-3 (WSEQ-2 Eddie) (WSEQ-1 Murphy)) (WSEQ-1 ,)) (WSEQ-1 Richard)) (WSEQ-1 Pryor)) (WSEQ-1 ,)) (WSEQ-1 Redd)) (WSEQ-1 Foxx)) (WSEQ-1 ,)) (WSEQ-1 Danny)) (WSEQ-1 Aiello)) (WSEQ-1 ,)) (WSEQ-1 Michael)) (WSEQ-1 Lerner)) (WSEQ-1 ,)) (WSEQ-1 Della)) (WSEQ-1 Reese)) (WSEQ-1 ,)) (WSEQ-1 Berlinda)) (WSEQ-1 Tolbert)) (WSEQ-1 ,)) (WSEQ-1 Stan)) (WSEQ-1 Shaw)) (WSEQ-1 ,)) (WSEQ-1 Jasmine)) (WSEQ-1 Guy)) (WSEQ-1 ,)) (WSEQ-1 Vic)) (WSEQ-1 Polizos)) (WSEQ-1 ,)) (WSEQ-1 Lela)) (WSEQ-1 Rochon)) (WSEQ-1 ,)) (WSEQ-1 David)) (WSEQ-1 Marciano)) (WSEQ-1 ,)) (WSEQ-1 Arsenio)) (WSEQ-0 Hall)))) (TR-0 (TD-2 (WSEQ-2 Director)) (TD-1 (WSEQ-1 Eddie) (WSEQ-0 Murphy))))))");
		PennTreeReader treeReader = new PennTreeReader(reader);
		Tree<String> tree = treeReader.next();

		//renderDot(tree);
		System.out.println(renderHTML(tree));
	}
	
	public static void renderDot(Tree<String> root) {
		if (root.isPreTerminal())
			System.out.format("%s [label=\"%s\"];\n", root.hashCode(), root.getChildren().get(0).getLabel());
		else {
			System.out.format("%s [label=\"%s\"];\n", root.hashCode(), root.getLabel());
			for (Tree<String> child : root.getChildren()) {
				System.out.format("%s [label=\"%s\"];\n", child.hashCode(), child.getLabel());
				System.out.format("%s -> %s;\n", root.hashCode(), child.hashCode());
				renderDot(child);
			}
		}
	}
	
	public static String renderHTML(Tree<String> root) {
		//System.out.println(root.getLabel());
		//System.out.println(root.getChildren().size());
		if (root.isPreTerminal()) {
			return closeHTML(root, root.getChildren().get(0).getLabel());
		} else {
			StringBuilder builder = new StringBuilder();
			for (Tree<String> child : root.getChildren())
				builder.append(renderHTML(child));
			String insideHTML = builder.toString();
			return closeHTML(root, insideHTML);
		}
	}
	
	public static String closeHTML(Tree<String> nonterminal, String text) {
		String htmlTag = getHTMLTag(nonterminal.getLabel());
		return String.format("<%s title=\"%s\" style=\"background-color:%s;padding:2px;margin:2px\">%s</%s>", 
				htmlTag, 
				nonterminal.getLabel(), 
				getColor(nonterminal.getLabel()),
				text,
				htmlTag);
	}
	
	public static String getColor(String label) {
		if (!colormap.containsKey(label))
			colormap.put(label, String.format("#%H%H%H", random.nextInt(256),random.nextInt(256),random.nextInt(256)));
		return colormap.get(label);
	}
	
	public static String getHTMLTag(String text) {
		if (text.startsWith("@"))
			return "font";
		else {
			String[] tokens = text.split("-");
			if (tokens[0].equalsIgnoreCase("wseq"))
				return "font";
			else
				return tokens[0];
		}
	}
}
