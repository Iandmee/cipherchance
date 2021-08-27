package com.company;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Locale;

public class MessengerWindow extends JFrame {
    private JPanel panel;
    private JPanel dialogueArea;
    private JPanel sendArea;
    private JButton sendButton;
    private JButton addFileButton;
    private JScrollPane messageScroll;
    private JTextArea messageText;
    private JScrollPane messagesListScroll;
    private JPanel cipherSettingsPanel;
    private JPanel handshakeSettings;
    private JComboBox keyAgreementAlgorithm;
    private JComboBox agreementKeyLength;
    private JPanel cipherSettings;
    private JComboBox cipherAlg;
    private JComboBox paddingType;
    private JComboBox cipherKeyLength;
    private JButton cipherButton;
    private JComboBox cipherMode;
    private JPanel messagesPanel;
    private JScrollPane dlScroll;
    private JList chatList;

    private ConnectMachine session = null;
    private Dialogue[] haveDialogues = null;
    private Message[] currentMessages = null;
    private CipherParametersKeeper[] currentCipherStates = null;

    private int currentDialogueIndex = -1;

    private class DialoguesSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            int ind = ((JList<?>) e.getSource()).
                    getSelectedIndex();
            if (ind == currentDialogueIndex) {
                return;
            }
            currentDialogueIndex = ind;

            messagesPanel.removeAll();
            messagesPanel.repaint();
            messagesPanel.revalidate();
            currentMessages = haveDialogues[ind].getAllMessages();
            for (Message msg : currentMessages) {
                messagesPanel.add(msg);
            }

