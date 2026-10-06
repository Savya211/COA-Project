import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * RLEEngine.java — Run-Length Encoding Compression & Decompression Engine
 * ========================================================================
 * This class implements the core RLE algorithm for both compression and
 * decompression of text data, with step-by-step visualization support
 * and complete file I/O capabilities.
 *
 * HOW RLE WORKS (Viva Explanation):
 *   RLE scans the input string from left to right. Whenever it finds a
 *   character that repeats consecutively, it replaces the run with the
 *   character followed by the count of repetitions (only if count > 1).
 *   Single-occurrence characters are written as-is to avoid inflating the output.
 *
 *   Example:  AAAABBBCD  →  A4B3CD
 *   Reverse:  A4B3CD     →  AAAABBBCD
 *
 * IMPORTANT COA CONCEPTS:
 *   • Each character is stored as 1 byte (ASCII) or 2 bytes (Unicode) in memory.
 *   • RLE reduces storage by replacing N identical bytes with a character + count,
 *     saving space when characters repeat more than twice.
 */
public class RLEEngine {

    // ═══════════════════════════ COMPRESSION ════════════════════════════

    /**
     * Compress a string using Run-Length Encoding.
     *
     * Algorithm:
     *   1. Start from the first character.
     *   2. Count how many times it repeats consecutively.
     *   3. Write the character followed by its count.
     *   4. Move to the next distinct character and repeat.
     *
     * @param text The input string to compress
     * @return The RLE-compressed string (e.g., "AAABBC" → "A3B2C")
     * @throws IllegalArgumentException if input is null or empty
     */
    public static String compress(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Input text is empty. Please provide some text to compress.");
        }

        StringBuilder compressed = new StringBuilder();
        int i = 0;
        int length = text.length();

        while (i < length) {
            char currentChar = text.charAt(i);  // The character we're counting
            int count = 1;                       // It appears at least once

            // Count consecutive repetitions
            while (i + count < length && text.charAt(i + count) == currentChar) {
                count++;
            }

            // Append character; only append count if > 1 to avoid inflating output
            compressed.append(currentChar);
            if (count > 1) {
                compressed.append(count);
            }

            // Move pointer past the run we just encoded
            i += count;
        }

