import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
/**
 * @author Aakash Patel  -B00807065
 */
public class HistoryMain {
	static String user="";
	static String password="";
	static String database=""; 
	static Connection connection = null;
	static String historyData = "";
	public static void main(String[] args) throws SQLException {
		
        Statement statement = null;
        retrieveCreds();		//retrieves credentials required to connect to db.
		
        try 
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
			connection = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, password);

            statement = connection.createStatement();

            statement.executeQuery("use "+database+";");
          
            String sqlProduct ="select * from products order by productid asc";

            ResultSet r = statement.executeQuery(sqlProduct);
            
            LinkedHashMap<Integer, ArrayList<Integer>> hMap = new LinkedHashMap<Integer, ArrayList<Integer>>();
            while(r.next())
            {
        	   	ArrayList<Integer> list = new ArrayList<Integer>();
               	int productID = Integer.parseInt(r.getString("ProductID"));
                 
                int dayEnd = Integer.parseInt(r.getString("UnitsInStock"));
             	
             	int reorderlevel = Integer.parseInt(r.getString("ReorderLevel"));
             	
             	int unitsOnOrder =  Integer.parseInt(r.getString("UnitsOnOrder"));
             	
             	list.add(dayEnd);
             	list.add(reorderlevel);
            	list.add(unitsOnOrder);
            	hMap.put(productID, list);
            }
           r.close();
           Iterator<Integer> i = hMap.keySet().iterator();
            while(i.hasNext())
            {
            	int productID = i.next();
            	ArrayList<Integer> list=hMap.get(productID);
            	int dayEnd = list.get(0);
            	int reorderlevel = list.get(1);
            	int unitsOnOrder= list.get(2);
            	historyData+="\nProduct ID: "+productID+"\n";
            	historyData+="PO_Date    \tQty\tPrice\tTotal\n";
            	
            	// To handle 4 different cases for different combinations of values possible for fields of products table.
				if (dayEnd > 0 && unitsOnOrder == 0 && reorderlevel > 0)
					case1(productID, dayEnd, reorderlevel, unitsOnOrder, statement);

				if (dayEnd >= 0 && unitsOnOrder > 0 && reorderlevel > 0)
					case2(productID, dayEnd, reorderlevel, unitsOnOrder, statement);

				if (dayEnd > 0 && unitsOnOrder == 0 && reorderlevel == 0)
					case3(productID, dayEnd, reorderlevel, unitsOnOrder, statement);

				if (dayEnd == 0 && unitsOnOrder == 0 && reorderlevel == 0)
					case2(productID, dayEnd, 20, unitsOnOrder, statement);
			}
            
            System.out.print(historyData);
            System.out.println("History exported sucessfully!");
            BufferedWriter writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir")+"/HistoryData.txt"));
            
            setUIS(statement); 
            
