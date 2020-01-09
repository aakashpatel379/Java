import java.util.Map;

/**
 * Interface for Huffman Algorithm functionalities
 *
 */
public interface FileCompressor {
	boolean encode(String input_filename, int level, boolean reset, String output_filename);

	boolean decode(String input_filename, String output_filename);

	Map<String, String> codebook();
}