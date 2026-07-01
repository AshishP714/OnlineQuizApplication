package com.quizapp.ui;

import com.quizapp.model.User;
import com.quizapp.service.AuthService;

import javax.swing.*;
import java.awt.*;

/** Login screen shown at application startup. */
public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        super("Online Quiz Application - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 380);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Theme.BG);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Theme.BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0; gbc.gridwidth = 2;

        JLabel title = new JLabel("Online Quiz Application");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.PRIMARY_DARK);
        gbc.gridy = 0;
        root.add(title, gbc);

        JLabel subtitle = new JLabel("Sign in to continue");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.MUTED);
        gbc.gridy = 1;
        root.add(subtitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0; gbc.anchor = GridBagConstraints.EAST;
        root.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        root.add(usernameField, gbc);

        gbc.gridy = 3; gbc.gridx = 0; gbc.anchor = GridBagConstraints.EAST;
        root.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        root.add(passwordField, gbc);

        JButton loginBtn = new JButton("Log In");
        loginBtn.setBackground(Theme.PRIMARY);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> doLogin());

        JButton registerBtn = new JButton("Create Account");
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> new RegisterDialog(this).setVisible(true));

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(Theme.BG);
        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);
        root.add(btnPanel, gbc);

        JLabel hint = new JLabel("<html><center>Default admin login:<br>username <b>admin</b> / password <b>admin123</b></center></html>");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.MUTED);
        gbc.gridy = 5;
        root.add(hint, gbc);

        getRootPane().setDefaultButton(loginBtn);
        add(root, BorderLayout.CENTER);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        try {
            User user = authService.login(username, password);
            new MainMenuFrame(user).setVisible(true);
            dispose();
        } catch (AuthService.AuthException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
