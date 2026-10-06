/**
 * COAAnalysis.java — Computer Organization & Architecture (COA) Analysis
 * ========================================================================
 * This class simulates low-level hardware concepts related to RLE
 * compression, including memory representation, register-level operations,
 * and the instruction cycle (fetch-decode-execute).
 *
 * VIVA KEY POINTS:
 *   • In memory, each ASCII character occupies 1 byte (8 bits).
 *   • Registers like AL, CL, SI, DI are used in assembly-level implementations.
 *   • The CPU processes instructions in a cycle: Fetch → Decode → Execute.
 *   • RLE at hardware level involves comparing bytes and incrementing counters.
 */

import java.util.*;

public class COAAnalysis {

    /**
     * Represents one character's memory layout.
     */
    public static class MemoryEntry {
        public String address;
        public char character;
        public int asciiDec;
        public String hex;
        public String binary;

        @Override
        public String toString() {
            return String.format("%s: '%c' = ASCII %d = %s = %s", address, character, asciiDec, hex, binary);
        }
    }

    // ═══════════════════════ MEMORY REPRESENTATION ═════════════════════

    /**
     * Show the memory-level representation of each character.
     *
     * For each character, returns: address, ASCII value, hex, binary (8-bit).
     * This demonstrates how text is stored in RAM at the byte level.
     *
     * @param text The input string
     * @return List of MemoryEntry objects
     */
    public static List<MemoryEntry> memoryRepresentation(String text) {
        List<MemoryEntry> result = new ArrayList<>();
        int baseAddress = 0x1000;  // Simulated starting memory address

        for (int i = 0; i < text.length(); i++) {
            MemoryEntry entry = new MemoryEntry();
            char ch = text.charAt(i);
            int ascii = (int) ch;

            entry.address = String.format("0x%04X", baseAddress + i);
            entry.character = ch;
            entry.asciiDec = ascii;
            entry.hex = String.format("0x%02X", ascii);
            entry.binary = String.format("%8s", Integer.toBinaryString(ascii)).replace(' ', '0');

            result.add(entry);
        }

        return result;
    }

    // ═══════════════════════ REGISTER SIMULATION ═══════════════════════

    /**
     * Simulate register-level operations during RLE compression.
     *
     * Traces CPU registers as the algorithm processes each character:
     *   AL  — Accumulator Low: holds the current character
     *   CL  — Counter register: counts consecutive occurrences
     *   SI  — Source Index: pointer to current position in input
     *   DI  — Destination Index: pointer to current position in output
     *   ZF  — Zero Flag: set when comparison result is zero (chars match)
     *
     * @param text The input string being compressed
     * @return List of trace strings
     */
    public static List<String> registerSimulation(String text) {
        List<String> trace = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            trace.add("No input — registers idle.");
            return trace;
        }

        trace.add("╔══════════════════════════════════════════════════════════╗");
        trace.add("║          REGISTER-LEVEL RLE SIMULATION                  ║");
        trace.add("╠══════════════════════════════════════════════════════════╣");
        trace.add(String.format("║  Input string: \"%s\"", text));
        trace.add(String.format("║  Length: %d bytes", text.length()));
        trace.add("╠══════════════════════════════════════════════════════════╣");
        trace.add("║  Initializing registers:                                ║");
        trace.add("║    SI ← 0x1000  (source index = start of input)        ║");
        trace.add("║    DI ← 0x2000  (dest index = start of output buffer)  ║");
        trace.add("║    CL ← 0       (counter = 0)                          ║");
        trace.add("╠══════════════════════════════════════════════════════════╣");

        int si = 0;
        int di = 0;
        int i = 0;

        while (i < text.length()) {
            char currentChar = text.charAt(i);
            int count = 1;
            int asciiVal = (int) currentChar;

            trace.add(String.format("║ ── Iteration (SI offset = %d) ──", si));
            trace.add(String.format("║  FETCH : Load byte at [SI+%d] into AL", si));
            trace.add(String.format("║  AL ← 0x%02X ('%c')  |  CL ← 1", asciiVal, currentChar));

            while (i + count < text.length() && text.charAt(i + count) == currentChar) {
                count++;
                trace.add(String.format("║  CMP   : [SI+%d] == AL?  → YES (ZF=1)  →  INC CL = %d", si + count - 1, count));
            }

            if (i + count < text.length()) {
                int nextAscii = (int) text.charAt(i + count);
                trace.add(String.format("║  CMP   : [SI+%d] = 0x%02X ('%c') ≠ AL  → ZF=0  →  STOP counting",
                    si + count, nextAscii, text.charAt(i + count)));
            } else {
                trace.add("║  END   : Reached end of input string");
            }

            trace.add(String.format("║  STORE : [DI+%d] ← AL = '%c'  |  [DI+%d] ← CL = %d", di, currentChar, di + 1, count));
            trace.add(String.format("║  Result: '%c%d' written to output buffer", currentChar, count));
            trace.add("║");

            si += count;
            di += 2;
            i += count;
        }

        trace.add("╠══════════════════════════════════════════════════════════╣");
        trace.add(String.format("║  Final SI = 0x%04X  (source fully consumed)", 0x1000 + si));
        trace.add(String.format("║  Final DI = 0x%04X  (output length = %d bytes)", 0x2000 + di, di));
        trace.add("╚══════════════════════════════════════════════════════════╝");

