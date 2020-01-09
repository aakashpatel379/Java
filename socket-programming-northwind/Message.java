/**
 * Message class for generic message builder to be sent as response.
 * @author Aakash Patel
 *
 */

public class Message {

	static final String CRLF ="\r\n";
	static final String version = "Order3901/1.0";   //specifies version of protocol
	int statusCode;						//variable to hold error code.
	String statusmessage;			//message for error code
}
