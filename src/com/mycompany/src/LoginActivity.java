    package com.mycompany.src;

    import javax.swing.*;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;


    public class LoginActivity extends JFrame {
        public static int cId;
        private JTextField emailField;
        private JPasswordField passwordField;


        public LoginActivity() {
            setTitle("Login Form");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new GridLayout(3, 2));
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new GridLayout(3, 2));

            JLabel emailLabel = new JLabel("Email:");
            JLabel passwordLabel = new JLabel("Password:");

            emailField = new JTextField();
            passwordField = new JPasswordField();
            JButton newUserButton = new JButton("New User ?");
            newUserButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    RegisterActivity r = new RegisterActivity();
                    r.setVisible(true);
                    LoginActivity.this.dispose();
                }
            });
            JButton loginButton = new JButton("Login");
            loginButton.addActionListener(e -> login());
            mainPanel.add(emailLabel);
            mainPanel.add(createSmallTextField(emailField));
            mainPanel.add(passwordLabel);
            mainPanel.add(createSmallPasswordField(passwordField));

            // Create a button panel with FlowLayout
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(newUserButton);
            buttonPanel.add(loginButton);

            // Add the main panel and button panel to the JFrame
            add(mainPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.CENTER);


            // Set the size of the JFrame
            setSize(400, 300);

            // Center the JFrame on the screen
            setLocationRelativeTo(null);
        }

        private JTextField createSmallTextField(JTextField textField) {
            textField.setPreferredSize(new Dimension(150, 20));
            return textField;
        }

        private JPasswordField createSmallPasswordField(JPasswordField passwordField) {
            passwordField.setPreferredSize(new Dimension(150, 20));
            return passwordField;
        }

        private JButton createSmallButton(JButton button) {
            button.setPreferredSize(new Dimension(150, 30));
            return button;
        }

        private void login() {
            String email = emailField.getText();
            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars);

            String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/LibrarySystem";
            String dbUsername = "Topguy";
            String dbPassword = "0000";

            try {
                Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);

                String query = "SELECT * FROM customers WHERE cEmail=? AND cPassword=?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, email);
                    preparedStatement.setString(2, password);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        JOptionPane.showMessageDialog(this, "Login successful!");
                         cId = resultSet.getInt("cId");
                        MainActivity main = new MainActivity(cId);
                        MainActivity m = new MainActivity();
                        System.out.print(cId);
                        main.setVisible(false);
                        m.setVisible(true);
                        LoginActivity.this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid email or password", "Login Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database connection error", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }


        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                new LoginActivity().setVisible(true);
            });
        }
    }
