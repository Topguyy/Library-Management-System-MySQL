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

public class RegisterActivity extends JFrame {

    public JTextField firstNameField, lastNameField, emailField, addressField, phoneField;
    private JPasswordField passwordField;

    public RegisterActivity() {
        setTitle("User Registration Form");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(8, 2, 10, 10));

        add(new JLabel("First Name:"));
        firstNameField = new JTextField();
        add(firstNameField);

        add(new JLabel("Last Name:"));
        lastNameField = new JTextField();
        add(lastNameField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Address:"));
        addressField = new JTextField();
        add(addressField);

        add(new JLabel("Phone Number:"));
        phoneField = new JTextField();
        add(phoneField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
        add(submitButton);

        JButton alreadyRegisteredButton = new JButton("Already Registered?");
        alreadyRegisteredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openLoginActivity();
            }
        });
        add(alreadyRegisteredButton);

        setVisible(true);
    }

    public void registerUser() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String address = addressField.getText();
        String phoneNumber = phoneField.getText();
        char[] password = passwordField.getPassword();

        // JDBC connection parameters
        String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/LibrarySystem";
        String dbUser = "Topguy";
        String dbPassword = "0000";

        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection
            Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);

            // Check if the user is already registered
            if (isUserAlreadyRegistered(connection, email)) {
                JOptionPane.showMessageDialog(this, "User already registered!");
            } else {
                // Prepare the SQL statement for inserting user data
                String sql = "INSERT INTO customers (cFirstName, cLastName, cEmail, cAddress, cPhoneNumber, cPassword) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, firstName);
                    statement.setString(2, lastName);
                    statement.setString(3, email);
                    statement.setString(4, address);
                    statement.setString(5, phoneNumber);
                    statement.setString(6, new String(password));

                    // Execute the SQL statement
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "User registered successfully!");
                        openLoginActivity();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "User registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            // Close the connection
            connection.close();

        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isUserAlreadyRegistered(Connection connection, String email) throws SQLException {
        // Check if the user with the given email already exists
        String query = "SELECT COUNT(*) FROM customers WHERE cEmail = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);
            return count > 0;
        }
    }
    private void openLoginActivity() {
         LoginActivity login = new LoginActivity();
         login.setVisible(true);
         this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RegisterActivity();
            }
        });
    }
}
