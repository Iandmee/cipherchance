package com.company;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

public class MessengerWindow extends JFrame {
    private JPanel panel;
    private JList dialoguesList;
    private JPanel dialogueArea;
    private JPanel sendArea;
    private JScrollPane dialogueListScroll;
    private JButton sendButton;
    private JButton addFileButton;
    private JScrollPane messageScroll;
    private JTextArea messageText;
    private JScrollPane messagesListScroll;
    private JList messagesList;
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

    private ConnectMachine session = null;
    private Dialogue[] haveDialogues = null;
    private Message[] currentMessages = null;
    private CipherParametersKeeper[] currentCipherStates = null;

    Timer autoUpdate = null;

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
            currentMessages = haveDialogues[ind].getAllMessages();
            messagesList.setListData(currentMessages);

            if (currentDialogueIndex == -1) {
                setComboBoxesEnabled(true);
            } else {
                currentCipherStates[currentDialogueIndex].setComboboxes();
            }
        }
    }

    private class MessagesCellRenderer implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            //int selectedIndex = (Integer) value;
            return currentMessages[index];
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


    public void init(ConnectMachine cnt) {
        session = cnt;
        //System.out.println(session);
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

        dialoguesList.addListSelectionListener(new DialoguesSelectionListener());
        messagesList.setCellRenderer(new MessagesCellRenderer());
        dialoguesList.setCellRenderer(new DialogueCellRenderer());

        sendButton.addActionListener(new SendButtonListener());
        cipherButton.addActionListener(new CipherButtonListener());
    }

    private void initButtons() {
        cipherButton.setText("Start session");
    }

    private void setDialoguesList() {
        haveDialogues = session.getDialogues();
        JLabel[] names = new JLabel[haveDialogues.length];
        currentCipherStates = new CipherParametersKeeper[haveDialogues.length];
        for (int i = 0; i < haveDialogues.length; i++) {
            names[i] = haveDialogues[i].getRenderObject();
            currentCipherStates[i] = new CipherParametersKeeper();
        }
        dialoguesList.setListData(names);

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
        currentMessages = haveDialogues[currentDialogueIndex].getAllMessages();
        messagesList.setListData(currentMessages);
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
        dialogueListScroll = new JScrollPane();
        dialogueListScroll.setHorizontalScrollBarPolicy(31);
        dialogueListScroll.setMinimumSize(new Dimension(256, 512));
        dialogueListScroll.setPreferredSize(new Dimension(256, 512));
        panel.add(dialogueListScroll, BorderLayout.WEST);
        dialoguesList = new JList();
        Font dialoguesListFont = this.$$$getFont$$$(null, -1, 24, dialoguesList.getFont());
        if (dialoguesListFont != null) dialoguesList.setFont(dialoguesListFont);
        dialoguesList.setMinimumSize(new Dimension(250, 512));
        dialoguesList.setPreferredSize(new Dimension(250, 512));
        dialoguesList.setSelectionMode(0);
        dialogueListScroll.setViewportView(dialoguesList);
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
        messagesListScroll.setPreferredSize(new Dimension(300, 448));
        dialogueArea.add(messagesListScroll, BorderLayout.CENTER);
        messagesList = new JList();
        messagesList.setEnabled(false);
        messagesListScroll.setViewportView(messagesList);
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
        label4.setLabelFor(dialogueListScroll);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }

}
