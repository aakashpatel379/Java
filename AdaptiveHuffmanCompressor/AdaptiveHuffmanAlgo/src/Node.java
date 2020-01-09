import java.util.HashMap;
import java.util.Map.Entry;
/**
 * @author Aakash Patel
 */

class Node {

	int freq;
	Node left;
	Node right;
	String charData;
	HashMap<String, Integer> elements=new HashMap<String, Integer>();        //Map to store frequency of characters encountered
	
	Node() {
		charData = null;
		this.left = new Node(Character.toString((char)216));
		this.right = new Node("EOF");
		this.freq= 0;
		this.elements.put(Character.toString((char)216), 0);
		this.elements.put("EOF", 0);
	}

	Node(String charName) {
		this.left = null;
		this.right = null;
		this.charData = charName;
		if(null!=this.charData && this.charData!="EOF" && !this.charData.contentEquals(Character.toString((char)216)))
		{
			this.freq = 0;
		}
		else
			this.freq= 0;
	}
	/**
	 * Adds new node to base node's rightmost end
	 * @return - Base node after addition
	 */
	public Node addRecursivelyRight(Node root, Node insertNode) {
		if (root == null) {
			return new Node();
		}
		Node middle = new Node(null);
		Node current = root;
		Node previous = null;
		if (root != null) {
			
			while (current.right != null) 
			{
				previous = current;
				current = current.right;
			}

			if (previous != null && current.left == null) 
			{
				middle.left = previous.right;
				middle.right = insertNode;
				previous.right = middle;
				current = middle;
			}
			else if(current.left!=null)
			{
				System.err.println("Something went wrong!");
			}
			
			setNewChar(root);
		}
		return root;
	}
	/**
	 * Updates frequency of new-character  
	 * @return success or failure result of operation intended
	 */
	public Boolean setNewChar(Node root) 
	{
		
		for(Entry<String, Integer> entry: root.elements.entrySet()) 
			{
				if(entry.getKey().equals(Character.toString((char)216))) {
					
					elements.put(Character.toString((char)216), entry.getValue()+1);
					return true;
				}
			}			
			
		return false;
	}
	/**
	 * Looks up for nodes as per data provided 
	 * @return - Results of searching in boolean
	 */
	public Boolean searchNewChar(Node root, String ch )
	{
		if(root==null)
			return false;
		
		if(null!=root.charData && root.charData.equals(ch))
			return true;
		
		if(searchNewChar(root.left, ch))
			return true;
		if(searchNewChar(root.right, ch))
			return true;
		
		return false;
	}
	/**
	 * Updates frequency of provided character in the given Base node 
	 * @return success or failure result of operation intended
	 */
	public Boolean updateNodeFrequency(Node root, String ch) {
		
		for(Entry<String, Integer> entry: root.elements.entrySet()) 
		{
			if(entry.getKey().equals(ch)) {
				elements.put(ch, entry.getValue()+1);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Helper method for updateNodeFrequency method
	 * @return - Unmodified base node
	 */
	public Node updateFrequencyForNode(Node root, String ch) {
		Node orginalRoot=root;
		updateNodeFrequency(root, ch);
		return orginalRoot;
	}
	
	public void traverseTree(Node node) {
		if(node==null)
			return;

		traverseTree(node.left);
		if(node.charData!=null)
		{
			System.out.println(node.charData+ " : "+ node.freq);
		}
		traverseTree(node.right);
	}

	public void displayNodePaths(Node fNode, String carryString) {

		if(null==fNode.left && null==fNode.right )
		{
			System.out.println(fNode.charData+ ":" +carryString );
			return;
		}
		
		displayNodePaths(fNode.left, carryString+ "0");
		displayNodePaths(fNode.right, carryString+"1");
	}
	/**
	 * Method to get parent node of base node provided
	 * @return Parent node element
	 */
	public Node getParentof(Node node) {
	   Node current=this;
	   if(null==node) {
		   System.err.println("Couldn't find parent of given object!");
	   }
	   if(current==node)
	   {
		   System.err.println("Can't find parent of same Node itself");
		   return null;
	   }
	   String dataofNode= node.charData;
	   while(searchNewChar(current, dataofNode))
	   {
		   if(searchNewChar(current.left, dataofNode))
		   {
			   if(current.left==node) {
				   return current;
			   }
			   current=current.left;
		   }
		   else
		   {
			   if(current.right==node) {
				   return current;
			   }
			   current=current.right;
		   }
	   }
	   return null;
	}
	/**
	 * Method to find path of leaf node elements
	 * @param ch - String data of leaf node
	 * @return Path to access leaf node
	 */
	public String getPathof(String ch) {
		   Node current=this;
		   if(null==ch) {
			   System.err.println("Couldn't find path of given object!");
		   }
		   String path="";
		   if(!searchNewChar(current, ch))
		   {
			   System.out.println("Searched node dont exists");
			   return path;
		   }
		   while(searchNewChar(current, ch))
		   {
			   if(searchNewChar(current.left, ch))
			   {
				   path+="0";
				   if(null!=current.left.charData && current.left.charData.equals(ch)) {
					   return path;
				   }
				   current=current.left;
			   }
			   else
			   {
				   path+="1";
				   if(null!=current.right.charData && current.right.charData.equals(ch)) {
					   return path;
				   }
				   current=current.right;
			   }
		   }
		   return path;
		}
	
	/**
	 * Searches node as per string data provided in Base node provided 
	 * @return - Searched node
	 */
	public Node searchNode(Node root, String ch )
	{
		if(root==null)
			return null;
		
		if(null!=root.charData && root.charData.equals(ch))
			return root;
		
		
		if(null!=searchNode(root.left, ch))
			return searchNode(root.left, ch);
		if(null!=searchNode(root.right, ch))
			return searchNode(root.right, ch);
		
		return null;
		
	}
	/**
	 * Method to get parent of current node
	 * @param ch - String data of whose parent is to be found
	 * @return - Parent node
	 */
	public Node getParent(Node root, String ch )
	{
		if(root==null)
			return null;
		
		if(null!=root.charData && root.charData.equals(ch))
			return root;
		
		
		if(null!=searchNode(root.left, ch))
			return searchNode(root.left, ch);
		if(null!=searchNode(root.right, ch))
			return searchNode(root.right, ch);
		
		return null;
		
	}

	public String checkForLeaf(String checkerString) {

		Node current=this;
		for(int pos=0; pos < checkerString.length() ; pos++)
		{
			if(current.charData!=null && current.left==null && current.right==null) {
				return current.charData;
			}
			if(checkerString.charAt(pos)=='0')
			{
				current=current.left;
			}
			else if(checkerString.charAt(pos)=='1')
			{
				current=current.right;
			}
		}
				
		return null;
	}
	
}
