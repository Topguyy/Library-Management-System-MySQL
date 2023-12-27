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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends JFrame {
    public static int customerId;
    public MainActivity(int cId) {
      int customerId = cId;
      this.customerId = customerId;
        System.out.println("customerId in MainActivity: " + customerId);
    }

    private List<Book> books;
    private JPanel booksPanel;
    private ShoppingCart shoppingCart;
    private JPanel customerInfoPanel;  // New panel for displaying customer information
     //static int customerId = LoginActivity.cId;
    // Customer information
    private String cFirstName;
    private String cLastName;
    private String cEmail;
    public  static String cAddress;
    public static String cPhoneNumber;

    public MainActivity() {
        setTitle("Library Buying Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize the list of books
        books = retrieveBooksFromDatabase();

        // Create a panel to display books
        booksPanel = new JPanel(new GridLayout(0, 3)); // Adjust the number of columns as needed

        // Create a shopping cart
        shoppingCart = new ShoppingCart();

        // Create a panel for displaying customer information
        customerInfoPanel = new JPanel();
        customerInfoPanel.setLayout(new BoxLayout(customerInfoPanel, BoxLayout.Y_AXIS));
        retrieveCustomerInfoFromDatabase();  // Retrieve customer information from the database
        addCustomerInfoLabels();
        addLogoutButton();

        // Populate the books panel with book information
        populateBooksPanel();

        // Create a scroll pane to accommodate a large number of books
        JScrollPane scrollPane = new JScrollPane(booksPanel);

        // Create a panel to hold both the customer information and shopping cart
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(customerInfoPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(shoppingCart, BorderLayout.EAST);

        // Add the main panel to the main JFrame
        add(mainPanel);

        // Set the size of the JFrame
        setSize(800, 600);

        // Center the JFrame on the screen
        setLocationRelativeTo(null);
    }

    private List<Book> retrieveBooksFromDatabase() {
        List<Book> books = new ArrayList<>();

        String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/LibrarySystem";
        String dbUsername = "Topguy";
        String dbPassword = "0000";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            String query = "SELECT bookName, authorId, bookPrice FROM books";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String bookName = resultSet.getString("bookName");
                    String authorId = resultSet.getString("authorId");
                    double bookPrice = resultSet.getDouble("bookPrice");

                    books.add(new Book(bookName, authorId, bookPrice));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving book information", "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        return books;
    }

    public void retrieveCustomerInfoFromDatabase() {
        String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/LibrarySystem";
        String dbUsername = "Topguy";
        String dbPassword = "0000";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            String query = "SELECT cFirstName, cLastName, cEmail, cAddress, cPhoneNumber FROM customers WHERE cId = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, customerId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                   // System.out.print(MainActivity.customerId);
                    cFirstName = resultSet.getString("cFirstName");
                    cLastName = resultSet.getString("cLastName");
                    cEmail = resultSet.getString("cEmail");
                    cPhoneNumber = resultSet.getString("cPhoneNumber");
                    cAddress = resultSet.getString("cAddress");

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving customer information", "Database Error", JOptionPane.ERROR_MESSAGE);
        }

    }
    private void populateBooksPanel() {
        for (Book book : books) {
            JButton bookButton = new JButton(book.getBookTitle());
            bookButton.addActionListener(new BuyButtonListener(book));

            JLabel priceLabel = new JLabel("Price: $" + book.getPrice());

            JPanel bookPanel = new JPanel(new BorderLayout());
            bookPanel.add(bookButton, BorderLayout.CENTER);
            bookPanel.add(priceLabel, BorderLayout.SOUTH);

            booksPanel.add(bookPanel);
        }
    }

    private class BuyButtonListener implements ActionListener {
        private Book book;

        public BuyButtonListener(Book book) {
            this.book = book;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Add the selected book to the shopping cart
            shoppingCart.addBook(book);

            // Show a confirmation message (you can customize this)
            String message = "Book added to the cart:\n" + book.getBookTitle();
            JOptionPane.showMessageDialog(MainActivity.this, message, "Book Added", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addCustomerInfoLabels() {
        JLabel nameLabel = new JLabel("Name: " + cFirstName + " " + cLastName);
        JLabel emailLabel = new JLabel("Email: " + cEmail);
        JLabel addressLabel = new JLabel("Address: " + cAddress);

        customerInfoPanel.add(nameLabel);
        customerInfoPanel.add(emailLabel);
        customerInfoPanel.add(addressLabel);
    }

    private void addLogoutButton() {
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new LogoutButtonListener());
        customerInfoPanel.add(logoutButton);
    }

    private class LogoutButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
        LoginActivity login = new LoginActivity();
        login.setVisible(true);
            MainActivity.this.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainActivity().setVisible(true);
        });
    }
}
class Book {
    private String bookNames;
    private String authorId;
    private double bookPrice;

    public Book(String bookNames, String authorId, double bookPrice) {
        this.bookNames = bookNames;
        this.authorId = authorId;
        this.bookPrice = bookPrice;
    }

    public String getBookTitle() {
        if (bookNames.isEmpty()) {
            return "";
        } else {
            // Combine book names with a separator (e.g., ", ")
            return String.join(", ", bookNames);
        }
    }

    public String getAuthor() {
        return authorId;
    }