            if (currentDialogueIndex == -1) {
                setComboBoxesEnabled(true);
            } else {
                currentCipherStates[currentDialogueIndex].setComboboxes();
            }
        }
    }

    private class DialogueCellRenderer implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            //int selectedIndex = (Integer) value;
            if (isSelected) {
                haveDialogues[index].getRenderObject().setBorder(BorderFactory.createLineBorder(Color.BLACK));
            } else {
                haveDialogues[index].getRenderObject().setBorder(BorderFactory.createEmptyBorder());
            }
            return haveDialogues[index].getRenderObject();
        }
    }

    private class SendButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String messageValue = messageText.getText();
            messageText.setText("");
            if (currentDialogueIndex == -1) {
                return;
            }
            haveDialogues[currentDialogueIndex].sendMessage(messageValue);
        }
    }

    private class CipherButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentDialogueIndex == -1) {
                return;
            }
            if (cipherButton.getText().equals("Abort")) {
                haveDialogues[currentDialogueIndex].abortCipher();
                cipherButton.setText("Start session");
                currentCipherStates[currentDialogueIndex].unfixState();
            } else {
                String alg = (String) ((ComboBoxHolder) cipherAlg.getSelectedItem()).getData();
                String mode = (String) ((ComboBoxHolder) cipherMode.getSelectedItem()).getData();
                String padding = (String) ((ComboBoxHolder) paddingType.getSelectedItem()).getData();
                int keyLength = (int) ((ComboBoxHolder) cipherKeyLength.getSelectedItem()).getData();
                haveDialogues[currentDialogueIndex].addNewCipher(alg, mode, padding, keyLength);
                cipherButton.setText("Abort");
                currentCipherStates[currentDialogueIndex].fixState();
            }
            currentCipherStates[currentDialogueIndex].setComboboxes();
        }
    }

    private class CipherParametersKeeper {
        private int keyAgreementAlgIndex, keyLengthIndex;
        private int cipherAlgIndex, cipherModeIndex, cipherPaddingIndex, cipherLengthIndex;
        private boolean isEnabled;

        public CipherParametersKeeper() {
            unfixState();
        }

        public void fixState() {
            keyAgreementAlgIndex = keyAgreementAlgorithm.getSelectedIndex();
            keyLengthIndex = agreementKeyLength.getSelectedIndex();
            cipherAlgIndex = cipherAlg.getSelectedIndex();
            cipherModeIndex = cipherMode.getSelectedIndex();
            cipherPaddingIndex = paddingType.getSelectedIndex();
            cipherLengthIndex = cipherKeyLength.getSelectedIndex();
            isEnabled = false;
        }

        public void unfixState() {
            keyAgreementAlgIndex = 0;
            keyLengthIndex = 0;
            cipherAlgIndex = 0;
            cipherModeIndex = 0;
            cipherPaddingIndex = 0;
            cipherLengthIndex = 0;
            isEnabled = true;
        }

        public void setComboboxes() {
            if (isEnabled) {
                cipherButton.setText("Start session");
            } else {
                cipherButton.setText("Abort");
            }
            keyAgreementAlgorithm.setSelectedIndex(keyAgreementAlgIndex);
            agreementKeyLength.setSelectedIndex(keyLengthIndex);
            cipherAlg.setSelectedIndex(cipherAlgIndex);
            cipherMode.setSelectedIndex(cipherModeIndex);
            paddingType.setSelectedIndex(cipherPaddingIndex);
            cipherKeyLength.setSelectedIndex(cipherLengthIndex);
            setComboBoxesEnabled(isEnabled);
        }
    }

    private class MessageLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Dimension res = new Dimension();
            Component[] have = parent.getComponents();
            int msgWidth = parent.getWidth() / 2 - 5;
            res.width = 512;
            int height = 5;
            for (int i = 0; i < have.length; i++) {
                height += 1;
                height += ((Message) have[i]).getMinimalHeight(msgWidth);
            }
            res.height = height;
            return res;
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            Component[] have = parent.getComponents();
            int msgWidth = parent.getWidth() / 2 - 5;
            int cntY = 1;
            for (int i = 0; i < have.length; i++) {
                if (((Message) have[i]).ours) {
                    have[i].setBounds(2 + msgWidth, cntY, msgWidth, ((Message) have[i]).getMinimalHeight(msgWidth));
                } else {
                    have[i].setBounds(2, cntY, msgWidth, ((Message) have[i]).getMinimalHeight(msgWidth));
                }
                cntY += 1;
                cntY += ((Message) have[i]).getMinimalHeight(msgWidth);
            }
        }
    }


    public void init(ConnectMachine cnt) {
        session = cnt;
        setVisible(true);
        setDialoguesList();
        initComboBoxes();
        initButtons();

        new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(400);
                    updateChat();
                }
            } catch (Exception e) {
                System.out.println("FUCK!");
            }
        }).start();
    }

    MessengerWindow() {
        setVisible(false);
        setContentPane(panel);
        setSize(900, 512);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        chatList.addListSelectionListener(new DialoguesSelectionListener());
        messagesPanel.setLayout(new MessageLayout());
        chatList.setCellRenderer(new DialogueCellRenderer());

        sendButton.addActionListener(new SendButtonListener());
        cipherButton.addActionListener(new CipherButtonListener());

        messagesListScroll.getVerticalScrollBar().setUnitIncrement(16);
    }

    private void initButtons() {
        cipherButton.setText("Start session");
    }

    private void setDialoguesList() {
        haveDialogues = session.getDialogues();
        JLabel[] names = new JLabel[haveDialogues.length];
        currentCipherStates = new CipherParametersKeeper[haveDialogues.length];
        System.err.println(haveDialogues.length);
        for (int i = 0; i < haveDialogues.length; i++) {
            names[i] = haveDialogues[i].getRenderObject();
            currentCipherStates[i] = new CipherParametersKeeper();
        }
        chatList.setListData(names);
    }

    private void initComboBoxes() {
        setComboBoxesEnabled(false);

        setKeyAgreementCombobox();
        setAgreementKeyLengthCombobox();
        setCipherAlgCombobox();
        setCipherModeCombobox();
        setPaddingTypeCombobox();
        setCipherKeyLengthCombobox();
    }

    private void setComboBoxesEnabled(boolean flag) {
        keyAgreementAlgorithm.setEnabled(flag);
        agreementKeyLength.setEnabled(flag);

        cipherAlg.setEnabled(flag);
        cipherMode.setEnabled(flag);
        paddingType.setEnabled(flag);
        cipherKeyLength.setEnabled(flag);
    }

    private void setKeyAgreementCombobox() {
        keyAgreementAlgorithm.removeAllItems();
        keyAgreementAlgorithm.addItem(new ComboBoxHolder("Diffie-Hellman", "DiffieHellman"));
        keyAgreementAlgorithm.addItem(new ComboBoxHolder("RSA", "RSA"));
    }

    private void setAgreementKeyLengthCombobox() {
        agreementKeyLength.removeAllItems();
        agreementKeyLength.addItem(new ComboBoxHolder("512", 512));
        agreementKeyLength.addItem(new ComboBoxHolder("1024", 1024));
        agreementKeyLength.addItem(new ComboBoxHolder("2048", 2048));
        agreementKeyLength.addItem(new ComboBoxHolder("4096", 4096));
    }

    private void setCipherAlgCombobox() {
        cipherAlg.removeAllItems();
        cipherAlg.addItem(new ComboBoxHolder("AES", "AES"));
        cipherAlg.addItem(new ComboBoxHolder("Blowfish", "Blowfish"));
    }

    private void setCipherModeCombobox() {
        cipherMode.removeAllItems();
        cipherMode.addItem(new ComboBoxHolder("CBC", "CBC"));
        cipherMode.addItem(new ComboBoxHolder("CFB", "CFB"));
        cipherMode.addItem(new ComboBoxHolder("CTS", "CTS"));
    }

    private void setPaddingTypeCombobox() {
        paddingType.removeAllItems();
        paddingType.addItem(new ComboBoxHolder("ISO10126", "ISO10126Padding"));
        paddingType.addItem(new ComboBoxHolder("PKCS5", "PKCS5Padding"));
    }

    private void setCipherKeyLengthCombobox() {
        cipherKeyLength.removeAllItems();
        cipherKeyLength.addItem(new ComboBoxHolder("256", 256));
        cipherKeyLength.addItem(new ComboBoxHolder("192", 192));
        cipherKeyLength.addItem(new ComboBoxHolder("128", 128));
    }

    private void updateChat() {
        if (currentDialogueIndex == -1) {
            return;
        }
        Message[] possiblyNew = haveDialogues[currentDialogueIndex].getAllMessages();
        for (int i = currentMessages.length; i < possiblyNew.length; i++) {
            messagesPanel.add(possiblyNew[i]);
        }
        messagesPanel.repaint();
        messagesPanel.revalidate();
        currentMessages = possiblyNew;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 0));
        dialogueArea = new JPanel();
        dialogueArea.setLayout(new BorderLayout(0, 0));
        dialogueArea.setMinimumSize(new Dimension(300, 512));
        dialogueArea.setPreferredSize(new Dimension(300, 512));
        panel.add(dialogueArea, BorderLayout.CENTER);
        sendArea = new JPanel();
        sendArea.setLayout(new BorderLayout(0, 0));
        sendArea.setMinimumSize(new Dimension(300, 64));
        sendArea.setPreferredSize(new Dimension(300, 64));
        dialogueArea.add(sendArea, BorderLayout.SOUTH);
        sendButton = new JButton();
        sendButton.setPreferredSize(new Dimension(128, 64));
        sendButton.setText("Send");
        sendArea.add(sendButton, BorderLayout.EAST);
        addFileButton = new JButton();
        addFileButton.setText("Select File");
        sendArea.add(addFileButton, BorderLayout.WEST);
        messageScroll = new JScrollPane();
        sendArea.add(messageScroll, BorderLayout.CENTER);
        messageText = new JTextArea();
        messageScroll.setViewportView(messageText);
        messagesListScroll = new JScrollPane();
        messagesListScroll.setHorizontalScrollBarPolicy(31);
        messagesListScroll.setMinimumSize(new Dimension(512, 5));
        messagesListScroll.setPreferredSize(new Dimension(520, 448));
        dialogueArea.add(messagesListScroll, BorderLayout.CENTER);
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BorderLayout(0, 0));
        messagesListScroll.setViewportView(messagesPanel);
        cipherSettingsPanel = new JPanel();
        cipherSettingsPanel.setLayout(new BorderLayout(0, 0));
        cipherSettingsPanel.setEnabled(true);
        cipherSettingsPanel.setMinimumSize(new Dimension(100, 512));
        cipherSettingsPanel.setPreferredSize(new Dimension(220, 512));
        panel.add(cipherSettingsPanel, BorderLayout.EAST);
        handshakeSettings = new JPanel();
        handshakeSettings.setLayout(new GridBagLayout());
        handshakeSettings.setPreferredSize(new Dimension(220, 92));
        cipherSettingsPanel.add(handshakeSettings, BorderLayout.NORTH);
        final JLabel label1 = new JLabel();
        label1.setText("Key Agreement Algorithm");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        handshakeSettings.add(label1, gbc);
        keyAgreementAlgorithm = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        keyAgreementAlgorithm.setModel(defaultComboBoxModel1);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        handshakeSettings.add(keyAgreementAlgorithm, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Key Length");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        handshakeSettings.add(label2, gbc);
        agreementKeyLength = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        agreementKeyLength.setModel(defaultComboBoxModel2);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        handshakeSettings.add(agreementKeyLength, gbc);
        cipherSettings = new JPanel();
        cipherSettings.setLayout(new GridBagLayout());
        cipherSettings.setPreferredSize(new Dimension(200, 184));
        cipherSettingsPanel.add(cipherSettings, BorderLayout.CENTER);
        final JLabel label3 = new JLabel();
        label3.setText("Cipher Algorithm");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        cipherSettings.add(label3, gbc);
        cipherAlg = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        cipherAlg.setModel(defaultComboBoxModel3);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        cipherSettings.add(cipherAlg, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Padding");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        cipherSettings.add(label4, gbc);
        paddingType = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel4 = new DefaultComboBoxModel();
        paddingType.setModel(defaultComboBoxModel4);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        cipherSettings.add(paddingType, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Cipher Key Length");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        cipherSettings.add(label5, gbc);
        cipherKeyLength = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel5 = new DefaultComboBoxModel();
        cipherKeyLength.setModel(defaultComboBoxModel5);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        cipherSettings.add(cipherKeyLength, gbc);
        cipherMode = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel6 = new DefaultComboBoxModel();
        cipherMode.setModel(defaultComboBoxModel6);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        cipherSettings.add(cipherMode, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Cipher Mode");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        cipherSettings.add(label6, gbc);
        cipherButton = new JButton();
        cipherButton.setPreferredSize(new Dimension(162, 60));
        cipherButton.setText("Something Went \nWrong");
        cipherSettingsPanel.add(cipherButton, BorderLayout.SOUTH);
        dlScroll = new JScrollPane();
        dlScroll.setHorizontalScrollBarPolicy(31);
        panel.add(dlScroll, BorderLayout.WEST);
        chatList = new JList();
        dlScroll.setViewportView(chatList);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }

}
