import java.sql.Connection;
/**
 * Test class to test inventoryControl interface.
 * @author Aakash Patel
 *
 */
public class InventoryTest {
	static String user="";
	static String password="";
	static String database=""; 
	static Connection connection = null;
	public static void main(String[] args)  {
		// TODO Auto-generated method stub
		InventoryManager test=new InventoryManager();
//		try {
//			test.Ship_order(11070);
//		} catch (OrderException e) {
//			// TODO Auto-generated catch block
//			System.out.println(e.getMessage());
//			System.out.println("OrderReference"+e.getReference());
//		}
//		
//	test.Issue_reorders(2019, 4, 1);
		
		try {
			test.Receive_order(3);
		} catch (OrderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("OrderReference"+e.getReference());
		}
	}
}
