package com.quizapp.ui;

import com.quizapp.service.AuthService;

import javax.swing.*;
import java.awt.*;

/** Dialog for creating a new user account. */
public class RegisterDialog extends JDialog {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JPasswordField confirmField = new JPasswordField(18);
    private final JCheckBox adminBox = new JCheckBox("Register as administrator");
    private final AuthService authService = new AuthService();

    public RegisterDialog(Frame owner) {
        super(owner, "Create Account", true);
        setSize(380, 320);
        setLocationRelativeTo(owner);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(confirmField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(adminBox, gbc);

        JButton createBtn = new JButton("Create Account");
        createBtn.setBackground(Theme.PRIMARY);
        createBtn.setForeground(Color.WHITE);
        createBtn.addActionListener(e -> doRegister());

        gbc.gridy = 4;
        panel.add(createBtn, gbc);

        add(panel);
        getRootPane().setDefaultButton(createBtn);
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            authService.register(username, password, adminBox.isSelected());
            JOptionPane.showMessageDialog(this, "Account created! You can now log in.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (AuthService.AuthException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
