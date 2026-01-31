package therapyMatch.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.AgentContainer;

public class TherapyMatchGUI extends JFrame {
    private JTextArea logArea;
    private JTextField symptomsField;
    private JSpinner urgencySpinner;
    private JComboBox<String> preferenceCombo;
    private ContainerController container;
    private int clientCounter = 1;

    public TherapyMatchGUI() {
        setTitle("Psychiatrist Matching System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top panel - System info
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(" Psychiatrist Matching System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel statusLabel = new JLabel("System Ready - Click 'Start JADE System' to begin", JLabel.CENTER);
        statusLabel.setForeground(new Color(0, 128, 0));
        topPanel.add(statusLabel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        //centre panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        //input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Create New Client Request"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        //symptoms
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Symptoms (comma-separated):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        symptomsField = new JTextField("anxiety,insomnia");
        inputPanel.add(symptomsField, gbc);

        //urgency
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        inputPanel.add(new JLabel("Urgency (1-5):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        urgencySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        inputPanel.add(urgencySpinner, gbc);

        //preference
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        inputPanel.add(new JLabel("Preference:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        preferenceCombo = new JComboBox<>(new String[]{"online", "in-person"});
        inputPanel.add(preferenceCombo, gbc);

        //add client button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton addClientBtn = new JButton("Submit Client Request");
        addClientBtn.setBackground(new Color(70, 130, 180));
        addClientBtn.setForeground(Color.WHITE);
        addClientBtn.setFont(new Font("Arial", Font.BOLD, 12));
        addClientBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createClient();
            }
        });
        inputPanel.add(addClientBtn, gbc);

        splitPane.setTopComponent(inputPanel);

        //log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("System Log"));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        logPanel.add(scrollPane, BorderLayout.CENTER);

        splitPane.setBottomComponent(logPanel);
        splitPane.setDividerLocation(200);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        //bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton startBtn = new JButton("Start JADE System");
        startBtn.setBackground(new Color(34, 139, 34));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Arial", Font.BOLD, 12));
        startBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startJADE();
                startBtn.setEnabled(false);
            }
        });

        JButton clearBtn = new JButton("Clear Log");
        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logArea.setText("");
            }
        });

        bottomPanel.add(startBtn);
        bottomPanel.add(clearBtn);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private void startJADE() {
        new Thread(() -> {
            try {
                log(" Starting JADE platform...\n");

                Runtime rt = Runtime.instance();
                Profile p = new ProfileImpl();
                p.setParameter(Profile.GUI, "true");

                container = rt.createMainContainer(p);

                log(" JADE platform started\n");
                log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

                //coordinator
                log(" Creating Coordinator Agent...\n");
                container.createNewAgent("coordinator", "therapyMatch.agents.CoordinatorAgent", null).start();
                Thread.sleep(500);


                log(" Creating Psychiatrist Agents...\n\n");

                Object[] psy1Args = new Object[] {"anxiety,stress", "5", "true"};
                container.createNewAgent("psy1", "therapyMatch.agents.PsychiatristAgent", psy1Args).start();
                log("  Dr. Psy1 - Specialties: Anxiety, Stress | Slots: 25 (5 days × 5 slots) | Online: Yes\n");
                Thread.sleep(300);

                Object[] psy2Args = new Object[] {"depression,trauma", "3", "false"};
                container.createNewAgent("psy2", "therapyMatch.agents.PsychiatristAgent", psy2Args).start();
                log("  Dr. Psy2 - Specialties: Depression, Trauma | Slots: 25 | Online: No\n");
                Thread.sleep(300);

                Object[] psy3Args = new Object[] {"anxiety,depression,insomnia", "4", "true"};
                container.createNewAgent("psy3", "therapyMatch.agents.PsychiatristAgent", psy3Args).start();
                log("  Dr. Psy3 - Specialties: Anxiety, Depression, Insomnia | Slots: 25 | Online: Yes\n");
                Thread.sleep(300);

                Object[] psy4Args = new Object[] {"psychosis,hearing-voices,paranoia", "4", "true"};
                container.createNewAgent("psy4", "therapyMatch.agents.PsychiatristAgent", psy4Args).start();
                log("  Dr. Psy4 - Specialties: Psychosis, Hearing Voices, Paranoia | Slots: 25 | Online: Yes\n");
                Thread.sleep(300);

                Object[] psy5Args = new Object[] {"narcissism,personality-disorder,antisocial", "3", "false"};
                container.createNewAgent("psy5", "therapyMatch.agents.PsychiatristAgent", psy5Args).start();
                log("  Dr. Psy5 - Specialties: Narcissism, Personality Disorder, Antisocial | Slots: 25 | Online: No\n");
                Thread.sleep(300);

                Object[] psy6Args = new Object[] {"child-trauma,ptsd,trauma", "5", "true"};
                container.createNewAgent("psy6", "therapyMatch.agents.PsychiatristAgent", psy6Args).start();
                log("  Dr. Psy6 - Specialties: Child Trauma, PTSD, Trauma | Slots: 25 | Online: Yes\n");
                Thread.sleep(300);

                Object[] psy7Args = new Object[] {"mania,bipolar,mood-disorder", "4", "true"};
                container.createNewAgent("psy7", "therapyMatch.agents.PsychiatristAgent", psy7Args).start();
                log("  Dr. Psy7 - Specialties: Mania, Bipolar, Mood Disorder | Slots: 25 | Online: Yes\n");
                Thread.sleep(300);

                Object[] psy8Args = new Object[] {"nightmares,insomnia,sleep-disorder", "3", "true"};
                container.createNewAgent("psy8", "therapyMatch.agents.PsychiatristAgent", psy8Args).start();
                log("  Dr. Psy8 - Specialties: Nightmares, Insomnia, Sleep Disorder | Slots: 25 | Online: Yes\n");

                log("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                log(" All agents initialized!\n");
                log(" Total: 8 psychiatrists, 200 available time slots\n");
                log("You can now submit client requests.\n\n");

            } catch (Exception e) {
                log(" Error starting JADE: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }).start();
    }

    private void createClient() {
        if (container == null) {
            JOptionPane.showMessageDialog(this,
                    "Please start JADE system first!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String symptoms = symptomsField.getText().trim();
        if (symptoms.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter symptoms!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                String clientName = "client" + clientCounter++;
                int urgency = (Integer) urgencySpinner.getValue();
                String preference = (String) preferenceCombo.getSelectedItem();

                log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                log(" Creating new client: " + clientName + "\n");
                log("   Symptoms: " + symptoms + "\n");
                log("   Urgency: " + urgency + "/5\n");
                log("   Preference: " + preference + "\n");
                log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

                Object[] clientArgs = new Object[] {symptoms, String.valueOf(urgency), preference};
                container.createNewAgent(clientName, "therapyMatch.agents.ClientAgent", clientArgs).start();

                // delay to see matching process
                Thread.sleep(3000);
                log("\n");

            } catch (Exception e) {
                log(" Error creating client: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }).start();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TherapyMatchGUI gui = new TherapyMatchGUI();
            gui.setVisible(true);
        });
    }
}
