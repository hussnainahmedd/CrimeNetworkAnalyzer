import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.Timer;

// Main Application Class
public class CrimeNetworkAnalyzer extends JFrame {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen());
    }
    
    private final JTabbedPane tabbedPane;
    private static final String DATA_DIR ="C:\\Users\\husss\\OneDrive\\Documents\\NetBeansProjects\\DSA PROEJCT\\CrimeNetworkAnalyzer\\data\\";
    private static final String REQUEST_FILE = DATA_DIR + "request.json";
    private static final String RESPONSE_FILE = DATA_DIR + "response.json";
    private static final String BACKEND_STATUS_FILE = DATA_DIR + ".backend_running";
    private final String currentUser;
    private final String userRole;
    
    public CrimeNetworkAnalyzer(String username, String role) {
        this.currentUser = username;
        this.userRole = role;
        
        // Ensure data directory exists
        createDataDirectory();
        
        // Check if backend is running
        checkBackendStatus();
        
        setTitle("Crime Network Analyzer - " + role.toUpperCase() + ": " + username);
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create background panel
        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // Create header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed pane with semi-transparent background
        tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);
        tabbedPane.setBackground(new Color(0, 0, 0, 150));
        tabbedPane.setForeground(Color.WHITE);
        
        if (role.equals("admin")) {
            // Admin has access to all tabs
            tabbedPane.addTab("Add Suspect", createSuspectPanel());
            tabbedPane.addTab("Add Crime Location", createLocationPanel());
            tabbedPane.addTab("Add Connection", createConnectionPanel());
            tabbedPane.addTab("Analyze Network", createAnalysisPanel());
            tabbedPane.addTab("Case Hierarchy", createCasePanel());
            tabbedPane.addTab("Manage Officers", createOfficerManagementPanel());
        } else {
            // Officers have limited access
            tabbedPane.addTab("My Assigned Cases", createOfficerDashboard());
            tabbedPane.addTab("View Network", createAnalysisPanel());
            tabbedPane.addTab("Case Details", createCasePanel());
        }
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
        setVisible(true);
    }
    
    // Check if C++ backend is running
    private void checkBackendStatus() {
        File statusFile = new File(BACKEND_STATUS_FILE);
        if (!statusFile.exists()) {
            int result = JOptionPane.showConfirmDialog(
                null,
                "⚠️ WARNING: C++ Backend Not Detected!\n\n" +
                "The backend server is not running. The application will not work properly.\n\n" +
                "Please:\n" +
                "1. Open a terminal/command prompt\n" +
                "2. Navigate to your project directory\n" +
                "3. Compile: g++ CrimeNetworkBackend.cpp -o backend\n" +
                "4. Run: ./backend (Linux/Mac) or backend.exe (Windows)\n\n" +
                "Do you want to continue anyway?",
                "Backend Not Running",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (result == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        }
    }
    
    // Create data directory if it doesn't exist
    private void createDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    // Custom Panel with Background Image
    class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Create gradient background
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(15, 32, 39),
                0, getHeight(), new Color(32, 58, 67)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Add grid pattern
            g2d.setColor(new Color(255, 255, 255, 20));
            for (int i = 0; i < getWidth(); i += 50) {
                g2d.drawLine(i, 0, i, getHeight());
            }
            for (int i = 0; i < getHeight(); i += 50) {
                g2d.drawLine(0, i, getWidth(), i);
            }
            
            // Add some decorative elements (evidence markers)
            g2d.setColor(new Color(231, 76, 60, 100));
            g2d.fillOval(100, 100, 15, 15);
            g2d.fillOval(getWidth() - 120, 150, 15, 15);
            g2d.fillOval(200, getHeight() - 150, 15, 15);
            
            // Draw connecting lines (investigation web)
            g2d.setStroke(new BasicStroke(1f));
            g2d.setColor(new Color(52, 152, 219, 80));
            g2d.drawLine(100, 100, getWidth() - 120, 150);
            g2d.drawLine(getWidth() - 120, 150, 200, getHeight() - 150);
            g2d.drawLine(200, getHeight() - 150, 100, 100);
        }
    }
    
    // Header Panel
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("🔍 CRIME NETWORK ANALYZER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(236, 240, 241));
        
        JLabel userLabel = new JLabel("👤 " + currentUser + " (" + userRole.toUpperCase() + ")");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(new Color(189, 195, 199));
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginScreen();
        });
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel, BorderLayout.NORTH);
        leftPanel.add(userLabel, BorderLayout.SOUTH);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(logoutBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    // Officer Dashboard
    private JPanel createOfficerDashboard() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("📋 MY ASSIGNED CASES");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        String[] columns = {"Case ID", "Priority", "Status", "Assigned Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        // Load assigned cases for this officer
        loadOfficerCases(model);
        
        JTable table = new JTable(model);
        table.setBackground(new Color(44, 62, 80, 200));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(52, 73, 94));
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        
        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setBackground(new Color(52, 152, 219));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            loadOfficerCases(model);
        });
        
        buttonPanel.add(refreshBtn);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Officer Management Panel (Admin only)
    private JPanel createOfficerManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("👮 OFFICER MANAGEMENT & CASE ASSIGNMENT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        // Assignment panel
        JPanel assignPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        assignPanel.setOpaque(false);
        assignPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            "Assign Case to Officer",
            0, 0, new Font("Arial", Font.BOLD, 14), Color.WHITE
        ));
        
        JLabel officerLabel = new JLabel("Officer ID:");
        officerLabel.setForeground(Color.WHITE);
        JTextField officerField = new JTextField();
        officerField.setBackground(new Color(44, 62, 80));
        officerField.setForeground(Color.WHITE);
        officerField.setCaretColor(Color.WHITE);
        
        JLabel caseLabel = new JLabel("Case ID:");
        caseLabel.setForeground(Color.WHITE);
        JTextField caseField = new JTextField();
        caseField.setBackground(new Color(44, 62, 80));
        caseField.setForeground(Color.WHITE);
        caseField.setCaretColor(Color.WHITE);
        
        JLabel priorityLabel = new JLabel("Priority:");
        priorityLabel.setForeground(Color.WHITE);
        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"High", "Medium", "Low"});
        priorityCombo.setBackground(new Color(44, 62, 80));
        priorityCombo.setForeground(Color.WHITE);
        
        assignPanel.add(officerLabel);
        assignPanel.add(officerField);
        assignPanel.add(caseLabel);
        assignPanel.add(caseField);
        assignPanel.add(priorityLabel);
        assignPanel.add(priorityCombo);
        
        JButton assignBtn = new JButton("Assign Case");
        assignBtn.setBackground(new Color(155, 89, 182));
        assignBtn.setForeground(Color.WHITE);
        assignBtn.setFocusPainted(false);
        
        JButton addOfficerBtn = new JButton("Add New Officer");
        addOfficerBtn.setBackground(new Color(52, 152, 219));
        addOfficerBtn.setForeground(Color.WHITE);
        addOfficerBtn.setFocusPainted(false);
        
        JTextArea statusArea = new JTextArea(5, 40);
        statusArea.setEditable(false);
        statusArea.setBackground(new Color(44, 62, 80, 200));
        statusArea.setForeground(Color.WHITE);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setOpaque(false);
        statusScroll.getViewport().setOpaque(false);
        
        assignBtn.addActionListener(e -> {
            String officerId = officerField.getText().trim();
            String caseId = caseField.getText().trim();
            String priority = priorityCombo.getSelectedItem().toString();
            
            if (!officerId.isEmpty() && !caseId.isEmpty()) {
                JSONObject request = new JSONObject();
                request.put("action", "assign_case");
                request.put("officer_id", officerId);
                request.put("case_id", caseId);
                request.put("priority", priority);
                
                writeRequest(request);
                statusArea.setText("⏳ Processing assignment...");
                
                // Wait for response
                Timer timer = new Timer(1500, evt -> {
                    String response = readResponse();
                    statusArea.setText(response);
                });
                timer.setRepeats(false);
                timer.start();
                
                officerField.setText("");
                caseField.setText("");
            } else {
                statusArea.setText("❌ Please fill in all fields!");
            }
        });
        
        addOfficerBtn.addActionListener(e -> showAddOfficerDialog(statusArea));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(assignBtn);
        buttonPanel.add(addOfficerBtn);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(assignPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusScroll, BorderLayout.SOUTH);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Styled Suspect Panel
    private JPanel createSuspectPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setOpaque(false);
        
        JLabel idLabel = new JLabel("Suspect ID:");
        idLabel.setForeground(Color.WHITE);
        idLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField idField = new JTextField();
        idField.setBackground(new Color(44, 62, 80));
        idField.setForeground(Color.WHITE);
        idField.setCaretColor(Color.WHITE);
        
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField nameField = new JTextField();
        nameField.setBackground(new Color(44, 62, 80));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        
        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setForeground(Color.WHITE);
        ageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField ageField = new JTextField();
        ageField.setBackground(new Color(44, 62, 80));
        ageField.setForeground(Color.WHITE);
        ageField.setCaretColor(Color.WHITE);
        
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setForeground(Color.WHITE);
        addressLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField addressField = new JTextField();
        addressField.setBackground(new Color(44, 62, 80));
        addressField.setForeground(Color.WHITE);
        addressField.setCaretColor(Color.WHITE);
        
        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(ageLabel);
        formPanel.add(ageField);
        formPanel.add(addressLabel);
        formPanel.add(addressField);
        
        JButton addButton = new JButton("🔒 Add Suspect");
        addButton.setBackground(new Color(231, 76, 60));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        JTextArea statusArea = new JTextArea(5, 40);
        statusArea.setEditable(false);
        statusArea.setBackground(new Color(44, 62, 80, 200));
        statusArea.setForeground(Color.WHITE);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusArea.setBorder(BorderFactory.createLineBorder(new Color(52, 73, 94)));
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        addButton.addActionListener(e -> {
            try {
                if (idField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
                    statusArea.setText("❌ Error: ID and Name are required!");
                    return;
                }
                
                JSONObject request = new JSONObject();
                request.put("action", "add_suspect");
                request.put("id", idField.getText().trim());
                request.put("name", nameField.getText().trim());
                request.put("age", ageField.getText().trim().isEmpty() ? "Unknown" : ageField.getText().trim());
                request.put("address", addressField.getText().trim().isEmpty() ? "Unknown" : addressField.getText().trim());
                
                writeRequest(request);
                
                statusArea.setText("⏳ Sending to backend...\nPlease wait for confirmation...");
                
                // Clear fields immediately
                idField.setText("");
                nameField.setText("");
                ageField.setText("");
                addressField.setText("");
                
                // Wait for response from C++ backend
                Timer timer = new Timer(1500, evt -> {
                    String response = readResponse();
                    statusArea.setText(response);
                });
                timer.setRepeats(false);
                timer.start();
                
            } catch (Exception ex) {
                statusArea.setText("❌ Error: " + ex.getMessage());
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Styled Location Panel
    private JPanel createLocationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setOpaque(false);
        
        JLabel idLabel = new JLabel("Location ID:");
        idLabel.setForeground(Color.WHITE);
        idLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField idField = new JTextField();
        idField.setBackground(new Color(44, 62, 80));
        idField.setForeground(Color.WHITE);
        idField.setCaretColor(Color.WHITE);
        
        JLabel nameLabel = new JLabel("Location Name:");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField nameField = new JTextField();
        nameField.setBackground(new Color(44, 62, 80));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        
        JLabel typeLabel = new JLabel("Crime Type:");
        typeLabel.setForeground(Color.WHITE);
        typeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField typeField = new JTextField();
        typeField.setBackground(new Color(44, 62, 80));
        typeField.setForeground(Color.WHITE);
        typeField.setCaretColor(Color.WHITE);
        
        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(typeLabel);
        formPanel.add(typeField);
        
        JButton addButton = new JButton("📍 Add Crime Location");
        addButton.setBackground(new Color(230, 126, 34));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        JTextArea statusArea = new JTextArea(5, 40);
        statusArea.setEditable(false);
        statusArea.setBackground(new Color(44, 62, 80, 200));
        statusArea.setForeground(Color.WHITE);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusArea.setBorder(BorderFactory.createLineBorder(new Color(52, 73, 94)));
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        addButton.addActionListener(e -> {
            try {
                if (idField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
                    statusArea.setText("❌ Error: ID and Location Name are required!");
                    return;
                }
                
                JSONObject request = new JSONObject();
                request.put("action", "add_location");
                request.put("id", idField.getText().trim());
                request.put("name", nameField.getText().trim());
                request.put("type", typeField.getText().trim().isEmpty() ? "Unknown" : typeField.getText().trim());
                
                writeRequest(request);
                
                statusArea.setText("⏳ Sending to backend...\nPlease wait for confirmation...");
                
                idField.setText("");
                nameField.setText("");
                typeField.setText("");
                
                Timer timer = new Timer(1500, evt -> {
                    String response = readResponse();
                    statusArea.setText(response);
                });
                timer.setRepeats(false);
                timer.start();
                
            } catch (Exception ex) {
                statusArea.setText("❌ Error: " + ex.getMessage());
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Styled Connection Panel
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setOpaque(false);
        
        JLabel fromLabel = new JLabel("From ID:");
        fromLabel.setForeground(Color.WHITE);
        fromLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField fromField = new JTextField();
        fromField.setBackground(new Color(44, 62, 80));
        fromField.setForeground(Color.WHITE);
        fromField.setCaretColor(Color.WHITE);
        
        JLabel toLabel = new JLabel("To ID:");
        toLabel.setForeground(Color.WHITE);
        toLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField toField = new JTextField();
        toField.setBackground(new Color(44, 62, 80));
        toField.setForeground(Color.WHITE);
        toField.setCaretColor(Color.WHITE);
        
        JLabel typeLabel = new JLabel("Relationship Type:");
        typeLabel.setForeground(Color.WHITE);
        typeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
            "friend", "family", "call_history", "visited_location", "accomplice", "witness"
        });
        typeCombo.setBackground(new Color(44, 62, 80));
        typeCombo.setForeground(Color.WHITE);
        
        formPanel.add(fromLabel);
        formPanel.add(fromField);
        formPanel.add(toLabel);
        formPanel.add(toField);
        formPanel.add(typeLabel);
        formPanel.add(typeCombo);
        
        JButton addButton = new JButton("🔗 Add Connection");
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        JTextArea statusArea = new JTextArea(5, 40);
        statusArea.setEditable(false);
        statusArea.setBackground(new Color(44, 62, 80, 200));
        statusArea.setForeground(Color.WHITE);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusArea.setBorder(BorderFactory.createLineBorder(new Color(52, 73, 94)));
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        addButton.addActionListener(e -> {
            try {
                if (fromField.getText().trim().isEmpty() || toField.getText().trim().isEmpty()) {
                    statusArea.setText("❌ Error: Both From ID and To ID are required!");
                    return;
                }
                
                JSONObject request = new JSONObject();
                request.put("action", "add_connection");
                request.put("from", fromField.getText().trim());
                request.put("to", toField.getText().trim());
                request.put("type", typeCombo.getSelectedItem().toString());
                
                writeRequest(request);
                
                statusArea.setText("⏳ Sending to backend...\nPlease wait for confirmation...");
                
                fromField.setText("");
                toField.setText("");
                
                Timer timer = new Timer(1500, evt -> {
                    String response = readResponse();
                    statusArea.setText(response);
                });
                timer.setRepeats(false);
                timer.start();
                
            } catch (Exception ex) {
                statusArea.setText("❌ Error: " + ex.getMessage());
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Styled Analysis Panel
    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setOpaque(false);
        
        JLabel startLabel = new JLabel("Start Suspect ID:");
        startLabel.setForeground(Color.WHITE);
        startLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField startField = new JTextField();
        startField.setBackground(new Color(44, 62, 80));
        startField.setForeground(Color.WHITE);
        startField.setCaretColor(Color.WHITE);
        
        JLabel endLabel = new JLabel("End Suspect ID:");
        endLabel.setForeground(Color.WHITE);
        endLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField endField = new JTextField();
        endField.setBackground(new Color(44, 62, 80));
        endField.setForeground(Color.WHITE);
        endField.setCaretColor(Color.WHITE);
        
        inputPanel.add(startLabel);
        inputPanel.add(startField);
        inputPanel.add(endLabel);
        inputPanel.add(endField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        
        JButton bfsButton = new JButton("🔍 Find Shortest Path (BFS)");
        bfsButton.setBackground(new Color(155, 89, 182));
        bfsButton.setForeground(Color.WHITE);
        bfsButton.setFocusPainted(false);
        bfsButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        JButton dfsButton = new JButton("🌐 Explore Network (DFS)");
        dfsButton.setBackground(new Color(52, 73, 94));
        dfsButton.setForeground(Color.WHITE);
        dfsButton.setFocusPainted(false);
        dfsButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        JButton displayButton = new JButton("📊 Display Full Graph");
        displayButton.setBackground(new Color(230, 126, 34));
        displayButton.setForeground(Color.WHITE);
        displayButton.setFocusPainted(false);
        displayButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        buttonPanel.add(bfsButton);
        buttonPanel.add(dfsButton);
        buttonPanel.add(displayButton);
        
        JTextArea resultArea = new JTextArea(20, 60);
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(44, 62, 80, 220));
        resultArea.setForeground(new Color(236, 240, 241));
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        bfsButton.addActionListener(e -> {
            try {
                if (startField.getText().trim().isEmpty() || endField.getText().trim().isEmpty()) {
                    resultArea.setText("❌ Error: Both Start and End IDs are required!");
                    return;
                }
                
                JSONObject request = new JSONObject();
                request.put("action", "bfs");
                request.put("start", startField.getText().trim());
                request.put("end", endField.getText().trim());
                
                writeRequest(request);
                
                resultArea.setText("🔄 Processing BFS...\n⏳ Waiting for C++ backend response...");
                
                Timer timer = new Timer(1500, evt -> {
                    String response = readResponse();
                    resultArea.setText(response);
                });
                timer.setRepeats(false);
                timer.start();
                
            } catch (Exception ex) {
                resultArea.setText("❌ Error: " + ex.getMessage());
            }
        });
        
        dfsButton.addActionListener(e -> {
            try {
                if (startField.getText().trim().isEmpty()) {
                    resultArea.setText("❌ Error: Start ID is required!");
                    return;
                }
                
                JSONObject request = new JSONObject();
                request.put("action", "dfs");
                request.put("start", startField.getText().trim());
                
                writeRequest(request);
                
                resultArea.setText("🔄 Processing DFS...\n⏳ Waiting for response...");
                
                Timer timer = new Timer(1500, evt -> {
                    String response = readResponse();
                    resultArea.setText(response);
                });
                timer.setRepeats(false);
                timer.start();
                
            } catch (Exception ex) {
                resultArea.setText("❌ Error: " + ex.getMessage());
            }
        });
        
        displayButton.addActionListener(e -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action", "display_graph");
                
                writeRequest(request);
                
                resultArea.setText("🔄 Fetching graph data...\n⏳ Waiting for response...");
                
                Timer timer = new Timer(1500, evt -> {
                    String response = readResponse();
                    resultArea.setText(response);
                });
                timer.setRepeats(false);
                timer.start();
                
            } catch (Exception ex) {
                resultArea.setText("❌ Error: " + ex.getMessage());
            }
        });
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Styled Case Panel
    private JPanel createCasePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setOpaque(false);
        
        JLabel caseLabel = new JLabel("Case ID:");
        caseLabel.setForeground(Color.WHITE);
        caseLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField caseField = new JTextField();
        caseField.setBackground(new Color(44, 62, 80));
        caseField.setForeground(Color.WHITE);
        caseField.setCaretColor(Color.WHITE);
        
        JLabel typeLabel = new JLabel("Item Type:");
        typeLabel.setForeground(Color.WHITE);
        typeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
            "evidence", "witness", "suspect", "timeline"
        });
        typeCombo.setBackground(new Color(44, 62, 80));
        typeCombo.setForeground(Color.WHITE);
        
        JLabel descLabel = new JLabel("Description:");
        descLabel.setForeground(Color.WHITE);
        descLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField descField = new JTextField();
        descField.setBackground(new Color(44, 62, 80));
        descField.setForeground(Color.WHITE);
        descField.setCaretColor(Color.WHITE);
        
        formPanel.add(caseLabel);
        formPanel.add(caseField);
        formPanel.add(typeLabel);
        formPanel.add(typeCombo);
        formPanel.add(descLabel);
        formPanel.add(descField);
        
        JButton addButton = new JButton("📁 Add Case Item");
        addButton.setBackground(new Color(41, 128, 185));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        JButton viewButton = new JButton("📋 View Case Hierarchy");
        viewButton.setBackground(new Color(22, 160, 133));
        viewButton.setForeground(Color.WHITE);
        viewButton.setFocusPainted(false);
        viewButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        buttonPanel.add(viewButton);
        
        JTextArea resultArea = new JTextArea(15, 60);
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(44, 62, 80, 220));
        resultArea.setForeground(new Color(236, 240, 241));
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        addButton.addActionListener(e -> {
            try {
                if (caseField.getText().trim().isEmpty() || descField.getText().trim().isEmpty()) {
                    resultArea.setText("❌ Error: Case ID and Description are required!");
                    return;
                }
                
                JSONObject request = new JSONObject();
                request.put("action", "add_case_item");
                request.put("case_id", caseField.getText().trim());
                request.put("type", typeCombo.getSelectedItem().toString());
                request.put("description", descField.getText().trim());
                
                writeRequest(request);
                
                resultArea.setText("⏳ Sending to backend...\nPlease wait for confirmation...");
                
                descField.setText("");
                
                Timer timer = new Timer(1500, evt -> {
                    String response = readResponse();
                    resultArea.setText(response);
                });
                timer.setRepeats(false);
                timer.start();
                
            } catch (Exception ex) {
                resultArea.setText("❌ Error: " + ex.getMessage());
            }
        });
        
        viewButton.addActionListener(e -> {
            try {
                if (caseField.getText().trim().isEmpty()) {
                    resultArea.setText("❌ Error: Case ID is required!");
                    return;
                }
                
                JSONObject request = new JSONObject();
                request.put("action", "view_case");
                request.put("case_id", caseField.getText().trim());
                
                writeRequest(request);
                
                resultArea.setText("🔄 Fetching case hierarchy...\n⏳ Waiting for response...");
                
                Timer timer = new Timer(1500, evt -> {
                    String response = readResponse();
                    resultArea.setText(response);
                });
                timer.setRepeats(false);
                timer.start();
                
            } catch (Exception ex) {
                resultArea.setText("❌ Error: " + ex.getMessage());
            }
        });
        
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // File handling methods
    private void writeRequest(JSONObject request) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(REQUEST_FILE))) {
            writer.println(request.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error writing request file: " + e.getMessage() + 
                "\n\nMake sure the 'data' folder exists in your project directory.",
                "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String readResponse() {
        try {
            // Wait a bit more if file doesn't exist yet
            int attempts = 0;
            File file = new File(RESPONSE_FILE);
            while (!file.exists() && attempts < 10) {
                Thread.sleep(200);
                attempts++;
            }
            
            if (!file.exists()) {
                return "⏳ Backend is processing...\n\n" +
                       "If this message persists:\n" +
                       "1. Make sure the C++ backend is running\n" +
                       "2. Check the terminal for any error messages\n" +
                       "3. Verify the 'data' folder exists";
            }
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            
            // Delete the response file after reading
            file.delete();
            
            String result = content.toString().trim();
            return result.isEmpty() ? "❌ Empty response from backend" : result;
            
        } catch (IOException | InterruptedException e) {
            return "❌ Error reading response: " + e.getMessage();
        }
    }
    
    // Officer management methods
    private void loadOfficerCases(DefaultTableModel model) {
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_DIR + "assignments.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5 && parts[0].equals(currentUser)) {
                    model.addRow(new Object[]{parts[1], parts[2], parts[4], parts[3]});
                }
            }
        } catch (IOException e) {
            // No assignments yet
            model.addRow(new Object[]{"No cases", "N/A", "N/A", "N/A"});
        }
    }
    
    private void showAddOfficerDialog(JTextArea statusArea) {
        JDialog dialog = new JDialog(this, "Add New Officer", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(44, 62, 80));
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel idLabel = new JLabel("Officer ID:");
        idLabel.setForeground(Color.WHITE);
        JTextField idField = new JTextField();
        idField.setBackground(new Color(52, 73, 94));
        idField.setForeground(Color.WHITE);
        idField.setCaretColor(Color.WHITE);
        
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        JPasswordField passField = new JPasswordField();
        passField.setBackground(new Color(52, 73, 94));
        passField.setForeground(Color.WHITE);
        passField.setCaretColor(Color.WHITE);
        
        panel.add(idLabel);
        panel.add(idField);
        panel.add(passLabel);
        panel.add(passField);
        
        JButton saveBtn = new JButton("Save Officer");
        saveBtn.setBackground(new Color(46, 204, 113));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(231, 76, 60));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        
        saveBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String pass = new String(passField.getPassword());
            
            if (!id.isEmpty() && !pass.isEmpty()) {
                JSONObject request = new JSONObject();
                request.put("action", "add_officer");
                request.put("officer_id", id);
                request.put("password", pass);
                
                writeRequest(request);
                statusArea.setText("⏳ Adding officer to system...");
                
                Timer timer = new Timer(1500, evt -> {
                    String response = readResponse();
                    statusArea.setText(response);
                });
                timer.setRepeats(false);
                timer.start();
                
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        panel.add(saveBtn);
        panel.add(cancelBtn);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
}

