package sd.main;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import sd.main.NewRMI.*;
import java.rmi.Naming;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUI extends JFrame {
    private JTextArea serverOutput;
    private JTextArea clientOutput;
    private JButton startServerButton;
    private JButton stopServerButton;
    private JButton startClientsButton;
    private JButton stopClientsButton;
    private JButton clearServerButton;
    private JButton clearClientsButton;
    private JButton killPortButton;
    
    private Process serverProcess;
    private Process clientProcess;
    private List<Thread> outputThreads;
    
    public GUI() {
        outputThreads = new ArrayList<>();
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("RMI Election System Control Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main content panel
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        
        // Create server panel
        JPanel serverPanel = createServerPanel();
        
        // Create client panel
        JPanel clientPanel = createClientPanel();
        
        mainPanel.add(serverPanel);
        mainPanel.add(clientPanel);
        
        // Create emergency control panel
        JPanel emergencyPanel = createEmergencyPanel();
        
        // Add panels to main frame
        add(mainPanel, BorderLayout.CENTER);
        add(emergencyPanel, BorderLayout.SOUTH);
        
        // Set frame properties
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setVisible(true);
        
        // Add shutdown hook to clean up processes
        Runtime.getRuntime().addShutdownHook(new Thread(this::forceCleanup));
    }
    
    private JPanel createEmergencyPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(new TitledBorder("Emergency Controls"));
        panel.setBackground(Color.LIGHT_GRAY);
        
        killPortButton = new JButton("🔧 Kill RMI Port (1099)");
        killPortButton.setBackground(Color.ORANGE);
        killPortButton.setForeground(Color.BLACK);
        killPortButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        panel.add(killPortButton);
        
        // Add listener for killPortButton (remove killAllButton listener)
        killPortButton.addActionListener(e -> killRMIPort());
        
        return panel;
    }
    
    private JPanel createServerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("RMI Server"));
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        startServerButton = new JButton("Start Server");
        stopServerButton = new JButton("Stop Server");
        clearServerButton = new JButton("Clear Output");
        
        startServerButton.setBackground(Color.GREEN);
        stopServerButton.setBackground(Color.RED);
        stopServerButton.setEnabled(false);
        
        buttonsPanel.add(startServerButton);
        buttonsPanel.add(stopServerButton);
        buttonsPanel.add(clearServerButton);
        
        // Create output area
        serverOutput = new JTextArea();
        serverOutput.setEditable(false);
        serverOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        serverOutput.setBackground(Color.BLACK);
        serverOutput.setForeground(Color.GREEN);
        JScrollPane serverScrollPane = new JScrollPane(serverOutput);
        serverScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(buttonsPanel, BorderLayout.NORTH);
        panel.add(serverScrollPane, BorderLayout.CENTER);
        
        // Add button listeners
        startServerButton.addActionListener(e -> startServer());
        stopServerButton.addActionListener(e -> stopAll());
        clearServerButton.addActionListener(e -> serverOutput.setText(""));
        
        return panel;
    }
    
    private JPanel createClientPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("RMI Clients"));
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        startClientsButton = new JButton("Start All Clients");
        stopClientsButton = new JButton("Stop Clients");
        clearClientsButton = new JButton("Clear Output");
        
        startClientsButton.setBackground(Color.GREEN);
        stopClientsButton.setBackground(Color.RED);
        stopClientsButton.setEnabled(false);
        
        buttonsPanel.add(startClientsButton);
        buttonsPanel.add(stopClientsButton);
        buttonsPanel.add(clearClientsButton);
        
        // Create output area
        clientOutput = new JTextArea();
        clientOutput.setEditable(false);
        clientOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        clientOutput.setBackground(Color.BLACK);
        clientOutput.setForeground(Color.CYAN);
        JScrollPane clientScrollPane = new JScrollPane(clientOutput);
        clientScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(buttonsPanel, BorderLayout.NORTH);
        panel.add(clientScrollPane, BorderLayout.CENTER);
        
        // Add button listeners
        startClientsButton.addActionListener(e -> startClients());
        stopClientsButton.addActionListener(e -> stopAll());
        clearClientsButton.addActionListener(e -> clientOutput.setText(""));
        
        return panel;
    }
    
    private void startServer() {
        try {
            appendToServerOutput("Building project first...\n");
            
            // First build the project
            ProcessBuilder buildPb = new ProcessBuilder("make", "compile");
            buildPb.redirectErrorStream(true);
            Process buildProcess = buildPb.start();
            
            // Wait for build to complete
            int buildResult = buildProcess.waitFor();
            if (buildResult != 0) {
                appendToServerOutput("Build failed! Cannot start server.\n");
                return;
            }
            
            appendToServerOutput("Build successful. Starting RMI Server...\n");
            ProcessBuilder pb = new ProcessBuilder("make", "rmi-server");
            pb.redirectErrorStream(true);
            serverProcess = pb.start();
            
            startServerButton.setEnabled(false);
            stopServerButton.setEnabled(true);
            
            // Create thread to read server output
            Thread serverOutputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(serverProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        appendToServerOutput(line + "\n");
                    }
                } catch (IOException e) {
                    appendToServerOutput("Error reading server output: " + e.getMessage() + "\n");
                }
                
                // Process ended
                SwingUtilities.invokeLater(() -> {
                    startServerButton.setEnabled(true);
                    stopServerButton.setEnabled(false);
                    appendToServerOutput("Server process ended.\n");
                });
            });
            
            outputThreads.add(serverOutputThread);
            serverOutputThread.start();
            
        } catch (IOException | InterruptedException e) {
            appendToServerOutput("Failed to start server: " + e.getMessage() + "\n");
            startServerButton.setEnabled(true);
            stopServerButton.setEnabled(false);
        }
    }
    
    private void startClients() {
        try {
            appendToClientOutput("Building project first...\n");
            
            // First build the project
            ProcessBuilder buildPb = new ProcessBuilder("make", "compile");
            buildPb.redirectErrorStream(true);
            Process buildProcess = buildPb.start();
            
            // Wait for build to complete
            int buildResult = buildProcess.waitFor();
            if (buildResult != 0) {
                appendToClientOutput("Build failed! Cannot start clients.\n");
                return;
            }
            
            appendToClientOutput("Build successful. Starting RMI Clients...\n");
            ProcessBuilder pb = new ProcessBuilder("make", "rmi-all-clients");
            pb.redirectErrorStream(true);
            clientProcess = pb.start();
            
            startClientsButton.setEnabled(false);
            stopClientsButton.setEnabled(true);
            
            // Create thread to read client output
            Thread clientOutputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        appendToClientOutput(line + "\n");
                    }
                } catch (IOException e) {
                    appendToClientOutput("Error reading client output: " + e.getMessage() + "\n");
                }
                
                SwingUtilities.invokeLater(() -> {
                    startClientsButton.setEnabled(true);
                    stopClientsButton.setEnabled(false);
                    appendToClientOutput("Client processes ended.\n");
                });
            });
            
            outputThreads.add(clientOutputThread);
            clientOutputThread.start();
            
        } catch (IOException | InterruptedException e) {
            appendToClientOutput("Failed to start clients: " + e.getMessage() + "\n");
            startClientsButton.setEnabled(true);
            stopClientsButton.setEnabled(false);
        }
    }
    
    private void stopAll() {
        if (clientProcess != null && clientProcess.isAlive()) {
            appendToClientOutput("Stopping RMI Clients...\n");
            clientProcess.destroyForcibly();
            startClientsButton.setEnabled(true);
            stopClientsButton.setEnabled(false);
        }
        
        // Get voting booth results
        try {
            VotingBoothService votingService = 
                (VotingBoothService) Naming.lookup("rmi://localhost/VotingBoothService");
            Map<String, Integer> voteCounts = votingService.getVoteCounts();
            appendToServerOutput("Official Vote Counts: " + voteCounts + "\n");
        } catch (Exception e) {
            appendToServerOutput("Failed to retrieve vote counts via RMI: " + e.getMessage() + "\n");
        }
        
        // Get pollster results
        try {
            PollsterService pollsterService = 
                (PollsterService) Naming.lookup("rmi://localhost/PollsterService");
            Map<String, Integer> pollCounts = pollsterService.getPollCounts();
            appendToServerOutput("Exit Poll Results: " + pollCounts + "\n");
            
            try {
                VotingBoothService votingService = 
                    (VotingBoothService) Naming.lookup("rmi://localhost/VotingBoothService");
                Map<String, Integer> voteCounts = votingService.getVoteCounts();
                
                if (!voteCounts.isEmpty() && !pollCounts.isEmpty()) {
                    int totalVotes = voteCounts.values().stream().mapToInt(Integer::intValue).sum();
                    int totalPolls = pollCounts.values().stream().mapToInt(Integer::intValue).sum();
                    
                    if (totalVotes > 0 && totalPolls > 0) {
                        StringBuilder sb = new StringBuilder("Poll vs. Actual Comparison:\n");
                        
                        for (String party : voteCounts.keySet()) {
                            double votePercent = voteCounts.get(party) * 100.0 / totalVotes;
                            double pollPercent = pollCounts.getOrDefault(party, 0) * 100.0 / totalPolls;
                            double difference = Math.abs(votePercent - pollPercent);
                            
                            sb.append(String.format("  %s: Actual %.1f%%, Polled %.1f%% (Diff: %.1f%%)\n", 
                                party, votePercent, pollPercent, difference));
                        }
                        
                        appendToServerOutput(sb.toString());
                    }
                }
            } catch (Exception ignored) {
            }
            
        } catch (Exception e) {
            appendToServerOutput("Failed to retrieve poll results via RMI: " + e.getMessage() + "\n");
        }
    
        if (serverProcess != null && serverProcess.isAlive()) {
            appendToServerOutput("Stopping RMI Server...\n");
            serverProcess.destroyForcibly();
            startServerButton.setEnabled(true);
            stopServerButton.setEnabled(false);
        }
    }
    
    private void killRMIPort() {
        appendToServerOutput("🔧 Attempting to free RMI port 1099...\n");
        appendToClientOutput("🔧 Attempting to free RMI port 1099...\n");
        
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                // Windows - find and kill process using port 1099
                pb = new ProcessBuilder("netstat", "-ano");
                Process netstatProcess = pb.start();
                
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(netstatProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains(":1099") && line.contains("LISTENING")) {
                            String[] parts = line.trim().split("\\s+");
                            if (parts.length > 4) {
                                String pid = parts[parts.length - 1];
                                ProcessBuilder killPb = new ProcessBuilder("taskkill", "/F", "/PID", pid);
                                killPb.start().waitFor();
                                appendToServerOutput("Killed process " + pid + " using port 1099\n");
                                appendToClientOutput("Killed process " + pid + " using port 1099\n");
                            }
                        }
                    }
                }
            } else {
                // Unix/Linux/Mac - kill process using port 1099
                pb = new ProcessBuilder("sh", "-c", "lsof -ti:1099 | xargs kill -9");
                pb.redirectErrorStream(true);
                Process killProcess = pb.start();
                killProcess.waitFor();
                
                appendToServerOutput("Attempted to kill processes using port 1099\n");
                appendToClientOutput("Attempted to kill processes using port 1099\n");
            }
            
        } catch (IOException | InterruptedException e) {
            appendToServerOutput("Failed to kill port processes: " + e.getMessage() + "\n");
            appendToClientOutput("Failed to kill port processes: " + e.getMessage() + "\n");
        }
    }
    
    private void appendToServerOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            serverOutput.append(text);
            serverOutput.setCaretPosition(serverOutput.getDocument().getLength());
        });
    }
    
    private void appendToClientOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            clientOutput.append(text);
            clientOutput.setCaretPosition(clientOutput.getDocument().getLength());
        });
    }
    
    private void forceCleanup() {
        // Stop all processes
        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroyForcibly();
        }
        if (clientProcess != null && clientProcess.isAlive()) {
            clientProcess.destroyForcibly();
        }
        
        // Try to kill any lingering RMI processes
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                pb = new ProcessBuilder("taskkill", "/F", "/IM", "rmiregistry.exe");
            } else {
                pb = new ProcessBuilder("pkill", "-f", "rmiregistry");
            }
            
            pb.start().waitFor();
        } catch (Exception ignored) {
            // Ignore cleanup errors
        }
        
        // Interrupt all output threads
        for (Thread thread : outputThreads) {
            thread.interrupt();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GUI();
        });
    }
}