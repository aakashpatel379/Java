package Dijkstra;

import java.util.ArrayList;
import java.util.List;

public class Vertex  implements Comparable<Vertex>{
   private String name;
   private List<Edge> adjacenciesList;
   private Vertex previousVertex;
   private boolean visited;
   private double minDistance= Double.MAX_VALUE;
   
  public  Vertex(String name) {
	   this.name=name;
	   this.adjacenciesList= new ArrayList<>();
	   
   }
   
   public void addNeighbour(Edge edge) {
	   this.adjacenciesList.add(edge);
   }

	public Vertex getPreviousVertex() {
		return previousVertex;
	}
	
	public void setPreviousVertex(Vertex previousVertex) {
		this.previousVertex = previousVertex;
	}
	
	public boolean isVisited() {
		return visited;
	}
	
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	public double getMinDistance() {
		return minDistance;
	}
	
	public void setMinDistance(double minDistance) {
		this.minDistance = minDistance;
	}
	
	public List<Edge> getAdjacenciesList() {
		return adjacenciesList;
	}
	   
	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int compareTo(Vertex otherVertex) {

		return Double.compare(this.minDistance, otherVertex.getMinDistance());
	}

}
