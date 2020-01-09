import java.util.Scanner;

public class OrderClient {


	// Class to connect to an order server and make orders as an employee
		
	public static void main(String[] args) {
		OrderConnection order = new OrderConnection();
		Scanner userInput = new Scanner( System.in );
		String answer = null;
		
		// Find out where we're connecting to and who we connect as
		
		if (order.getSetupInfo()) {
			do {
				// Process and send out the order.
			
				order.openOrder();
				order.addProductsToOrder();
				order.endOrder();
				
				// Find out if we want to do more orders.
				
				System.out.println("Do you want to enter another order (Y/N)? ");
				answer = userInput.nextLine();
				
			} while (answer.equals("Y"));
			
		} else {
			System.out.println("Connections or credentials failed.");
		}
	
		// Before ending, close up the order.
		
		order.tearDown();

		userInput.close();
	}

}

