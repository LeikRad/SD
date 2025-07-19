package sd.main;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.BorderFactory;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.PrintStream;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class GUI implements ActionListener {
    
    private JFrame frame = new JFrame("Voting System");
    private JFrame secondFrame;
    
    private JButton startButton = new JButton("Start Voting");
    private JButton detailsButton = new JButton("Show Details");
    private JButton exitButton = new JButton("Exit");

    private JTextArea consoleTextArea = new JTextArea();
    
    public GUI() {

        JPanel panel1 = new JPanel(new GridLayout(7, 1, 20, 20));
        panel1.setBorder(BorderFactory.createEmptyBorder(50, 50, 40, 50));

        // create labels and text fields for numClerks, numBooths, numVoters, numPollsters, and voter_line_size
        String[] labels = {"Number of Clerks", "Number of Booths", "Number of Voters", "Number of Pollsters", "Voter Line Size"};
        int[] defaultValues = {DataStruct.numClerks, DataStruct.numBooths, DataStruct.numVoters, DataStruct.numPollsters, DataStruct.voter_line_size};
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            JPanel fieldPanel = new JPanel(new BorderLayout(10, 10));
            JLabel fieldLabel = new JLabel(labels[i] + ":");
            fieldLabel.setPreferredSize(new Dimension(150, fieldLabel.getPreferredSize().height));
            fieldPanel.add(fieldLabel, BorderLayout.WEST);
            JTextField textField = new JTextField(String.valueOf(defaultValues[i]));
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (textField.getText().equals(String.valueOf(defaultValues[index]))) {
                        textField.setText("");
                    }
                }
                @Override
                public void focusLost(FocusEvent e) {
                    if (textField.getText().isEmpty()) {
                        textField.setText(String.valueOf(defaultValues[index]));
                    }
                }
            });
            fieldPanel.add(textField, BorderLayout.CENTER);
            panel1.add(fieldPanel);
        }

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        buttonPanel.add(startButton);
        buttonPanel.add(detailsButton);
        buttonPanel.add(exitButton);
        startButton.addActionListener(this);
        detailsButton.addActionListener(this);
        exitButton.addActionListener(this);
        panel1.add(buttonPanel);

        // Setup panel2 with JTextArea wrapped in a JScrollPane
        consoleTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(consoleTextArea);
        // Optionally set a preferred size:
        scrollPane.setPreferredSize(new Dimension(400, 600));
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(scrollPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, panel2);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerSize(1);
    
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.setSize(1000, 600);
        frame.setVisible(true);
        frame.setTitle("Voting System");
        
        // Redirect System.out to the consoleTextArea
        System.setOut(new PrintStream(new TextAreaOutputStream(consoleTextArea), true));
        System.setErr(new PrintStream(new TextAreaOutputStream(consoleTextArea), true));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            System.out.println("Voting started.");
            new Thread(() -> startVoting()).start();
        } else if (e.getSource() == detailsButton) {
            System.out.println("Showing details.");
            showDetails();
        } else if (e.getSource() == exitButton) {
            System.exit(0);
        }
    }

    public void startVoting() {

        // Get the split pane from the frame's content pane
        JSplitPane splitPane = (JSplitPane) frame.getContentPane().getComponent(0);
        // Get the left panel (which contains the text fields)
        JPanel leftPanel = (JPanel) splitPane.getLeftComponent();
        
        // Retrieve text fields from each row of leftPanel
        int numClerks = Integer.parseInt(
            ((JTextField) ((JPanel) leftPanel.getComponent(0)).getComponent(1)).getText()
        );
        int numBooths = Integer.parseInt(
            ((JTextField) ((JPanel) leftPanel.getComponent(1)).getComponent(1)).getText()
        );
        int numVoters = Integer.parseInt(
            ((JTextField) ((JPanel) leftPanel.getComponent(2)).getComponent(1)).getText()
        );
        int numPollsters = Integer.parseInt(
            ((JTextField) ((JPanel) leftPanel.getComponent(3)).getComponent(1)).getText()
        );
        int voterLineSize = Integer.parseInt(
            ((JTextField) ((JPanel) leftPanel.getComponent(4)).getComponent(1)).getText()
        );
        
        DataStruct.numClerks = numClerks;
        DataStruct.numBooths = numBooths;
        DataStruct.numVoters = numVoters;
        DataStruct.numPollsters = numPollsters;
        DataStruct.voter_line_size = voterLineSize;

        // initialize shared memory
        CountDownLatch voterLatch = new CountDownLatch(DataStruct.numVoters);
        CountDownLatch pollsterLatch = new CountDownLatch(DataStruct.numPollsters);
        CountDownLatch clerkLatch = new CountDownLatch(DataStruct.numClerks);
        
        // create threads
        for (int i = 0; i < DataStruct.numClerks; i++) {
            new Thread(new Clerk(clerkLatch)).start();
        }

        for (int i = 0; i < DataStruct.numVoters; i++) {
            new Thread(new Voter(voterLatch)).start();
        }

        for (int i = 0; i < DataStruct.numPollsters; i++) {
            new Thread(new Pollster(pollsterLatch)).start();
        }

        System.out.println(DataStruct.numClerks + " clerks have been created.");

        // print out status of each semaphore
        System.out.println("Semaphores:");
        System.out.println("  mutex: " + DataStruct.mutex.availablePermits());
        for (Semaphore i : DataStruct.clerksAvailable) {
            System.out.println("  clerk: " + i.availablePermits());
        }

        System.out.println(DataStruct.clerkRequest.availablePermits() + " request is available.");

        try {
            Thread.sleep(DataStruct.timeToClose);
            DataStruct.votersclosed = true;
            // System.out.println(voterLatch.getCount() + " voters are still waiting.");

            voterLatch.await();
            DataStruct.pollstersclosed = true;
            while (pollsterLatch.getCount() > 0) {
                DataStruct.pollsterRequest.release();
            }
            DataStruct.clerksclosed = true;
            while (clerkLatch.getCount() > 0) {
                DataStruct.clerkRequest.release();
            }
            } catch (Exception e) {
        }

        System.out.println("Candidates: ");
        for (String key : DataStruct.Candidates.keySet()) {
            System.out.println("  " + key + ": " + DataStruct.Candidates.get(key));
        }

        System.out.println("Voter responses: ");
        for (String key : DataStruct.voterResponsesMap.keySet()) {
            System.out.println("  " + key + ": " + DataStruct.voterResponsesMap.get(key));
        }
    }

    public void showDetails() {
        if (secondFrame != null && secondFrame.isVisible()) {
            secondFrame.dispose();
        }
        secondFrame = new JFrame("Second Window");
        secondFrame.setSize(600, 400);
        secondFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Left Label 1: Conversation Probability
        JPanel leftPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        JPanel rowPanel1 = new JPanel(new BorderLayout(10, 10));
        JLabel fieldLabel1 = new JLabel("<html>" + "Conversation Probability (0-1):" + "</html>");
        fieldLabel1.setPreferredSize(new Dimension(150, fieldLabel1.getPreferredSize().height));
        rowPanel1.add(fieldLabel1, BorderLayout.WEST);
        JTextField textField1 = new JTextField(String.valueOf(Pollster.conversationProbability));
        textField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField1.getText().equals(String.valueOf(Pollster.conversationProbability))) {
                    textField1.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField1.getText().isEmpty()) {
                    textField1.setText(String.valueOf(Pollster.conversationProbability));
                }
            }
        });
        rowPanel1.add(textField1, BorderLayout.CENTER);
        leftPanel.add(rowPanel1);

        // Left Label 2: Answer Probability
        JPanel rowPanel2 = new JPanel(new BorderLayout(10, 10));
        JLabel fieldLabel2 = new JLabel("<html>" + "Answer Probability (0-1):" + "</html>");
        fieldLabel2.setPreferredSize(new Dimension(150, fieldLabel2.getPreferredSize().height));
        rowPanel2.add(fieldLabel2, BorderLayout.WEST);
        JTextField textField2 = new JTextField(String.valueOf(Voter.answerProbability));
        textField2.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField2.getText().equals(String.valueOf(Voter.answerProbability))) {
                    textField2.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField2.getText().isEmpty()) {
                    textField2.setText(String.valueOf(Voter.answerProbability));
                }
            }
        });
        rowPanel2.add(textField2, BorderLayout.CENTER);
        leftPanel.add(rowPanel2);
        
        // Left Label 3: Lie Probability
        JPanel rowPanel3 = new JPanel(new BorderLayout(10, 10));
        JLabel fieldLabel3 = new JLabel("<html>" + "Lie Probability (0-1):" + "</html>");
        fieldLabel3.setPreferredSize(new Dimension(150, fieldLabel3.getPreferredSize().height));
        rowPanel3.add(fieldLabel3, BorderLayout.WEST);
        JTextField textField3 = new JTextField(String.valueOf(Voter.lieProbability));
        textField3.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField3.getText().equals(String.valueOf(Voter.lieProbability))) {
                    textField3.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField3.getText().isEmpty()) {
                    textField3.setText(String.valueOf(Voter.lieProbability));
                }
            }
        });
        rowPanel3.add(textField3, BorderLayout.CENTER);
        leftPanel.add(rowPanel3);

        // Left Label 4: Rebirth Probability
        JPanel rowPanel4 = new JPanel(new BorderLayout(10, 10));
        JLabel fieldLabel4 = new JLabel("<html>" + "Change ID Probability (0-1):" + "</html>");
        fieldLabel4.setPreferredSize(new Dimension(150, fieldLabel4.getPreferredSize().height));
        rowPanel4.add(fieldLabel4, BorderLayout.WEST);
        JTextField textField4 = new JTextField(String.valueOf(Voter.changeVIProbability));
        textField4.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField4.getText().equals(String.valueOf(Voter.changeVIProbability))) {
                    textField4.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField4.getText().isEmpty()) {
                    textField4.setText(String.valueOf(Voter.changeVIProbability));
                }
            }
        });
        rowPanel4.add(textField4, BorderLayout.CENTER);
        leftPanel.add(rowPanel4);

        JPanel rightPanel = new JPanel(new GridLayout(5, 1, 10, 10));

        // Right Label 1: Voter Delay 
        JPanel rowPanel5 = new JPanel(new BorderLayout(10, 10));
        JLabel fieldLabel5 = new JLabel("Voter Delay (ms):");
        fieldLabel5.setPreferredSize(new Dimension(150, fieldLabel5.getPreferredSize().height));
        rowPanel5.add(fieldLabel5, BorderLayout.WEST);
        JTextField textField5 = new JTextField(String.valueOf((int)(Math.random() * 16)));
        textField5.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField5.getText().equals(String.valueOf((int)(Math.random() * 16)))) {
                    textField5.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField5.getText().isEmpty()) {
                    textField5.setText(String.valueOf((int)(Math.random() * 16)));
                }
            }
        });
        rowPanel5.add(textField5, BorderLayout.CENTER);
        rightPanel.add(rowPanel5);

        
        // Right Label 2: Pollster Delay
        JPanel rowPanel6 = new JPanel(new BorderLayout(10, 10));
        JLabel fieldLabel6 = new JLabel("Pollster Delay (ms):");
        fieldLabel6.setPreferredSize(new Dimension(150, fieldLabel6.getPreferredSize().height));
        rowPanel6.add(fieldLabel6, BorderLayout.WEST);
        JTextField textField6 = new JTextField(String.valueOf(5 + (int) (Math.random() * 6)));  
        textField6.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField6.getText().equals(String.valueOf(5 + (int) (Math.random() * 6)))) {
                    textField6.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField6.getText().isEmpty()) {
                    textField6.setText(String.valueOf(5 + (int) (Math.random() * 6)));
                }
            }
        });
        rowPanel6.add(textField6, BorderLayout.CENTER);
        rightPanel.add(rowPanel6);

        // Right Label 3: Clerk Delay
        JPanel rowPanel7 = new JPanel(new BorderLayout(10, 10));
        JLabel fieldLabel7 = new JLabel("Clerk Delay (ms):");
        fieldLabel7.setPreferredSize(new Dimension(150, fieldLabel7.getPreferredSize().height));
        rowPanel7.add(fieldLabel7, BorderLayout.WEST);
        JTextField textField7 = new JTextField(String.valueOf(5 + (int) (Math.random() * 6)));
        textField7.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField7.getText().equals(String.valueOf(5 + (int) (Math.random() * 6)))) {
                    textField7.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField7.getText().isEmpty()) {
                    textField7.setText(String.valueOf(5 + (int) (Math.random() * 6)));
                }
            }
        });
        rowPanel7.add(textField7, BorderLayout.CENTER);
        rightPanel.add(rowPanel7);

        // Right Label 4: Time to Close
        JPanel rowPanel8 = new JPanel(new BorderLayout(10, 10));
        JLabel fieldLabel8 = new JLabel("Time to Close (ms):");
        fieldLabel8.setPreferredSize(new Dimension(150, fieldLabel8.getPreferredSize().height));
        rowPanel8.add(fieldLabel8, BorderLayout.WEST);
        JTextField textField8 = new JTextField(String.valueOf(DataStruct.timeToClose));
        textField8.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField8.getText().equals(String.valueOf(DataStruct.timeToClose))) {
                    textField8.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField8.getText().isEmpty()) {
                    textField8.setText(String.valueOf(DataStruct.timeToClose));
                }
            }
        });
        rowPanel8.add(textField8, BorderLayout.CENTER);
        rightPanel.add(rowPanel8);

        JSplitPane secondSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        secondSplit.setResizeWeight(0.5);
        secondSplit.setDividerSize(2);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton confirmButton = new JButton("Confirm");
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(confirmButton);

        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Pollster.conversationProbability = Double.parseDouble(textField1.getText());
                Voter.answerProbability = Double.parseDouble(textField2.getText());
                Voter.lieProbability = Double.parseDouble(textField3.getText());
                Voter.changeVIProbability = Double.parseDouble(textField4.getText());
                Voter.delay = Integer.parseInt(textField5.getText());
                Pollster.delay = Integer.parseInt(textField6.getText());
                Clerk.delay = Integer.parseInt(textField7.getText());
                DataStruct.timeToClose = Integer.parseInt(textField8.getText());

                secondFrame.dispose();
            }
        });

        contentPanel.add(secondSplit, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        secondFrame.add(contentPanel);
        secondFrame.setVisible(true);
    }
}