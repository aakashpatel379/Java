import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeSet;
/**
 * @author Aakash Patel - B00807065
 */
public class HuffmanTree {

	static HashMap<String, Node> nodeDictionary=new HashMap<String, Node>();     //Dictionary for nodes generated in tree rebuilding process
	
	/**
	 * Method to rebuild tree
	 * @param root - given node to be rebuilt
	 * @param reset - reset choice by user
	 * @returns - Rebuilt root node 
	 */
	public Node rebuild(Node root, Boolean reset) {

		TreeSet<Entry<String, Integer>> frequencySet = new TreeSet<Entry<String,Integer>>(new MyComparator());
		
		for (Entry<String, Integer> entry : root.elements.entrySet()) {
		    String key = entry.getKey();
		    if(key.length()!=1 && key.contains("+")) {
		    	continue;
		    }
		    else
			{
		    	if(reset) {
		    		entry.setValue(0);
		    	}
		    	frequencySet.add(entry);
			}
		}
		Entry<String, Integer> entryObj = null;
		Entry<String, Integer> entry1=null, entry2=null;
		String lastNodeKey=""; int mergedFreq=0;
		Iterator<Entry<String,Integer>> iteratorObject =frequencySet.iterator();
		while(iteratorObject.hasNext())
		{
			entry1=iteratorObject.next();
			if(iteratorObject.hasNext()) 
			{
				entry2=iteratorObject.next();
				frequencySet.pollFirst();
				frequencySet.pollFirst();
				String mergeKey=merge(entry1, entry2);
				mergedFreq=frequencyAdjuster(entry1, entry2, root);
				root.elements.put(mergeKey, mergedFreq );
				HashMap<String, Integer> setMap = new HashMap<String, Integer>();
				setMap.put(mergeKey, mergedFreq);
				for(Entry<String,Integer> setEntry: setMap.entrySet()) 
				{
					entryObj=setEntry;
				}
				frequencySet.add(entryObj);
				iteratorObject=frequencySet.iterator();
			}
			else {
				lastNodeKey=frequencySet.first().getKey();
			}
		}
		
		Node rebuiltNode = nodeDictionary.get(lastNodeKey);
		rebuiltNode.elements=root.elements;
		nodeDictionary.put(lastNodeKey, rebuiltNode);
		return rebuiltNode;
	}
	
	/**
	 * Method to set frequency of two node after merging
	 * @param e1 - Entry for first node element
	 * @param e2 - Entry for second node element
	 * @return Updated frequency after merging
	 */
	public int frequencyAdjuster(Entry<String, Integer> e1, Entry<String, Integer> e2, Node root) {
		
		int freValue=0;
		if(null!= root.elements.get(e1.getKey()))
		{
			freValue=root.elements.get(e1.getKey());
		}
		if(null!= root.elements.get(e2.getKey()))
		{
			freValue+=root.elements.get(e2.getKey());
		}
		
		return freValue;
	}
	/**
	 * Merges two entries provided
	 * @param o1 - first entry in frequency set
	 * @param o2 - second entry in frequency set
	 * @return key string of merged node element generated
	 */
	public String merge(Entry<String, Integer> o1, Entry<String, Integer> o2)
	{
		Node mNode;							//Merged Node
		Node obj1=null ,obj2=null;
		if(o1.getKey().length()!=1 && o1.getKey().contains("+"))
		{
			obj1= nodeDictionary.get(o1.getKey());
			if(null==obj1)
			{
				System.err.println("Couldn't find node for "+o1.getKey()+ "!");
				return null;
			}
		}
		if(o2.getKey().length()!=1 && o2.getKey().contains("+"))
		{
			obj2= nodeDictionary.get(o2.getKey());
			if(null==obj2)
			{
				System.err.println("Couldn't find node for "+o2.getKey()+ "!");
				return null;
			}
		}
		if(null==obj1)
		{
			obj1=new Node(o1.getKey());
		}
		if(null==obj2)
		{
			obj2=new Node(o2.getKey());
		}
		mNode=new Node();
		mNode.left=obj1;
		mNode.right=obj2;
		String mergeKey=o1.getKey() + "+"+ o2.getKey();
		nodeDictionary.put(o1.getKey() + "+"+ o2.getKey(), mNode);
		return mergeKey;
	}
}
