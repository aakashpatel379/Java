import java.util.Scanner;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

// A class to manage the connection between an employee in the Northwood system and the
// online system that accepts orders using the Order3901 protocol.

public class OrderConnection {
	// Keep information about the connection to the server through the network
	private Socket toServer = null;
	private BufferedReader in = null;
	private PrintWriter out = null;

	// Once connected and authenticated, we are given a cookie to identify
	// ourselves.  Store that cookie.

	private String sessionCookie = null;

	// For debugging, allow the user to ask to see the information sent to the server.

	private boolean showToScreen = false;


	// Send message m across the network to the server and retrieve a response.
	// The response is expected to have no body to it.  We report the return
	// code of the response along with the message that the server told us.

	private void sendAndReportOutcome( Message m ) {
		Message response = new Message();
	
		// Identify who the message is coming from.  Avoid sending a message
		// if we haven't logged in yet.
		
		if (sessionCookie != null) {
			m.addHeader( Message.cookieHeader,  sessionCookie );
			try {
				m.sendOutgoingMessage( out, showToScreen );
			
		   		// See what answer we get back.
		    		
		    		response.receiveIncomingMessage( in, showToScreen );
		    		
		    		int theCode = response.getReturnCode();
		   		if ((theCode >= Message.successStart) && (theCode < Message.successEnd)) {
		   			System.out.println("Operation successful");				
				} else {
					System.out.println("Operation failed: "+response.getReturnMessage());
					
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	// Similar to senaAndReportOutcome, send message m across the network to the 
	// server and retrieve a response.
	// The response is expected to have a list in the results.  We report the return
	// code of the response along with the message that the server told us.
	// Also report the whole body of the returned message.

	private void sendAndReportList( Message m) {
		Message response = new Message();
		
		if (sessionCookie != null) {
			m.addHeader( Message.cookieHeader,  sessionCookie );
			try {
				m.sendOutgoingMessage( out, showToScreen );
			
    				// See what answer we get back.
    		
    				response.receiveIncomingMessage( in, showToScreen );
    		
    				int theCode = response.getReturnCode();
    				if ((theCode >= Message.successStart) && (theCode < Message.successEnd)) {
    					// Print the list
    			
    					ArrayList<String> bodyInfo = response.getBody();
    				
    					for (String line : bodyInfo) {
    						System.out.println(line);
    					}
    					System.out.println("");
    				}
			} catch( Exception e) {
				System.out.println("Request to see the list failed: " + response.getReturnMessage());
			}
		}
	}
	
	
	// Find out who we are connecting to and who we are connecting as.
	
	public boolean getSetupInfo () {
		boolean connected = false;
		String host = null;
		String userName = null;
		String password = null;
		int port = 0;
		String answer = null;
		
		Scanner userInput = new Scanner( System.in );
		
		// Get the connection information
		
		System.out.print("Host to connect to the server? " );
		host = userInput.nextLine();
		
		System.out.print("Port to connect to on the server? ");
		port = Integer.valueOf(userInput.nextLine());
		
		// Let's test out the connection
		
		try {
            		System.setProperty("java.net.preferIPv4Stack", "true");

            		toServer = new Socket(host, port);
            		out = new PrintWriter(toServer.getOutputStream(), true);
            		in = new BufferedReader(new InputStreamReader(toServer.getInputStream()));

			// Find out if the user wants to see the raw messages too
		
			System.out.print("Do you want to see the text that is sent directly to the network (Y/N)? ");
			answer = userInput.nextLine();
			if (answer.equals("Y")) {
				showToScreen = true;
			}
		
    			// Get the sign-in credentials
    		
    			System.out.print("Employee last name? ");
    			userName = userInput.nextLine();
    		
    			System.out.print("Employees birthdate YYYY-MM-DD? "); 
    			password = userInput.nextLine();
    		
    			// Try out the credentials
    		
    			Message outRequest = new Message();
    		
    			outRequest.setTask( Message.login, userName );
    			outRequest.addHeader( "Password",  password );
    			outRequest.sendOutgoingMessage( out, showToScreen );
    	
    			// See what answer we get back.
    	
    			Message response = new Message();

   			response.receiveIncomingMessage( in, showToScreen );
   	
    			int theCode = response.getReturnCode();
    			if ((theCode >= Message.successStart) && (theCode < Message.successEnd)) {
    				sessionCookie = response.getHeaderMatch( Message.setCookieHeader );
    		
    				connected = true;
    			}
    		
		} catch (Exception e) {
			// Nothing to do with the exception for now except fail the set-up.
		}
		
		return connected;
	}

	// Log a user out of their current session with the server.
	// Expect a response back from the server.
	
	public void exitConnection() {
		// Only proceed if we're in an actual session.
		
		if (sessionCookie != null) {
			Message logoutRequest = new Message();
			
			logoutRequest.setTask( Message.logout, Message.logout );
			logoutRequest.addHeader( Message.cookieHeader, sessionCookie );
			try {
				logoutRequest.sendOutgoingMessage( out, showToScreen );
			} catch (Exception e) {
				// Nothing to do if the logout failed.
			}
			
			sessionCookie = null;
		}
	}

	// Close the network connection to the server, logging out of our
	// session, if necessary.
	
	public void tearDown() {
		if (toServer != null) {
			// If we haven't logged out yet, do so.
			
			if (sessionCookie != null) {
				exitConnection();
			}
			
			// Close all of the streams of data.
			try {
				in.close();
				out.close();
				toServer.close();
			} catch (Exception e) {
				// Nothing to do on the failed close attempts.
			}
			
			// Reset the connection information.
			in = null;
			out = null;
			toServer = null;
		}
	}

	// Start an order for a customer.  It requires that the employee be logged in already.
	// The basic order just needs the customer ID.
	// We do have the chance to update all of the shipping information at one time, if we want.
	
	public void openOrder() {
		String customer = null;
		
		String answer = null;
		Message request = new Message();
		Scanner userInput = new Scanner( System.in );
		
		// Figure out who the order is going to.  Exit the while loop with
		// "customer" set to a string with the ID of a customer.
		
		while (customer == null) {
			System.out.println("Enter customer ID or 'list' to list the customers");
			customer = userInput.nextLine();
			
			if (customer.equals("list")) {
				// Get the set of customers and show it.
				
				Message customerList = new Message();

	    			customerList.setTask( Message.list_info, "customer");
	    			sendAndReportList( customerList );

				customer = null;
			}
		}
		
		// Find out if we want to change the delivery information
		
		System.out.print("Do you want to change the destination information (Y/N)? ");
		answer = userInput.nextLine();
		
		if (answer.equals("Y")) {
			// Make a list of the questions to ask the user and the corresponding message headers to use.
			
			String[] parts = {"street address", "city", "province", "postal code", "country"};
			String[] headInfo = {"Address", "City", "Region", "PostalCode", "Country"};
			
			for (int i = 0; i < parts.length; i++) {
				System.out.print("Enter "+parts[i]+" information: ");
				answer = userInput.nextLine();
				request.addHeader( headInfo[i],  answer );
			}
		}
		
		// Send out the request and get the response.
		
		request.setTask( Message.new_order, customer );
		sendAndReportOutcome( request );
	}

	// Items are added to an order one at a time, each one being sent to the
	// server in its own message.  Allow the user to add items or to get a list
	// of either the current products to order or the current contents of the order
		
	public void addProductsToOrder() {		
		String answer = null;
		Scanner userInput = new Scanner( System.in );
		
		// Get the product code and quantity
		
		System.out.println("While entering orders, enter 'product' to get a list of products or 'order' to see what's in your order.");
		System.out.println("Ender 'done' when you are finished with adding items.");
		System.out.println("");
		
		while ((answer == null) || !answer.equals("done")) {
			System.out.println("Product and quantity to add (separated by a space): " );
			answer = userInput.nextLine();
			
			if ((answer.equals("product")) || (answer.equals("order"))) {
				// Get the requested list and show it.
				
				Message theList = new Message();

	    			theList.setTask( Message.list_info, answer);
	    			sendAndReportList( theList );
	    		
			} else if (!answer.equals("done")) {
				// Send the product for the order.
				
				Message newItem = new Message();
	    			String[] parts = answer.split(" ", 2);

				// I need the product ID and the quantity to make an order
				if (parts.length < 2) {
					System.out.println("Not enough parameters for adding a product");
				} else {
	    				newItem.setTask( Message.add_item, parts[0]);
					newItem.appendBody( parts[1] );				
					sendAndReportOutcome( newItem );
				}
			}
		}
	}
	

	// Close an order for the client, either makign the order or asking to abandon the order.
	
	public void endOrder() {		
		String answer = null;
		String op = null;
		Scanner userInput = new Scanner( System.in );
		
		// Get the product code and quantity
		
		while (op == null) {
			System.out.println("Do you want to send the order (Y/N)?");
			answer = userInput.nextLine();
			if (answer.equals("Y")) {
				op = Message.place_order;
			} else {
				System.out.println("Do you want to abandon the order (Y/N)?");
				answer = userInput.nextLine();
				if (answer.equals("Y")) {
					op = Message.abandon_order;
				} else {
					System.out.println("Not sure what you want to do.  Let's try again.");
				}
			}
		}
		
		Message closing = new Message();

	    	closing.setTask( op, op);
	    	sendAndReportOutcome( closing );
	}
}

