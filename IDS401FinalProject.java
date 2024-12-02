import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class IDS401FinalProject {
    // Correct SQLite DB URL
    private static final String DB_URL = "jdbc:sqlite:C:/Users/dangelo/eclipse-workspace/IDS401FinalProject/College_Rooms.db";

    private JFrame frame;
    private JTextField nameField;
    private JTextField addressField;
    private JTable userTable;
    private JTable roomTable;
    private DefaultTableModel userTableModel;
    private DefaultTableModel roomTableModel;

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
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        // Add a panel for the form to add a new user
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3, 2));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add User"));

        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField();
        JLabel addressLabel = new JLabel("Address:");
        addressField = new JTextField();
        JButton addUserButton = new JButton("Add User");

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(addressLabel);
        formPanel.add(addressField);
        formPanel.add(addUserButton);

        frame.getContentPane().add(formPanel, BorderLayout.NORTH);

        // Add button action to add user to database
        addUserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addUser();
            }
        });

        // Add tables for Users and Rooms
        JTabbedPane tabbedPane = new JTabbedPane();

        // User table
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());

        userTableModel = new DefaultTableModel(new Object[]{"UserID", "Name", "Address"}, 0);
        userTable = new JTable(userTableModel);
        JScrollPane userScrollPane = new JScrollPane(userTable);
        userPanel.add(userScrollPane, BorderLayout.CENTER);

        JButton loadUsersButton = new JButton("Load Users");
        loadUsersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadUsers();
            }
        });
        userPanel.add(loadUsersButton, BorderLayout.SOUTH);
        tabbedPane.addTab("Users", userPanel);

        // Room table
        JPanel roomPanel = new JPanel();
        roomPanel.setLayout(new BorderLayout());

        roomTableModel = new DefaultTableModel(new Object[]{"RoomID", "Room Name"}, 0);
        roomTable = new JTable(roomTableModel);
        JScrollPane roomScrollPane = new JScrollPane(roomTable);
        roomPanel.add(roomScrollPane, BorderLayout.CENTER);

        JButton loadRoomsButton = new JButton("Load Rooms");
        loadRoomsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadRooms();
            }
        });
        roomPanel.add(loadRoomsButton, BorderLayout.SOUTH);
        tabbedPane.addTab("Rooms", roomPanel);

        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }

    // Add a user to the database
    private void addUser() {
        String name = nameField.getText();
        String address = addressField.getText();

        if (name.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Both fields are required!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "INSERT INTO User_Table (Name, Address) VALUES (?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, name);
                pst.setString(2, address);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(frame, "User added successfully!");
                loadUsers();  // Reload users table
                nameField.setText("");
                addressField.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error adding user: " + e.getMessage());
        }
    }

    // Load user data from the database and populate the user table
    private void loadUsers() {
        // Clear the existing data in the table
        userTableModel.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT * FROM User_Table";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    int userId = rs.getInt("UserID");
                    String name = rs.getString("Name");
                    String address = rs.getString("Address");
                    userTableModel.addRow(new Object[]{userId, name, address});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading users: " + e.getMessage());
        }
    }

    // Load room data from the database and populate the room table
    private void loadRooms() {
        // Clear the existing data in the table
        roomTableModel.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT * FROM Room_Table";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    int roomId = rs.getInt("RoomID");
                    String roomName = rs.getString("RoomName");
                    roomTableModel.addRow(new Object[]{roomId, roomName});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading rooms: " + e.getMessage());
        }
    }
}