import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.TreeSet;


/** 
 * @author Aakash Patel  -B00807065
 */
public class InventoryManager implements inventoryControl{

	static String user="";
	static String password="";
	static String database=""; 
	static int reorderSuppliersCount=0;  //NO of suppliers which had reorder done
	static Connection connection = null;
	
	@Override
	/**
	 * Method to handle Shippment of orders
	 */
	public void Ship_order(int orderNumber) throws OrderException {

		Statement statement = null;
	     retrieveCreds();
		 try 
	        {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	            
				connection = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, password);

	            statement = connection.createStatement();

	            statement.executeQuery("use "+database+";");
	            
	            ResultSet r =statement.executeQuery("select *from orders where orderid="+orderNumber+";");
	            
	            if (r.next() == false) 
	            {
	            	throw new OrderException("Order not present in database!", orderNumber);
	            } 
	           
	            else 
	            {
	                do {
	                	String shippedDate=r.getString("ShippedDate");
	                	if(shippedDate!=null)
		            	{
		            		throw new OrderException("Order Already Shipped!", orderNumber); 
		            	}
	                } while (r.next());
	             }
	            
	            manageQty(orderNumber,statement);
	            statement.executeUpdate("update orders set ShippedDate= curdate() where orderid="+orderNumber+";");

	            System.out.println("Order Number: "+orderNumber+" shipped sucessfully!");
	            statement.close();
	            connection.close();
		        }
		 	
			 	catch(OrderException e)
			 	{
			 		e.printStackTrace();
			 	}
		 	
		 		catch(Exception e) 
		        {
		 			
		 			System.out.println("e");
		        	if(e.getMessage().contains("Communications link failure"))
		        	{
		        		System.err.println("\nVPN is disconnected. Program terminated!");
		        		System.exit(1);
		        	}
		        	e.printStackTrace();
		        }
		 		
	}
	/**
	 * Method to manage Quantity of products w.r.t given ordernumber
	 */
	private void manageQty(int orderNumber, Statement statement) throws SQLException, OrderException {
		
		String sqlOrderDetails ="select od.OrderID, o.ShippedDate, od.ProductID, od.UnitPrice, od.Quantity, p.UnitsInStock, p.Discontinued\r\n" + 
				"from orders as o inner join orderdetails as od \r\n" + 
				"on o.OrderID= od.OrderID\r\n" + 
				"inner join products as p \r\n" + 
				"on od.ProductID = p.ProductID \r\n" + 
				"where o.orderid="+orderNumber+";";
		
		ResultSet rs = statement.executeQuery(sqlOrderDetails);
		LinkedHashMap<Integer, Integer> productSetMap = new LinkedHashMap<Integer, Integer>();
		while(rs.next())
		{
			int productID = Integer.parseInt(rs.getString("ProductID"));
			int requiredQty = Integer.parseInt(rs.getString("Quantity"));
			int unitsInStock = Integer.parseInt(rs.getString("UnitsInStock"));
			int discontinued = Integer.parseInt(rs.getString("Discontinued"));

			if(discontinued ==1)
			{
				throw new OrderException("Order can't be shipped due to some discontinued products!", orderNumber);
			}

			if(requiredQty > unitsInStock)
			{
				throw new OrderException("Order can't be shipped since product quantity not adequate for product ID:"+productID, orderNumber);
			}
			productSetMap.put(productID, unitsInStock - requiredQty);
		}
		rs.close();
		productQySetter(orderNumber,productSetMap, statement);		
	}
