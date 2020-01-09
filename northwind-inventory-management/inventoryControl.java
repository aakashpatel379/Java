/**
 * Interface for Controlling inventory operations
 * @author Aakash Patel
 *
 */
public interface inventoryControl {
	public void Ship_order( int orderNumber ) throws OrderException;
	public int Issue_reorders( int year, int month, int day );
	public void Receive_order( int internal_order_reference ) throws OrderException;
}