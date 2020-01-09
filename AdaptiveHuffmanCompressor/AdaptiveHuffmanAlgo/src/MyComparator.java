import java.util.Comparator;
import java.util.Map.Entry;
/**
 * @author Aakash Patel
 */
class MyComparator implements Comparator<Entry<String,Integer>> {						//Class to define custom comparison procedure of Map objects

	public MyComparator() {
	}

	/**
	 * Compares two map entries and decides which to be smaller or larger
	 * return - Negative integer when first entry is small and positive integer when otherwise.
	 */
	@Override
	public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
		
		if(o1.getValue() < o2.getValue()) {
			return -1;       //-1 means first one as small
		}
		else if(o1.getValue() == o2.getValue()) 
		{
			if(o1.getKey().contains("+") && o1.getKey().length() !=1) 
			{
				if(o2.getKey().contains("+") && o2.getKey().length() !=1) 
				{
					long c1=o1.getKey().chars().filter(ch -> ch == '+').count();
					long c2 = o2.getKey().chars().filter(ch -> ch == '+').count();
					if(c1<c2) {
						return -1;
					}
					else
						return 1;
				}
				else
					return 1;
			}
			else if(o2.getKey().contains("+") && o2.getKey().length() !=1) 
			{
				return -1;
			}
			else
			{
				if(o1.getKey().length()==1  && o2.getKey().length()==1) 
				{
					if((int)o1.getKey().charAt(0) < (int)o2.getKey().charAt(0))
					{
						return -1;
					}
					else
						return 1;
				}
				else
					return -1;
			}
		}
		else
		{
			return 1;
		}
	}
}
