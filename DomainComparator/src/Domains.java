import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import Dijkstra.Algorithm;
import Dijkstra.Edge;
import Dijkstra.EditDistanceRecursive;
import Dijkstra.Vertex;

/**
 * @author Aakash Patel - B00807065
 */
 class Domains {
	
	static HashMap<Integer, LinkedList<Vertex>> finalLevelMap=null;      //Map of track final vertex list according to levels set as keys.  
	static HashMap<String, Vertex> inputMap=new HashMap<String, Vertex>();
	static SortedSet<String> detachedNodes = new TreeSet<String>();
	
	/**
	 * Loads domains in Set. Readies class for queries.
	 * @param domainSet - Input set object of String type. 
	 */
	public Boolean load(Set<String> domainSet) {
		HashMap<Integer, LinkedList<Vertex>> levelVerticesMap =null;     //Map of (Level, Vertices) according to difference from Base Vertex
		levelVerticesMap= levelSetter(domainSet);  						//sets levels according to source string
		Set<Integer> levels = levelVerticesMap.keySet();
		Integer[] keys = levels.toArray(new Integer[levels.size()]);
		for(int j=keys.length-1; j>=0 ; j--)
		{
			int key =keys[j];
			if((j-1) >= 0)
			{
				if(key - keys[j-1] !=1)									//Maintains disjoint level vertices in detachedNode set.
				{
					LinkedList<Vertex> levelVertexList =levelVerticesMap.remove(key);
					Iterator<Vertex> iterator = levelVertexList.iterator();
					while(iterator.hasNext())
					{
						Vertex v =iterator.next();
						detachedNodes.add(v.toString());
					}
				}
			}
			
		}
		
		interLevelConnector(levelVerticesMap);
		
		intralevelConnector();
		
		return true;
	}
	
	/**
	 * Method to Establish links between nodes in same level
	 */
	private void intralevelConnector() {

		Iterator<Entry<Integer, LinkedList<Vertex>>> levelIterator = null;
		int level=0;
		levelIterator=finalLevelMap.entrySet().iterator();
		while(levelIterator.hasNext())
		{
			Entry<Integer, LinkedList<Vertex>> entry = levelIterator.next();
			LinkedList<Vertex> vlist = entry.getValue();
			LinkedList<Vertex> updatedVList=new LinkedList<Vertex>();
			Vertex[] vArray = vlist.toArray(new Vertex[vlist.size()]);
			for(int i=0; i< vArray.length; i++)
			{
				Vertex u=vArray[i];
				for(int j=i+1; j<vArray.length ; j++)
				{
					Vertex v= vArray[j];
					int diff =EditDistanceRecursive.calculate(u.toString(), v.toString());
					if(diff==1) 
					{
						u.addNeighbour(new Edge(1, u, v));
					}
				}
				updatedVList.add(u);
			}
			finalLevelMap.put(level, updatedVList);
			level++;
		}
		
	}
	/**
	 * Connects nodes of one level to nodes of previous level (having 1 manipulation difference)
	 * @param levelVerticesMap - Takes input having key as level index and Value as LinkedList of Vertices corresponding to the level
	 */
	private void interLevelConnector(HashMap<Integer, LinkedList<Vertex>> levelVerticesMap) {

		Iterator<Entry<Integer, LinkedList<Vertex>>> levelIterator = levelVerticesMap.entrySet().iterator();   //iterator to iterate input map.
		LinkedList<Vertex> firstLevelUpdatedList , secondLevelUpdatedList;                                     //LinkedList objects to store updated chain of Vertices
		firstLevelUpdatedList = new LinkedList<Vertex>();
		LinkedList<Vertex> firstList =null;
		secondLevelUpdatedList = new LinkedList<Vertex>();
		Entry<Integer, LinkedList<Vertex>> firstEntry=null;

		int level=0;
		finalLevelMap=new HashMap<Integer, LinkedList<Vertex>>(); 
		while(levelIterator.hasNext()) 
		{
			if(firstList==null)
			{
				firstEntry = levelIterator.next();
				firstList = firstEntry.getValue();
			}
			if(levelIterator.hasNext())
			{
				
				Entry<Integer, LinkedList<Vertex>> secondEntry = levelIterator.next();
				LinkedList<Vertex> secondList= secondEntry.getValue();
				Vertex[] vArray = secondList.toArray(new Vertex[secondList.size()]);
				for(int i=0; i<vArray.length ;i++)
				{
					Vertex v=vArray[i];
					Vertex[] uArray=null;
					if(!firstLevelUpdatedList.isEmpty())
					{
						uArray = firstLevelUpdatedList.toArray(new Vertex[firstLevelUpdatedList.size()]);
					}
					else
					{
						uArray = firstList.toArray(new Vertex[firstList.size()]);
					}
					firstLevelUpdatedList=new LinkedList<Vertex>();
					for(int h=0; h<uArray.length;h++) {
						Vertex u= uArray[h];
						int diff= EditDistanceRecursive.calculate(u.toString(), v.toString());
						
						if(diff==1)																			
							u.addNeighbour(new Edge(1, u, v));													//Establish links to nodes with 1 difference in previous level
						
						firstLevelUpdatedList.add(u);
					}
					secondLevelUpdatedList.add(v);
				}
				finalLevelMap.put(level, firstLevelUpdatedList);												//Storing levelmap with updated chain of vertex objects
				level++;
				firstLevelUpdatedList=new LinkedList<Vertex>();
				firstList=secondEntry.getValue();
				
				if(!levelIterator.hasNext())
				{
					finalLevelMap.put(level, secondLevelUpdatedList);
				}
				secondLevelUpdatedList=new LinkedList<Vertex>();
			}
		}
		
		if(finalLevelMap.isEmpty()) {
			finalLevelMap=levelVerticesMap;
		}
		
	}
	
	/**
	 * Takes set of domains as input. Affixes first node as base domain.
	 * Calculates manipulation difference of all domains w.r.t base.
	 * Aggregates domains with common difference (can be called as levels).
	 * @return - updated level map with key as level index and value as chain of nodes in particular level.
	 */
	private HashMap<Integer, LinkedList<Vertex>> levelSetter(Set<String> domainSet) {
		LinkedList<String> datasetLinkedList= new LinkedList<String>();

		Iterator<String> dataIterator = domainSet.iterator();
		while(dataIterator.hasNext())
		{
			datasetLinkedList.add(dataIterator.next());
		}
		
		Iterator<String> iterator=datasetLinkedList.iterator();
		HashMap<Integer, LinkedList<Vertex>> levelVerticesMap = new HashMap<Integer, LinkedList<Vertex>>();
		String sourceString = iterator.next();
		LinkedList<Vertex> vertexList =new LinkedList<Vertex>();
		Vertex source= new Vertex(sourceString);
		vertexList.add(source);
		levelVerticesMap.put(0, vertexList);
		
		while(iterator.hasNext())
		{
				String newDomainName=iterator.next();
				Vertex newNode = new Vertex(newDomainName);
				int difference= EditDistanceRecursive.calculate(sourceString, newDomainName);
				LinkedList<Vertex> existingvertexList =null;
				existingvertexList= levelVerticesMap.get(difference);
				if(existingvertexList==null)
				{
					existingvertexList=new LinkedList<Vertex>();
					existingvertexList.add(newNode);
					levelVerticesMap.put(difference, existingvertexList);
				}
				else 
				{
					existingvertexList.add(newNode);
					levelVerticesMap.put(difference, existingvertexList);
				}
		}
		
		return levelVerticesMap;
	}

	/**
	 * Method to look for an domains presence in domain set.
	 * @param domain - Argument domain name
	 * @return - Returns success or failure in terms of domain's presence.
	 */
	public Boolean domainLookup(String domain)
	{
		String base = finalLevelMap.get(0).get(0).toString();
		int diff = EditDistanceRecursive.calculate(base, domain);
		LinkedList<Vertex> levelList = null;
		levelList=finalLevelMap.get(diff);
		if(levelList==null)
		{
			return false;
		}
		Vertex[] vListArray = levelList.toArray(new Vertex[levelList.size()]);
		for(int pos=0; pos < vListArray.length;pos++)
		{
			Vertex v= vListArray[pos];
			if (v.toString().equals(domain)) 
			{
				inputMap.put(v.toString(), v);           //Adds prompted domain to map for later reference
				return true;
			}
		}
		return false;
	}

	/**
	 * Method to find edit distance between domain names provided.
	 * @param domainName1 - Source domain name.
	 * @param domainName2 - Target domain name.
	 * @return - Edit distance between two domains.
	 */
	public int editDistance(String domainName1, String domainName2) {
		
		if(!domainLookup(domainName1)) {
			
			if(detachedNodes.contains(domainName1))
			{
				System.out.println("Provided domains aren't convertible!");
				return -1;
			}
			else
			{
				System.out.println("Domain 1 does not exist!");
				return -1;
			}
		}
		
		if(!domainLookup(domainName2))
		{
			if(detachedNodes.contains(domainName2))
			{
				System.out.println("Provided domains aren't convertible!");
				return -1;
			}
			else
			{
				System.out.println("Domain 2 does not exist!");
				return -1;
			}
		}
		else 
		{
			if(domainName1.equals(domainName2))
			{
				System.out.println("Both domain names are equal!");
				return 0;
			}
			else
			{
				Algorithm algorithm = new Algorithm();
				Vertex baseNode = finalLevelMap.get(0).get(0);
				Vertex endVertex = algorithm.computePaths(baseNode);
				List<Vertex> fullPath =algorithm.getShortestPathToNode(baseNode, endVertex);
				if(!fullPath.contains(inputMap.get(domainName1)) || !fullPath.contains(inputMap.get(domainName2)))
				{ 
					System.out.println("Provided domains aren't convertible!");
					return -1;
				}
				
				if(baseNode.toString().equals(domainName2))
				{
					List<Vertex> path = algorithm.getShortestPathTo(inputMap.get(domainName1));
					Collections.reverse(path);
					return (path.size() -1);
				}
				else 
				{
					int diff1=0 , diff2 =0;
					diff1= EditDistanceRecursive.calculate(baseNode.toString(), inputMap.get(domainName1).toString());
					diff2 = EditDistanceRecursive.calculate(baseNode.toString(), inputMap.get(domainName2).toString());
					
					if(diff1 > diff2)
					{
						List<Vertex> path = algorithm.getShortestPathToNode(inputMap.get(domainName2), inputMap.get(domainName1));
						
						Collections.reverse(path);
						return (path.size() -1);
					}
					else if(diff1 < diff2)
					{
						List<Vertex> path = algorithm.getShortestPathToNode(inputMap.get(domainName1), inputMap.get(domainName2));
						return (path.size() -1);
					}
					else {
						List<Vertex> path = algorithm.getShortestPathToNode(inputMap.get(domainName1), inputMap.get(domainName2));
					    Vertex first=path.get(0);
						if(!first.toString().equals(domainName1))
						{
							Collections.reverse(path);
						}
						return (path.size() -1);
					}
				}
			}
		}
	}
	
	String[] editPath(String domainName1, String domainName2) {
		
		String[] pathArray=null;
		if(!domainLookup(domainName1)) {
			
			if(detachedNodes.contains(domainName1))
			{
				System.out.println("Provided domains aren't convertible!");
				return new String[1];
			}
			else
			{
				System.out.println("Domain 1 does not exist!");
				return new String[1];
			}
		}
		
		if(!domainLookup(domainName2))
		{
			if(detachedNodes.contains(domainName2))
			{
				System.out.println("Provided domains aren't convertible!");
				return new String[1];
			}
			else
			{
				System.out.println("Domain 2 does not exist!");
				return new String[1];
			}
		}
		else 
		{
			if(domainName1.equals(domainName2))
			{
				return new String[] {domainName1};    //Both domain names are equal!
			}
			else
			{
				Algorithm algorithm = new Algorithm();
				Vertex baseNode = finalLevelMap.get(0).get(0);
				Vertex endVertex = algorithm.computePaths(baseNode);
				List<Vertex> fullPath =algorithm.getShortestPathToNode(baseNode, endVertex);
				if(!fullPath.contains(inputMap.get(domainName1)) || !fullPath.contains(inputMap.get(domainName2)))
				{ 
					System.out.println("Provided domains aren't convertible!");
					return new String[1];
				}
	
				
				if(baseNode.toString().equals(domainName2))
				{
					List<Vertex> path = algorithm.getShortestPathTo(inputMap.get(domainName1));
					Collections.reverse(path);
					pathArray = listConverter(path);
					return pathArray;
				}
				else 
				{
					int diff1=0 , diff2 =0;
					diff1= EditDistanceRecursive.calculate(baseNode.toString(), inputMap.get(domainName1).toString());
					diff2 = EditDistanceRecursive.calculate(baseNode.toString(), inputMap.get(domainName2).toString());
					
					if(diff1 > diff2)
					{
						List<Vertex> path = algorithm.getShortestPathToNode(inputMap.get(domainName2), inputMap.get(domainName1));
						Collections.reverse(path);
						pathArray = listConverter(path);
						return pathArray;
					}
					else if(diff1 < diff2)
					{
						List<Vertex> path = algorithm.getShortestPathToNode(inputMap.get(domainName1), inputMap.get(domainName2));
						pathArray = listConverter(path);
						return pathArray;
					}
					else 
					{
						List<Vertex> path = algorithm.getShortestPathToNode(inputMap.get(domainName1), inputMap.get(domainName2));
					    Vertex first=path.get(0);
						if(!first.toString().equals(domainName1))
						{
							Collections.reverse(path);
						}
						pathArray = listConverter(path);
						return pathArray;
					}
				}
			}
		}
	}
	/**
	 * Method to determine number of domains within domain name set with at most 'distance'
	 * @param domainName - Given domain name.
	 * @param distance - Distance upto which count is to be calculated.
	 */
	public int numNearby(String domainName, int distance) {
		if(!domainLookup(domainName))
		{
			System.out.println("Domain name doesn't exist in Set!");
			return 0;
		}
		else if(distance==0){
			return 0;
		}
		else 
		{
			Algorithm algorithm = new Algorithm();
			Vertex baseNode = finalLevelMap.get(0).get(0);
			Vertex endVertex = algorithm.computePaths(baseNode);
			List<Vertex> fullPath =algorithm.getShortestPathToNode(baseNode, endVertex);
			if(!fullPath.contains(inputMap.get(domainName)))
			{ 
				System.out.println("Provided domains isn't accessible!");
				return 0;
			}
			
			int beforeCount =algorithm.reachNearbyNodesBefore(inputMap.get(domainName), distance);
			List<Vertex> path = algorithm.getShortestPathToNode(inputMap.get(domainName), endVertex);
			Iterator<Vertex> iterator = path.iterator();
			iterator.next();
			int afterCount=0;
			while (iterator.hasNext()) {
				distance--;
				afterCount++;
				if(distance==0)
				break;
			}
			return (afterCount+beforeCount);
		}
	}
	/**
	 * Converts list of Vertex to array of string domain names
	 * @param path - list object of vertices
	 * @return - String array of string domain names
	 */
	public String[] listConverter(List<Vertex> path)
	{
		String[] result = new String[path.size()];
		int pos=0;
		for(Vertex v: path)
		{
			result[pos]=v.toString();
			pos++;
		}
		return result;
	}
}
