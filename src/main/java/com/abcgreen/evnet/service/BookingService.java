package com.abcgreen.evnet.service;

import com.abcgreen.evnet.model.Models.Booking;
import com.abcgreen.evnet.model.Models.Charger;
import com.abcgreen.evnet.model.Models.Vehicle;
import static com.abcgreen.evnet.model.Models.BOOKING;
import static com.abcgreen.evnet.model.Models.VEHICLE;
import com.abcgreen.evnet.util.Validators;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

/** FR-05, FR-06: slot booking + smart queue (Module IV: Queue, LinkedList, lambda). */
@Service
public class BookingService {
    /** Average fast-charge session used for wait estimates. */
    public static final int AVG_SESSION_MIN = 30;

    private final JdbcTemplate db;
    private final StationService stations;
    private final AuditService audit;

    public BookingService(JdbcTemplate db, StationService stations, AuditService audit) {
        this.db = db; this.stations = stations; this.audit = audit;
    }

    // ---- vehicles (FR-03) ----
    public Vehicle addVehicle(int userId, String regNo, String model, double batteryKwh, String actor) {
        Validators.requireRegNo(regNo);
        try {
            db.update("INSERT INTO vehicles(user_id,reg_no,model,battery_kwh) VALUES(?,?,?,?)",
                    userId, regNo.trim().toUpperCase(), model, batteryKwh);
        } catch (DuplicateKeyException e) {
            throw new Exceptions.DuplicateRecordException("Vehicle " + regNo.toUpperCase());
        }
        audit.log(actor, "Registered vehicle " + regNo.toUpperCase());
        return db.query("SELECT * FROM vehicles WHERE reg_no=?", VEHICLE, regNo.trim().toUpperCase()).get(0);
    }

    public List<Vehicle> vehiclesOf(int userId) {
        return db.query("SELECT * FROM vehicles WHERE user_id=? ORDER BY id", VEHICLE, userId);
    }

    // ---- booking ----
    public Booking book(int userId, int vehicleId, int stationId, String chargerType,
                        Timestamp slotTime, String actor) {
        stations.byId(stationId);
        if (!"FAST".equals(chargerType) && !"SLOW".equals(chargerType))
            throw new IllegalArgumentException("Charger type must be FAST or SLOW");
        List<Vehicle> mine = db.query("SELECT * FROM vehicles WHERE id=? AND user_id=?", VEHICLE, vehicleId, userId);
        if (mine.isEmpty()) throw new Exceptions.NotFoundException("Vehicle");

        List<Charger> free = stations.freeChargers(stationId, chargerType);
        Booking b;
        if (!free.isEmpty()) {
            Charger c = free.get(0);
            db.update("INSERT INTO bookings(user_id,vehicle_id,station_id,charger_id,charger_type,slot_time,status) VALUES(?,?,?,?,?,?,?)",
                    userId, vehicleId, stationId, c.id(), chargerType, slotTime, "ALLOCATED");
            db.update("UPDATE chargers SET status='OCCUPIED' WHERE id=?", c.id());
            int bookingId = lastInsertId();
            db.update("INSERT INTO sessions(booking_id, charger_id) VALUES(?,?)", bookingId, c.id());
            audit.log(actor, "Booked slot at station #" + stationId + ", charger " + c.code() + " allocated");
            b = byId(bookingId);
        } else {
            int position = queueAt(stationId, chargerType).size() + 1;
            db.update("INSERT INTO bookings(user_id,vehicle_id,station_id,charger_type,slot_time,status,est_wait_min) VALUES(?,?,?,?,?,?,?)",
                    userId, vehicleId, stationId, chargerType, slotTime, "QUEUED", position * AVG_SESSION_MIN);
            audit.log(actor, "Joined queue at station #" + stationId + " (position " + position + ")");
            b = byId(lastInsertId());
        }
        return b;
    }

    /** Queued bookings for a station+type, oldest first (Module IV: Queue). */
    public Queue<Booking> queueAt(int stationId, String chargerType) {
        return new LinkedList<>(db.query(
                "SELECT * FROM bookings WHERE station_id=? AND charger_type=? AND status='QUEUED' ORDER BY created_at",
                BOOKING, stationId, chargerType));
    }

    public List<Booking> allQueued() {
        return db.query("SELECT * FROM bookings WHERE status='QUEUED' ORDER BY created_at", BOOKING);
    }

    public List<Booking> mine(int userId) {
        return db.query("SELECT * FROM bookings WHERE user_id=? ORDER BY id DESC", BOOKING, userId);
    }

    public List<Booking> active() {
        return db.query("SELECT * FROM bookings WHERE status IN ('ALLOCATED','QUEUED','IN_SESSION') ORDER BY id DESC", BOOKING);
    }

    public Booking byId(int id) {
        List<Booking> b = db.query("SELECT * FROM bookings WHERE id=?", BOOKING, id);
        if (b.isEmpty()) throw new Exceptions.NotFoundException("Booking");
        return b.get(0);
    }

    public void cancel(int bookingId, int userId, boolean isAdmin, String actor) {
        Booking b = byId(bookingId);
        if (!isAdmin && b.userId() != userId) throw new Exceptions.AuthException("Not your booking");
        if (!"ALLOCATED".equals(b.status()) && !"QUEUED".equals(b.status()))
            throw new IllegalStateException("Only waiting bookings can be cancelled");
        if (b.chargerId() != null) {
            db.update("UPDATE chargers SET status='FREE' WHERE id=?", b.chargerId());
            db.update("DELETE FROM sessions WHERE booking_id=? AND status='READY'", bookingId);
        }
        db.update("UPDATE bookings SET status='CANCELLED' WHERE id=?", bookingId);
        audit.log(actor, "Cancelled booking #" + bookingId);
    }

    /** Called by the queue processor when a charger frees up. */
    public synchronized boolean tryAllocateNext(int stationId, String chargerType) {
        Queue<Booking> queue = queueAt(stationId, chargerType);
        Booking next = queue.poll();
        if (next == null) return false;
        List<Charger> free = stations.freeChargers(stationId, chargerType);
        if (free.isEmpty()) return false;
        Charger c = free.get(0);
        db.update("UPDATE bookings SET charger_id=?, status='ALLOCATED', est_wait_min=0 WHERE id=?", c.id(), next.id());
        db.update("UPDATE chargers SET status='OCCUPIED' WHERE id=?", c.id());
        db.update("INSERT INTO sessions(booking_id, charger_id) VALUES(?,?)", next.id(), c.id());
        audit.log("system", "Auto-allocated queue booking #" + next.id() + " to charger " + c.code());
        refreshWaitEstimates(stationId, chargerType);
        return true;
    }

    private void refreshWaitEstimates(int stationId, String chargerType) {
        int pos = 0;
        for (Booking b : queueAt(stationId, chargerType)) {
            pos++;
            db.update("UPDATE bookings SET est_wait_min=? WHERE id=?", pos * AVG_SESSION_MIN, b.id());
        }
    }

    private int lastInsertId() {
        return db.queryForObject("SELECT MAX(id) FROM bookings", Integer.class);
    }
}
