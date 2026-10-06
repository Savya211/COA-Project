# 🗜️ File Compression Simulator using Run-Length Encoding (RLE)
### With GUI and Low-Level (COA) Analysis

A complete Java-based project that demonstrates **Run-Length Encoding (RLE)** for file compression and decompression, featuring a modern dark-themed GUI, analytics dashboard, computer organization concepts, and performance benchmarking.

---

## ✨ Features

| # | Feature | Description |
|---|---------|-------------|
| 1 | **RLE Compression** | Compress text by replacing consecutive repeated characters with character + count (e.g., `AAABBB` → `A3B3`) |
| 2 | **RLE Decompression** | Reverse the process to restore original text |
| 3 | **File Upload & Save** | Read `.txt` files, save compressed/decompressed output |
| 4 | **Drag & Drop** | Drop a `.txt` file directly onto the input area |
| 5 | **Dark-Themed GUI** | Modern Swing interface with styled panels, buttons, and dark color palette |
| 6 | **Step-by-Step Visualization** | See how RLE processes each character run in real time |
| 7 | **Compression Metrics** | Original size, compressed size, compression ratio, space saved (%) |
| 8 | **COA Analysis** | Memory representation (hex/binary), register-level simulation, instruction cycle explanation |
| 9 | **Performance Comparison** | Timing for compress vs decompress, RLE vs frequency-based encoding, with bar charts |
| 10 | **Assembly Snippet** | x86 NASM implementation of RLE for low-level understanding |
| 11 | **Real-Time Preview** | Input character count updates as you type |
| 12 | **Error Handling** | User-friendly messages for empty input, invalid formats, and file errors |

---

## 🛠️ Technology Stack

- **Language:** Java (JDK 8+)
- **GUI Framework:** Java Swing
- **Assembly:** x86 NASM (educational reference)
- **IDE:** Any Java IDE (IntelliJ, Eclipse, VS Code, or command line)

---

## 📁 Project Structure

```
COA Project/
├── src/
│   ├── Main.java                  ← Application entry point
│   ├── RLEEngine.java             ← Core RLE compression/decompression engine
│   ├── Analytics.java             ← Compression metrics & performance timing
│   ├── COAAnalysis.java           ← Memory representation & register simulation
│   └── RLECompressorGUI.java      ← Dark-themed Swing GUI (3 tabs)
├── samples/
│   ├── sample_input.txt           ← Example input text
│   ├── sample_compressed.txt      ← RLE-compressed output
│   └── sample_decompressed.txt    ← Decompressed (matches original)
├── rle.asm                        ← x86 Assembly implementation of RLE
└── README.md                      ← This file
```

---

## 🚀 How to Compile & Run

### Prerequisites
- **Java JDK 8 or higher** installed
- Verify with: `java -version` and `javac -version`

### Compile
```bash
cd "COA Project"
javac -d out src/*.java
```

### Run the GUI
```bash
java -cp out Main
```

### Run Individual Module Tests (optional)
```bash
java -cp out RLEEngine      # Tests compression/decompression with step log
java -cp out Analytics       # Tests metrics and algorithm comparison
```

---

## 🖥️ GUI Overview

The application has **3 tabs**:

### Tab 1: ⚙ Compress / Decompress
- Enter text manually or upload a `.txt` file
- Click **COMPRESS** or **DECOMPRESS**
- View output, metrics (size, ratio, space saved), and step-by-step log
- Save results to file

### Tab 2: 🔬 COA Analysis
- **Memory Map** — Shows ASCII, hex, and binary representation of each character (before and after compression)
- **Register Simulation** — Traces AL, CL, SI, DI registers during RLE
- **Fetch-Decode-Execute** — Detailed explanation of the CPU instruction cycle applied to RLE

### Tab 3: 📊 Performance
- Times compression and decompression operations
- Compares RLE encoding vs frequency-based encoding
- Visual bar charts for size and time comparison

---

## 📖 Viva Preparation Guide

### Q: What is Run-Length Encoding?
**A:** RLE is a lossless compression algorithm that replaces consecutive runs of the same character with the character followed by its count. Example: `AAABBB` → `A3B3`.

### Q: When is RLE effective?
**A:** RLE works best when data has many consecutive repeated characters (e.g., simple images, DNA sequences). It is **not effective** when characters rarely repeat — the output may be larger than the input.

### Q: What is the compression ratio?
**A:** `Compression Ratio = Original Size / Compressed Size`. A ratio > 1 means compression was beneficial. Space saved `(%) = (1 - Compressed/Original) × 100`.

### Q: How does RLE relate to Computer Organization?
**A:**
- **Memory:** Each ASCII character occupies 1 byte (8 bits). RLE reduces the number of bytes stored.
- **Registers:** AL holds the current character, CL counts repetitions, SI/DI are source/destination pointers.
- **Instruction Cycle:** The CPU processes RLE using Fetch → Decode → Execute for each MOV, CMP, INC, and JMP instruction.

### Q: Explain the Fetch-Decode-Execute cycle.
**A:**
1. **Fetch** — The CPU reads the next instruction from memory using the Program Counter (PC).
2. **Decode** — The Control Unit interprets the opcode and identifies operands.
3. **Execute** — The ALU performs the operation (compare bytes, increment counter, store result).

### Q: What registers are used in RLE at the assembly level?
**A:**
| Register | Purpose |
|----------|---------|
| AL | Accumulator — holds the current character |
| CL | Counter — counts consecutive occurrences |
| SI | Source Index — pointer to input buffer |
| DI | Destination Index — pointer to output buffer |
| ZF | Zero Flag — set when CMP finds matching bytes |

### Q: What is the alternative algorithm you compared?
**A:** We implemented a simple **frequency-based encoding** that counts total occurrences of each unique character (e.g., `ABBA` → `A2B2`). Unlike RLE, it loses positional information and is not reversible—it serves purely as a performance baseline.

### Q: How do you handle errors?
**A:** The application validates for: empty input, non-.txt files, invalid RLE format during decompression, and file I/O errors. All errors show user-friendly messages in the GUI status bar and dialog boxes.

---

## 📝 Sample Input/Output

**Input:** `AAAABBBCCDDDDDDEEEFFFFFFF`

**Compressed:** `A4B3C2D6E3F7`

**Analysis:**
- Original: 25 bytes
- Compressed: 12 bytes
- Compression Ratio: 2.0833x
- Space Saved: 52.00%

---

## 📄 License

This project was created for educational purposes as part of a Computer Organization & Architecture (COA) course project.
