/**
 * Analytics.java — Compression Analytics & Performance Metrics
 * ==============================================================
 * This class provides tools for measuring compression effectiveness
 * and comparing algorithm performance.
 *
 * VIVA KEY POINTS:
 *   • Compression ratio = original_size / compressed_size
 *   • Space saved (%) = (1 - compressed/original) × 100
 *   • We use System.nanoTime() for high-precision timing in Java.
 */

public class Analytics {

    /**
     * Holds all compression metrics in a single object.
     */
    public static class Metrics {
        public int originalSize;
        public int compressedSize;
        public double compressionRatio;
        public double spaceSavedPct;
        public boolean isBeneficial;

        @Override
        public String toString() {
            return String.format(
                "Original: %d bytes | Compressed: %d bytes | Ratio: %.4f | Saved: %.2f%% | Beneficial: %s",
                originalSize, compressedSize, compressionRatio, spaceSavedPct, isBeneficial ? "Yes" : "No"
            );
        }
    }

    /**
     * Holds the result of a timed operation.
     */
    public static class TimedResult {
        public String result;
        public double elapsedMs;

        public TimedResult(String result, double elapsedMs) {
            this.result = result;
            this.elapsedMs = elapsedMs;
        }
    }

    /**
     * Holds comparison results for two algorithms.
     */
    public static class AlgorithmComparison {
        public String rleOutput;
        public int rleSize;
        public double rleTimeMs;

        public String freqOutput;
        public int freqSize;
        public double freqTimeMs;
    }

    // ═══════════════════════════ METRICS ════════════════════════════════

    /**
     * Compute compression analytics.
     *
     * @param original   The original text string
     * @param compressed The RLE-compressed text string
     * @return Metrics object with all computed values
     */
    public static Metrics computeMetrics(String original, String compressed) {
        Metrics m = new Metrics();

        m.originalSize = original.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        m.compressedSize = compressed.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;

        if (m.compressedSize == 0) {
            m.compressionRatio = 0.0;
        } else {
            m.compressionRatio = (double) m.originalSize / m.compressedSize;
        }

        if (m.originalSize == 0) {
            m.spaceSavedPct = 0.0;
        } else {
            m.spaceSavedPct = (1.0 - (double) m.compressedSize / m.originalSize) * 100.0;
        }

        // Round to 2 decimal places
        m.compressionRatio = Math.round(m.compressionRatio * 10000.0) / 10000.0;
        m.spaceSavedPct = Math.round(m.spaceSavedPct * 100.0) / 100.0;

        m.isBeneficial = m.compressedSize < m.originalSize;

        return m;
    }

    // ═══════════════════════════ TIMING ═════════════════════════════════

    /**
     * Time a compression operation with nanosecond precision.
     *
     * @param text      The input text
     * @param isCompress true for compression, false for decompression
     * @return TimedResult with the output and elapsed time
     */
    public static TimedResult timeOperation(String text, boolean isCompress) {
        long startNano = System.nanoTime();
        String result;

        if (isCompress) {
            result = RLEEngine.compress(text);
        } else {
            result = RLEEngine.decompress(text);
        }

        long endNano = System.nanoTime();
        double elapsedMs = (endNano - startNano) / 1_000_000.0;

        return new TimedResult(result, Math.round(elapsedMs * 10000.0) / 10000.0);
    }

    // ═══════════════════════ ALGORITHM COMPARISON ══════════════════════

    /**
     * Compare RLE compression against simple frequency-based encoding.
     *
     * @param text The input string to compress
     * @return AlgorithmComparison with results for each algorithm
     */
    public static AlgorithmComparison compareAlgorithms(String text) {
        AlgorithmComparison comp = new AlgorithmComparison();

        // Time RLE compression
        long start = System.nanoTime();
        comp.rleOutput = RLEEngine.compress(text);
        long end = System.nanoTime();
        comp.rleTimeMs = Math.round((end - start) / 1_000_000.0 * 10000.0) / 10000.0;
        comp.rleSize = comp.rleOutput.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;

        // Time frequency-based compression
        start = System.nanoTime();
        comp.freqOutput = RLEEngine.compressFrequency(text);
        end = System.nanoTime();
        comp.freqTimeMs = Math.round((end - start) / 1_000_000.0 * 10000.0) / 10000.0;
        comp.freqSize = comp.freqOutput.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;

        return comp;
    }

    // ═══════════════════════ MODULE SELF-TEST ═══════════════════════════

    public static void main(String[] args) {
        String original = "AAAABBBCCDDDDDD";
        String compressed = RLEEngine.compress(original);

        System.out.println("── Compression Metrics ──");
        Metrics m = computeMetrics(original, compressed);
        System.out.println("  " + m);

        System.out.println("\n── Algorithm Comparison ──");
        AlgorithmComparison comp = compareAlgorithms(original);
        System.out.printf("  RLE:       size=%dB, time=%.4fms%n", comp.rleSize, comp.rleTimeMs);
        System.out.printf("  Frequency: size=%dB, time=%.4fms%n", comp.freqSize, comp.freqTimeMs);
    }
}
