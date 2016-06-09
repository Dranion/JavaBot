import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.jgrapht.EdgeFactory;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.DOTImporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.EdgeProvider;
import org.jgrapht.ext.ImportException;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.ext.VertexProvider;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.traverse.ClosestFirstIterator;


/**
 * 
 */

/**
 * @author s-thompsonk
 * 
 * This program uses GRAPH data structures wherein a word is a "point" 
 * and the connects are the "lines", in order to simulate the experience
 * of a learning web of "neurons". Talking to this program should increase
 * its ability to speak back to you. 
 * 
 * Please do not delete any text files saved, as this will reset
 * the program's learning. 
 *
 */
public class index {

	static boolean justStrings = true; //trying some stuff....
	static String filename = "dic.dot";
	VertexNameProvider<String> namer = new IntegerNameProvider<String>();
	VertexNameProvider<String> labeler = new StringNameProvider<String>();
	EdgeNameProvider<DefaultWeightedEdge> edgen = new IntegerEdgeNameProvider<DefaultWeightedEdge>();


	/**
	 * returns: void
	 * @param <V>
	 * @param <E>
	 * @param args
	 * @throws FileNotFoundException 
	 *
	 */
	@SuppressWarnings("rawtypes")
	public static DefaultDirectedWeightedGraph dic;
	@SuppressWarnings({ "rawtypes", "unchecked"})
	public static void main(String[] args) throws IOException {
		System.out.println("WELCOME USER");
		if(new File(filename).isFile()){ //used to be new File("dictionary.txt").isFile()
			try {
				load();
			} catch (ImportException e) {
				System.out.println("Error loading graph: ");
				e.printStackTrace();
				System.out.println("Creating new... ");
				//Load an empty Weighted Directional Graph :<
				EdgeFactory<String, DefaultWeightedEdge> nl = new ClassBasedEdgeFactory<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
				dic = new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(nl);
				System.out.println("Creating words...");
				createString();
				System.out.println("Done! Creating weight....");
				createWeight(); //creates connections based on n-grams
				System.out.println("Okay! Ready to talk");
			}
		}
		else{
			makeGraph();
		}
		Scanner input = new Scanner(System.in);
		System.out.println("Nice to meet you! If you want to stop talking, say quit");
		String answer = "";
		while(answer != "quit"){
			//System.out.println("In WHILE");
			answer = input.nextLine();
			//System.out.println("Recieved input: \"" + answer + "\"");
			String str = " "; 
			//System.out.println("Before IF");
			if(justStrings){
				//System.out.println("After IF");
				String[] words = answer.split(" ");
				for(int i = 0; i < words.length; i++){
					words[i] = words[i].toLowerCase().trim();
				}
				//System.out.println("Generating connections");
				generateConnections(words);
				for(int i = 0; i < words.length; i++){
					//System.out.println("For loop i (" + i + ") for words length " + words.length);
					ClosestFirstIterator gen = new ClosestFirstIterator(dic, words[i]);
					//System.out.println("gen ready");
					for(int j = (int) (Math.random() * 4) + 1; j > 0; j--){ //amount of words per word inputted
						//System.out.println("For loop j");
					if(dic.containsVertex(words[i])){
						String add = "";
						int rand = (int) (Math.random() * 10);
						while(rand > 0 && gen.hasNext()){ //getting randomly closet one within 10 
							add = ((String) gen.next()).trim();
							rand--;
						}
						str += add + " ";
						//System.out.println("Added " + add);
					}
					else {
						//System.out.println("Didn't contain");
					}
					}
				}
				System.out.println(str);
			}
			else {
				//im... honestly not sure how to handle this yet -_-
			}
		}
		System.out.println("goodbye!");
		save();
		input.close();
	}

