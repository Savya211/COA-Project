import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import java.util.List;

/**
 * RLECompressorGUI.java — Modern Dark-Themed Swing GUI
 * ======================================================
 * This is the main graphical user interface for the RLE File Compression
 * Simulator. It provides a tabbed layout with:
 *
 *   Tab 1: Compress / Decompress  — main workflow with text input, file
 *          upload, output display, metrics, and step-by-step visualization.
 *
 *   Tab 2: COA Analysis — memory representation tables, register-level
 *          simulation trace, and fetch-decode-execute explanation.
 *
 *   Tab 3: Performance — compression vs decompression timing, algorithm
 *          comparison with a visual bar chart.
 *
 * FEATURES:
 *   • Dark mode with modern color palette
 *   • Drag-and-drop file upload
 *   • Real-time compression preview
 *   • Save compressed/decompressed output to file
 *   • Graphical bar chart for compression ratio
 */
public class RLECompressorGUI extends JFrame {

    // ─── Color Palette (Dark Theme) ────────────────────────────────────
    private static final Color BG_DARK       = new Color(18, 18, 24);
    private static final Color BG_PANEL      = new Color(28, 28, 40);
    private static final Color BG_INPUT      = new Color(38, 38, 55);
    private static final Color BG_ACCENT     = new Color(48, 48, 68);
    private static final Color TEXT_PRIMARY   = new Color(230, 230, 240);
    private static final Color TEXT_SECONDARY = new Color(160, 160, 180);
    private static final Color ACCENT_BLUE    = new Color(80, 140, 255);
    private static final Color ACCENT_GREEN   = new Color(80, 220, 130);
    private static final Color ACCENT_RED     = new Color(255, 90, 90);
    private static final Color ACCENT_PURPLE  = new Color(160, 100, 255);
    private static final Color ACCENT_ORANGE  = new Color(255, 160, 60);
    private static final Color BORDER_COLOR   = new Color(60, 60, 80);

    // ─── Fonts ─────────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 13);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 13);

    // ─── GUI Components ────────────────────────────────────────────────
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JTextArea stepLogArea;
    private JLabel statusLabel;
    private JLabel metricsOrigSize, metricsCompSize, metricsRatio, metricsSaved;
    private JPanel barChartPanel;
    private JTextArea coaRegisterArea;
    private JTextArea coaInstructionArea;
    private JTable coaMemoryOrigTable;
    private JTable coaMemoryCompTable;
    private JTextArea perfResultArea;
    private JPanel perfBarChartPanel;

    // ─── State ─────────────────────────────────────────────────────────
    private String lastOriginal = "";
    private String lastCompressed = "";
    private String currentFilePath = null;

    // ═══════════════════════════ CONSTRUCTOR ════════════════════════════

    public RLECompressorGUI() {
        setTitle("RLE File Compression Simulator — COA Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 820);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);

        // Apply dark look globally
        setupLookAndFeel();

        // Main layout
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_DARK);

        // Title bar
        mainPanel.add(createTitleBar(), BorderLayout.NORTH);

        // Tabbed pane
        JTabbedPane tabbedPane = createTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    // ═══════════════════════════ LOOK AND FEEL ═════════════════════════

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("TabbedPane.selected", BG_ACCENT);
        UIManager.put("TabbedPane.contentAreaColor", BG_PANEL);
        UIManager.put("ToolTip.background", BG_ACCENT);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
    }

    // ═══════════════════════════ TITLE BAR ═════════════════════════════

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(BG_DARK);
        titleBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("⚡ RLE File Compression Simulator");
        title.setFont(FONT_TITLE);
        title.setForeground(ACCENT_BLUE);
        titleBar.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Computer Organization & Architecture Project  ");
        subtitle.setFont(FONT_SMALL);
        subtitle.setForeground(TEXT_SECONDARY);
        titleBar.add(subtitle, BorderLayout.EAST);