    public double getPrice() {
        return bookPrice;
    }
}

class ShoppingCart extends JPanel {
    private Map<Book, Integer> cart;  // Map to store books and their quantities
    private JLabel totalCostLabel;
    private JButton makeOrderButton;
    public ShoppingCart() {
        cart = new HashMap<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Create a label for displaying the total cost
        totalCostLabel = new JLabel("Total Cost: $0.00");
        add(totalCostLabel);
        makeOrderButton = new JButton("Make Order");
        makeOrderButton.addActionListener(e -> makeOrder());

        // Add the Make Order button to the shopping cart
        add(makeOrderButton);
    }
    private void makeOrder() {
        // Perform the Make Order logic (insert order details into the orders table)
        boolean success = insertOrderDetails();

        if (success) {
            // Display a message for successful order
            JOptionPane.showMessageDialog(this, "Order placed successfully.", "Make Order", JOptionPane.INFORMATION_MESSAGE);

            // Clear the cart
            cart.clear();
            updateCartDisplay();
        } else {
            // Display a message for unsuccessful order
            JOptionPane.showMessageDialog(this, "Order failed. Please try again.", "Make Order Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    private boolean insertOrderDetails() {
        // Insert order details into the orders table
        String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/LibrarySystem";
        String dbUsername = "Topguy";
        String dbPassword = "0000";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
            String insertOrderQuery = "INSERT INTO orders (customerId, orderDate, totalCost ,booksOrdered ,cPhoneNumber ,cAddress) VALUES (?, CURRENT_DATE, ? , ? , ? , ? )";
            try (PreparedStatement insertOrderStatement = connection.prepareStatement(insertOrderQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                insertOrderStatement.setInt(1, MainActivity.customerId);
                insertOrderStatement.setDouble(2, calculateTotalCost());
                String booksOrdered = getBooksOrderedString(cart);
                insertOrderStatement.setString(3, booksOrdered);
                insertOrderStatement.setString(4, MainActivity.cPhoneNumber);
                insertOrderStatement.setString(5, MainActivity.cAddress);
                // Execute the insert statement
                int rowsAffected = insertOrderStatement.executeUpdate();

                // Check if the order was inserted successfully
                if (rowsAffected > 0) {
                    // Retrieve the generated order ID
                    ResultSet generatedKeys = insertOrderStatement.getGeneratedKeys();
                    int orderId = -1;
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);
                    }

                    // Optional: Print the orderId
                    System.out.println("Generated Order ID: " + orderId);

                    // Perform any additional logic related to the order if needed

                    return true;  // Indicate success
                }

                return false;  // Indicate failure
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error inserting order details", "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private String getBooksOrderedString(Map<Book, Integer> cart) {
        StringBuilder booksOrdered = new StringBuilder();
        for (Map.Entry<Book, Integer> entry : cart.entrySet()) {
            Book book = entry.getKey();
            int quantity = entry.getValue();

            // Append book names and quantities to the StringBuilder
            booksOrdered.append(book.getBookTitle()).append(" (Qty: ").append(quantity).append("), ");
        }

        // Remove the trailing comma and space
        if (booksOrdered.length() > 0) {
            booksOrdered.delete(booksOrdered.length() - 2, booksOrdered.length());
        }

        return booksOrdered.toString();
    }

    public void addBook(Book book) {
        cart.merge(book, 1, Integer::sum);  // Increment quantity or add new book with quantity 1
        updateCartDisplay();
    }
    private double calculateTotalCost() {
        double totalCost = 0.0;

        for (Map.Entry<Book, Integer> entry : cart.entrySet()) {
            Book book = entry.getKey();
            int quantity = entry.getValue();
            totalCost += book.getPrice() * quantity;
        }

        return totalCost;
    }

    private void updateCartDisplay() {
        // Remove only the book-related components, leaving the makeOrderButton intact
        for (Component component : getComponents()) {
            if (component != totalCostLabel && component != makeOrderButton) {
                remove(component);
            }
        }

        double totalCost = 0.0;

        for (Map.Entry<Book, Integer> entry : cart.entrySet()) {
            Book book = entry.getKey();
            int quantity = entry.getValue();

            JPanel bookPanel = new JPanel();
            bookPanel.setLayout(new BoxLayout(bookPanel, BoxLayout.Y_AXIS));

            // Create a label for displaying the book details including quantity
            JLabel cartItemLabel = new JLabel(book.getBookTitle() + " - $" + book.getPrice() + " x" + quantity);

            // Create a remove button
            JButton removeButton = new JButton("Remove");
            removeButton.addActionListener(e -> removeBook(book));

            // Add the label, remove button, and book details to the panel
            bookPanel.add(cartItemLabel);
            bookPanel.add(removeButton);

            // Add the panel to the shopping cart
            add(bookPanel);

            // Update the total cost
            totalCost += book.getPrice() * quantity;
        }

        // Display the updated total cost
        totalCostLabel.setText("Total Cost: $" + String.format("%.2f", totalCost));

        revalidate();
        repaint();
    }


    private void removeBook(Book book) {
        int quantity = cart.get(book);
        if (quantity > 1) {
            cart.put(book, quantity - 1);  // Decrease quantity if more than 1
        } else {
            cart.remove(book);  // Remove the book if the quantity is 1
        }
        updateCartDisplay();
    }
}
