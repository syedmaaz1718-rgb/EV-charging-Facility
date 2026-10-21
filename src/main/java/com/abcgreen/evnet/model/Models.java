package com.abcgreen.evnet.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.RowMapper;

/** Domain records + JDBC row mappers (Module II: encapsulation; Module V: JDBC). */
public class Models {

    public record User(int id, String name, String email, String phone, String passwordHash, String role, Timestamp createdAt) {
        public boolean isAdmin() { return "ADMIN".equals(role); }
    }
    public static final RowMapper<User> USER = (rs, n) -> new User(
            rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("phone"),
            rs.getString("password_hash"), rs.getString("role"), rs.getTimestamp("created_at"));

    public record Vehicle(int id, int userId, String regNo, String model, double batteryKwh) {
        public String label() { return model + " - " + regNo; }
    }
    public static final RowMapper<Vehicle> VEHICLE = (rs, n) -> new Vehicle(
            rs.getInt("id"), rs.getInt("user_id"), rs.getString("reg_no"), rs.getString("model"), rs.getDouble("battery_kwh"));

    public record Station(int id, String name, String area, String address, String status) {}
    public static final RowMapper<Station> STATION = (rs, n) -> new Station(
            rs.getInt("id"), rs.getString("name"), rs.getString("area"), rs.getString("address"), rs.getString("status"));

    public record Charger(int id, int stationId, String code, String type, double powerKw, double ratePerKwh, String status) {
        public boolean isFree() { return "FREE".equals(status); }
    }
    public static final RowMapper<Charger> CHARGER = (rs, n) -> new Charger(
            rs.getInt("id"), rs.getInt("station_id"), rs.getString("code"), rs.getString("type"),
            rs.getDouble("power_kw"), rs.getDouble("rate_per_kwh"), rs.getString("status"));

    public record Booking(int id, int userId, int vehicleId, int stationId, Integer chargerId,
                          String chargerType, Timestamp slotTime, String status, int estWaitMin, Timestamp createdAt) {}
    public static final RowMapper<Booking> BOOKING = (rs, n) -> {
        int cid = rs.getInt("charger_id");
        return new Booking(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("vehicle_id"), rs.getInt("station_id"),
                rs.wasNull() ? null : cid, rs.getString("charger_type"), rs.getTimestamp("slot_time"),
                rs.getString("status"), rs.getInt("est_wait_min"), rs.getTimestamp("created_at"));
    };

    public record Session(int id, int bookingId, int chargerId, Timestamp startTime, Timestamp endTime,
                          double energyKwh, double amount, String status) {}
    public static final RowMapper<Session> SESSION = (rs, n) -> new Session(
            rs.getInt("id"), rs.getInt("booking_id"), rs.getInt("charger_id"), rs.getTimestamp("start_time"),
            rs.getTimestamp("end_time"), rs.getDouble("energy_kwh"), rs.getDouble("amount"), rs.getString("status"));

    public record Invoice(int id, int sessionId, String invoiceNo, double amount, String status, Timestamp issuedAt) {}
    public static final RowMapper<Invoice> INVOICE = (rs, n) -> new Invoice(
            rs.getInt("id"), rs.getInt("session_id"), rs.getString("invoice_no"), rs.getDouble("amount"),
            rs.getString("status"), rs.getTimestamp("issued_at"));

    public record Maintenance(int id, int chargerId, String dueDate, String note, String status) {}
    public static final RowMapper<Maintenance> MAINTENANCE = (rs, n) -> new Maintenance(
            rs.getInt("id"), rs.getInt("charger_id"), rs.getString("due_date"), rs.getString("note"), rs.getString("status"));

    /** Joined view for history / admin tables. */
    public record SessionView(int sessionId, String date, String station, String chargerCode, String chargerType,
                              double energyKwh, long durationMin, double amount, String status, String invoiceNo, Integer invoiceId) {}

    private Models() {}
}