        return titleBar;
    }

    // ═══════════════════════════ STATUS BAR ════════════════════════════

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(BG_DARK);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        statusLabel = new JLabel("Ready — Enter text or upload a file to begin");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(TEXT_SECONDARY);
        statusBar.add(statusLabel, BorderLayout.WEST);

        return statusBar;
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    // ═══════════════════════════ TABBED PANE ═══════════════════════════

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(FONT_HEADER);
        tabs.setOpaque(true);

        tabs.addTab("  ⚙ Compress / Decompress  ", createCompressTab());
        tabs.addTab("  🔬 COA Analysis  ", createCOATab());
        tabs.addTab("  📊 Performance  ", createPerformanceTab());

        // Style tab backgrounds
        tabs.setBackgroundAt(0, BG_PANEL);
        tabs.setBackgroundAt(1, BG_PANEL);
        tabs.setBackgroundAt(2, BG_PANEL);

        return tabs;
    }

    // ═══════════════════════ TAB 1: COMPRESS/DECOMPRESS ═══════════════

    private JPanel createCompressTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBackground(BG_DARK);
        tab.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top section: Input + Buttons
        JPanel topSection = new JPanel(new BorderLayout(10, 10));
        topSection.setBackground(BG_DARK);

        // Input panel (left side)
        JPanel inputPanel = createStyledPanel("📝 Input");
        inputPanel.setLayout(new BorderLayout(5, 5));

        inputArea = createStyledTextArea(8, 40);
        inputArea.setToolTipText("Type text here or upload a .txt file");

        // Enable drag and drop
        setupDragAndDrop(inputArea);

        // Real-time preview listener
        inputArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { onInputChanged(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { onInputChanged(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { onInputChanged(); }
        });

        JScrollPane inputScroll = new JScrollPane(inputArea);
        styleScrollPane(inputScroll);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        // File upload button
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filePanel.setBackground(BG_PANEL);

        JButton uploadBtn = createStyledButton("📂 Upload File", ACCENT_BLUE);
        uploadBtn.addActionListener(e -> onUploadFile());
        filePanel.add(uploadBtn);

        JButton saveCompBtn = createStyledButton("💾 Save Compressed", ACCENT_GREEN);
        saveCompBtn.addActionListener(e -> onSaveCompressed());
        filePanel.add(saveCompBtn);

        JButton saveDecompBtn = createStyledButton("💾 Save Decompressed", ACCENT_PURPLE);
        saveDecompBtn.addActionListener(e -> onSaveDecompressed());
        filePanel.add(saveDecompBtn);

        inputPanel.add(filePanel, BorderLayout.SOUTH);

        topSection.add(inputPanel, BorderLayout.CENTER);

        // Button panel (right side)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(BG_DARK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        JButton compressBtn = createStyledButton("▶ COMPRESS", ACCENT_GREEN);
        compressBtn.setMaximumSize(new Dimension(200, 45));
        compressBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        compressBtn.addActionListener(e -> onCompress());

        JButton decompressBtn = createStyledButton("◀ DECOMPRESS", ACCENT_BLUE);
        decompressBtn.setMaximumSize(new Dimension(200, 45));
        decompressBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        decompressBtn.addActionListener(e -> onDecompress());

        JButton clearBtn = createStyledButton("✕ CLEAR ALL", ACCENT_RED);
        clearBtn.setMaximumSize(new Dimension(200, 45));
        clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearBtn.addActionListener(e -> onClear());

        JButton compressFileBtn = createStyledButton("📦 COMPRESS FILE", ACCENT_ORANGE);
        compressFileBtn.setMaximumSize(new Dimension(200, 45));
        compressFileBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        compressFileBtn.addActionListener(e -> onCompressFile());

        JButton decompressFileBtn = createStyledButton("📤 DECOMPRESS FILE", ACCENT_PURPLE);
        decompressFileBtn.setMaximumSize(new Dimension(200, 45));
        decompressFileBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        decompressFileBtn.addActionListener(e -> onDecompressFile());

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(compressBtn);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(decompressBtn);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(compressFileBtn);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(decompressFileBtn);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(clearBtn);
        buttonPanel.add(Box.createVerticalGlue());

        topSection.add(buttonPanel, BorderLayout.EAST);

        tab.add(topSection, BorderLayout.NORTH);

        // Bottom section: Output + Metrics + Step Log
        JPanel bottomSection = new JPanel(new GridLayout(1, 3, 10, 0));
        bottomSection.setBackground(BG_DARK);

        // Output panel
        JPanel outputPanel = createStyledPanel("📤 Output (Compressed / Decompressed)");
        outputPanel.setLayout(new BorderLayout(5, 5));
        outputArea = createStyledTextArea(10, 20);
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        styleScrollPane(outputScroll);
        outputPanel.add(outputScroll, BorderLayout.CENTER);

        // Metrics panel
        JPanel metricsPanel = createStyledPanel("📊 Metrics");
        metricsPanel.setLayout(new GridLayout(6, 1, 5, 8));

        metricsOrigSize = createMetricLabel("Original Size:", "—");
        metricsCompSize = createMetricLabel("Compressed Size:", "—");
        metricsRatio    = createMetricLabel("Compression Ratio:", "—");
        metricsSaved    = createMetricLabel("Space Saved:", "—");

        metricsPanel.add(metricsOrigSize);
        metricsPanel.add(metricsCompSize);
        metricsPanel.add(metricsRatio);
        metricsPanel.add(metricsSaved);

        // Mini bar chart for compression ratio
        barChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCompressionBar(g);
            }
        };
        barChartPanel.setBackground(BG_PANEL);
        barChartPanel.setPreferredSize(new Dimension(200, 60));
        metricsPanel.add(barChartPanel);

        // Step log panel
        JPanel stepPanel = createStyledPanel("🔍 Step-by-Step Log");
        stepPanel.setLayout(new BorderLayout(5, 5));
        stepLogArea = createStyledTextArea(10, 20);
        stepLogArea.setEditable(false);
        stepLogArea.setFont(FONT_MONO);
        JScrollPane stepScroll = new JScrollPane(stepLogArea);
        styleScrollPane(stepScroll);
        stepPanel.add(stepScroll, BorderLayout.CENTER);

        bottomSection.add(outputPanel);
        bottomSection.add(metricsPanel);
        bottomSection.add(stepPanel);

        tab.add(bottomSection, BorderLayout.CENTER);

        return tab;
    }

    // ═══════════════════════ TAB 2: COA ANALYSIS ══════════════════════

    private JPanel createCOATab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBackground(BG_DARK);
        tab.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top: Memory tables (before and after)
        JPanel memorySection = new JPanel(new GridLayout(1, 2, 10, 0));
        memorySection.setBackground(BG_DARK);

        // Original memory table
        JPanel origMemPanel = createStyledPanel("🧠 Memory Map — Original");
        origMemPanel.setLayout(new BorderLayout());
        coaMemoryOrigTable = createMemoryTable();
        JScrollPane origScroll = new JScrollPane(coaMemoryOrigTable);
        styleScrollPane(origScroll);
        origMemPanel.add(origScroll, BorderLayout.CENTER);

        // Compressed memory table
        JPanel compMemPanel = createStyledPanel("🧠 Memory Map — Compressed");
        compMemPanel.setLayout(new BorderLayout());
        coaMemoryCompTable = createMemoryTable();
        JScrollPane compScroll = new JScrollPane(coaMemoryCompTable);
        styleScrollPane(compScroll);
        compMemPanel.add(compScroll, BorderLayout.CENTER);

        memorySection.add(origMemPanel);
        memorySection.add(compMemPanel);

        tab.add(memorySection, BorderLayout.NORTH);

        // Bottom: Register simulation + Instruction cycle
        JPanel bottomSection = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomSection.setBackground(BG_DARK);

        // Register simulation panel
        JPanel registerPanel = createStyledPanel("🔧 Register-Level Simulation");
        registerPanel.setLayout(new BorderLayout());
        coaRegisterArea = createStyledTextArea(12, 30);
        coaRegisterArea.setEditable(false);
        coaRegisterArea.setFont(FONT_MONO);
        JScrollPane regScroll = new JScrollPane(coaRegisterArea);
        styleScrollPane(regScroll);

        JButton runSimBtn = createStyledButton("▶ Run Simulation", ACCENT_GREEN);
        runSimBtn.addActionListener(e -> onRunCOASimulation());
        registerPanel.add(regScroll, BorderLayout.CENTER);
        registerPanel.add(runSimBtn, BorderLayout.SOUTH);

        // Instruction cycle panel
        JPanel instructionPanel = createStyledPanel("📖 Fetch-Decode-Execute Cycle");
        instructionPanel.setLayout(new BorderLayout());
        coaInstructionArea = createStyledTextArea(12, 30);
        coaInstructionArea.setEditable(false);
        coaInstructionArea.setFont(FONT_MONO);
        coaInstructionArea.setText(COAAnalysis.instructionCycleExplanation());
        JScrollPane instrScroll = new JScrollPane(coaInstructionArea);
        styleScrollPane(instrScroll);
        instructionPanel.add(instrScroll, BorderLayout.CENTER);

        bottomSection.add(registerPanel);
        bottomSection.add(instructionPanel);

        tab.add(bottomSection, BorderLayout.CENTER);

        return tab;
    }

    // ═══════════════════════ TAB 3: PERFORMANCE ═══════════════════════

    private JPanel createPerformanceTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBackground(BG_DARK);
        tab.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Results text area
        JPanel resultsPanel = createStyledPanel("⏱ Performance Results");
        resultsPanel.setLayout(new BorderLayout());
        perfResultArea = createStyledTextArea(10, 40);
        perfResultArea.setEditable(false);
        perfResultArea.setFont(FONT_MONO);
        JScrollPane resultScroll = new JScrollPane(perfResultArea);
        styleScrollPane(resultScroll);
        resultsPanel.add(resultScroll, BorderLayout.CENTER);

        JButton runPerfBtn = createStyledButton("▶ Run Performance Test", ACCENT_ORANGE);
        runPerfBtn.addActionListener(e -> onRunPerformanceTest());
        resultsPanel.add(runPerfBtn, BorderLayout.SOUTH);

        tab.add(resultsPanel, BorderLayout.NORTH);

        // Bar chart panel
        JPanel chartPanel = createStyledPanel("📊 Algorithm Comparison Chart");
        chartPanel.setLayout(new BorderLayout());

        perfBarChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPerformanceChart(g);
            }
        };
        perfBarChartPanel.setBackground(BG_PANEL);
        perfBarChartPanel.setPreferredSize(new Dimension(600, 300));
        chartPanel.add(perfBarChartPanel, BorderLayout.CENTER);

        tab.add(chartPanel, BorderLayout.CENTER);

        return tab;
    }

    // ═══════════════════════ EVENT HANDLERS ════════════════════════════

    private void onInputChanged() {
        // Real-time preview: show character count
        String text = inputArea.getText();
        if (!text.isEmpty()) {
            setStatus("Input: " + text.length() + " characters — Press Compress to encode", TEXT_SECONDARY);
        }
    }

    private void onCompress() {
        try {
            String input = inputArea.getText();
            if (input.isEmpty()) {
                throw new IllegalArgumentException("Input text is empty. Please enter some text first.");
            }

            lastOriginal = input;

            // Compress
            Analytics.TimedResult result = Analytics.timeOperation(input, true);
            lastCompressed = result.result;

            // Display output
            outputArea.setText(lastCompressed);

            // Compute & display metrics
            updateMetrics(lastOriginal, lastCompressed);

            // Step-by-step log
            displayCompressSteps(input);

            setStatus("✓ Compression successful! Time: " + result.elapsedMs + " ms", ACCENT_GREEN);

        } catch (IllegalArgumentException ex) {
            setStatus("✗ Error: " + ex.getMessage(), ACCENT_RED);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Compression Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDecompress() {
        try {
            String input = inputArea.getText();
            if (input.isEmpty()) {
                throw new IllegalArgumentException("Input text is empty. Please enter compressed text first.");
            }

            // Decompress
            Analytics.TimedResult result = Analytics.timeOperation(input, false);

            // Display output
            outputArea.setText(result.result);
            lastOriginal = result.result;
            lastCompressed = input;

            // Compute & display metrics
            updateMetrics(lastOriginal, lastCompressed);

            // Step-by-step log
            displayDecompressSteps(input);

            setStatus("✓ Decompression successful! Time: " + result.elapsedMs + " ms", ACCENT_GREEN);

        } catch (IllegalArgumentException ex) {
            setStatus("✗ Error: " + ex.getMessage(), ACCENT_RED);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Decompression Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onClear() {
        inputArea.setText("");
        outputArea.setText("");
        stepLogArea.setText("");
        lastOriginal = "";
        lastCompressed = "";
        currentFilePath = null;

        metricsOrigSize.setText("Original Size:       —");
        metricsCompSize.setText("Compressed Size:     —");
        metricsRatio.setText("Compression Ratio:   —");
        metricsSaved.setText("Space Saved:         —");

        barChartPanel.repaint();
        setStatus("Ready — Enter text or upload a file to begin", TEXT_SECONDARY);
    }

    private void onUploadFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        chooser.setDialogTitle("Select a Text File");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String filepath = chooser.getSelectedFile().getAbsolutePath();
                String content = RLEEngine.readFile(filepath);
                inputArea.setText(content);
                currentFilePath = filepath;
                setStatus("✓ File loaded: " + filepath, ACCENT_GREEN);
            } catch (Exception ex) {
                setStatus("✗ Error loading file: " + ex.getMessage(), ACCENT_RED);
                JOptionPane.showMessageDialog(this, ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onSaveCompressed() {
        if (lastCompressed.isEmpty()) {
            setStatus("✗ No compressed data to save. Compress something first.", ACCENT_RED);
            return;
        }
        saveFileDialog(lastCompressed, "compressed");
    }

    private void onSaveDecompressed() {
        if (lastOriginal.isEmpty()) {
            setStatus("✗ No decompressed data to save.", ACCENT_RED);
            return;
        }
        saveFileDialog(lastOriginal, "decompressed");
    }

    private void onCompressFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        chooser.setDialogTitle("Select a File to Compress");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String inputPath = chooser.getSelectedFile().getAbsolutePath();

                // Ask where to save
                JFileChooser saveChooser = new JFileChooser();
                saveChooser.setDialogTitle("Save Compressed File As");
                saveChooser.setSelectedFile(new File(
                    inputPath.replaceAll("\\.txt$", "") + "_compressed.txt"
                ));

                if (saveChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    String outputPath = saveChooser.getSelectedFile().getAbsolutePath();
                    String[] result = RLEEngine.compressFile(inputPath, outputPath);

                    lastCompressed = result[0];
                    lastOriginal = RLEEngine.readFile(inputPath);
                    inputArea.setText(lastOriginal);
                    outputArea.setText(lastCompressed);

                    updateMetrics(lastOriginal, lastCompressed);
                    displayCompressSteps(lastOriginal);

                    setStatus("✓ File compressed & saved to: " + result[1], ACCENT_GREEN);
                }
            } catch (Exception ex) {
                setStatus("✗ Error: " + ex.getMessage(), ACCENT_RED);
                JOptionPane.showMessageDialog(this, ex.getMessage(), "File Compression Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDecompressFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        chooser.setDialogTitle("Select a Compressed File to Decompress");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String inputPath = chooser.getSelectedFile().getAbsolutePath();

                // Ask where to save
                JFileChooser saveChooser = new JFileChooser();
                saveChooser.setDialogTitle("Save Decompressed File As");
                saveChooser.setSelectedFile(new File(
                    inputPath.replaceAll("\\.txt$", "") + "_decompressed.txt"
                ));

                if (saveChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    String outputPath = saveChooser.getSelectedFile().getAbsolutePath();
                    String[] result = RLEEngine.decompressFile(inputPath, outputPath);

                    lastOriginal = result[0];
                    lastCompressed = RLEEngine.readFile(inputPath);
                    inputArea.setText(lastCompressed);
                    outputArea.setText(lastOriginal);

                    updateMetrics(lastOriginal, lastCompressed);
                    displayDecompressSteps(lastCompressed);

                    setStatus("✓ File decompressed & saved to: " + result[1], ACCENT_GREEN);
                }
            } catch (Exception ex) {
                setStatus("✗ Error: " + ex.getMessage(), ACCENT_RED);
                JOptionPane.showMessageDialog(this, ex.getMessage(), "File Decompression Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onRunCOASimulation() {
        String input = inputArea.getText();
        if (input.isEmpty()) {
            setStatus("✗ Enter text first, then run COA simulation.", ACCENT_RED);
            return;
        }

        String compressed = lastCompressed.isEmpty() ? RLEEngine.compress(input) : lastCompressed;

        // Register simulation
        List<String> trace = COAAnalysis.registerSimulation(input);
        StringBuilder sb = new StringBuilder();
        for (String line : trace) {
            sb.append(line).append("\n");
        }
        coaRegisterArea.setText(sb.toString());
        coaRegisterArea.setCaretPosition(0);

        // Memory tables
        List<COAAnalysis.MemoryEntry>[] tables = COAAnalysis.generateMemoryTable(input, compressed);
        updateMemoryTable(coaMemoryOrigTable, tables[0]);
        updateMemoryTable(coaMemoryCompTable, tables[1]);

        setStatus("✓ COA simulation complete — see memory maps and register trace", ACCENT_GREEN);
    }

    private void onRunPerformanceTest() {
        String input = inputArea.getText();
        if (input.isEmpty()) {
            setStatus("✗ Enter text first, then run performance test.", ACCENT_RED);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append("           PERFORMANCE TEST RESULTS\n");
        sb.append("═══════════════════════════════════════════════════════\n\n");

        // Compression timing
        Analytics.TimedResult compResult = Analytics.timeOperation(input, true);
        sb.append(String.format("  ▶ Compression Time:    %.4f ms\n", compResult.elapsedMs));

        // Decompression timing
        Analytics.TimedResult decompResult = Analytics.timeOperation(compResult.result, false);
        sb.append(String.format("  ◀ Decompression Time:  %.4f ms\n", decompResult.elapsedMs));

        sb.append("\n───────────────────────────────────────────────────────\n");
        sb.append("           ALGORITHM COMPARISON\n");
        sb.append("───────────────────────────────────────────────────────\n\n");

        // Algorithm comparison
        Analytics.AlgorithmComparison comp = Analytics.compareAlgorithms(input);

        sb.append(String.format("  RLE Encoding:\n"));
        sb.append(String.format("    Output:  %s\n", truncate(comp.rleOutput, 80)));
        sb.append(String.format("    Size:    %d bytes\n", comp.rleSize));
        sb.append(String.format("    Time:    %.4f ms\n\n", comp.rleTimeMs));

        sb.append(String.format("  Frequency Encoding:\n"));
        sb.append(String.format("    Output:  %s\n", truncate(comp.freqOutput, 80)));
        sb.append(String.format("    Size:    %d bytes\n", comp.freqSize));
        sb.append(String.format("    Time:    %.4f ms\n\n", comp.freqTimeMs));

        sb.append("═══════════════════════════════════════════════════════\n");

        // Store comparison data for chart
        perfBarChartPanel.putClientProperty("rleSize", comp.rleSize);
        perfBarChartPanel.putClientProperty("freqSize", comp.freqSize);
        perfBarChartPanel.putClientProperty("rleTime", comp.rleTimeMs);
        perfBarChartPanel.putClientProperty("freqTime", comp.freqTimeMs);
        perfBarChartPanel.putClientProperty("origSize", input.length());
        perfBarChartPanel.putClientProperty("compTime", compResult.elapsedMs);
        perfBarChartPanel.putClientProperty("decompTime", decompResult.elapsedMs);
        perfBarChartPanel.putClientProperty("hasData", true);

        perfResultArea.setText(sb.toString());
        perfResultArea.setCaretPosition(0);
        perfBarChartPanel.repaint();

        setStatus("✓ Performance test complete", ACCENT_GREEN);
    }

    // ═══════════════════════ HELPER METHODS ═══════════════════════════

    private void updateMetrics(String original, String compressed) {
        Analytics.Metrics m = Analytics.computeMetrics(original, compressed);

        metricsOrigSize.setText("  Original Size:       " + m.originalSize + " bytes");
        metricsCompSize.setText("  Compressed Size:     " + m.compressedSize + " bytes");
        metricsRatio.setText("  Compression Ratio:   " + m.compressionRatio + "x");
        metricsSaved.setText("  Space Saved:         " + m.spaceSavedPct + "%"
            + (m.isBeneficial ? "  ✓" : "  (no saving)"));

        barChartPanel.repaint();
    }

    private void displayCompressSteps(String input) {
        List<String[]> steps = RLEEngine.compressStepwise(input);
        StringBuilder sb = new StringBuilder();
        sb.append("── RLE Compression Steps ──\n\n");
        for (String[] step : steps) {
            sb.append(step[4]).append("\n");
        }
        stepLogArea.setText(sb.toString());
        stepLogArea.setCaretPosition(0);
    }

    private void displayDecompressSteps(String encoded) {
        List<String[]> steps = RLEEngine.decompressStepwise(encoded);
        StringBuilder sb = new StringBuilder();
        sb.append("── RLE Decompression Steps ──\n\n");
        for (String[] step : steps) {
            sb.append(step[5]).append("\n");
        }
        stepLogArea.setText(sb.toString());
        stepLogArea.setCaretPosition(0);
    }

    private void saveFileDialog(String content, String suffix) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save " + suffix + " file");
        String defaultName = "output_" + suffix + ".txt";
        if (currentFilePath != null) {
            String base = currentFilePath.replaceAll("\\.txt$", "");
            defaultName = base + "_" + suffix + ".txt";
        }
        chooser.setSelectedFile(new File(defaultName));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String path = RLEEngine.saveToFile(
                    chooser.getSelectedFile().getAbsolutePath(), content
                );
                setStatus("✓ Saved to: " + path, ACCENT_GREEN);
            } catch (Exception ex) {
                setStatus("✗ Save error: " + ex.getMessage(), ACCENT_RED);
            }
        }
    }

    private void updateMemoryTable(JTable table, List<COAAnalysis.MemoryEntry> entries) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (COAAnalysis.MemoryEntry entry : entries) {
            model.addRow(new Object[]{
                entry.address,
                String.valueOf(entry.character),
                entry.asciiDec,
                entry.hex,
                entry.binary
            });
        }
    }

    private String truncate(String s, int maxLen) {
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }

    // ═══════════════════════ DRAG & DROP ═══════════════════════════════

    private void setupDragAndDrop(JTextArea textArea) {
        new DropTarget(textArea, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable t = dtde.getTransferable();

                    if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                        if (!files.isEmpty()) {
                            File file = files.get(0);
                            String content = RLEEngine.readFile(file.getAbsolutePath());
                            textArea.setText(content);
                            currentFilePath = file.getAbsolutePath();
                            setStatus("✓ File loaded via drag-and-drop: " + file.getName(), ACCENT_GREEN);
                        }
                    }
                    dtde.dropComplete(true);
                } catch (Exception ex) {
                    setStatus("✗ Drag-and-drop error: " + ex.getMessage(), ACCENT_RED);
                    dtde.dropComplete(false);
                }
            }
        });
    }

    // ═══════════════════════ CHART DRAWING ═════════════════════════════

    private void drawCompressionBar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = barChartPanel.getWidth();
        int h = barChartPanel.getHeight();

        if (lastOriginal.isEmpty() || lastCompressed.isEmpty()) {
            g2.setColor(TEXT_SECONDARY);
            g2.setFont(FONT_SMALL);
            g2.drawString("Compress data to see chart", 10, h / 2 + 5);
            return;
        }

        int origLen = lastOriginal.length();
        int compLen = lastCompressed.length();
        int maxLen = Math.max(origLen, compLen);

        int barHeight = 18;
        int margin = 10;

        // Original bar
        int origBarW = (int) ((double) origLen / maxLen * (w - 100));
        g2.setColor(ACCENT_BLUE);
        g2.fillRoundRect(margin, 5, origBarW, barHeight, 6, 6);
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(FONT_SMALL);
        g2.drawString("Original: " + origLen + "B", margin + origBarW + 5, 18);

        // Compressed bar
        int compBarW = (int) ((double) compLen / maxLen * (w - 100));
        g2.setColor(ACCENT_GREEN);
        g2.fillRoundRect(margin, 30, compBarW, barHeight, 6, 6);
        g2.setColor(TEXT_PRIMARY);
        g2.drawString("Compressed: " + compLen + "B", margin + compBarW + 5, 43);
    }


    private void drawPerformanceChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = perfBarChartPanel.getWidth();
        int h = perfBarChartPanel.getHeight();

        Object hasData = perfBarChartPanel.getClientProperty("hasData");
        if (hasData == null || !(Boolean) hasData) {
            g2.setColor(TEXT_SECONDARY);
            g2.setFont(FONT_HEADER);
            g2.drawString("Run a performance test to see charts", w / 2 - 150, h / 2);
            return;
        }

        int origSize = (Integer) perfBarChartPanel.getClientProperty("origSize");
        int rleSize = (Integer) perfBarChartPanel.getClientProperty("rleSize");
        int freqSize = (Integer) perfBarChartPanel.getClientProperty("freqSize");
        double compTime = (Double) perfBarChartPanel.getClientProperty("compTime");
        double decompTime = (Double) perfBarChartPanel.getClientProperty("decompTime");
        double rleTime = (Double) perfBarChartPanel.getClientProperty("rleTime");
        double freqTime = (Double) perfBarChartPanel.getClientProperty("freqTime");

        int margin = 40;
        int barWidth = 50;
        int chartHeight = h - 80;
        int baseY = h - 40;

        // ─── Size Comparison Chart (left half) ───
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(FONT_HEADER);
        g2.drawString("Size Comparison (bytes)", margin, 25);

        int maxSize = Math.max(origSize, Math.max(rleSize, freqSize));
        if (maxSize == 0) maxSize = 1;

        // Original bar
        int barH = (int) ((double) origSize / maxSize * chartHeight);
        g2.setColor(ACCENT_BLUE);
        g2.fillRoundRect(margin, baseY - barH, barWidth, barH, 8, 8);
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(FONT_SMALL);
        g2.drawString("Original", margin, baseY + 15);
        g2.drawString(origSize + "B", margin + 5, baseY - barH - 5);

        // RLE bar
        barH = (int) ((double) rleSize / maxSize * chartHeight);
        g2.setColor(ACCENT_GREEN);
        g2.fillRoundRect(margin + 70, baseY - barH, barWidth, barH, 8, 8);
        g2.setColor(TEXT_PRIMARY);
        g2.drawString("RLE", margin + 80, baseY + 15);
        g2.drawString(rleSize + "B", margin + 75, baseY - barH - 5);

        // Frequency bar
        barH = (int) ((double) freqSize / maxSize * chartHeight);
        g2.setColor(ACCENT_PURPLE);
        g2.fillRoundRect(margin + 140, baseY - barH, barWidth, barH, 8, 8);
        g2.setColor(TEXT_PRIMARY);
        g2.drawString("Freq", margin + 148, baseY + 15);
        g2.drawString(freqSize + "B", margin + 145, baseY - barH - 5);

        // ─── Time Comparison Chart (right half) ───
        int rightStart = w / 2 + 20;
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(FONT_HEADER);
        g2.drawString("Time Comparison (ms)", rightStart, 25);

        double maxTime = Math.max(Math.max(compTime, decompTime), Math.max(rleTime, freqTime));
        if (maxTime == 0) maxTime = 0.001;

        // Compress bar
        barH = (int) (compTime / maxTime * chartHeight);
        barH = Math.max(barH, 3);
        g2.setColor(ACCENT_GREEN);
        g2.fillRoundRect(rightStart, baseY - barH, barWidth, barH, 8, 8);
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(FONT_SMALL);
        g2.drawString("Comp", rightStart + 5, baseY + 15);
        g2.drawString(String.format("%.3fms", compTime), rightStart, baseY - barH - 5);

        // Decompress bar
        barH = (int) (decompTime / maxTime * chartHeight);
        barH = Math.max(barH, 3);
        g2.setColor(ACCENT_BLUE);
        g2.fillRoundRect(rightStart + 70, baseY - barH, barWidth, barH, 8, 8);
        g2.setColor(TEXT_PRIMARY);
        g2.drawString("Decomp", rightStart + 70, baseY + 15);
        g2.drawString(String.format("%.3fms", decompTime), rightStart + 65, baseY - barH - 5);

        // RLE time bar
        barH = (int) (rleTime / maxTime * chartHeight);
        barH = Math.max(barH, 3);
        g2.setColor(ACCENT_ORANGE);
        g2.fillRoundRect(rightStart + 140, baseY - barH, barWidth, barH, 8, 8);
        g2.setColor(TEXT_PRIMARY);
        g2.drawString("RLE", rightStart + 150, baseY + 15);
        g2.drawString(String.format("%.3fms", rleTime), rightStart + 135, baseY - barH - 5);

        // Freq time bar
        barH = (int) (freqTime / maxTime * chartHeight);
        barH = Math.max(barH, 3);
        g2.setColor(ACCENT_PURPLE);
        g2.fillRoundRect(rightStart + 210, baseY - barH, barWidth, barH, 8, 8);
        g2.setColor(TEXT_PRIMARY);
        g2.drawString("Freq", rightStart + 218, baseY + 15);
        g2.drawString(String.format("%.3fms", freqTime), rightStart + 205, baseY - barH - 5);
    }

    // ═══════════════════════ UI COMPONENT FACTORIES ═══════════════════

    private JPanel createStyledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createEmptyBorder(),
                    title,
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    FONT_HEADER,
                    ACCENT_BLUE
                ),
                BorderFactory.createEmptyBorder(5, 8, 8, 8)
            )
        ));
        return panel;
    }

    private JTextArea createStyledTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setBackground(BG_INPUT);
        area.setForeground(TEXT_PRIMARY);
        area.setCaretColor(ACCENT_BLUE);
        area.setFont(FONT_MONO);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return area;
    }

    private JButton createStyledButton(String text, Color accentColor) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setForeground(Color.WHITE);
        button.setBackground(accentColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Hover effect
        Color hoverColor = accentColor.brighter();
        Color pressColor = accentColor.darker();

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(accentColor);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(pressColor);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(hoverColor);
            }
        });

        return button;
    }

    private JLabel createMetricLabel(String label, String value) {
        JLabel lbl = new JLabel("  " + label + "       " + value);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    private JTable createMemoryTable() {
        String[] columns = {"Address", "Char", "ASCII", "Hex", "Binary"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(model);
        table.setBackground(BG_INPUT);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setFont(FONT_MONO);
        table.setRowHeight(24);
        table.getTableHeader().setBackground(BG_ACCENT);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(FONT_HEADER);

        // Center align all cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(BG_INPUT);
        centerRenderer.setForeground(TEXT_PRIMARY);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        return table;
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(BG_INPUT);
    }
}
