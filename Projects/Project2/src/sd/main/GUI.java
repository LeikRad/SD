package sd.main;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GUI extends JFrame {

    private final List<Process> childProcesses = new ArrayList<>();

    public GUI() {
        setTitle("Dynamic Process GUI from run.sh");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);

        JTabbedPane tabbedPane = new JTabbedPane();

        List<String[]> processes = parseRunScript("run.sh");

        for (String[] process : processes) {
            String title = process[0];
            String className = process[1];

            JTextArea outputArea = new JTextArea();
            outputArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(outputArea);
            tabbedPane.addTab(title, scrollPane);

            startProcess(className, outputArea);
        }

        add(tabbedPane);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (Process process : childProcesses) {
                    process.destroy();
                }
            }
        });
    }

    private List<String[]> parseRunScript(String scriptPath) {
        List<String[]> results = new ArrayList<>();
        Pattern pattern = Pattern.compile("gnome-terminal --title=\"(.*?)\".*?java -cp build/output\\.jar ([\\w\\.]+)");

        try (BufferedReader reader = new BufferedReader(new FileReader(scriptPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String title = matcher.group(1);
                    String className = matcher.group(2);
                    results.add(new String[]{title, className});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to read run.sh: " + e.getMessage());
        }

        return results;
    }

    private void startProcess(String className, JTextArea outputArea) {
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("java", "-cp", "build/output.jar", className);
                Process process = pb.start();
                childProcesses.add(process);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );

                String line;
                while ((line = reader.readLine()) != null) {
                    final String logLine = line;
                    SwingUtilities.invokeLater(() -> outputArea.append(logLine + "\n"));
                }

                // Also read error stream
                BufferedReader errReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream())
                );

                while ((line = errReader.readLine()) != null) {
                    final String errLine = line;
                    SwingUtilities.invokeLater(() -> outputArea.append("[ERROR] " + errLine + "\n"));
                }

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        outputArea.append("Error starting process: " + e.getMessage() + "\n"));
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}
