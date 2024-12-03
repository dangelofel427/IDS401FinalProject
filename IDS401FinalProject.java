import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IDS401FinalProject {
    private static final String DB_URL = "jdbc:sqlite:C:/IDS401/IDS401/IDS401fp/College_Rooms.db";
    private JFrame frame;
    private JTextField nameField, addressField, searchRoomField, bookingDateField;
    private JTable roomTable;
    private DefaultTableModel roomTableModel;
    private int currentUserId;
    private JPanel mainPanel;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                IDS401FinalProject window = new IDS401FinalProject();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public IDS401FinalProject() {
        // Initialize the main frame
        frame = new JFrame("College Room Booking App");
        frame.setBounds(100, 100, 900, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        frame.getContentPane().setBackground(new Color(240, 240, 240)); // Light Gray background

        // Create the login screen
        showLoginScreen();
    }

    // Show the login screen or account creation screen
    private void showLoginScreen() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 2, 10, 10));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Login / Create Account"));
        loginPanel.setBackground(new Color(220, 220, 220));

        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField();
        JLabel addressLabel = new JLabel("Address:");
        addressField = new JTextField();

        JButton loginButton = new JButton("Log In");
        JButton createAccountButton = new JButton("Create Account");

        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        createAccountButton.setFont(new Font("Arial", Font.BOLD, 14));

        // Set button actions
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginUser();
            }
        });

        createAccountButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createAccount();
            }
        });

        loginPanel.add(nameLabel);
        loginPanel.add(nameField);
        loginPanel.add(addressLabel);
        loginPanel.add(addressField);
        loginPanel.add(loginButton);
        loginPanel.add(createAccountButton);

        frame.getContentPane().removeAll();
        frame.getContentPane().add(loginPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    // Create a new account in the User_Table
    private void createAccount() {
        String name = nameField.getText();
        String address = addressField.getText();

        if (name.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Both fields are required!");
            return;
        }

        try (Connection conn = getConnection()) {
            String query = "INSERT INTO User_Table (Name, Address) VALUES (?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, name);
                pst.setString(2, address);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Account created successfully!");
                nameField.setText("");
                addressField.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error creating account: " + e.getMessage());
        }
    }

    // Log in the user by verifying UserID and Address
    private void loginUser() {
        String name = nameField.getText();
        String address = addressField.getText();

        if (name.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Both fields are required!");
            return;
        }

        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM User_Table WHERE Name = ? AND Address = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, name);
                pst.setString(2, address);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    currentUserId = rs.getInt("UserID");
                    showRoomPanel(); // Show the room booking screen
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid login credentials!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error during login: " + e.getMessage());
        }
    }

    // Display the room search and booking panel
    private void showRoomPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Room table - Display all rooms right away
        roomTableModel = new DefaultTableModel(new Object[]{"RoomID", "Room Name", "Availability"}, 0);
        roomTable = new JTable(roomTableModel) {
            // Make the table non-editable
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomTable.setFont(new Font("Arial", Font.PLAIN, 14));
        roomTable.setRowHeight(30);

        // Load all rooms and availability
        loadAllRooms();

        JScrollPane roomScrollPane = new JScrollPane(roomTable);
        mainPanel.add(roomScrollPane, BorderLayout.CENTER);

        // Add search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(new Color(220, 220, 220));

        JLabel searchLabel = new JLabel("Search Room:");
        searchRoomField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setBackground(new Color(0, 123, 255));
        searchButton.setForeground(Color.WHITE);

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchRooms();
            }
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchRoomField);
        searchPanel.add(searchButton);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Add room booking functionality
        JPanel bookingPanel = new JPanel();
        JLabel bookingDateLabel = new JLabel("Booking Date (YYYY-MM-DD):");
        bookingDateField = new JTextField(10);
        JButton bookRoomButton = new JButton("Book Room");
        JButton cancelBookingButton = new JButton("Cancel Booking");

        bookingDateField.setFont(new Font("Arial", Font.PLAIN, 14));
        bookRoomButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelBookingButton.setFont(new Font("Arial", Font.BOLD, 14));
        bookRoomButton.setBackground(new Color(0, 123, 255));
        cancelBookingButton.setBackground(new Color(255, 69, 0));

        bookRoomButton.setForeground(Color.WHITE);
        cancelBookingButton.setForeground(Color.WHITE);

        // Button actions
        bookRoomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bookRoom();
            }
        });

        cancelBookingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelBooking();
            }
        });

        bookingPanel.add(bookingDateLabel);
        bookingPanel.add(bookingDateField);
        bookingPanel.add(bookRoomButton);
        bookingPanel.add(cancelBookingButton);

        mainPanel.add(bookingPanel, BorderLayout.SOUTH);

        // Set main panel
        frame.getContentPane().removeAll();
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    // Load all rooms with availability status
    private void loadAllRooms() {
        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM Room_Table";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                ResultSet rs = pst.executeQuery();
                
                // Clear the existing rows in the table
                roomTableModel.setRowCount(0);
    
                // Add new room data to the table
                while (rs.next()) {
                    int roomId = rs.getInt("RoomID");
                    String roomName = rs.getString("RoomName");
                    String availability = rs.getString("Availability");
                    roomTableModel.addRow(new Object[]{roomId, roomName, availability});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading rooms: " + e.getMessage());
        }
    }

    // Search rooms based on user input
    private void searchRooms() {
        String searchKeyword = searchRoomField.getText().trim();
        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM Room_Table WHERE RoomName LIKE ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, "%" + searchKeyword + "%");
                ResultSet rs = pst.executeQuery();
                roomTableModel.setRowCount(0); // Clear existing rows
                while (rs.next()) {
                    int roomId = rs.getInt("RoomID");
                    String roomName = rs.getString("RoomName");
                    String availability = rs.getString("Availability");
                    roomTableModel.addRow(new Object[]{roomId, roomName, availability});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error searching for rooms: " + e.getMessage());
        }
    }

    // Book the selected room
    private void bookRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a room to book.");
            return;
        }
    
        int roomId = (int) roomTableModel.getValueAt(selectedRow, 0);
        String bookingDate = bookingDateField.getText();
    
        if (bookingDate.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a booking date.");
            return;
        }
    
        try (Connection conn = getConnection()) {
            String availability = (String) roomTableModel.getValueAt(selectedRow, 2);
            if ("Full".equals(availability)) {
                JOptionPane.showMessageDialog(frame, "This room is already booked.");
                return;
            }
    
            // Insert booking into the database
            String query = "INSERT INTO Booking_Table (RoomID, UserID, BookingDate) VALUES (?, ?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, roomId);
                pst.setInt(2, currentUserId);
                pst.setString(3, bookingDate);
                pst.executeUpdate();
    
                // Get the last inserted BookingID using SQLite's last_insert_rowid()
                String lastInsertedIdQuery = "SELECT last_insert_rowid()";
                try (PreparedStatement pstId = conn.prepareStatement(lastInsertedIdQuery)) {
                    ResultSet rs = pstId.executeQuery();
                    if (rs.next()) {
                        int bookingId = rs.getInt(1); // This is the last inserted BookingID
                        JOptionPane.showMessageDialog(frame, "Room booked successfully! Booking ID: " + bookingId);
    
                        // Mark the room as 'Full' in the database
                        String updateQuery = "UPDATE Room_Table SET Availability = 'Full' WHERE RoomID = ?";
                        try (PreparedStatement updatePst = conn.prepareStatement(updateQuery)) {
                            updatePst.setInt(1, roomId);
                            updatePst.executeUpdate();
                        }
    
                        loadAllRooms(); // Reload the room availability information
                    }
                }
    
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error booking room: " + e.getMessage());
        }
    }

    // Cancel the booking
    private void cancelBooking() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a room to cancel.");
            return;
        }
    
        int roomId = (int) roomTableModel.getValueAt(selectedRow, 0);
        String bookingDate = bookingDateField.getText();
    
        if (bookingDate.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a booking date.");
            return;
        }
    
        try (Connection conn = getConnection()) {
            // Check if this booking belongs to the current user
            String query = "SELECT * FROM Booking_Table WHERE RoomID = ? AND UserID = ? AND BookingDate = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, roomId);
                pst.setInt(2, currentUserId); // Ensure the current user booked this room
                pst.setString(3, bookingDate);
                ResultSet rs = pst.executeQuery();
    
                // If no matching booking is found, show an error
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(frame, "You cannot cancel a booking that you have not made.");
                    return;
                }
    
                // Proceed with the cancellation process
                String deleteQuery = "DELETE FROM Booking_Table WHERE RoomID = ? AND UserID = ? AND BookingDate = ?";
                try (PreparedStatement deletePst = conn.prepareStatement(deleteQuery)) {
                    deletePst.setInt(1, roomId);
                    deletePst.setInt(2, currentUserId);
                    deletePst.setString(3, bookingDate);
                    deletePst.executeUpdate();
    
                    // Update the room availability to 'Free'
                    String updateQuery = "UPDATE Room_Table SET Availability = 'Free' WHERE RoomID = ?";
                    try (PreparedStatement updatePst = conn.prepareStatement(updateQuery)) {
                        updatePst.setInt(1, roomId);
                        updatePst.executeUpdate();
                    }
    
                    JOptionPane.showMessageDialog(frame, "Booking cancelled successfully!");
                    loadAllRooms(); // Reload the room availability
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error cancelling booking: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}