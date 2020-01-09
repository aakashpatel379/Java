package Dijkstra;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Algorithm {
	
	static Vertex lastVertex=null;     //to keep record of last vertex traversed after calculation of paths.
	public Vertex computePaths(Vertex sourceVertex) {
		
		Vertex endNode=null;
		sourceVertex.setMinDistance(0);
		PriorityQueue<Vertex> priortyQueue = new PriorityQueue<>();
		priortyQueue.add(sourceVertex);
		while( !priortyQueue.isEmpty()) 
		{
			
			Vertex actualVertex = priortyQueue.poll();
			for (Edge edge: actualVertex.getAdjacenciesList()) {
				
				Vertex v = edge.getTargetVertex();
				Vertex u=edge.getStartVertex();
				double weight =edge.getWeight();
				double minDistanceViaV = actualVertex.getMinDistance() + weight;
				
				if(minDistanceViaV < v.getMinDistance())
				{
					priortyQueue.remove(v);
					v.setPreviousVertex(actualVertex);
					v.setMinDistance(minDistanceViaV);
					priortyQueue.add(v);
				}
				endNode=v;
			}
		}
		if(lastVertex==null)
		{	
			lastVertex=endNode;
		}
		return lastVertex;
	}
	/*
	 * To find shortest path to target node
	 */
	public List<Vertex> getShortestPathTo(Vertex targetVertex){
		List<Vertex> path = new ArrayList<Vertex>();
		for(Vertex vertex = targetVertex; vertex!=null; vertex=vertex.getPreviousVertex() )
		{
			path.add(vertex);
		}
			
		Collections.reverse(path);
		return path;
	}
	/*
	 * TO find shortest path to target node from source node
	 */
	public List<Vertex> getShortestPathToNode(Vertex source, Vertex target)
	{
		List<Vertex> path = new ArrayList<Vertex>();
		for(Vertex vertex = target; vertex!=null || vertex==source; vertex=vertex.getPreviousVertex() )
		{
			path.add(vertex);
			if(vertex==source) break;
		}
		Collections.reverse(path);
		return path;
	}
	
	/*
	 * Traversing shortest path upto provided distance
	 */
	public int reachNearbyNodesBefore(Vertex current, int distance)
	{
		int count=0;
		for(Vertex vertex = current.getPreviousVertex(); distance!=0; vertex=vertex.getPreviousVertex() )
		{
			if(vertex==null) break;
			distance--;
			count++;	
		}
		return count;
	}
	
}
