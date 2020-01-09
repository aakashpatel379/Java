
import java.util.ArrayList;

/**
 * To manage Order information detail ( with the products list) 
 * @author Aakash Patel
 */
public class Order {

	private int OrderID=0;
	public int getOrderID() {
		return OrderID;
	}

	public void setOrderID(int orderID) {
		OrderID = orderID;
	}

	ArrayList<Product> prodList=new ArrayList<Product>();

	public ArrayList<Product> getProdList() {
		return prodList;
	}

	public void setProdList(ArrayList<Product> prodList) {
		this.prodList = prodList;
	}
	
	
}
