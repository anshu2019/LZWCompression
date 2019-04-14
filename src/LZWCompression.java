import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class applies LZW algorithm to compress and decompress files
 * @author Anshu Anand
 *
 */
public class LZWCompression {
	// Common Variables
	private static String File_Input = null;
	private static String File_Output = null;
	private static boolean cFlag = true; // flag for compression and decompression switch
	private static boolean dFlag = false; // flag for compression and decompression switch
	private static boolean vFlag = false; // flag for verbose, by default off
	private static double MAX_TABLE_SIZE;
	private static int table_Size;
	private static int Bit_Length = 12;
	private static int mapsize = 127;
	private static double inByteCounter = 0;
	private static double outByteCounter = 0;
	private static double noOfSymbolEncoded = 0;

	// Compression variable
	private static StringBuilder encoderString = new StringBuilder();
	private static MyHashMap<String, Integer> ENCODE_TABLE;

	// Decompression variable
	private static StringBuilder encoded_string = new StringBuilder();
	private static StringBuilder decoded_string = new StringBuilder();
	private static MyHashMap<String, String> DECODE_TABLE;
	public static String E_values = "";
	public static String D_value = "";
	public static StringBuffer decoded_values;
    public static String SYMBOL = null;
    
	/**
	 * This is the static void main method
	 * @param args
	 * @throws IOException
	 * 
	 * @Comments This code is working for both the scenario
	 *           1. ASCII Files
	 *              FILE                                 Degree Of compression
	 *           ------------------------------------------------------------
	 *              words.html                            1.7781992750507492
	 *              CrimeLatLonXY1990.csv                 2.2458838862518444
	 *              
	 *           2. Binary Files
	 *              FILE                                 Degree Of compression
	 *           ------------------------------------------------------------
	 *              01_Overview.mp4                      0.6898739345076466
	 */  
	public static void main(String args[]) throws IOException {

		// Handle Arguments .....Avoid exceptions gracefully.....
		// Get all the arguments...
		int argSize = args.length;
		int i = 0;
		if (argSize < 3) {
			System.out.println("Not enough argument passed");
			System.exit(0);
		} else {
			while (i < argSize) {
				if (args[i].startsWith("-")) {
					if (args[i].equals("-c")) { // Compress
						cFlag = true;
						dFlag = false;
					} else if (args[i].equals("-d")) { // De-compress
						cFlag = false;
						dFlag = true;
					} else if (args[i].equals("-v")) { // verbose
						vFlag = true;
					}
				}
				i++;
			}

			File_Input = args[argSize - 2]; // second last argument
			File_Output = args[argSize - 1]; // last argument
			MAX_TABLE_SIZE = (int) Math.pow(2, Bit_Length);
			table_Size = 255;
		}

		// Compress
		if (cFlag) {
			String data = readBytesFromFile();
			CompressData(data);
			WriteCompressedDataToFile();
			
			if (vFlag) {
				System.out.println("Bytes Read: " + inByteCounter);
				System.out.println("Bytes Written: " + outByteCounter);
				System.out.println("Compression Ratio: " + inByteCounter/outByteCounter);
			}

		}
		// De-compress
		else if (dFlag) {

			readComprassedFile();
			initialize_dcomp();
			decode_12_bitstring();
			//Decompress();
			writeBytesToFile(decoded_values.toString());
			
			if (vFlag) {
				System.out.println("Bytes Read: " + inByteCounter);
				System.out.println("Bytes Written: " + outByteCounter);
				//System.out.println("Compression Ratio: " + inByteCounter/outByteCounter);
			}

		}
		

	}

