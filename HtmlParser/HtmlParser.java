import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author aakash - B00807065
 */

public class HtmlParser {
	
	/**
	 * Main function
	 * @param args the command line arguments - Unused
     * @throws IOException when trying to read next token in file with no more tokens.
	 * @throws InterruptedException when a thread enters in unstable state
	 */
	
	public static void main(String[] args) throws IOException, InterruptedException{
		processFile();
	}
	
	/**
	 * Gets text file from User and forwards it for processing.
	 * The text file must be next to this file in directory.
	 */
	private static void processFile() throws IOException, InterruptedException {
		Scanner in=new Scanner(System.in);
		System.out.println("Please enter the file name: (with extension)"); 
		String fileName=in.next();
		BufferedReader br=null;
		try 
		{
			br=new BufferedReader(new FileReader("./"+fileName));
		} 
		catch (FileNotFoundException f)      //to check whether file exists or not
		{
			System.out.println("Mentioned File does not exists!");
			System.exit(1);
		}
		in.close();
		processData(br);
	}

	/**
	 * Process data for title tag, forwards unprocessed content 
	 * It then retrieves the processed content and places it after body tag element
	 */
	private static void processData(BufferedReader br) throws IOException, InterruptedException {
		String line;
		LinkedList<String> tagLinkedList = new LinkedList<>(); // Linked list to maintain chain of html tags
		tagLinkedList.add("<html>");
		tagLinkedList.add("<head>");
		boolean titleFlag = false; 						// Flag to keep track of title line occurance
		boolean titleSectionFlag=false;					// Flag to keep track whether execution pointer has entered title handling code section
		String unprocessedString = "";
		String fileContent = "";
		ArrayList<String> lineList = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				lineList.add(line);
				fileContent+=line;
			}
			if(fileContent.isEmpty())										//To check the content of file
			{
				System.out.println("Provided file is empty!");
				System.exit(1);
			}
			for (int i = 0; i < lineList.size(); i++) 
			{
				line=lineList.get(i);
				if(line.isEmpty() && !titleSectionFlag)
				{
					while(i<lineList.size() && line.isEmpty()) {
						i++;
						line=lineList.get(i);
					}
					titleSectionFlag=true;
				}
				if (line.contains("title: ") && titleFlag == false)          //For processing title tag
				{
					titleFlag = true;
					titleSectionFlag= true;
					int startIndex = 0;
					startIndex = line.indexOf("title: ");
					if(startIndex!=0)
					{
						if(i!=lineList.size()-1)
						{
							unprocessedString += line + "\n";
						}
						else
						{
							unprocessedString += line;
						}
						titleFlag=false;
						continue;
					}
					String titleString = "";
					for (int pos = 7; pos < line.length(); pos++) 
					{
						titleString += line.charAt(pos);
					}
					tagLinkedList.add("<title>" + titleString + "</title>");
				}
				else
				{
					if(i!=0 && !titleFlag) 
					{
						tagLinkedList.add("<title></title>");
						titleFlag=true;
					}
					if(i!=lineList.size()-1)
					unprocessedString += line + "\n";
					else
						unprocessedString += line;
				}
			}
		 tagLinkedList.add("</head>");
		 tagLinkedList.add("<body>");
		 String parsedBodyContent=paragraphFinder(unprocessedString);		 
         tagLinkedList.add(parsedBodyContent);								//Adding processed parsed content at specific location in linked list
         tagLinkedList.add("</body>");
		 tagLinkedList.add("</html>");
         int i = 0;
         while (i< tagLinkedList.size()) {
        	 Thread.sleep(100);
        	 System.out.println(tagLinkedList.get(i));
        	 i++;
         }
	}
	/**
	 * Processes input content divide into paragraph chunks
	 * Content processed is then sent to Chunk processor
	 * @param rawString - Parameter for unprocessed file content
	 * @return Processed Chunks result string is returned
	 * @throws IOException - When bufferedReader object fails to read lines 
	 */
	private static String paragraphFinder(String rawString) throws IOException {
		BufferedReader br = new BufferedReader(new StringReader(rawString));
		ArrayList<String> lineList = new ArrayList<String>();
		String line;
		while ((line = br.readLine()) != null) {
			lineList.add(line);
		}
		String processedChunks = "";									//To manage paragraph chunks generated in their original order
		for (int i = 0; i < lineList.size();) 
		{
			if (lineList.get(i).trim().equals("")) 
			{
				String paraString = "";
				int j;
				for (j = i + 1; j < lineList.size(); j++) 
				{
					if (!lineList.get(j).trim().equals("")) 			// not blank
					{
						paraString += lineList.get(j) + "\n";		
					}
					if (paraString != "" && lineList.get(j).trim().equals(""))
					{
						i = j + 1;
						processedChunks += processChunk("\n"+paraString, "p");      	//Forwards split content to chunk processor method
						break;
					}
					if (paraString != "" && j == lineList.size() - 1) 
					{
						i = j;
						processedChunks += processChunk("\n"+paraString, "p");
						break;
					}
				}
				if (j == lineList.size() - 1)
				{
					i = j + 1;
					break;
				}
			} 
			else 
			{
				String hold = lineList.get(i) + "\n";

				int j;
				if (i == lineList.size() - 1) 
				{
					processedChunks += processChunk("\n"+hold, "np");
					i++;
					continue;
				}
				for (j = i + 1; j < lineList.size(); j++) 
				{
					if (lineList.get(j).trim().equals(""))
					{
						i = j;
						processedChunks += processChunk("\n"+hold, "np");    
						break;
					}
					else 
					{
						hold += lineList.get(j) + "\n";
					}
					if (hold != "" && j == lineList.size() - 1) 
					{
						i = j;
						processedChunks += processChunk("\n"+hold, "np");
						break;
					}
				}
				if (j == lineList.size() - 1) 
				{
					i = j + 1;
					break;
				}
			}
		}
		return processedChunks;
	}
	/**
	 * Receives data for chunks and delegates to two different methods for processing common and special patterns
	 * @param chunkData - Content String of Chunk 
	 * @param type - Distinction made for type of chunk for paragraphing later.
	 * @return processed Chunk string after manipulation as per chunk type
	 */
	private static String processChunk(String chunkData, String type) throws IOException {
		String phase1;
		phase1 = commonPatternLookup(chunkData, type);
		String phase2 = specialPatternLookup(phase1, type);
		if (type == "p") {
			phase2 = "<p>\n" + phase2 + "</p>";
		}
		return phase2;
	}
	/**
	 * Confined space to forward received String content for special parsing criteria of bold and list tags 
	 * @param phase1 - Receives phase 1 processed string from Chunk processor method
	 * @param type - Distinction made for type of chunk for paragraphing later.
	 * @return parsed string operated upon for special criteria of bold and list tags (i.e for '!' and '-' symbols)
	 */
	private static String specialPatternLookup(String phase1, String type) throws IOException {
		String resultString = boldLookup(phase1);
		String updatedString = listLookup(resultString, type);
		return updatedString;
	}

	/**
	 * Method to parse list content in file
	 * @param resultString - Receives String text to lookup for list items   
	 * @param type - Defines a distinction made for the type of chunk
	 * @return parsed string generated after processing list item tags
	 */
	private static String listLookup(String resultString, String type) throws IOException {
		BufferedReader br = new BufferedReader(new StringReader(resultString));
		ArrayList<String> lineList = new ArrayList<String>();
		String line;
		while ((line = br.readLine()) != null) {
			lineList.add(line);
		}
		Boolean listFlag = false;		// Flag to keep check of on-going list item
		String builder = "";
		Boolean antiList = false;      // Flag to avoid second list in a text chunk
		for (int i = 0; i < lineList.size(); i++) {
			if (type == "p") {
				builder += lineList.get(i) + '\n';
				continue;
			}
			if (type == "np" && lineList.get(i).charAt(0) == '-') {
				if (antiList) {
					builder += lineList.get(i) + "\n";
					continue;
				}
				if (!listFlag) {
					builder += "<ul>\n<li>";
					listFlag = true;
				} else {
					builder += "<li>";
				}
				for (int pos = 1; pos < lineList.get(i).length(); pos++) {
					builder += lineList.get(i).charAt(pos);
				}
				builder += "</li>\n";
			} else
			{	
				if(i!=lineList.size()-1)
				builder += lineList.get(i) + "\n";
				else
				builder+=lineList.get(i);
			}
			if (i == lineList.size() - 1 && listFlag) {
				listFlag = false;
				builder += "</ul>";
				antiList = true;
			}
			if (lineList.get(i).charAt(0) != '-' && listFlag) {
				listFlag = false;
				builder += "</ul>\n";
				antiList = true;
				builder += lineList.get(i) + "\n";
			}
		}
		
		return builder;
	}
	/**
	 * Method to parse content for special bolding criteria (i.e for '!' symbol)
	 * @param rawString - Receives input string in this parameter
	 * @return Parsed String after manipulation as per annotation formatting criteria
	 */
	private static String boldLookup(String rawString) throws IOException {
		String targetString = "";
		String prevWord = "";    		//String to hold word text occurring before exclamation mark
		BufferedReader br = new BufferedReader(new StringReader(rawString));
		String line;
		ArrayList<String> lineList = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			lineList.add(line);
		}
		for (int i = 0; i < lineList.size(); i++) {
			line = lineList.get(i);
			prevWord = "";
			for (int pos = 0; pos < line.length(); pos++) {
				char c = line.charAt(pos);
				if (c == ' ') {
					targetString += prevWord + " ";
					if (pos == line.length() - 1)
						targetString += '\n';
					prevWord = "";
					continue;
				}
				if (c != '!') {
					if (pos + 1 == line.length()) {
						targetString += prevWord + c + '\n';
						break;
					}
					prevWord += c;
					if (line.charAt(pos + 1) == '!') {
						pos++;
						if (prevWord != "") {
							targetString += prevWord + "!";
							prevWord = "";
						} else {
							pos--;
						}
						continue;
					}
				}
				String afterword = "";   //String to handle word text occuring after exclamation mark
				if (c == '!') {
					if ((pos + 1) < line.length()) {
						pos++;
						if (line.charAt(pos) == '!') {
							targetString += '!';
							pos--;
							continue;
						}
						while (line.charAt(pos) == ' ' && pos < line.length()) {
							pos++;
							targetString += " ";
						}
					} else {
						targetString += c;
						continue;
					}
					while (line.charAt(pos) != ' ' && pos < line.length()) {
						afterword += line.charAt(pos);
						pos++;
					}

					afterword = "<b>" + afterword + "</b>";
					if (line.charAt(pos) == ' ') {
						afterword += " ";
					} else {
						afterword += "\n";
					}
					targetString += afterword;
				}
			}
		}
		return targetString;
	}
	/**
	 * Method to parse text for common scenarios like text between the symbols (where symbols are namely '_', '*', '%') 
	 * @param chunkData - Input string text for parsing
	 * @return Parsed text 
	 */
	private static String commonPatternLookup(String chunkData, String type) throws IOException {
		BufferedReader br = new BufferedReader(new StringReader(chunkData));
		String line;
		ArrayList<String> lineList = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			lineList.add(line);
		}
		Boolean iFlag = false, bFlag = false, uFlag = false;		//Flags to keep track on opened html tags for italics, bold and underline respectively
		String parsedText = "";
		for (int i = 0; i < lineList.size(); i++) {

			String temp = lineList.get(i);
			for (int pos = 0; pos < temp.length(); pos++) {
				char c = temp.charAt(pos);
				if (c == '_') {
					if (!iFlag) {
						if(i==temp.length()-1)
						{
						parsedText += "<i></i>";
						}
						else
						{
							parsedText+="<i>";
						}
						iFlag = true;
					} else {
						parsedText += "</i>";
						iFlag = false;
					}
					continue;
				}
				if (c == '*') {
					if (!bFlag) {
						if(i==temp.length()-1)
						{
							parsedText+="<b></b>";
						}
						else
						{
							parsedText += "<b>";
						}
						bFlag = true;
					} else {
						parsedText += "</b>";
						bFlag = false;
					}
					continue;
				}
				if (c == '%') {
					if (!uFlag) {
						if(i==temp.length()-1)
						{
							parsedText += "<u></u>";
						}
						else
						{
							parsedText += "<u>";
						}
						uFlag = true;
					} else {
						parsedText += "</u>";
						uFlag = false;
					}
					continue;
				}
				if (pos == temp.length() - 1 && (iFlag || bFlag || uFlag)) {				//Construct to close the unclosed tags before end of line
					if (iFlag) {
						parsedText += c + "</i>";
						iFlag = false;
					}
					if (bFlag) {
						parsedText += c + "</b>";
						bFlag = false;
					}
					if (uFlag) {
						parsedText += c + "</u>";
						bFlag = false;
					}
					continue;
				}
				parsedText += c;
			}
			if (i == lineList.size() - 1 && (iFlag || bFlag || uFlag)) {				   //Construct to close the unclosed tags before end of content of chunk
				if (iFlag) {
					parsedText += "</i>";
					iFlag = false;
				}
				if (bFlag) {
					parsedText += "</b>";
					bFlag = false;
				}
				if (uFlag) {
					parsedText += "</u>";
					bFlag = false;
				}

			}
			if(i!=lineList.size() - 1)
			parsedText += "\n";
		}
		return parsedText;
	}
}
