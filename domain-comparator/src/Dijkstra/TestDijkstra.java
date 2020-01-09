package Dijkstra;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

public class TestDijkstra {

	static LinkedList<Vertex> firstList =null;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LinkedList<String> datasetLinkedList= new LinkedList<String>();
		datasetLinkedList.add("google.ca");
		datasetLinkedList.add("gogle.ca");
		datasetLinkedList.add("g0gle.ca");
		datasetLinkedList.add("g00gle.ca");
		datasetLinkedList.add("g00g1e.ca");
		datasetLinkedList.add("gooogle.ca");

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
		
		Iterator<Entry<Integer, LinkedList<Vertex>>> levelIterator = levelVerticesMap.entrySet().iterator();
		LinkedList<Vertex> firstLevelUpdatedList , secondLevelUpdatedList; 
		firstLevelUpdatedList = new LinkedList<Vertex>();
		secondLevelUpdatedList = new LinkedList<Vertex>();
		Entry<Integer, LinkedList<Vertex>> firstEntry=null;

		int level=0;
		HashMap<Integer, LinkedList<Vertex>> finalLevelMap=new HashMap<Integer, LinkedList<Vertex>>(); 
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
							u.addNeighbour(new Edge(1, u, v));
						
						firstLevelUpdatedList.add(u);
					}
					secondLevelUpdatedList.add(v);
				}
				finalLevelMap.put(level, firstLevelUpdatedList);				
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
		level=0;
		levelIterator=finalLevelMap.entrySet().iterator();
		while(levelIterator.hasNext())
		{
			Entry<Integer, LinkedList<Vertex>> entry = levelIterator.next();
			LinkedList<Vertex> vlist = entry.getValue();
			LinkedList<Vertex> updatedVList=new LinkedList<Vertex>();
			Vertex[] vArray = vlist.toArray(new Vertex[vlist.size()]);
			for(int i=0; i<vArray.length; i++)
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
		Algorithm algorithm = new Algorithm();
		Vertex sourceV = finalLevelMap.get(0).getFirst();
		algorithm.computePaths(sourceV);
		
		Vertex destV = finalLevelMap.get(3).getFirst();
		System.out.println(algorithm.getShortestPathTo(destV));
		System.exit(1);
	}
}
