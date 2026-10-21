package com.abcgreen.evnet.service;

import com.abcgreen.evnet.model.Models.*;
import static com.abcgreen.evnet.model.Models.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** FR-07, FR-10: charging sessions, billing, invoices, history. */
@Service
public class SessionService {
    private final JdbcTemplate db;
    private final StationService stations;
    private final BookingService bookings;
    private final AuditService audit;

    public SessionService(JdbcTemplate db, StationService stations, BookingService bookings, AuditService audit) {
        this.db = db; this.stations = stations; this.bookings = bookings; this.audit = audit;
    }

    public Session start(int bookingId, int userId, boolean isAdmin, String actor) {
        Booking b = bookings.byId(bookingId);
        if (!isAdmin && b.userId() != userId) throw new Exceptions.AuthException("Not your booking");
        if (!"ALLOCATED".equals(b.status())) throw new IllegalStateException("Booking is not allocated yet");
        db.update("UPDATE sessions SET status='RUNNING', start_time=CURRENT_TIMESTAMP WHERE booking_id=?", bookingId);
        db.update("UPDATE bookings SET status='IN_SESSION' WHERE id=?", bookingId);
        audit.log(actor, "Started charging session for booking #" + bookingId);
        return byBooking(bookingId);
    }

    /** Stop: compute energy + cost, free the charger, issue invoice (FR-10). */
    public Session stop(int sessionId, int userId, boolean isAdmin, String actor) {
        Session s = byId(sessionId);
        Booking b = bookings.byId(s.bookingId());
        if (!isAdmin && b.userId() != userId) throw new Exceptions.AuthException("Not your session");
        if (!"RUNNING".equals(s.status())) throw new IllegalStateException("Session is not running");
        Charger c = stations.chargerById(s.chargerId());

        db.update("UPDATE sessions SET end_time=CURRENT_TIMESTAMP WHERE id=?", sessionId);
        Timestamp end = db.queryForObject("SELECT end_time FROM sessions WHERE id=?", Timestamp.class, sessionId);
        long seconds = Math.max(60, (end.getTime() - s.startTime().getTime()) / 1000);
        double hours = seconds / 3600.0;
        double energy = Math.round(c.powerKw() * hours * 0.92 * 100.0) / 100.0; // 92% efficiency
        double amount = Math.round(energy * c.ratePerKwh() * 100.0) / 100.0;

        db.update("UPDATE sessions SET energy_kwh=?, amount=?, status='COMPLETED' WHERE id=?", energy, amount, sessionId);
        db.update("UPDATE bookings SET status='COMPLETED' WHERE id=?", b.id());
        db.update("UPDATE chargers SET status='FREE' WHERE id=?", c.id());
        String invoiceNo = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + sessionId;
        db.update("INSERT INTO invoices(session_id, invoice_no, amount) VALUES(?,?,?)", sessionId, invoiceNo, amount);
        audit.log(actor, "Stopped session #" + sessionId + " - " + energy + " kWh, Rs " + amount + ", invoice " + invoiceNo);
        bookings.tryAllocateNext(b.stationId(), b.chargerType());
        return byId(sessionId);
    }

    public void payInvoice(int invoiceId, int userId, boolean isAdmin, String actor) {
        Invoice inv = invoiceById(invoiceId);
        Session s = byId(inv.sessionId());
        Booking b = bookings.byId(s.bookingId());
        if (!isAdmin && b.userId() != userId) throw new Exceptions.AuthException("Not your invoice");
        db.update("UPDATE invoices SET status='PAID' WHERE id=?", invoiceId);
        audit.log(actor, "Paid invoice " + inv.invoiceNo() + " (Rs " + inv.amount() + ")");
    }

    public Session byId(int id) {
        List<Session> s = db.query("SELECT * FROM sessions WHERE id=?", SESSION, id);
        if (s.isEmpty()) throw new Exceptions.NotFoundException("Session");
        return s.get(0);
    }

    public Session byBooking(int bookingId) {
        List<Session> s = db.query("SELECT * FROM sessions WHERE booking_id=?", SESSION, bookingId);
        if (s.isEmpty()) throw new Exceptions.NotFoundException("Session");
        return s.get(0);
    }

    public Invoice invoiceById(int id) {
        List<Invoice> i = db.query("SELECT * FROM invoices WHERE id=?", INVOICE, id);
        if (i.isEmpty()) throw new Exceptions.NotFoundException("Invoice");
        return i.get(0);
    }

    public List<Map<String, Object>> historyFor(int userId) {
        return db.queryForList("""
            SELECT s.id session_id, s.start_time, s.end_time, s.energy_kwh, s.amount, s.status,
                   st.name station, c.code charger_code, c.type charger_type,
                   i.id invoice_id, i.invoice_no, i.status invoice_status
            FROM sessions s
            JOIN bookings b ON b.id = s.booking_id
            JOIN stations st ON st.id = b.station_id
            JOIN chargers c ON c.id = s.charger_id
            LEFT JOIN invoices i ON i.session_id = s.id
            WHERE b.user_id=? AND s.status='COMPLETED'
            ORDER BY s.id DESC""", userId);
    }

    public List<Map<String, Object>> running() {
        return db.queryForList("""
            SELECT s.id, s.start_time, st.name station, c.code charger_code, u.name customer
            FROM sessions s
            JOIN bookings b ON b.id = s.booking_id
            JOIN stations st ON st.id = b.station_id
            JOIN chargers c ON c.id = s.charger_id
            JOIN users u ON u.id = b.user_id
            WHERE s.status='RUNNING' ORDER BY s.start_time""");
    }
}