// Login Screen Class
class LoginScreen extends JFrame {
    private static final String DATA_DIR = "C:\\Users\\husss\\OneDrive\\Documents\\NetBeansProjects\\DSA PROEJCT\\CrimeNetworkAnalyzer\\data\\";
    private static final String USERS_FILE = DATA_DIR + "users.txt";
    
    public LoginScreen() {
        // Ensure data directory exists
        new File(DATA_DIR).mkdirs();
        
        setTitle("Crime Network Analyzer - Login");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Background panel
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(44, 62, 80),
                    0, getHeight(), new Color(52, 73, 94)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        
        // Login panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(255, 255, 255, 30));
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        
        // Title
        JLabel titleLabel = new JLabel("🔐 CRIME NETWORK ANALYZER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        loginPanel.add(titleLabel, gbc);
        
        JLabel subtitleLabel = new JLabel("Secure Login Portal");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);
        loginPanel.add(subtitleLabel, gbc);
        
        // Spacer
        loginPanel.add(Box.createVerticalStrut(20), gbc);
        
        // Username
        JLabel userLabel = new JLabel("Username / Officer ID:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.BOLD, 12));
        loginPanel.add(userLabel, gbc);
        
        JTextField userField = new JTextField(20);
        userField.setBackground(new Color(44, 62, 80));
        userField.setForeground(Color.WHITE);
        userField.setCaretColor(Color.WHITE);
        userField.setFont(new Font("Arial", Font.PLAIN, 14));
        userField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        loginPanel.add(userField, gbc);
        
        // Password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("Arial", Font.BOLD, 12));
        loginPanel.add(passLabel, gbc);
        
        JPasswordField passField = new JPasswordField(20);
        passField.setBackground(new Color(44, 62, 80));
        passField.setForeground(Color.WHITE);
        passField.setCaretColor(Color.WHITE);
        passField.setFont(new Font("Arial", Font.PLAIN, 14));
        passField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        loginPanel.add(passField, gbc);
        
        // Login button
        JButton loginBtn = new JButton("🔓 LOGIN");
        loginBtn.setBackground(new Color(46, 204, 113));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginPanel.add(loginBtn, gbc);
        
        // Status label
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(231, 76, 60));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        loginPanel.add(statusLabel, gbc);
        
        // Add hint label
        JLabel hintLabel = new JLabel("<html><center>Default Login:<br>admin / admin123</center></html>");
        hintLabel.setForeground(new Color(149, 165, 166));
        hintLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        hintLabel.setHorizontalAlignment(JLabel.CENTER);
        loginPanel.add(hintLabel, gbc);
        
        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("❌ Please enter both username and password");
                return;
            }
            
            String[] result = authenticateUser(username, password);
            if (result != null) {
                dispose();
                new CrimeNetworkAnalyzer(result[0], result[1]);
            } else {
                statusLabel.setText("❌ Invalid credentials!");
                passField.setText("");
            }
        });
        
        // Enter key support
        passField.addActionListener(e -> loginBtn.doClick());
        
        mainPanel.add(loginPanel);
        setContentPane(mainPanel);
        
        // Initialize default users if file doesn't exist
        initializeUsers();
        
        setVisible(true);
    }
    
    private void initializeUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
                writer.println("admin|admin123|admin");
                writer.println("officer1|pass123|officer");
                writer.println("officer2|pass456|officer");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private String[] authenticateUser(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3 && parts[0].equals(username) && parts[1].equals(password)) {
                    return new String[]{parts[0], parts[2]}; // [username, role]
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

// Simple JSON helper class
class JSONObject {
    private Map<String, String> data = new HashMap<>();
    
    public void put(String key, String value) {
        data.put(key, value);
    }
    
    @Override
    public String toString() {
        StringBuilder json = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":\"")
                .append(entry.getValue()).append("\"");
            if (i < data.size() - 1) json.append(",");
            i++;
        }
        json.append("}");
        return json.toString();
    }
}