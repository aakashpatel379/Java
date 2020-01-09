/**
 * Class Data Structure for managing Products.
 * @author Aakash Patel
 */
public class Product {

	private int productID;
	private int prodQty;
	private double prodPrice=0;
	

	public int getProductID() {
		return productID;
	}
	public void setProductID(int productID) {
		this.productID = productID;
	}
	public double getProdPrice() {
		return prodPrice;
	}
	public void setProdPrice(double prodPrice) {
		this.prodPrice = prodPrice;
	}
	public int getProdQty() {
		return prodQty;
	}
	public void setProdQty(int prodQty) {
		this.prodQty = prodQty;
	}
	
}