        return compressed.toString();
    }

    /**
     * Compress with step-by-step logging for GUI visualization.
     *
     * Each step is a String array:
     *   [0] = step number
     *   [1] = character
     *   [2] = count
     *   [3] = partial result so far
     *   [4] = human-readable description
     *
     * @param text The input string to compress
     * @return List of step descriptions
     */
    public static List<String[]> compressStepwise(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Input text is empty.");
        }

        List<String[]> steps = new ArrayList<>();
        StringBuilder compressed = new StringBuilder();
        int i = 0;
        int length = text.length();
        int stepNum = 0;

        while (i < length) {
            char currentChar = text.charAt(i);
            int count = 1;

            while (i + count < length && text.charAt(i + count) == currentChar) {
                count++;
            }

            compressed.append(currentChar);
            if (count > 1) {
                compressed.append(count);
            }
            stepNum++;

            String description = String.format(
                "Step %d: Read '%c' — found %d consecutive occurrence(s) → encode as '%c%d'  |  Result so far: %s",
                stepNum, currentChar, count, currentChar, count, compressed.toString()
            );

            steps.add(new String[]{
                String.valueOf(stepNum),
                String.valueOf(currentChar),
                String.valueOf(count),
                compressed.toString(),
                description
            });

            i += count;
        }

        return steps;
    }

    // ═══════════════════════════ DECOMPRESSION ══════════════════════════

    /**
     * Decompress an RLE-encoded string back to its original form.
     *
     * Algorithm:
     *   1. Read a character.
     *   2. Read the following digit(s) to get the count.
     *   3. Repeat the character 'count' times.
     *   4. Move to the next pair and repeat.
     *
     * @param encoded The RLE-compressed string (e.g., "A3B2C1")
     * @return The decompressed original string
     * @throws IllegalArgumentException if input is null, empty, or invalid format
     */
    public static String decompress(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            throw new IllegalArgumentException("Compressed text is empty. Please provide data to decompress.");
        }

        // Validate format: non-digit characters optionally followed by digits
        if (!encoded.matches("([^\\d]\\d*)+")) {
            throw new IllegalArgumentException(
                "Invalid compressed format. Expected entries like 'A3B2C'. "
                + "Each entry must be a non-digit character optionally followed by a count."
            );
        }

        StringBuilder decompressed = new StringBuilder();
        int i = 0;
        int length = encoded.length();

        while (i < length) {
            char ch = encoded.charAt(i);
            i++;

            // Read all following digits (if any)
            StringBuilder numStr = new StringBuilder();
            while (i < length && Character.isDigit(encoded.charAt(i))) {
                numStr.append(encoded.charAt(i));
                i++;
            }

            // If no digits follow, the count is implicitly 1
            int count = (numStr.length() > 0) ? Integer.parseInt(numStr.toString()) : 1;

            // Repeat character 'count' times
            for (int j = 0; j < count; j++) {
                decompressed.append(ch);
            }
        }

        return decompressed.toString();
    }

    /**
     * Decompress with step-by-step logging for GUI visualization.
     *
     * Each step is a String array:
     *   [0] = step number
     *   [1] = character
     *   [2] = count
     *   [3] = expanded substring
     *   [4] = partial result so far
     *   [5] = human-readable description
     *
     * @param encoded The RLE-compressed string
     * @return List of step descriptions
     */
    public static List<String[]> decompressStepwise(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            throw new IllegalArgumentException("Compressed text is empty.");
        }

        if (!encoded.matches("([^\\d]\\d*)+")) {
            throw new IllegalArgumentException("Invalid compressed format. Expected entries like 'A3B2C'.");
        }

        List<String[]> steps = new ArrayList<>();
        StringBuilder decompressed = new StringBuilder();
        int i = 0;
        int length = encoded.length();
        int stepNum = 0;

        while (i < length) {
            char ch = encoded.charAt(i);
            i++;

            StringBuilder numStr = new StringBuilder();
            while (i < length && Character.isDigit(encoded.charAt(i))) {
                numStr.append(encoded.charAt(i));
                i++;
            }

            int count = (numStr.length() > 0) ? Integer.parseInt(numStr.toString()) : 1;
            StringBuilder expanded = new StringBuilder();
            for (int j = 0; j < count; j++) {
                expanded.append(ch);
            }

            decompressed.append(expanded);
            stepNum++;

            String description = String.format(
                "Step %d: Read '%c' with count %d → expand to '%s'  |  Result so far: %s",
                stepNum, ch, count, expanded.toString(), decompressed.toString()
            );

            steps.add(new String[]{
                String.valueOf(stepNum),
                String.valueOf(ch),
                String.valueOf(count),
                expanded.toString(),
                decompressed.toString(),
                description
            });
        }

        return steps;
    }

    // ═══════════════════════════ FILE I/O ═══════════════════════════════

    /**
     * Read text content from a file.
     *
     * @param filepath Path to the input file
     * @return The file contents as a string
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if file is empty or not .txt
     */
    public static String readFile(String filepath) throws IOException {
        File file = new File(filepath);

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filepath);
        }

        if (!filepath.toLowerCase().endsWith(".txt")) {
            throw new IllegalArgumentException("Only .txt files are supported for upload.");
        }

        String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");

        if (content.trim().isEmpty()) {
            throw new IllegalArgumentException("The uploaded file is empty.");
        }

        return content;
    }

    /**
     * Save text content to a file.
     *
     * @param filepath Destination file path
     * @param content  The text to write
     * @return The absolute path of the saved file
     * @throws IOException if file cannot be written
     */
    public static String saveToFile(String filepath, String content) throws IOException {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("No content to save.");
        }

        File file = new File(filepath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        Files.write(file.toPath(), content.getBytes("UTF-8"));
        return file.getAbsolutePath();
    }

    /**
     * Read a file, compress its contents using RLE, and save the result.
     *
     * @param inputPath  Path to the input .txt file
     * @param outputPath Path for the compressed output file (null for auto-naming)
     * @return Array of [compressed_text, output_file_path]
     */
    public static String[] compressFile(String inputPath, String outputPath) throws IOException {
        String content = readFile(inputPath);
        String compressed = compress(content);

        if (outputPath == null) {
            String baseName = inputPath.replaceAll("\\.txt$", "");
            outputPath = baseName + "_compressed.txt";
        }

        String savedPath = saveToFile(outputPath, compressed);
        return new String[]{compressed, savedPath};
    }

    /**
     * Read a compressed file, decompress its contents, and save the result.
     *
     * @param inputPath  Path to the compressed .txt file
     * @param outputPath Path for the decompressed output file (null for auto-naming)
     * @return Array of [decompressed_text, output_file_path]
     */
    public static String[] decompressFile(String inputPath, String outputPath) throws IOException {
        String content = readFile(inputPath);
        String decompressed = decompress(content);

        if (outputPath == null) {
            String baseName = inputPath.replaceAll("\\.txt$", "");
            outputPath = baseName + "_decompressed.txt";
        }

        String savedPath = saveToFile(outputPath, decompressed);
        return new String[]{decompressed, savedPath};
    }

    // ═══════════════════ SIMPLE ALTERNATIVE ALGORITHM ══════════════════

    /**
     * A simple frequency-based encoding (NOT positional like RLE).
     * Used only for performance comparison purposes.
     *
     * This encodes each unique character with its total frequency,
     * sorted by first appearance. It is NOT a real compression
     * algorithm — it loses positional info — but serves as a baseline.
     *
     * @param text The input string
     * @return Frequency-encoded string
     */
    public static String compressFrequency(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Input text is empty.");
        }

        // LinkedHashMap preserves insertion order
        LinkedHashMap<Character, Integer> freq = new LinkedHashMap<>();
        for (char ch : text.toCharArray()) {
            freq.put(ch, freq.getOrDefault(ch, 0) + 1);
        }

        StringBuilder result = new StringBuilder();
        for (Map.Entry<Character, Integer> entry : freq.entrySet()) {
            result.append(entry.getKey()).append(entry.getValue());
        }

        return result.toString();
    }

    // ═══════════════════════ MODULE SELF-TEST ═══════════════════════════

    public static void main(String[] args) {
        String testInput = "AAAABBBCCDDDDDD";
        System.out.println("Original   : " + testInput);

        String encoded = compress(testInput);
        System.out.println("Compressed : " + encoded);

        String decoded = decompress(encoded);
        System.out.println("Decompressed: " + decoded);

        assert decoded.equals(testInput) : "Round-trip failed!";
        System.out.println("\n✓ Round-trip test passed!");

        System.out.println("\n── Step-by-step compression ──");
        for (String[] s : compressStepwise(testInput)) {
            System.out.println("  " + s[4]);
        }

        System.out.println("\n── Step-by-step decompression ──");
        for (String[] s : decompressStepwise(encoded)) {
            System.out.println("  " + s[5]);
        }
    }
}