            writer.write(historyData);
            writer.close();
	        }
	        catch (Exception e) 
	        {
	        	if(e.getMessage().contains("Communications link failure"))
	        	{
	        		System.err.println("\nVPN is disconnected. Program terminated!");
	        		System.exit(1);
	        	}
	        	e.printStackTrace();
	        }
        	finally {
        		statement.close();
        		connection.close();
        	}
	}

	/**
	 * TO reset UnitsInStock for products after building history.
	 */
	private static void setUIS(Statement stmt) throws SQLException {
		stmt.executeUpdate("update products set UnitsInStock =UnitsInStock + UnitsOnOrder,UnitsOnOrder=0;");
		
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

	private static void case3(int productID, int dayEnd, int reorderlevel, int unitsOnOrder, Statement statement) throws SQLException {
		String sqlShipperSales = "select o.ShippedDate,ProductID,(UnitPrice/1.15) as Price, sum(Quantity) as UnitsSold\r\n" + 
        		"from orders as o inner join orderdetails as od\r\n" + 
        		"on o.orderID = od.OrderID\r\n" + 
        		"where ShippedDate is not null and productid="+productID+"\r\n" + 
        		"group by o.ShippedDate, UnitPrice\r\n" + 
        		"order by ShippedDate desc,ProductID asc\r\n" + 
        		";";
		int targetLevel = 0;
        ResultSet rSales = statement.executeQuery(sqlShipperSales);
        int reorderUnits, salesDayEnd, daySales, dayStart=0;
        double price;
        String shipDay="";
		 while(rSales.next()) 
         {	
         	shipDay = rSales.getString("ShippedDate");
         	
         	if(reorderlevel==0)
         	{
         		reorderlevel =  dayEnd/4;
         		targetLevel=dayEnd;
         	}
         	
         	if(dayEnd >= targetLevel)
         	{
         		salesDayEnd = reorderlevel;
         		reorderUnits = dayEnd - salesDayEnd;
         		daySales=Integer.parseInt(rSales.getString("UnitsSold"));
             	dayStart = daySales+salesDayEnd;
             	price = Double.parseDouble(rSales.getString("Price"));
         		if(reorderUnits!=0)
         			historyData+=shipDay+"\t"+reorderUnits+"\t"+String.format("%.2f", price)+"\t"+String.format("%.2f", price*reorderUnits)+"\n";
             	dayEnd=dayStart;
         	}
         	
         	else if(dayEnd < targetLevel)
         	{
         		reorderUnits=0;
         		salesDayEnd =dayEnd;
         		daySales=Integer.parseInt(rSales.getString("UnitsSold"));
         		dayStart = daySales+salesDayEnd;
         		price = Double.parseDouble(rSales.getString("Price"));
         		if(reorderUnits!=0)
         			historyData+=shipDay+"\t"+reorderUnits+"\t"+String.format("%.2f", price)+"\t"+String.format("%.2f", price*reorderUnits)+"\n";
         		dayEnd=dayStart;
         	}
         }
		rSales.close(); 
		
	}

	private static void case2(int productID, int dayEnd, int reorderlevel, int unitsOnOrder, Statement statement) throws SQLException {
		String sqlShipperSales = "select o.ShippedDate,ProductID,(UnitPrice/1.15) as Price, sum(Quantity) as UnitsSold\r\n" + 
        		"from orders as o inner join orderdetails as od\r\n" + 
        		"on o.orderID = od.OrderID\r\n" + 
        		"where ShippedDate is not null and productid="+productID+"\r\n" + 
        		"group by o.ShippedDate, UnitPrice\r\n" + 
        		"order by ShippedDate desc,ProductID asc\r\n" + 
        		";";
		
		int targetLevel = reorderlevel*4;
        ResultSet rSales = statement.executeQuery(sqlShipperSales);
        int reorderUnits, salesDayEnd, daySales, dayStart=0;
        double price;
        String shipDay="";
		 while(rSales.next()) 
         {	
         	shipDay = rSales.getString("ShippedDate");
         	if(dayEnd < targetLevel )
         	{
             	salesDayEnd=dayEnd;
             	if (dayStart==0)
             	{
             		reorderUnits=unitsOnOrder;
             		dayEnd =reorderUnits+ salesDayEnd;
             	}
             	else 
             		reorderUnits=0;
             	daySales=Integer.parseInt(rSales.getString("UnitsSold"));
             	dayStart= salesDayEnd + daySales;
             	price = Double.parseDouble(rSales.getString("Price"));
             	if(reorderUnits!=0)
             		historyData+=shipDay+"\t"+reorderUnits+"\t"+String.format("%.2f", price)+"\t"+String.format("%.2f", price*reorderUnits)+"\n";
            	dayEnd=dayStart;
         	}
         	else if(dayEnd>= targetLevel)
         	{
         		if(dayStart!=0)
         			dayEnd = dayStart;
         		
         		salesDayEnd = reorderlevel;
         		reorderUnits = dayEnd - salesDayEnd;
         		daySales=Integer.parseInt(rSales.getString("UnitsSold"));
         		dayStart= salesDayEnd + daySales;
             	price = Double.parseDouble(rSales.getString("Price"));
         		if(reorderUnits!=0)
         			historyData+=shipDay+"\t"+reorderUnits+"\t"+String.format("%.2f", price)+"\t"+String.format("%.2f", price*reorderUnits)+"\n";
            	dayEnd=dayStart;
         	}

         }
	     rSales.close(); 
	}

	private static void case1(int productID, int dayEnd, int reorderlevel, int unitsOnOrder, Statement statement)  throws SQLException{
		String sqlShipperSales = "select o.ShippedDate,ProductID,(UnitPrice/1.15) as Price, sum(Quantity) as UnitsSold\r\n" + 
        		"from orders as o inner join orderdetails as od\r\n" + 
        		"on o.orderID = od.OrderID\r\n" + 
        		"where ShippedDate is not null and productid="+productID+"\r\n" + 
        		"group by o.ShippedDate, UnitPrice\r\n" + 
        		"order by ShippedDate desc,ProductID asc\r\n" + 
        		";";
		
		int targetLevel = reorderlevel*4;
        ResultSet rSales = statement.executeQuery(sqlShipperSales);
        int reorderUnits, salesDayEnd, daySales, dayStart=0;
        double price;
        String shipDay="";
		 while(rSales.next()) 
         {	
         	shipDay = rSales.getString("ShippedDate");
         		
         	if(dayEnd<targetLevel)
         	{
         		reorderUnits=0;
         		salesDayEnd=dayEnd;
         		daySales=Integer.parseInt(rSales.getString("UnitsSold"));
         		dayStart= salesDayEnd+daySales;
             	price = Double.parseDouble(rSales.getString("Price"));
             	if(reorderUnits!=0)
             		historyData+=shipDay+"\t"+reorderUnits+"\t"+String.format("%.2f", price)+"\t"+String.format("%.2f", price*reorderUnits)+"\n";
             	dayEnd = dayStart;
	            	
         	}
         	else if(dayEnd>=targetLevel)
         	{
         		if(dayStart!=0)
             		dayEnd = dayStart;
         		salesDayEnd = reorderlevel;
         		reorderUnits = dayEnd - reorderlevel;
         		daySales=Integer.parseInt(rSales.getString("UnitsSold"));
         		dayStart= salesDayEnd + daySales;		
             	price = Double.parseDouble(rSales.getString("Price"));
         		if(reorderUnits!=0)
         			historyData+=shipDay+"\t"+reorderUnits+"\t"+String.format("%.2f", price)+"\t"+String.format("%.2f", price*reorderUnits)+"\n";
            	dayEnd=dayStart;
         	}
	     }
		 rSales.close(); 
	}

}
