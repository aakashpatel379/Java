import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * @author Aakash Patel - B00807065
 */
class HuffmanEncodeDecode implements FileCompressor{
  
	static HashMap<String, String> codeMap=new HashMap<String, String>();    //code map for codes generated by encoding
	static String outputString="";											 //output string to be written in output file
	
	/**
	 * Method to encode content in file as per constraints provided
	 */
	@Override
	public boolean encode(String input_filename, int level, boolean reset, String output_filename) {
		
		outputString+=Integer.toString(level);
		if(reset)
		{
			outputString+=Integer.toString(1);
		}
		else
		{
			outputString+=Integer.toString(0);
		}
		Scanner in=Huffman.in;
		String content="";
		HashMap<Integer, String> substringMap=null;
		
		while(in.hasNext()) {
			content+=in.nextLine();
		}

		Node fNode=new Node();
		
		if(level!=0)														//Manages logic for normal levels input (other than 0)
		{
			substringMap=contentShredder(content,level);
			Iterator<Entry<Integer, String>> iteratorObj = substringMap.entrySet().iterator();
			
			while(iteratorObj.hasNext()) {
				Entry<Integer,String> entry=iteratorObj.next();
				String str=entry.getValue();
				for(int pos=0; pos < str.length();pos++)
				{
					Node insertNode=null;
					Character ch= str.charAt(pos);
					
					if(fNode!=null && fNode.searchNewChar(fNode, ch.toString())==false ) 
					{
						insertNode=new Node(ch.toString());
						outputString+=fNode.getPathof(Character.toString((char)216))  + ch.toString();
						fNode=fNode.addRecursivelyRight(fNode, insertNode);
						fNode.elements.put(ch.toString(), 1);
					}
					else if(fNode.searchNewChar(fNode, ch.toString())) {
						outputString+=fNode.getPathof(ch.toString());
						fNode=fNode.updateFrequencyForNode(fNode, ch.toString());
					}
				}
				if(!iteratorObj.hasNext())
				{
					outputString+=fNode.getPathof("EOF");
					fNode=fNode.updateFrequencyForNode(fNode, "EOF");
				}
				fNode=treeRebuilder(fNode, reset);
			}
			//fNode.displayNodePaths(fNode, "");
			setCodeMap(fNode);
			fileGenerator(outputString, output_filename);
			return true;
		}
		else 														////Manages logic for level input 0
		{
				for(int pos=0; pos < content.length();pos++)
				{
					Node insertNode=null;
					Character ch= content.charAt(pos);
					if(fNode!=null && fNode.searchNewChar(fNode, ch.toString())==false ) 
					{
						insertNode=new Node(ch.toString());
						outputString+=fNode.getPathof(Character.toString((char)216))  + ch.toString();
						fNode=fNode.addRecursivelyRight(fNode, insertNode);
						fNode.elements.put(ch.toString(), 1);
					}
					else if(fNode.searchNewChar(fNode, ch.toString())) {
						outputString+=fNode.getPathof(ch.toString());
						fNode=fNode.updateFrequencyForNode(fNode, ch.toString());
					}
					if(content.length()==pos+1)
					{
						outputString+=fNode.getPathof("EOF");
						fNode=fNode.updateFrequencyForNode(fNode, "EOF");
					}
					fNode=treeRebuilder(fNode,reset);
				}
				//fNode.displayNodePaths(fNode, "");
				setCodeMap(fNode);
				fileGenerator(outputString, output_filename);
			return true;
		}
		
	}
	/**
	 * Adds leaf nodes to map for its path.
	 * @param fNode 
	 */
	public void setCodeMap(Node fNode) {
		
		for (Entry<String, Integer> entry : fNode.elements.entrySet()) {
		    String key = entry.getKey();
		    if(key.length()!=1 && key.contains("+")) {
		    	continue;
		    }
		    else
			{
		    	String nodePath=fNode.getPathof(entry.getKey());
		    	codeMap.put(entry.getKey(), nodePath);
			}
		}
		
	}
	/**
	 * Manages tree rebuilding procedure
	 * @param fNode - Last processed node
	 * @param reset - Reset parameter
	 * @return - Returns rebuilt node
	 */
	private Node treeRebuilder(Node fNode, Boolean reset) {
		HuffmanTree hTree=new HuffmanTree();
		fNode= hTree.rebuild(fNode, reset);
		return fNode;
	}