        return trace;
    }

    // ═══════════════════ INSTRUCTION CYCLE EXPLANATION ═════════════════

    /**
     * Return a detailed explanation of the Instruction Cycle
     * (Fetch-Decode-Execute) as it relates to RLE processing.
     *
     * @return Multi-line formatted string
     */
    public static String instructionCycleExplanation() {
        return
            "╔══════════════════════════════════════════════════════════════════╗\n" +
            "║              INSTRUCTION CYCLE: FETCH → DECODE → EXECUTE       ║\n" +
            "╠══════════════════════════════════════════════════════════════════╣\n" +
            "║                                                                  ║\n" +
            "║  The CPU executes every instruction in 3 main phases:            ║\n" +
            "║                                                                  ║\n" +
            "║  ┌─────────────────────────────────────────────────────────┐    ║\n" +
            "║  │  1. FETCH                                                │    ║\n" +
            "║  │     • The Program Counter (PC) holds the address of the  │    ║\n" +
            "║  │       next instruction in memory.                        │    ║\n" +
            "║  │     • The CPU reads the instruction from memory and      │    ║\n" +
            "║  │       loads it into the Instruction Register (IR).       │    ║\n" +
            "║  │     • PC is incremented to point to the next instruction.│    ║\n" +
            "║  └─────────────────────────────────────────────────────────┘    ║\n" +
            "║                          ↓                                       ║\n" +
            "║  ┌─────────────────────────────────────────────────────────┐    ║\n" +
            "║  │  2. DECODE                                               │    ║\n" +
            "║  │     • The Control Unit (CU) reads the opcode from IR.    │    ║\n" +
            "║  │     • It determines what operation to perform and which  │    ║\n" +
            "║  │       operands (registers/memory) are involved.          │    ║\n" +
            "║  │     • For RLE: decode CMP, INC, MOV, JMP instructions.  │    ║\n" +
            "║  └─────────────────────────────────────────────────────────┘    ║\n" +
            "║                          ↓                                       ║\n" +
            "║  ┌─────────────────────────────────────────────────────────┐    ║\n" +
            "║  │  3. EXECUTE                                              │    ║\n" +
            "║  │     • The ALU performs the operation (compare, add, etc.)│    ║\n" +
            "║  │     • Results are stored in registers or memory.         │    ║\n" +
            "║  │     • Flags (ZF, CF) are updated based on the result.    │    ║\n" +
            "║  │     • For RLE: ALU compares bytes, increments counter.   │    ║\n" +
            "║  └─────────────────────────────────────────────────────────┘    ║\n" +
            "║                                                                  ║\n" +
            "║  ── RLE Example: Encoding \"AAA\" ──                               ║\n" +
            "║                                                                  ║\n" +
            "║  Cycle 1: FETCH MOV AL, [SI]    → Load 'A' into AL             ║\n" +
            "║           DECODE → MOV instruction, source=[SI], dest=AL         ║\n" +
            "║           EXECUTE → AL = 0x41 ('A')                              ║\n" +
            "║                                                                  ║\n" +
            "║  Cycle 2: FETCH MOV CL, 1       → Initialize counter            ║\n" +
            "║           DECODE → MOV instruction, immediate, dest=CL           ║\n" +
            "║           EXECUTE → CL = 1                                       ║\n" +
            "║                                                                  ║\n" +
            "║  Cycle 3: FETCH CMP [SI+1], AL  → Compare next byte with AL    ║\n" +
            "║           DECODE → CMP instruction, source=[SI+1], dest=AL      ║\n" +
            "║           EXECUTE → 0x41 - 0x41 = 0 → ZF = 1 (match!)          ║\n" +
            "║                                                                  ║\n" +
            "║  Cycle 4: FETCH INC CL          → Increment counter             ║\n" +
            "║           DECODE → INC instruction, operand=CL                   ║\n" +
            "║           EXECUTE → CL = 2                                       ║\n" +
            "║                                                                  ║\n" +
            "║  Cycle 5: FETCH CMP [SI+2], AL  → Compare next byte             ║\n" +
            "║           DECODE → CMP instruction                                ║\n" +
            "║           EXECUTE → 0x41 - 0x41 = 0 → ZF = 1 (match!)          ║\n" +
            "║                                                                  ║\n" +
            "║  Cycle 6: FETCH INC CL          → CL = 3                        ║\n" +
            "║                                                                  ║\n" +
            "║  Cycle 7: FETCH CMP [SI+3], AL  → Next byte is different        ║\n" +
            "║           EXECUTE → ZF = 0 (no match) → exit counting loop      ║\n" +
            "║                                                                  ║\n" +
            "║  Cycle 8: FETCH MOV [DI], AL    → Store 'A' in output           ║\n" +
            "║  Cycle 9: FETCH MOV [DI+1], CL  → Store '3' in output          ║\n" +
            "║                                                                  ║\n" +
            "╚══════════════════════════════════════════════════════════════════╝\n";
    }

    // ═══════════════════ MEMORY TABLE GENERATION ══════════════════════

    /**
     * Generate before/after memory maps for comparison.
     *
     * @param original   The original text
     * @param compressed The RLE-compressed text
     * @return Array of [originalMap, compressedMap] lists
     */
    @SuppressWarnings("unchecked")
    public static List<MemoryEntry>[] generateMemoryTable(String original, String compressed) {
        List<MemoryEntry> origMap = memoryRepresentation(original);
        List<MemoryEntry> compMap = memoryRepresentation(compressed);
        return new List[]{origMap, compMap};
    }

    // ═══════════════════════ MODULE SELF-TEST ═══════════════════════════

    public static void main(String[] args) {
        String test = "AAABBC";

        System.out.println("── Memory Representation ──");
        for (MemoryEntry entry : memoryRepresentation(test)) {
            System.out.println("  " + entry);
        }

        System.out.println("\n── Register Simulation ──");
        for (String line : registerSimulation(test)) {
            System.out.println(line);
        }

        System.out.println("\n── Instruction Cycle ──");
        System.out.println(instructionCycleExplanation());
    }
}
