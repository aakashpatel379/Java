
/**
 * Class for Customer information.
 * @author Aakash Patel
 */
public class Customer {

	private String address;
	private String city;
	private String region;
	private String postalCode;
	private String country;
	private String custID;
	public Order orderData;    //For current order
	
	public Order getOrderData() {
		return orderData;
	}

	public void setOrderData(Order orderData) {
		this.orderData = orderData;
	}

	Customer()
	{
		address="";
		city="";
		region="";
		postalCode="";
		country="";
		custID="";
		orderData=new Order();
	}
	
	public String getCustID() {
		return custID;
	}
	public void setCustID(String custID) {
		this.custID = custID;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	
	
}