	public static void makeGraph(){
		//Load an empty Weighted Directional Graph :<
		EdgeFactory<String, DefaultWeightedEdge> nl = new ClassBasedEdgeFactory<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		dic = new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(nl);
		System.out.println("Creating words...");
		try {
			createString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done! Creating weight....");
		try {
			createWeight();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //creates connections based on n-grams
		System.out.println("Done! Saving...");
		try {
			save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Okay! Ready to talk");
	}
	/**
	 * Generates connections between every word in the array, including creating vertexes for unlisted items.
	 * returns: void
	 * @param <E>
	 * @param words2
	 *
	 */
	@SuppressWarnings("unchecked")
	private static <E> void generateConnections(String[] words) {
		for(int i = 0; i < words.length; i++){
			if(!dic.containsVertex(words[i])){
				System.out.println("Added vertex: " + words[i]);
				dic.addVertex(words[i].toLowerCase().trim());
			}
		}
		for(int i = 0; i < words.length; i++){
			for(int j = i + 1; j < words.length; j++){
				dic.addEdge(words[i].toLowerCase().trim(), words[j].toLowerCase().trim());
				Object edge = dic.getEdge(words[i].toLowerCase().trim(), words[j].toLowerCase().trim());
				dic.setEdgeWeight(edge, (dic.getEdgeWeight(edge) + 10));
			}
		}
	}

	/**
	 * This exports the graph in a format that can later be imported so that data gathered can last
	 * across runs of the program. 
	 * 
	 * EXAMPLE FOR VERTICES:
	 *   1 [ label="the" ];
	 *   
	 * EXAMPLE OF EDGES: 
	 *   27 -> 354 [ weight="1146.0" ];
	 * returns: void
	 * @param dic
	 * @throws IOException
	 *
	 */
	@SuppressWarnings("unchecked")
	public static <E, V> void save() throws IOException{   

		 IntegerNameProvider<String> p1=new IntegerNameProvider<String>(); //provides the 1
		    StringNameProvider<String> p2=new StringNameProvider<String>(); //provides the [ label="the"]
		    ComponentAttributeProvider<DefaultWeightedEdge> p4 = //provides all of the edge
		       new ComponentAttributeProvider<DefaultWeightedEdge>() {
		            public Map<String, String> getComponentAttributes(DefaultWeightedEdge e) {
		                Map<String, String> map =new LinkedHashMap<String, String>();
		                map.put("weight", Double.toString(dic.getEdgeWeight(e)));
		                return map;
		            }
		       };
		    DOTExporter<String, DefaultWeightedEdge> export = new DOTExporter<String, DefaultWeightedEdge>(p1, p2, null, null, p4);
		    try {
		        export.export(new FileWriter(filename), dic);
		    }catch (IOException e){}
	}

	/**
	 * returns: void
	 * @param dic
	 * @throws IOException
	 * @throws ImportException
	 *
	 */
	@SuppressWarnings("unchecked")
	public static void load() throws IOException, ImportException{
		VertexProvider<String> vertexProvide = 
				new VertexProvider<String>(){
			public String buildVertex(String label, Map<String,String> attributes){
				return label; //The saver doesn't even use attributes for Vertices, so just return the label. 
			}
		};
		
		EdgeProvider<String,DefaultWeightedEdge> edgeProvide =
				new EdgeProvider<String,DefaultWeightedEdge>() {

					@Override
					public DefaultWeightedEdge buildEdge(String from,
							String to, String label,
							Map<String, String> attributes) {
						// TODO Auto-generated method stub
						dic.addEdge(from, to);
						dic.setEdgeWeight(dic.getEdge(from, to), Double.parseDouble(attributes.get("weight")));
						return (DefaultWeightedEdge) dic.getEdge(from,to);
						
					}
		};

		System.out.println("Creating importer");
		DOTImporter<String, DefaultWeightedEdge> load = new DOTImporter<String, DefaultWeightedEdge>(vertexProvide, edgeProvide); 
		String reader = new String(Files.readAllBytes(Paths.get(filename)));
		System.out.println("Reading file");
		load.read(reader, dic); //tbh its going to throw it.... don't do it.... //GOSH DANG IT IT THREW IT 

	}

	/**
	 * Uses the 2 n-gram from http://www.ngrams.info/ to create artificial edge weight, for some semblance of communication 
	 * with a new program. 
	 * returns: void
	 * @param dic
	 * @throws FileNotFoundException
	 *
	 */
	@SuppressWarnings("unchecked")
	public static void createWeight() throws FileNotFoundException{
		//System.out.println("In Program");
		File file = new File("corpus.txt");
		//System.out.println("Loaded file");
		Scanner data = new Scanner(file);
		while(data.hasNextLine()){
			String[] info = data.nextLine().split("\t"); //the data is split by tabs so its easy to segment
			int weight = Integer.parseInt(info[0]);
			info[0] = null; //clearing it since i stored it elsewhere, since this data needed conversion
			//Object v = info[1]; //Suppressing the data warnings so I can easily modify it if something comes up
			//Object v2 = info[2];

			if(!dic.containsVertex(info[1])){
				if(justStrings){
					dic.addVertex(info[1]);
				}
			}
			if(!dic.containsVertex(info[2])){
				if(justStrings){
					dic.addVertex(info[2]);
				}
			}
			dic.addEdge(info[1], info[2]); //Only adds an edge if there isn't already one. 
			dic.setEdgeWeight(dic.getEdge(info[1], info[2]), (double) weight); //Sets the weight!	
		}
		data.close(); //unneeded now, so close it. 
		System.out.println("Finished");
	}



	/**
	 *  * This uses the free 5,000 word frequency-sorted corpus from http://www.wordfrequency.info/
	 * to create 5,000 words that are commonly used. Good for, like, life and stuff. 
	 * 
	 * Creates them as Strings. 
	 * 
	 * Thank you to that site for providing such a useful and large list for free <3
	 * 
	 * returns: void
	 * @throws FileNotFoundException
	 *
	 */
	@SuppressWarnings("unchecked")
	public static void createString() throws FileNotFoundException{
		File file = new File("word-data.csv");
		Scanner data = new Scanner(file);
		while(data.hasNextLine()){
			java.lang.String str = data.nextLine().toLowerCase().trim();
			dic.addVertex((str.substring(3,  str.length()-2)).toLowerCase().trim());
		}
		data.close();
	}
}
