import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

class Huffman {

	static Scanner in=null;
	/**
	 * Manages input from user and forwards crucial data to be encoded or decoded.
	 */
	public void manageUserInput() {
		
		in=new Scanner(System.in);
		int level=0;
		Boolean reset=false;
		System.out.println("Enter the name of input file: (with extension)");
		String inputFileName=null;
		inputFileName=in.next();
		if(inputFileName==null) {
			System.err.println("File name couldn't be retrieved.!");
			System.exit(1);
		}
		if(inputFileName=="") {
			System.err.println("File name cannot be empty!");
			System.exit(1);
		}
		System.out.println("Enter the name of output file:  (with extension)");
		String outputFileName="";
		outputFileName=in.next();
		
		System.out.println("Do you want encode or decode? (E/D)");
		String encodeDecodeChoice= in.next();
		Character choice=null;
		if(encodeDecodeChoice.length()==1)
		{
			choice=encodeDecodeChoice.charAt(0);
			if(choice=='D') {
				manageDecode(inputFileName, outputFileName);
			}
			else if(choice!='E'){
				System.err.println("Invalid input for reset choice. Program will close!");
				System.exit(1);
			}
			
			//continue encoding
			System.out.println("Do you want to reset between levels? (Y/N)");
			String resetString= in.next();
			Character ch=null;
			if(resetString.length()==1)
			{
				ch=resetString.charAt(0);
				if(ch=='Y') {
					reset=true;
				}
				else if(ch=='N') {
					reset=false;
				}
				else {
					System.err.println("Invalid input for reset choice. Program will close!");
					System.exit(1);
				}
			}
			else
			{
				System.err.println("Improper input for reset choice. Program will close!");
				System.exit(1);
			}
			
			System.out.println("Maximum level value? (0-9)");
			try {
				level=Integer.parseInt(in.next());
				if(level<0 || level>9) {
					System.err.println("Out of range input for level. Program will close");
					System.exit(1);
				}
			}
			catch(Exception e){
				System.err.println("Invalid input for level. Program will close!");
			}

			manageEncode(inputFileName, level, reset,  outputFileName);
			//manageDecode(inputFileName, level, reset,  outputFileName);
		}
		
	}
	
	/**
	 * Manages encoding operation
	 * @param inputFileName -Input file name
	 * @param level - Maximum level value
	 * @param reset - Resetting choice parameter between levels
	 * @param outputFileName - Output File name
	 */
	public void manageEncode(String inputFileName, int level, Boolean reset, String outputFileName)
	{
		File f=new File("./"+inputFileName);
		HuffmanEncodeDecode encoderObj = new HuffmanEncodeDecode();
		try {
			in=new Scanner(f);
		} catch (FileNotFoundException e) {
			System.out.println("Mentioned File does not exists!");
			System.exit(1);
		}
		if(encoderObj.encode("./"+inputFileName, level, reset,  outputFileName)) {
			System.out.println("Encoded file generated successfully!");
			
		}
		in.close();
		displayCodeBook(encoderObj.codebook());
		
	}
	
	/**
	 * Displays codebook with choice of user
	 * @param hashMap 
	 */
	private void displayCodeBook(HashMap<String, String> mapObj) {
		Scanner in=new Scanner(System.in);
		System.out.println("Do you want to display code book? (Y/N)");
		String displayChoice= in.next();
		Character ch=null;
		if(displayChoice.length()==1)
		{
			ch=displayChoice.charAt(0);
			if(ch=='Y') {
				for(Entry<String,String> entry: mapObj.entrySet()) {
					System.out.println(entry.getKey()+ " : " +entry.getValue());
				}
				
			}
			else if(ch=='N') {
				System.out.println("Program will close!");
			}
			else {
				System.err.println("Invalid input for reset choice. Program will close!");
				System.exit(1);
			}
		}
		else
		{
			System.err.println("Improper input for reset choice. Program will close!");
			System.exit(1);
		}
		in.close();
	}

	/**
	 * Manages decoding operation
	 * @param inputFileName -Input file name
	 * @param level - Maximum level value
	 * @param reset - Resetting choice parameter between levels
	 * @param outputFileName - Output File name
	 */
	public void manageDecode(String inputFileName, String outputFileName){
//		displayCodeBook(encoderObj.codebook());
		File f=new File("./"+inputFileName);
		HuffmanEncodeDecode decoderObj = new HuffmanEncodeDecode();
		try {
			in=new Scanner(f);
		} catch (FileNotFoundException e) {
			System.out.println("Mentioned File does not exists!");
			System.exit(1);
		}
		decoderObj.decode(inputFileName, inputFileName);
//		if(encoderObj.encode("./"+inputFileName, level, reset,  outputFileName)) {
//			System.out.println("Encoded file generated successfully!");
			
		}
//		in.close();
		//displayCodeBook(encoderObj.codebook());
		
	}

