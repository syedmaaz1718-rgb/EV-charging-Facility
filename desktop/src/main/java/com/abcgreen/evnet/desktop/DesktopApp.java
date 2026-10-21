package com.abcgreen.evnet.desktop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * EV ChargeNet Desktop GUI (Module IV: Swing, event handling; Module V: JDBC).
 * Connects directly to the same MySQL database as the web app.
 * Run with: java -jar ev-chargenet-desktop-1.0.0.jar
 * DB settings come from env vars DB_URL / DB_USER / DB_PASS (or the login dialog fields).
 */
public class DesktopApp extends JFrame {

    private Connection conn;
    private String userEmail;
    private boolean admin;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final DefaultTableModel stationsModel = new DefaultTableModel(
            new String[]{"Station", "Area", "Free", "Occupied", "Faulty", "Status"}, 0);
    private final DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"Date", "Station", "Charger", "Energy (kWh)", "Amount (Rs)"}, 0);

    public DesktopApp() {
        super("EV ChargeNet - Desktop");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        root.add(loginPanel(), "login");
        root.add(mainPanel(), "main");
        add(root);
        cards.show(root, "login");
    }

    private JPanel loginPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(11, 18, 32));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        JLabel title = new JLabel("EV ChargeNet");
        title.setForeground(new Color(34, 197, 94));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        JTextField url = new JTextField(System.getenv().getOrDefault("DB_URL",
                "jdbc:mysql://localhost:3306/evnet"), 24);
        JTextField user = new JTextField(System.getenv().getOrDefault("DB_USER", "root"), 24);
        JPasswordField dbpass = new JPasswordField(System.getenv().getOrDefault("DB_PASS", ""), 24);
        JTextField email = new JTextField(24);
        JPasswordField pw = new JPasswordField(24);
        JButton connect = new JButton("Connect & Sign In");
        JLabel status = new JLabel(" ");
        status.setForeground(new Color(248, 113, 113));
        String[] labels = {"Database URL", "DB user", "DB password", "Account email", "Account password"};
        Component[] fields = {url, user, dbpass, email, pw};
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; p.add(title, g);
        for (int i = 0; i < labels.length; i++) {
            g.gridwidth = 1; g.gridx = 0; g.gridy = i + 1;
            JLabel l = new JLabel(labels[i]); l.setForeground(Color.LIGHT_GRAY); p.add(l, g);
            g.gridx = 1; p.add(fields[i], g);
        }
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; p.add(connect, g);
        g.gridy = 7; p.add(status, g);
        connect.addActionListener(e -> {
            try {
                Class.forName(url.getText().trim().startsWith("jdbc:postgresql") ? "org.postgresql.Driver" : "com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(url.getText().trim(), user.getText().trim(),
                        new String(dbpass.getPassword()));
                PreparedStatement ps = conn.prepareStatement("SELECT role FROM users WHERE email=?");
                ps.setString(1, email.getText().trim().toLowerCase());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) { status.setText("Account not found"); return; }
                userEmail = email.getText().trim().toLowerCase();
                admin = "ADMIN".equals(rs.getString("role"));
                refresh();
                cards.show(root, "main");
            } catch (Exception ex) {
                status.setText("Connection failed: " + ex.getMessage());
            }
        });
        return p;
    }

    private JPanel mainPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        JTable stations = new JTable(stationsModel);
        JTable history = new JTable(historyModel);
        tabs.addTab("Stations", new JScrollPane(stations));
        tabs.addTab("My charging history", new JScrollPane(history));
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        JButton book = new JButton("Book slot (first free fast charger)");
        book.addActionListener(e -> bookSlot());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refresh); top.add(book);
        p.add(top, BorderLayout.NORTH);
        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    private void refresh() {
        try {
            stationsModel.setRowCount(0);
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT s.name, s.area, s.status, " +
                "SUM(CASE WHEN c.status='FREE' THEN 1 ELSE 0 END) free, SUM(CASE WHEN c.status='OCCUPIED' THEN 1 ELSE 0 END) occ, SUM(CASE WHEN c.status='FAULTY' THEN 1 ELSE 0 END) faulty " +
                "FROM stations s LEFT JOIN chargers c ON c.station_id=s.id GROUP BY s.id, s.name, s.area, s.status");
            while (rs.next())
                stationsModel.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getInt(4),
                        rs.getInt(5), rs.getInt(6), rs.getString(3)});
            historyModel.setRowCount(0);
            PreparedStatement ps = conn.prepareStatement(
                "SELECT s.start_time, st.name, c.code, s.energy_kwh, s.amount " +
                "FROM sessions s JOIN bookings b ON b.id=s.booking_id " +
                "JOIN stations st ON st.id=b.station_id JOIN chargers c ON c.id=s.charger_id " +
                "JOIN users u ON u.id=b.user_id WHERE u.email=? AND s.status='COMPLETED' ORDER BY s.id DESC");
            ps.setString(1, userEmail);
            ResultSet h = ps.executeQuery();
            while (h.next())
                historyModel.addRow(new Object[]{h.getTimestamp(1), h.getString(2), h.getString(3),
                        h.getDouble(4), h.getDouble(5)});
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Refresh failed: " + ex.getMessage());
        }
    }

    private void bookSlot() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT c.id, c.station_id FROM chargers c WHERE c.type='FAST' AND c.status='FREE' " +
                "AND c.station_id IN (SELECT id FROM stations WHERE status='ONLINE') LIMIT 1");
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { JOptionPane.showMessageDialog(this, "All fast chargers busy - use the web app to join the queue"); return; }
            int chargerId = rs.getInt(1), stationId = rs.getInt(2);
            PreparedStatement vu = conn.prepareStatement(
                "SELECT v.id, u.id FROM vehicles v JOIN users u ON u.id=v.user_id WHERE u.email=? LIMIT 1");
            vu.setString(1, userEmail);
            ResultSet v = vu.executeQuery();
            if (!v.next()) { JOptionPane.showMessageDialog(this, "Register a vehicle in the web app first"); return; }
            conn.createStatement().executeUpdate("UPDATE chargers SET status='OCCUPIED' WHERE id=" + chargerId);
            conn.createStatement().executeUpdate(
                "INSERT INTO bookings(user_id,vehicle_id,station_id,charger_id,charger_type,slot_time,status) " +
                "VALUES(" + v.getInt(2) + "," + v.getInt(1) + "," + stationId + "," + chargerId +
                ",'FAST',CURRENT_TIMESTAMP,'ALLOCATED')");
            ResultSet idr = conn.createStatement().executeQuery("SELECT MAX(id) FROM bookings");
            idr.next();
            conn.createStatement().executeUpdate(
                "INSERT INTO sessions(booking_id,charger_id) VALUES(" + idr.getInt(1) + "," + chargerId + ")");
            JOptionPane.showMessageDialog(this, "Slot booked - charger allocated. Start charging from the web app.");
            refresh();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Booking failed: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DesktopApp().setVisible(true));
    }
}
