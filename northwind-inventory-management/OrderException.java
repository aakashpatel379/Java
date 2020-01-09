/**
 * Custom Exception Class for handling differnt type of Order related exception
 *
 */
public class OrderException extends Exception {
	
	int internal_order_reference;
	
	public OrderException(String message, int orderNumber) {
		super(message);
		internal_order_reference=orderNumber;
	}

	public int getReference() {
		return internal_order_reference;
	}
}
