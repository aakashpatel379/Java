
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

/**
 * Server class to manage operations and open a socket for client to connect.
 * @author Aakash Patel
 */
public class Server {
	
	public static int portnumber = 0;								//Port number for server to be opened at.
	public static String cookie= null;								//Manages cookie while authentication of employees.
	public static int employeeID=0;
	public static Customer customerData=new Customer();				//Customer class to manage customer data
	public static String user="";
	public static String password="";
	public static String database="";
	public static void main(String[] args) throws Exception {

		ServerSocket serverSocket = null;
		System.out.println("Enter port number:");
		Scanner scanner=new Scanner(System.in);
		portnumber=scanner.nextInt();
		
		retrieveCreds();		//retrieves credentials required to connect to db.
		try {

			System.setProperty("java.net.preferIPv4Stack", "true");
			serverSocket = new ServerSocket(portnumber, 1);  
			
		} catch (IOException e) {
			System.out.println("Could not listen on port: " + portnumber);
			System.exit(-1);
		}

		Socket clientSocket = null;
		while (true) {
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				System.out.println("Accept failed: " + portnumber);
				System.exit(-1);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  //reader for client socket.
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			String userInput;

			userInput = in.readLine();
			String outString="";
			while (userInput != null) {   							//main while loop to iterate input messages.
				
				String[] socketResponse = userInput.split(" ");
				String operation = socketResponse[0];				

				if(operation.equals("AUTH") && socketResponse[2].equals(Message.version))			//code construct to handle AUTH command messages from socket.
				{
					String secondLine = in.readLine();
					if(secondLine!=null)
					{
						String[] respArray = secondLine.split(" ");
						String pass = respArray[1];
						String target = socketResponse[1];
						Message m = auth(target, pass,out);
						outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
						if(cookie!=null)
						outString+="Set-Cookie:"+" "+cookie + m.CRLF;
						outString+=m.CRLF;
						out.println(outString);
						outString="";
					}
				}
				
				else if(operation.equals("LIST") && socketResponse[2].equals(Message.version))				//section to handle LIST operation.
				{
					String target =socketResponse[1];
					String cookieLine = in.readLine();
					if(cookieLine!=null)
					{
						String[] respArray =cookieLine.split(" ");
						String rcvdCookie = respArray[1];
						Message m = new Message();
						if(cookie!=null && !rcvdCookie.equals(cookie))
						{
							m.statusCode=404;
							m.statusmessage="Bad Client.Provided cookie doesn't exist.";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							continue;
						}
						else if(cookie==null)
						{
							m.statusCode=402;
							m.statusmessage="Wrong State.No prior Cookie exists";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							continue;
						}
						
						if(target.equals("customer"))				//methods to delegate specific operation as per target specified.
						listCustomer(out);

						else if(target.equals("product"))
						listProduct(out);
						
						else if(target.equals("order"))
						listOrder(out);
						
						else
						{
							m.statusCode= 403;
							m.statusmessage="Bad Target";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
						}
						
					}
				}
				else if(operation.equals("ORDER") && socketResponse[2].equals(Message.version))						//section to handle ORDER command messages
				{
					String target =socketResponse[1];
					String cookieLine = in.readLine();
					if(cookieLine!=null)
					{
						String[] respArray =cookieLine.split(" ");
						String rcvdCookie = respArray[1];
						Message m = new Message();
						if(cookie!=null && !rcvdCookie.equals(cookie))
						{
							m.statusCode=404;
							m.statusmessage="Bad Client.Provided cookie doesn't exist.";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							continue;
						}
						else if(cookie==null)
						{
							m.statusCode=402;
							m.statusmessage="Wrong State.No prior Cookie exists";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							continue;
						}
						
						if(target.equals("ORDER"))
						{
							if(!customerData.getCustID().equals("") && !customerData.getOrderData().prodList.isEmpty())
							{
								completeOrder(out);
							}
							else
							{
								if(customerData.getCustID().equals(""))
								{
									m.statusCode = 402;
									m.statusmessage="Bad State.Order not yet created.";
									outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
									outString+=m.CRLF;
									out.println(outString);
									outString="";
								}
								else if(customerData.getOrderData().prodList.isEmpty())
								{
									m.statusCode=402;
									m.statusmessage="Bad State"+"No products for the order.";
									outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
									outString+=m.CRLF;
									out.println(outString);
									outString="";
											
								}
							}
						}
						else {
							m.statusCode= 403;
							m.statusmessage="Bad Target";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
						}
						
					}
				}
				else if(operation.equals("DROP") && socketResponse[2].equals(Message.version))			//Code construct to handle DROP operation commands
				{
					String target =socketResponse[1];
					String cookieLine = in.readLine();
					if(cookieLine!=null)
					{
						String[] respArray =cookieLine.split(" ");
						String rcvdCookie = respArray[1];
						Message m = new Message();
						if(cookie!=null && !rcvdCookie.equals(cookie))
						{
							m.statusCode=404;
							m.statusmessage="Bad Client.Provided cookie doesn't exist.";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							continue;
						}
						else if(cookie==null)
						{
							m.statusCode=402;
							m.statusmessage="Wrong State.No prior Cookie exists";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							continue;
						}
						
						if(target.equals("DROP"))
						{
							ArrayList<Product> prodList= customerData.orderData.prodList;
							if(prodList.isEmpty() && customerData.getCustID().isEmpty())
							{
								m.statusCode= 402;
								m.statusmessage="Bad State.No order opened.";
								outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
								outString+=m.CRLF;
								out.println(outString);
								outString="";
							}
							else 
							{
								customerData=new Customer();
								m.statusCode= 200;
								m.statusmessage="ok";
								outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
								outString+=m.CRLF;
								out.println(outString);
								outString="";
							}
						}
						else
						{
							m.statusCode= 402;
							m.statusmessage="Bad Target";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
						}
					}
					
				}
				
				else if(operation.equals("LOGOUT") && socketResponse[2].equals(Message.version))   //To handle LOGOUT of employee
				{
					String target =socketResponse[1];
					String cookieLine = in.readLine();
					if(cookieLine!=null)
					{
						String[] respArray =cookieLine.split(" ");
						String rcvdCookie = respArray[1];
						Message m = new Message();
						if(cookie!=null && !rcvdCookie.equals(cookie))
						{
							m.statusCode=404;
							m.statusmessage="Bad Client.Provided cookie doesn't exist.";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							continue;
						}
						else if(cookie==null)
						{
							m.statusCode=402;
							m.statusmessage="Wrong State.No prior Cookie exists";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							continue;
						}
						
						if(target.equals("LOGOUT"))
						{
							
							m.statusCode=200;
							m.statusmessage="ok";
							cookie=null;
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							clientSocket.close();
							out.close();
							in.close();
						}
						else 
						{
							m.statusCode= 403;
							m.statusmessage="Bad Target";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
						}
						
					}
					
				}
				
				else if(operation.equals("ORDER") && socketResponse[2].equals(Message.version))  //Deals with Order and related operations
				{
					String target = socketResponse[1];
					String cookieLine = in.readLine();
					if(cookieLine!=null)
					{
						String[] respArray =cookieLine.split(" ");
						String rcvdCookie = respArray[1];
						Message m = new Message();
						if(cookie!=null && !rcvdCookie.equals(cookie))
						{
							m.statusCode=404;
							m.statusmessage="Bad Client.Provided cookie doesn't exist.";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							continue;
						}
						else if(cookie==null)
						{
							m.statusCode=402;
							m.statusmessage="Wrong State.No prior Cookie exists";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							continue;
						}
						
						if(target.equals("ORDER"))
						{
							if(customerData.getCustID().isEmpty())
							{
								m.statusCode=402;
								m.statusmessage="Bad State.Order doesn't exist.";
								outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
								outString+=m.CRLF;
								out.println(outString);
								outString="";
								continue;
							}
							else if(customerData.getOrderData().prodList.isEmpty())
							{
								m.statusCode=403;
								m.statusmessage="No products added yet.";
								outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
								outString+=m.CRLF;
								out.println(outString);
								outString="";
								continue;
							}
							else 
							{
								completeOrder(out);
							}
							
							
							
						}
						
						
					}
				}
				
				
				
				else if(operation.equals("NEW") && socketResponse[2].equals(Message.version))           //Handles operations for NEW related commands to open orders.
				{
					String custID = socketResponse[1];
					String cookieLine = in.readLine();
					if(cookieLine!=null)
					{
						String[] respArray =cookieLine.split(" ");
						String rcvdCookie = respArray[1];
						Message m = new Message();
						if(cookie!=null && !rcvdCookie.equals(cookie))
						{
							m.statusCode=404;
							m.statusmessage="Bad] Client.Provided cookie doesn't exist.";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							continue;
						}
						else if(cookie==null)
						{
							m.statusCode=402;
							m.statusmessage="Wrong State.No prior Cookie exists";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							continue;
						}
						
						if(!customerData.getCustID().isEmpty())
						{
							m.statusCode=406;
							m.statusmessage="Open order already exists.";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							continue;
						}
						
						String custInfo[] =new String[6];
						String address="", region="", postalcode="", country="", city="";
						int completenessCount=0;
						for(int i=0; i<=4 ; i++)
						{
							String inp = " ";
							inp=in.readLine();
							
							if(i==0 && inp.trim().equals(""))
							{
								break;
							}
							if(inp == null || inp.equals(""))
							{
								m.statusCode=405;
								m.statusmessage="Incomplete Address";
								outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
								outString+=m.CRLF;
								out.println(outString);
								outString="";
								m=new Message();
								continue;
							}
							else
							{
								String data[] = inp.split(" ");
								
								if(data[0].equals("Address:"))
							    {	
									completenessCount++;
									address=data[1];
								}
								else if(data[0].equals("Region:"))
								{
									completenessCount++;
									region = data[1];
								}
								else if(data[0].equals("Country:"))
								{
									completenessCount++;
									country=data[1];
								}
								else if(data[0].equals("City:"))
								{
									completenessCount++;
									city = data[1];
								}
								else if(data[0].equals("PostalCode:"))
								{
									completenessCount++;
									postalcode=data[1];
								}
								else {
									m.statusCode=402;
									m.statusmessage="Wrong State! Other information got in than Customers.";
									outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
									outString+=m.CRLF;
									out.println(outString);
									outString="";
									m=new Message();
									continue;	
								}
							}
						}
						if(completenessCount!=5)
						{
							if(!custID.isEmpty() && address.isEmpty() && country.isEmpty() && region.isEmpty() && city.isEmpty() && postalcode.isEmpty())
							{
								checkAndUpdateCustomer(custID,address, country, city, region, postalcode, out);                      //To update Customer as per data provided.
								
							}
							
							else
							{
							m.statusCode=405;
							m.statusmessage="Incomplete Address.";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							}
						}
						else
						checkAndUpdateCustomer(custID,address, country, city, region, postalcode, out);
						
					}
				}
				else if(operation.equals("ADD") && socketResponse[2].equals(Message.version))						//section to handle ADD command related operations.
				{
					int prodID=0;
					try{
						prodID=Integer.parseInt(socketResponse[1]);
					}
					catch(Exception e)
					{
						Message m =new Message();
						m.statusCode=406;
						m.statusmessage="Bad Product ID! " +e.getMessage();
						outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
						outString+=m.CRLF;
						out.println(outString);
						outString="";
					}
					
					String cookieLine = in.readLine();
					System.out.println(cookieLine);
					if(cookieLine!=null)
					{
						String[] respArray =cookieLine.split(" ");
						String rcvdCookie = respArray[1];
						Message m = new Message();
						if(cookie!=null && !rcvdCookie.equals(cookie))
						{
							m.statusCode=404;
							m.statusmessage="Bad Client.Provided cookie doesn't exist.";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							in.readLine();
							in.readLine();
							in.readLine();
							userInput=in.readLine();
							continue;
						}
						else if(cookie==null)
						{
							m.statusCode=402;
							m.statusmessage="Wrong State.No prior Cookie exists";
							outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
							outString+=m.CRLF;
							out.println(outString);
							outString="";
							m=new Message();
							in.readLine();
							in.readLine();
							in.readLine();
							userInput=in.readLine();
							continue;
						}
						
						String contentLine=in.readLine();
						contentLine= contentLine.replace(":", ": ");
						String contentArray[] = contentLine.split(" ");
						String content="";
						
						int contentLen = Integer.parseInt(contentArray[1]) -2;
						int charCount=0, last=0;
						while(contentLen > content.length())
						{
							String line=in.readLine();
							content+=line;
						}
						String bodyContent=content.substring(0, contentLen);
						bodyContent.replace("\r\n", "");
						bodyContent.trim();
						
						int prodQty;
						try
						{
						if(bodyContent.equals("0"))
							throw new Exception("Qty is zero");
						
						prodQty = Integer.valueOf(bodyContent);
						if(prodID!=0 && prodQty!=0)
						{
							if(!customerData.getCustID().equals(""))
							{
								addProduct(prodID, prodQty, out);
							}
							else
							{
								m.statusCode=405;
								m.statusmessage="No order created.";
								outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
								outString+=m.CRLF;
								out.println(outString);
								outString="";
							}
						}
						else
						{
							Message m1 =new Message();
							if(prodID==0)
							{
								m1.statusCode=409;
								m1.statusmessage="Bad ProductID";
								outString+=Message.version+ " "+ m1.statusCode+ " "+ m1.statusmessage+ m1.CRLF;
								outString+=m1.CRLF;
								out.println(outString);
								outString="";
							}
						}
						}
						catch(NumberFormatException e)
						{
							Message m1=new Message();
							m1.statusCode=410;
							m1.statusmessage="Unrecognizable Product Qty! "+ e.getMessage();
							outString+=Message.version+ " "+ m1.statusCode+ " "+ m1.statusmessage+ m1.CRLF;
							outString+=m1.CRLF;
							out.println(outString);
							outString="";
						}
						
						catch(Exception e)
						{
							Message m1 =new Message();
							
							if(e.getMessage().equals("Qty is zero"))
							{
								m1.statusCode=408;
								m1.statusmessage="Bad Order! ";
							}
							outString+=Message.version+ " "+ m1.statusCode+ " "+ m1.statusmessage+ m1.CRLF;
							outString+=m1.CRLF;
							out.println(outString);
							outString="";
						}
					}
				}

				userInput = in.readLine();
			}
		}

	}
	/** Method to complete current.Order 
	 *  Called in response to ORDER command.
	 * @param out
	 */
	private static void completeOrder(PrintWriter out) {
		// TODO Auto-generated method stub

		String outString="";
		Connection connection = null;
		Message m=new Message();
		int orderCount=0;
		try 
        {
            String address=customerData.getAddress();
            String city=customerData.getCity();
            String region=customerData.getRegion();
            String postalcode=customerData.getPostalCode();
            String custID=customerData.getCustID();
            String country=customerData.getCountry();
            
			Class.forName("com.mysql.cj.jdbc.Driver");
            
			connection = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, password);

            Statement statement = connection.createStatement();

			statement.executeQuery("use "+database+";");
			
			String sqlMaxOrderFetch="select max(OrderID) from orders limit 1;";
			ResultSet rs= statement.executeQuery(sqlMaxOrderFetch);
			
			if(rs.next())
			{
				orderCount= Integer.parseInt(rs.getString("max(OrderID)"));
				
			}
			orderCount++;
			customerData.orderData.setOrderID(orderCount);
			
			String sqlOrderInsert = "insert into orders(OrderID,CustomerID,EmployeeID,OrderDate, ShipAddress, ShipCity, ShipRegion, ShipPostalCode, ShipCountry)\n" + 
					"values(?, ?, ?, curdate(), ?, ?, ?, ? ,?);";
			PreparedStatement pst=connection.prepareStatement(sqlOrderInsert);
			pst.setInt(1, orderCount);
			pst.setString(2, custID);
			pst.setInt(3, employeeID);
			pst.setString(4, address);
			pst.setString(5, city );
			pst.setString(6, region);
			pst.setString(7, postalcode);
			pst.setString(8, country);
			
			pst.executeUpdate();
			
			ArrayList<Product> prodList =customerData.orderData.prodList; 
			String sqlProd = "insert into orderdetails(OrderID, ProductID,Quantity,UnitPrice) values(?, ?, ? ,?);";
			Statement stmt =connection.prepareStatement(sqlProd);
			for(Product p: prodList)
			{
				String query="insert into orderdetails(OrderID, ProductID,Quantity,UnitPrice) values("+orderCount+", "+p.getProductID() +", "+p.getProdQty()+" ,"+p.getProdPrice()+");";
				stmt.executeUpdate(query);
			}
			stmt.close();
			m.statusCode=200;
			String orderString = orderCount+"";
			m.statusmessage="ok";
			outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
			outString+="Content-Length:"+(orderString.length()+2)+ m.CRLF;
			outString+=m.CRLF;
			outString+=orderString;
			out.println(outString);
			outString="";
			
			connection.close();
			customerData=new Customer();
			
        }
		catch (Exception e) {

			Message msg = new Message();
			if (e.getMessage().contains("Communications link failure")) {
				msg.statusCode = 500;
				msg.statusmessage = "VPN Disconnected! Internal Server error";

			} else {
				msg.statusCode = 501;
				msg.statusmessage = "Internal Server error. " + e.getMessage();
			}
			outString += Message.version + " " + msg.statusCode + " " + msg.statusmessage + msg.CRLF;
			outString += msg.CRLF;
			out.println(outString);
		}
	}

	/**
	 * Method to list products added so far in product with their quantity.
	 * @param out
	 */
	private static void listOrder(PrintWriter out) {
		// TODO Auto-generated method stub
		ArrayList<Product> productList = customerData.orderData.prodList;
		String respContent ="", outString="";
		
		Message m=new Message();
		if(productList.isEmpty())
		{
			m.statusCode=401;
			m.statusmessage="No products in current open order.";
			outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
			outString+=m.CRLF;
			out.println(outString);
		}
		else
		{
			for(Product p: productList)
			{
				m.statusCode=200;
				m.statusmessage="ok";
				respContent+=p.getProductID()+ "\t"+ p.getProdQty() +Message.CRLF;
				
			}
			outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage +m.CRLF;
			outString+="Content-Length:"+respContent.length()+ m.CRLF;
			outString+=m.CRLF;
			outString+=respContent;
			out.println(outString);
		}
		
		
	}
	/**
	 * Method to list all products in DB.
	 * @param out
	 */
	private static void listProduct(PrintWriter out) {

		String outString="";
		Connection connection = null;
		Message m=new Message();
		try 
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
			connection = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, password);

            Statement statement = connection.createStatement();

			statement.executeQuery("use "+database+";");
			
			String sqlProductFetch="select ProductID,ProductName from products ORDER by ProductID asc;";
			
			ResultSet rs=statement.executeQuery(sqlProductFetch);
			if(rs.next()==true)
			{
				String respContent ="";
				do
				{
					respContent+= rs.getString("ProductID")+ "\t"+ rs.getString("ProductName")+ Message.CRLF;
					
				}while(rs.next());
				
				m.statusCode=200;
				m.statusmessage="ok";
				outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
				outString+="Content-Length:"+respContent.length()+ m.CRLF;
				outString+=m.CRLF;
				outString+=respContent;
				out.println(outString);
				outString="";
				rs.close();

			}
			else
			{
				m.statusCode=401;
				m.statusmessage="No products to display.";
				outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
				outString+=m.CRLF;
				out.println(outString);
			}
			
        }
		catch (Exception e) {

			Message msg = new Message();
			if (e.getMessage().contains("Communications link failure")) {
				msg.statusCode = 500;
				msg.statusmessage = "VPN Disconnected! Internal Server error";

			} else {
				msg.statusCode = 501;
				msg.statusmessage = "Internal Server error. " + e.getMessage();
			}
			outString += Message.version + " " + msg.statusCode + " " + msg.statusmessage + msg.CRLF;
			outString += msg.CRLF;
			out.println(outString);
		}
		
	}

	/**
	 * Method to list all the customers in the database.
	 * @param out
	 */
	private static void listCustomer(PrintWriter out) {
		
		String outString="";
		Connection connection = null;
		Message m=new Message();
		try 
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
			connection = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, password);

            Statement statement = connection.createStatement();

			statement.executeQuery("use "+database+";");
			
			String sqlCustomerFetch="select CustomerID, CompanyName from customers order by CustomerID asc;";
			
			ResultSet rs=statement.executeQuery(sqlCustomerFetch);
			if(rs.next()==true)
			{
				String respContent ="";
				do
				{
					String line =  rs.getString("CustomerID")+ "\t"+ rs.getString("CompanyName")+ Message.CRLF;
					respContent+=line;

				}while(rs.next());
				m.statusCode=200;
				m.statusmessage="ok";
				outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
				outString+="Content-Length:"+respContent.length()+ m.CRLF;
				outString+=m.CRLF;
				outString+=respContent;
				out.println(outString);
				outString="";
				rs.close();
			}
			else
			{
				m.statusCode=401;
				m.statusmessage="No customers to display.";
				outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
				outString+=m.CRLF;
				out.println(outString);
			}
			
        }
		catch (Exception e) {

			Message msg = new Message();
			if (e.getMessage().contains("Communications link failure")) {
				msg.statusCode = 500;
				msg.statusmessage = "VPN Disconnected! Internal Server error";

			} else {
				msg.statusCode = 501;
				msg.statusmessage = "Internal Server error" + e.getMessage();
			}
			outString += Message.version + " " + msg.statusCode + " " + msg.statusmessage + msg.CRLF;
			outString += msg.CRLF;
			out.println(outString);
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
 * Method to add product in order
 * @param prodID -ID of product
 * @param prodQty -Quantity
 */
	private static void addProduct(int prodID, int prodQty, PrintWriter out) {

		String outString="";
		double unitPrice =0;
		Connection connection = null;
		try 
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
			connection = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, password);

            Statement statement = connection.createStatement();

			statement.executeQuery("use "+database+";");
			
			String sqlProductFetch="select *from products where ProductID='"+prodID+"';";
			
			ResultSet rs=statement.executeQuery(sqlProductFetch);
			Message m =new Message();
			if (rs.next()) {
				int discontinued = Integer.parseInt(rs.getString("Discontinued"));
				int unitsInStock = Integer.parseInt(rs.getString("UnitsInStock"));
				unitPrice = Double.parseDouble(rs.getString("UnitPrice"));
				if (discontinued == 1) {
					if (unitsInStock > 0) {
						ArrayList<Product> prodList = customerData.orderData.prodList;
						ArrayList<Product> updated = new ArrayList<Product>();
						Boolean containsFlag = false;
						if (!prodList.isEmpty()) {
							Product current = new Product();
							current.setProductID(prodID);
							current.setProdQty(prodQty);
							current.setProdPrice(unitPrice);
							for (Product p : prodList) {
								if (p.getProductID() == prodID) {
									containsFlag = true;
									current.setProdQty(current.getProdQty() + p.getProdQty());
								} else {
									updated.add(p);
								}
							}
							if (containsFlag) {
								updated.add(current);
								customerData.orderData.setProdList(updated);
							} else {
								updated.add(current);
								customerData.orderData.setProdList(updated);
							}

						} else {
							Product current = new Product();
							current.setProductID(prodID);
							current.setProdQty(prodQty);
							current.setProdPrice(unitPrice);
							customerData.orderData.prodList.add(current);
						}
						m.statusCode = 200;
						m.statusmessage = "ok";
						outString += Message.version + " " + m.statusCode + " " + m.statusmessage + m.CRLF;
						outString += m.CRLF;
						out.println(outString);
						outString = "";
						statement.close();
						connection.close();
						rs.close();
						return;

					}
					else
					{
						m.statusCode = 407;
						m.statusmessage="Discontinued Product.";
						outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
						outString+=m.CRLF;
						out.println(outString);
						outString="";
						statement.close();
						connection.close();
						rs.close();
						return;
						
					}
				}

				else
				{
					ArrayList<Product> prodList= customerData.orderData.prodList;
					ArrayList<Product> updated =new ArrayList<Product>();
					Boolean containsFlag=false;
					if(!prodList.isEmpty())
					{
						Product current = new Product();
						current.setProductID(prodID);
						current.setProdQty(prodQty);
						current.setProdPrice(unitPrice);
						for(Product p: prodList)
						{
							if(p.getProductID()==prodID)
							{
								containsFlag=true;
								current.setProdQty(current.getProdQty()+ p.getProdQty());
							}
							else 
							{
								updated.add(p);
							}
						}
						if(containsFlag)
						{
							updated.add(current);
							customerData.orderData.setProdList(updated);
						}
						else {
							updated.add(current);
							customerData.orderData.setProdList(updated);	
						}
						
					}
					else {
						Product current = new Product();
						current.setProductID(prodID);
						current.setProdQty(prodQty);
						current.setProdPrice(unitPrice);
						customerData.orderData.prodList.add(current);
					}
					m.statusCode=200;
					m.statusmessage="ok";
					outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
					outString+=m.CRLF;
					out.println(outString);
					outString="";
					statement.close();
					connection.close();
					rs.close();
					return;
				}
			}
			else
			{
				m.statusCode=406;
				m.statusmessage="Bad Product";
				outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
				outString+=m.CRLF;
				out.println(outString);
				outString="";
				statement.close();
				connection.close();
				rs.close();
				return;
			}
        }
		catch (Exception e) {

			Message m = new Message();
			if (e.getMessage().contains("Communications link failure")) {
				m.statusCode = 500;
				m.statusmessage = "VPN Disconnected! Internal Server error";

			} else {
				m.statusCode = 501;
				m.statusmessage = "Internal Server error" + e.getMessage();
			}
			outString += Message.version + " " + m.statusCode + " " + m.statusmessage + m.CRLF;
			outString += m.CRLF;
			out.println(outString);
		}
	}

	/**
	 * Method to Update Customer as per data recieved from client side.
	 * @param custID - Customer ID
	 * @param address -Address of customer
	 * @param country -Country variable
	 * @param city -City
	 * @param region -Region
	 * @param postalcode -POstal code
	 * @param out
	 */
	private static void checkAndUpdateCustomer(String custID, String address,  String country, String city, String region,String postalcode, PrintWriter out) {

			Connection connection = null;
			try 
	        {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	            
				connection = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, password);

	            Statement statement = connection.createStatement();

				statement.executeQuery("use "+database+";");

	            String sqlSearchCust="SELECT * FROM customers where CustomerID='"+ custID+"';";
	            ResultSet rs = statement.executeQuery(sqlSearchCust);
	            Message m=new Message();
	            String outString="";
	            if(rs.next() == false)
	            {
	            	m.statusCode=404;
	            	m.statusmessage="Bad Client!";
	            	outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
					outString+=m.CRLF;
					out.println(outString);
					outString="";
					rs.close();
					statement.close();
		            connection.close();
					return;
	            }
	            else
	            {
	            	if(address.isEmpty())
	            	{
		            	String currAddress = rs.getString("Address");
		            	String currRegion = rs.getString("Region");
		            	String currPostalCode = rs.getString("PostalCode");
		            	String currCountry =rs.getString("Country");
		            	String currCity=rs.getString("City");
		            	customerData.setCustID(custID);
		            	customerData.setAddress(currAddress);
		            	customerData.setCity(currCity);
		            	customerData.setCountry(currCountry);
		            	customerData.setPostalCode(currPostalCode);
		            	customerData.setRegion(currRegion);
	
	            	}
	            	else {
	            	customerData.setCustID(custID);
	            	customerData.setAddress(address);
	            	customerData.setCity(city);
	            	customerData.setCountry(country);
	            	customerData.setPostalCode(postalcode);
	            	customerData.setRegion(region);
	            	}
	            }
	            m.statusCode=200;
				m.statusmessage="ok";
				outString+=Message.version+ " "+ m.statusCode+ " "+ m.statusmessage+ m.CRLF;
				outString+=m.CRLF;
				out.println(outString);
				rs.close();
				statement.close();
	            connection.close();
		    }
		 			 	
			catch (Exception e) {

				Message m = new Message();
				String outString = "";
				if (e.getMessage().contains("Communications link failure")) {
					m.statusCode = 500;
					m.statusmessage = "VPN Disconnected! Internal Server error";

				} else {
					m.statusCode = 501;
					m.statusmessage = "Internal Server error" + e.getMessage();
				}

				outString += Message.version + " " + m.statusCode + " " + m.statusmessage + m.CRLF;
				outString += m.CRLF;
				out.println(outString);
			}
		
	}

	/**
	 * Method to handle authentication of employee from information recieved.
	 * @return - Returns Message format object for later validation of success. 
	 */
	private static Message auth(String lastname, String dob, PrintWriter out) {

		String username = "";
		Connection connection = null;
		Message msg = new Message();
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");

			connection = DriverManager.getConnection(
					"jdbc:mysql://db.cs.dal.ca:3306?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
					user, password);

			Statement statement = connection.createStatement();

			statement.executeQuery("use " + database + ";");

			String sqlSearchEmp = "SELECT * FROM employees where lastname='" + lastname + "' and BirthDate = '" + dob
					+ "';";
			ResultSet rs = statement.executeQuery(sqlSearchEmp);

			if (rs.next() == false) {
				System.out.println("No employee found.");

				msg.statusCode = 401;
				msg.statusmessage = "Unauthorized!";

			} else {
				int empid = Integer.parseInt(rs.getString("EmployeeID"));
				employeeID = empid;
				username += empid;
				cookie = lastname + username;
				msg.statusCode = 200;
				msg.statusmessage = "ok";
			}

			statement.close();
			connection.close();
		}

		catch (Exception e) {

			Message m = new Message();
			String outString = "";
			if (e.getMessage().contains("Communications link failure")) {
				m.statusCode = 500;
				m.statusmessage = "VPN Disconnected! Internal Server error";

			} else {
				m.statusCode = 501;
				m.statusmessage = "Internal Server error" + e.getMessage();
			}

			outString += Message.version + " " + m.statusCode + " " + m.statusmessage + m.CRLF;
			outString += m.CRLF;
			out.println(outString);
		}

		finally {
			return msg;
		}

	}
}
