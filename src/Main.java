import javax.swing.*;

/**
 * Main.java — Application Entry Point
 * =====================================
 * Launches the RLE File Compression Simulator GUI.
 *
 * HOW TO RUN:
 *   1. Compile all source files:   javac -d out src/*.java
 *   2. Run the application:        java -cp out Main
 *
 * VIVA KEY POINT:
 *   • We use SwingUtilities.invokeLater() to ensure the GUI is created on
 *     the Event Dispatch Thread (EDT), which is Java Swing's requirement
 *     for thread-safe GUI operations.
 */
public class Main {

    public static void main(String[] args) {
        // Launch the GUI on the Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            RLECompressorGUI gui = new RLECompressorGUI();
            gui.setVisible(true);
        });
    }
}