	/**
	 * This method reads Bytes from File.
	 * @return
	 * @throws IOException
	 */
	public static String readBytesFromFile() throws IOException {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(File_Input)));

		StringBuilder sb = new StringBuilder();
		byte byteIn;
		try {
			while (true) {
				byteIn = in.readByte();
				// System.out.println("Incoming Bytes: " + byteIn);
				inByteCounter++;
				char c = (char) byteIn;
				c = (char) (c & 0xFF);
				sb.append(c);

			}
		} catch (EOFException e) {
			in.close();
		}

		return sb.toString();

	}

	/**
	 * This method applies Compression using LZW algorithm
	 * @param input_string
	 * @throws IOException
	 */
	public static void CompressData(String input_string) throws IOException {

		ENCODE_TABLE = new MyHashMap<String, Integer>(mapsize);

		for (int i = 0; i <= 255; i++) {
			ENCODE_TABLE.add("" + (char) i, i);
		}

		String initSTR = "";

		for (char SYMBOL : input_string.toCharArray()) {
			String Str_Symbol = initSTR + SYMBOL;

			if (ENCODE_TABLE.contains(Str_Symbol)) {
				initSTR = Str_Symbol;
			} else {
				// get 12 Bit code for compression....
				String h = getBitCode(ENCODE_TABLE.get(initSTR));
				encoderString.append(h);
				noOfSymbolEncoded++;

				if (table_Size < MAX_TABLE_SIZE) {
					ENCODE_TABLE.add(Str_Symbol, (int) table_Size++);
				}

				initSTR = "" + SYMBOL;
			}
		}

		if (!initSTR.equals("")) {
			String h = getBitCode(ENCODE_TABLE.get(initSTR));
			encoderString.append(h);
			noOfSymbolEncoded++;
		}

	}

	/**
	 * This method writes the compressed data to a file
	 * @throws IOException
	 */
	private static void WriteCompressedDataToFile() throws IOException {
		if (noOfSymbolEncoded % 2 != 0) {
			encoderString.append("0000");
		}

		// create bytes from encoded bits..
		byte[] byteData = getBytesFromString(encoderString.toString());

		// byte[] byteData = new BigInteger(encoderString.toString(), 2).toByteArray();
		DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(File_Output)));
		try {
			for (byte b : byteData) {
				writer.writeByte(b);
				outByteCounter++;
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method generates bits for a integer value
	 * @param bt
	 * @return
	 */
	public static String getBitCode(int bt) {
		String out = "";
		// String prefix = "0000"; // append four zero
		String rs = Integer.toString(bt, 2);
		out = ("000000000000" + rs).substring(rs.length());
		return out;

	}

	/**
	 * This method generates byte array for a string
	 * @param s
	 * @return
	 */
	public static byte[] getBytesFromString(String s) {
		int byteSize = s.length() / 8;

		byte[] out = new byte[byteSize];
		int indx = 0;
		int a = 0;
		int b = 8;

		while (b <= s.length() - 1) {
			int foo = Integer.parseInt(s.substring(a, b), 2);
			byte bite = (byte) foo;

			out[indx] = bite;
			indx++;
			a = b;
			b = b + 8;
		}

		return out;
	}

	// DE-COMPRESSION CODE----------------------------------------------

	/**
	 * This method initializes the Decompression
	 */
	public static void initialize_dcomp() {
		DECODE_TABLE = new MyHashMap<String, String>(mapsize);

		for (int i = 0; i <= 255; i++) {
			DECODE_TABLE.add(Integer.toString(i), "" + (char) i);
		}
	}

	/**
	 * This method applies LZW decompression on whole data at once
	 * @throws IOException
	 */
	public static void Decompress() throws IOException {

		
		String dd = decoded_string.toString().substring(1); // remove first char which is comma
		String[] decodedVal = dd.split(",");
		String Encode_values = "" + (char) Integer.parseInt(decodedVal[0]);
		decoded_values = new StringBuffer(Encode_values);

		String SYMBOL = null;

		for (int i = 1; i < decodedVal.length; i++) {
			String check_key = decodedVal[i];

			if (DECODE_TABLE.contains(check_key)) {
				SYMBOL = DECODE_TABLE.get(check_key);
			} else if (Integer.parseInt(check_key) == table_Size) {
				SYMBOL = Encode_values + Encode_values.charAt(0);
			}

			decoded_values.append(SYMBOL);
			noOfSymbolEncoded++;

			if (table_Size < MAX_TABLE_SIZE) {
				String tableEntry = Integer.toString(table_Size++);
				DECODE_TABLE.add(tableEntry, Encode_values + SYMBOL.charAt(0));
			}

			Encode_values = SYMBOL;
		}
		//System.out.println("Decompression is done");
		
		//System.out.println("writing Files done");

	}


    /**
     * This method de compresses data one at a time
     * Written to avoid Java Memory Overflow(Out of memory exception) resulting in Exception
     * @param U
     * @param ss
     * @throws IOException
     */
	public static void easy_decompress(int U, String ss) throws IOException {
		
		if (U == 0) {
			E_values = "" + (char) Integer.parseInt(ss);
			decoded_values = new StringBuffer(E_values);
		} else {
			String check_key = ss;

			if (DECODE_TABLE.contains(check_key)) {				
				SYMBOL = DECODE_TABLE.get(check_key);				
			} else if (Integer.parseInt(check_key) == table_Size) {
				SYMBOL = E_values + E_values.charAt(0);
			}

			decoded_values.append(SYMBOL);
			noOfSymbolEncoded++;

			if (table_Size < MAX_TABLE_SIZE) {
				
				String tableEntry = Integer.toString(table_Size++);
				DECODE_TABLE.add(tableEntry, E_values + SYMBOL.charAt(0));
			}

			E_values = SYMBOL;

		}
	}

	/**
	 * This method reads compressed file
	 * @throws IOException
	 */
	public static void readComprassedFile() throws IOException {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(File_Input)));
		byte byteIn;
		try {
			while (true) {
				byteIn = in.readByte();
				String bitcode = getBitCodeForByte(byteIn);
				encoded_string.append(bitcode);
				inByteCounter++;
			}

		} catch (EOFException e) {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		in.close();
	}

	/**
	 * This method writes bytes to file
	 * @param decoded_values
	 * @throws IOException
	 */
	private static void writeBytesToFile(String decoded_values) throws IOException {

		DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(File_Output)));

		byte[] byteData = decoded_values.getBytes();

		for (byte b : byteData) {
			writer.writeByte(b);
			outByteCounter++;
		}
		writer.flush();
		writer.close();
	}

	/**
	 * This method returns bits for byte data
	 * 
	 * @param bt
	 * @return
	 */
	public static String getBitCodeForByte(byte bt) {
		String out = "";
		// String prefix = "0000"; // append four zero
		String rs = Integer.toBinaryString((bt & 0xFF) + 256).substring(1);
		out = rs;
		return out;

	}

	/**
	 * This method extracts 12 bit codeword from encoded string
	 * @throws IOException
	 */
	public static void decode_12_bitstring() throws IOException {

		String rawString = "";
		rawString = encoded_string.toString();
		int beginIndex = 0;
		int endIndex = 12;
		int len = rawString.length();
		int u = 0;

		while (len - endIndex >= 4) {
			String correctBits = rawString.substring(beginIndex, endIndex);
			int foo = Integer.parseInt(correctBits, 2);

			decoded_string.append("," + foo);
			easy_decompress(u, Integer.toString(foo));
			u++;
			beginIndex += Bit_Length;
			endIndex += Bit_Length;
		}

	}

}
