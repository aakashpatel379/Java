import java.util.Set;
import java.util.TreeSet;

/**
 * @author Aakash Patel - B00807065
 */


public class DomainTest {

	public static void main(String[] args) {
		Set<String> domainSet= new TreeSet<String>();

		domainSet.add("g00g1e.ca");
		domainSet.add("gooogle.ca");
		domainSet.add("google1234.ca");
		domainSet.add("google.ca");
		domainSet.add("gogle.ca");
		domainSet.add("g0gle.ca");
		domainSet.add("g00gle.ca");
		domainSet.add("g1e.ca");
		
		Domains d=new Domains();
		d.load(domainSet);
		int dist=0;
		dist = d.editDistance("gooogle.ca", "gogle.ca");   //last node first
		System.out.println(dist);
		displayPath((d.editPath("gooogle.ca", "gogle.ca")));
		
		dist = d.editDistance("google.ca", "g00g1e.ca");   //standard
		System.out.println(dist);
		displayPath((d.editPath("google.ca", "g00g1e.ca")));

		dist = d.editDistance("g00g1e.ca", "google.ca");   //reverse std
		System.out.println(dist);
		displayPath(d.editPath("g00g1e.ca", "google.ca"));
		
		dist = d.editDistance("gooogle.ca", "gooogle.ca");  //same both ***path
		System.out.println(dist);
		displayPath(d.editPath("gooogle.ca", "gooogle.ca"));
		
		dist= d.editDistance("g00g1e.ca", "g00g1e.ca");     //same both ***path
		System.out.println(dist);
		displayPath(d.editPath("g00g1e.ca", "g00g1e.ca"));
		
		dist = d.editDistance("gogle.ca", "google.ca");    //***
		System.out.println(dist);
		displayPath(d.editPath("gogle.ca", "google.ca"));
		
		dist= d.editDistance("google.ca", "g1e.ca");   //non reachable node working
		System.out.println(dist);
		displayPath(d.editPath("google.ca", "g1e.ca"));

		dist= d.editDistance("g1e.ca", "google.ca");   //working
		System.out.println(dist);
		displayPath(d.editPath("g1e.ca", "google.ca"));
		
		
		System.out.println("Nearest count: "+ d.numNearby("gooogle.ca", 12));
		
		System.out.println("Nearest count: "+ d.numNearby("g00gle.ca", 3));
		
		System.out.println("Nearest count: "+ d.numNearby("g00g1e.ca", 1));
		
		System.out.println("Nearest count: "+ d.numNearby("g1e.ca", 1));
	}

	/**
	 * Method to display path sequence obtained from editPath method
	 * @param pathArray - String array object containing path sequence
	 */
	public static void displayPath(String[] pathArray)
	{
		for (int i = 0; i < pathArray.length; i++) {
			System.out.print(pathArray[i]+ " ");
		}
		System.out.println();
	}

}