	/**
	 * Shreds input file content according to levels
	 * @param inputString - String Content of the file 
	 * @param level - Maximum level value
	 * @return - Sequenced map of substrings indexed by integer
	 */
	private HashMap<Integer, String> contentShredder(String inputString, int level) {

		HashMap<Integer, String> subStringMap=new HashMap<Integer, String>();
		int levelCount=1, charCount=0, contentIndex=0;
		String subString="";
		for(int pos=0; pos<inputString.length(); pos++)
		{
			charCount++;
			if( charCount == Math.pow(2, levelCount) )
			{
				subStringMap.put(contentIndex, subString+inputString.charAt(pos));
				levelCount++;
				if(levelCount>level) {										//when reached level max point
					levelCount--;
				}
				contentIndex++;
				subString="";
				charCount=0;
				continue;
			}
			if(charCount< Math.pow(2, levelCount) && pos+1==inputString.length() ) {
				subStringMap.put(contentIndex, subString+inputString.charAt(pos));
				levelCount++;
				if(levelCount>level) {
					levelCount--;
				}
				contentIndex++;
				subString="";
				charCount=0;
				continue;
			}
			char ch=inputString.charAt(pos);
			subString+=ch;
			
		}
		return subStringMap;
	}
    /**
     * Decodes content of file
     */
	@Override
	public boolean decode(String input_filename, String output_filename) {
		
		outputString="";
		String content="";
		Scanner in=new Scanner(System.in);
		File f=new File("./"+input_filename);
		try {
			in=new Scanner(f);
		} catch (FileNotFoundException e) {
			System.out.println("Mentioned File does not exists!");
			System.exit(1);
		}
		
		while(in.hasNext()) {
			content+=in.nextLine();
		}

		String levelString="", resetString="";
		levelString+=content.charAt(0);
		resetString+=content.charAt(1);
		int level=0;
		int reset=0;
		try {
			level = Integer.parseInt(levelString);
			reset = Integer.parseInt(resetString);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			System.err.println("Invalid content in file");
			System.exit(1);
		}
		
		boolean resetBoolean= false;
		if(reset==1) {
			resetBoolean =true;
		}
		
		HashMap<Integer, String> substringMap=null;
		
		while(in.hasNext()) {
			content+=in.nextLine();
		}

		Node fNode=new Node();
		HashSet<Integer> levelSet=new HashSet<Integer>();
		
		if(level!=0)
		{
			for(int k=1; k<=level; k++) {
				levelSet.add((int)Math.pow(2, k));
			}
		}
		
		if(level!=0)														//Manages logic for normal levels input (other than 0)
		{
//			substringMap=contentShredder(content,level);
			for(int pos=2; pos < content.length();pos++)
			{
				if(levelSet.contains(outputString.length()) ) 
				{
					//below section to check if proceeding code means EOF
					String checkerString="";
					int temp=0;
					String pathToEof= fNode.getPathof("EOF");
					for(temp=pos; temp< pos+pathToEof.length(); temp++) {
						checkerString+= content.charAt(temp);
						if(checkerString.equals(pathToEof))
						{
							fNode=fNode.updateFrequencyForNode(fNode, "EOF");
							pos=temp;
							continue;
						}
						
					}
					if(pos==temp) {
						fNode=treeRebuilder(fNode, resetBoolean);      //to rebuild
						continue;
					}
				}
				String pathToNewChar = fNode.getPathof(Character.toString((char)216));
				int pathLength=pathToNewChar.length();
				String checkerString="";
				int temp=0;
				
				//below section for getting first char after sensing new-character
				Boolean newCharFlag=false;
				for(temp=pos; temp< pos+pathToNewChar.length(); temp++) {
					checkerString= ""+content.charAt(temp);
					if(checkerString.equals(pathToNewChar)) {
						if(temp+1==content.length()) {
							continue;
						}
						outputString+=content.charAt(temp+1);
						Node insertNode=new Node(""+content.charAt(temp));
						fNode.addRecursivelyRight(fNode, insertNode);
						fNode.setNewChar(fNode);
						pos=temp++;
						newCharFlag= true;
						break;
					}
				}
				if(newCharFlag) {
					continue;
				}
				//below section to check is upcoming node is an existing node
				checkerString="";
				for(temp=pos; temp< content.length(); temp++) {
					checkerString+= content.charAt(temp);
					if(null!=fNode.checkForLeaf(checkerString))
					{
						fNode=fNode.updateFrequencyForNode(fNode, fNode.checkForLeaf(checkerString));
					}

				}
				
			}
			
//			while(iteratorObj.hasNext()) {
//				Entry<Integer,String> entry=iteratorObj.next();
//				String str=entry.getValue();
//				for(int pos=0; pos < str.length();pos++)
//				{
//					Node insertNode=null;
//					Character ch= str.charAt(pos);
//					
//					if(fNode!=null && fNode.searchNewChar(fNode, ch.toString())==false ) 
//					{
//						insertNode=new Node(ch.toString());
//						outputString+=fNode.getPathof(Character.toString((char)216))  + ch.toString();
//						fNode=fNode.addRecursivelyRight(fNode, insertNode);
//						fNode.elements.put(ch.toString(), 1);
//					}
//					else if(fNode.searchNewChar(fNode, ch.toString())) {
//						outputString+=fNode.getPathof(ch.toString());
//						fNode=fNode.updateFrequencyForNode(fNode, ch.toString());
//					}
//				}
//				if(!iteratorObj.hasNext())
//				{
//					outputString+=fNode.getPathof("EOF");
//					fNode=fNode.updateFrequencyForNode(fNode, "EOF");
//				}
//				fNode=treeRebuilder(fNode, reset);
//			}
			//fNode.displayNodePaths(fNode, "");
			setCodeMap(fNode);
			fileGenerator(outputString, output_filename);
			return true;
			
		}
		else 														//Manages logic for level input 0
		{
//				for(int pos=0; pos < content.length();pos++)
//				{
//					Node insertNode=null;
//					Character ch= content.charAt(pos);
//					if(fNode!=null && fNode.searchNewChar(fNode, ch.toString())==false ) 
//					{
//						insertNode=new Node(ch.toString());
//						outputString+=fNode.getPathof(Character.toString((char)216))  + ch.toString();
//						fNode=fNode.addRecursivelyRight(fNode, insertNode);
//						fNode.elements.put(ch.toString(), 1);
//					}
//					else if(fNode.searchNewChar(fNode, ch.toString())) {
//						outputString+=fNode.getPathof(ch.toString());
//						fNode=fNode.updateFrequencyForNode(fNode, ch.toString());
//					}
//					if(content.length()==pos+1)
//					{
//						outputString+=fNode.getPathof("EOF");
//						fNode=fNode.updateFrequencyForNode(fNode, "EOF");
//					}
//					fNode=treeRebuilder(fNode,reset);
//				}
//				//fNode.displayNodePaths(fNode, "");
//				setCodeMap(fNode);
//				fileGenerator(outputString, output_filename);
			return true;

		}
		
	}

	/**
	 * Method to return dictionary for  node element paths
	 */
	@Override
	public HashMap<String, String> codebook() {
		return codeMap;
	}
	
	/**
	 * Generates file for String content provided
	 * @param resultString - Input result String after encoding 
	 */
	private void fileGenerator(String resultString, String output_filename) {
		try {
			Files.write(Paths.get("./"+ output_filename), resultString.getBytes());			//Don't require object to be closed
		} catch (IOException e) {
			System.err.println("Error while writing the file.");
		}
	}
}