package com.mycompany.src;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class TableInterface extends JFrame {
    private JTable table;
    private JButton customersButton;
    private JButton authorsButton;
    private JButton booksButton;
    private JButton ordersButton;
    private JButton saveChangesButton;
    private JButton addEntryButton;

    public TableInterface() {
        setTitle("Table Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create components
        table = new JTable();
        table.setModel(new DefaultTableModel());

        customersButton = new JButton("Customers");
        customersButton.addActionListener(e -> showTableData("customers"));

        authorsButton = new JButton("Authors");
        authorsButton.addActionListener(e -> showTableData("authors"));

        booksButton = new JButton("Books");
        booksButton.addActionListener(e -> showTableData("books"));

        ordersButton = new JButton("Orders");
        ordersButton.addActionListener(e -> showTableData("orders"));

        saveChangesButton = new JButton("Save Changes");
        saveChangesButton.addActionListener(e -> saveChanges());
        addEntryButton = new JButton("Add Entry"); // New button for adding entries
        addEntryButton.addActionListener(e -> addNewEntry());

        // Layout components
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(customersButton);
        buttonPanel.add(authorsButton);
        buttonPanel.add(booksButton);
        buttonPanel.add(ordersButton);
        buttonPanel.add(saveChangesButton);
        buttonPanel.add(addEntryButton);
        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Set the size of the JFrame
        setSize(800, 600);

        // Center the JFrame on the screen
        setLocationRelativeTo(null);

        // Initial data load
        //refreshData(); // Commented out as it was not provided

    }

    private void showTableData(String tableName) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/LibrarySystem", "Topguy", "0000")) {
            DefaultTableModel model = new DefaultTableModel();
            addColumnsToModel(model, connection, tableName);
            addDataToModel(model, connection, tableName);
            table.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addColumnsToModel(DefaultTableModel model, Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet columns = metaData.getColumns(null, null, tableName, null);

        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            model.addColumn(columnName);
        }
    }

    private void addDataToModel(DefaultTableModel model, Connection connection, String tableName) throws SQLException {
        String query = "SELECT * FROM " + tableName;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Object[] rowData = new Object[model.getColumnCount()];
                for (int i = 0; i < rowData.length; i++) {
                    rowData[i] = resultSet.getObject(i + 1);
                }
                model.addRow(rowData);
            }
        }
    }


    private void saveChanges() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rowCount = model.getRowCount();
        String tableName = getTableName();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/LibrarySystem", "Topguy", "0000")) {
            for (int i = 0; i < rowCount; i++) {
                Object primaryKeyValue = model.getValueAt(i, model.findColumn(getPrimaryKeyColumnName(tableName)));

                // If the primary key value is null, it means it's a new entry
                if (primaryKeyValue == null) {
                    StringBuilder insertQuery = new StringBuilder("INSERT INTO ").append(tableName).append(" (");

                    // Build column names for the insert query
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        insertQuery.append(model.getColumnName(j));
                        if (j < model.getColumnCount() - 1) {
                            insertQuery.append(", ");
                        }
                    }

                    insertQuery.append(") VALUES (");

                    // Build values for the insert query
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object cellValue = model.getValueAt(i, j);

                        // Handle different data types
                        if (cellValue instanceof String) {
                            insertQuery.append("'").append(cellValue).append("'");
                        } else {
                            insertQuery.append(cellValue);
                        }

                        if (j < model.getColumnCount() - 1) {
                            insertQuery.append(", ");
                        }
                    }

                    insertQuery.append(")");

                    // Execute the insert query
                    try (Statement insertStatement = connection.createStatement()) {
                        insertStatement.executeUpdate(insertQuery.toString(), Statement.RETURN_GENERATED_KEYS);

                        // Retrieve the auto-generated key (if any)
                        ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            model.setValueAt(generatedKeys.getObject(1), i, model.findColumn(getPrimaryKeyColumnName(tableName)));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error inserting data into the database", "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else { // Existing entry, perform update
                    StringBuilder updateQuery = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
                    int columnCount = model.getColumnCount();

                    // Iterate through columns to build the update query
                    for (int j = 0; j < columnCount; j++) {
                        String columnName = model.getColumnName(j);
                        Object cellValue = model.getValueAt(i, j);

                        // Handle different data types
                        if (cellValue instanceof String) {
                            updateQuery.append(columnName).append(" = '").append(cellValue).append("'");
                        } else {
                            updateQuery.append(columnName).append(" = ").append(cellValue);
                        }

                        if (j < columnCount - 1) {
                            updateQuery.append(", ");
                        }
                    }

                    // Build the WHERE clause for the update query
                    updateQuery.append(" WHERE ").append(getPrimaryKeyColumnName(tableName)).append(" = ").append(primaryKeyValue);

                    // Execute the update query
                    try (Statement updateStatement = connection.createStatement()) {
                        updateStatement.executeUpdate(updateQuery.toString());
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error updating data in the database", "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database", "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        JOptionPane.showMessageDialog(this, "Data updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
    }


    private String getPrimaryKeyColumnName(String tableName) {
        switch (tableName) {
            case "customers":
                return "cId";
            case "books":
                return "bookId";
            case "authors":
                return "authorId";
            case "orders":
                return "orderId";
            default:
                return "id";
        }
    }


    private String getTableName() {
        // Assuming you have a method to get the current table name
        return "customers"; // Replace with your logic to get the table name
    }
    private void addNewEntry() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addRow(new Object[model.getColumnCount()]);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TableInterface().setVisible(true);
        });
    }
}