/**
 *Method to set product Quantity to be reordered for given Supplier 	
 */
	private void productQySetter(int orderNumber,LinkedHashMap<Integer, Integer> productSetMap, Statement statement) throws SQLException {
		
		Iterator<Integer> iterator = productSetMap.keySet().iterator();
		while(iterator.hasNext())
		{
			int productID= iterator.next();
			int qty = productSetMap.get(productID);
			statement.executeUpdate("update products set UnitsInStock ="+qty+" where productID ="+productID+"; ");
		}
	}

	/**
	 * Method to retrieve information like database credentials, info 
	 */
	public static void retrieveCreds() {
		Properties prop = new Properties();
        String rootPath =System.getProperty("user.dir");		
        String info_file =  rootPath+"/config.properties";		//Retrieves data from properties file
        InputStream input =null;

		try {
			input = new FileInputStream(info_file);
			prop.load(input);
			database=prop.getProperty("database");
			user=prop.getProperty("user");
			password=prop.getProperty("password");
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	/**
	 * Method to handle reorders for a given date
	 */
	@Override
	public int Issue_reorders(int year, int month, int day) {
		Statement statement = null;
		String mm="",dd="";
		if(month<9)
			mm+="0"+month;
		
		if(day<9)
			dd+="0"+day;
		
		String givenDate = year+"-"+mm+"-"+dd;
	    
		
		retrieveCreds();
		 try 
	        {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	            
				connection = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, password);

	            statement = connection.createStatement();

	            statement.executeQuery("use "+database+";");
	            
	            String sqlShippingDetails = "select od.OrderID, o.ShippedDate, od.ProductID, od.UnitPrice, od.Quantity, p.UnitsInStock,p.UnitsOnOrder,p.ReorderLevel, p.Discontinued, s.SupplierID\r\n" + 
	            		"from orders as o inner join orderdetails as od \r\n" + 
	            		"on o.OrderID= od.OrderID\r\n" + 
	            		"inner join products as p \r\n" + 
	            		"on od.ProductID = p.ProductID\r\n" + 
	            		"inner join suppliers as s\r\n" + 
	            		"on p.SupplierID = s.SupplierID\r\n" + 
	            		"where o.ShippedDate = '"+givenDate +"'"+ 
	            		"group by o.ShippedDate,od.OrderID, od.ProductID\r\n" + 
	            		"order by o.ShippedDate desc";
	            
	            LinkedHashMap<Integer, TreeSet<Integer>> supplierProductsMap=new LinkedHashMap<Integer,TreeSet<Integer>>();
	            
	            ResultSet rs =statement.executeQuery(sqlShippingDetails);
	            while(rs.next())
	            {
	            	int supplierID  = Integer.parseInt(rs.getString("SupplierID"));
	            	 if(supplierProductsMap.containsKey(supplierID))
	                 {
	            		 TreeSet<Integer> prodList = supplierProductsMap.get(supplierID);
	            		 prodList.add(Integer.parseInt(rs.getString("ProductID")));
	            		 supplierProductsMap.put(supplierID, prodList);
	                 }
	                 else 
	            	 {
	                	 TreeSet<Integer> prodList= new TreeSet<Integer>();
	                	 prodList.add(Integer.parseInt(rs.getString("ProductID")));
	                	 supplierProductsMap.put(supplierID, prodList);
		             }
	            }
	            
	            Iterator<Integer> iterator =supplierProductsMap.keySet().iterator();
	            while(iterator.hasNext())
	            {
	            	int supplier =iterator.next();
	            	TreeSet<Integer> prodList = supplierProductsMap.get(supplier);
	            	handleSupplier(givenDate,supplier,prodList, statement);
	            }
	            
	            statement.close();
	            connection.close();
		        System.out.println("Sucessfully done!");
	        	}
		 	
			 	catch(Exception e) 
		        {
		 			System.out.println("e");
		        	if(e.getMessage().contains("Communications link failure"))
		        	{
		        		System.err.println("\nVPN is disconnected. Program terminated!");
		        		System.exit(1);
		        	}
		        	e.printStackTrace();
		        }
		
		return reorderSuppliersCount;
	}

	/**
	 *Method to handle each supplier individually for reorders at EOD for given date.  
	 */
	private void handleSupplier(String givenDate,int supplier, TreeSet<Integer> prodList, Statement statement) throws SQLException {
		// TODO Auto-generated method stub
		Iterator<Integer> iterator =prodList.iterator();
		
		String sqlProd = "";
		int lastPOrderID =addPurchaseOrder( givenDate,supplier, statement);
		Boolean supplierFlag=false;
		while(iterator.hasNext())
		{
			int product= iterator.next();
				String sql = "select od.OrderID, o.ShippedDate, od.ProductID, (od.UnitPrice/1.15) as Price, od.Quantity, p.UnitsInStock,p.UnitsOnOrder,p.ReorderLevel, p.Discontinued, s.SupplierID\r\n" + 
            		"from orders as o inner join orderdetails as od \r\n" + 
            		"on o.OrderID= od.OrderID\r\n" + 
            		"inner join products as p \r\n" + 
            		"on od.ProductID = p.ProductID\r\n" + 
            		"inner join suppliers as s\r\n" + 
            		"on p.SupplierID = s.SupplierID\r\n" + 
            		"where o.ShippedDate = '"+givenDate +"'"+ 
            		"and od.ProductID = "+product+
            		" group by o.ShippedDate,od.OrderID, od.ProductID\r\n" + 
            		"order by o.ShippedDate desc";
			
				ResultSet rs =statement.executeQuery(sql);
				rs.next();
	            int unitsInStock = Integer.parseInt(rs.getString("UnitsInStock"));
	            int unitsOnOrder =Integer.parseInt(rs.getString("UnitsOnOrder"));
	            int reorderLevel = Integer.parseInt(rs.getString("ReorderLevel"));
	            int discontinued = Integer.parseInt(rs.getString("Discontinued"));
	            double price = Double.parseDouble(rs.getString("Price"));
	            
	            if(reorderLevel==0)
	            	reorderLevel=20;
	            if(unitsOnOrder==0 && unitsInStock<=reorderLevel && discontinued!=1)
	            {
	            	if(supplierFlag==false )
	            	{
	            		reorderSuppliersCount++;
	            		supplierFlag=true;
	            	}
	            	
	            	addOrderDetail(lastPOrderID,product, price,reorderLevel, unitsInStock ,statement);  
	            	
	            }
		}
		
	}
	
	/**
	 * Method to Updating pur_orderdetails table.
	 * @param lastPOrderID - Parameter to hold last POrderID in purchaseorders table;
	 * @param price - Price at which product is to be purchsed from supplier
	 * @param reorderLevel - Level at which reorder is done  for a product.
	 */
	private void addOrderDetail(int lastPOrderID, int product, double price, int reorderLevel,int unitsInStock, Statement statement) throws SQLException {

	int orderQuantity = reorderLevel*4- unitsInStock;
		
		String sqlPurchaseOrderDetails ="insert into pur_orderdetails(POrderID, ProductID, Price, PQuantity)"
				+"values("+lastPOrderID+ ","+product+","+price+","+orderQuantity+")";  
		
		statement.executeUpdate(sqlPurchaseOrderDetails);
		
		String sqlUpdateInStock = "update products set UnitsOnOrder="+orderQuantity+" where productID ="+product+"\r\n"; 
		statement.executeUpdate(sqlUpdateInStock);
		
	}
	/**
	 * Method to add PO entry into purchaseorders table.
	 */
	private int addPurchaseOrder(String givenDate,int supplierID,Statement statement) throws SQLException {
		
		String sqlPurchaseOrder = "insert into purchaseorders(POrderDate, SupplierID)"
				+ "values('"+givenDate +"',"+supplierID+")";
		
		statement.executeUpdate(sqlPurchaseOrder);
		
		String sqlFindOrderID= "select max(POrderID) from purchaseorders;";
		
		ResultSet rs = statement.executeQuery(sqlFindOrderID);
		int lastOrderID = 0;
		while(rs.next())
		{
			lastOrderID=Integer.parseInt(rs.getString("max(POrderID)"));
		}
		return lastOrderID;
		
	}
	
	@Override
	/**
	 * Method to handle recieving orders from suppliers
	 */
	public void Receive_order(int internal_order_reference) throws OrderException {
		Statement statement = null;
	     retrieveCreds();
		 try 
	        {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	            
				connection = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, password);

	            statement = connection.createStatement();

	            statement.executeQuery("use "+database+";");
	            
	            ResultSet r =statement.executeQuery("select *from purchaseorders where POrderID ="+internal_order_reference+";");
	            
	            if (r.next() == false) 
	            {
	            	throw new OrderException("Purchase Order with given not present in database!", internal_order_reference);
	            } 
	           
	            else 
	            {
	                do {
	                	String suppliedDate=r.getString("SuppliedDate");
	                	if(suppliedDate!=null)
		            	{
		            		throw new OrderException("Order Already Shipped!", internal_order_reference); 
		            	}
	                } while (r.next());
	             }
	            
	            
	            statement.executeUpdate("update purchaseorders set SuppliedDate= curdate() where POrderID="+internal_order_reference+";");

	            ResultSet rs= statement.executeQuery("SELECT *from pur_orderdetails where POrderID="+internal_order_reference+";");
	            ArrayList<Integer> prodList = new ArrayList<Integer>();	          
	            while(rs.next())
	            {
	            	int prod =Integer.parseInt(rs.getString("ProductID"));
	            	prodList.add(prod);
	            }
	            
	            for (Integer prod : prodList) {
	            	statement.executeUpdate("update products set UnitsInStock=UnitsInStock+UnitsOnOrder where productid="+prod+";");
		            
		            statement.executeUpdate("update products set UnitsOnOrder=0 where productid="+prod+";");
	            	
				}
	            
	            System.out.println("Order Number: "+internal_order_reference+" arrived sucessfully!");
	            statement.close();
	            connection.close();
		        }
		 	
			 	catch(OrderException e)
			 	{
			 		e.printStackTrace();
			 	}
		 	
		 		catch(Exception e) 
		        {
		 			
		 			System.out.println("e");
		        	if(e.getMessage().contains("Communications link failure"))
		        	{
		        		System.err.println("\nVPN is disconnected. Program terminated!");
		        		System.exit(1);
		        	}
		        	e.printStackTrace();
		        }
		 		
	}

}
