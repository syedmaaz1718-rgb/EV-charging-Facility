package com.abcgreen.evnet.service;

import com.abcgreen.evnet.model.Models.Charger;
import com.abcgreen.evnet.model.Models.Station;
import static com.abcgreen.evnet.model.Models.CHARGER;
import static com.abcgreen.evnet.model.Models.STATION;
import com.abcgreen.evnet.util.Validators;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

import java.util.*;

/** FR-04, FR-09: stations, charger configuration, infrastructure status. */
@Service
public class StationService {
    private final JdbcTemplate db;
    private final AuditService audit;

    public StationService(JdbcTemplate db, AuditService audit) {
        this.db = db; this.audit = audit;
    }

    public Station addStation(String name, String area, String address, int fast, int slow,
                              double fastRate, double slowRate, String actor) {
        Validators.requireNonBlank(name, "Station name");
        try {
            db.update("INSERT INTO stations(name,area,address) VALUES(?,?,?)", name.trim(), area, address);
        } catch (DuplicateKeyException e) {
            throw new Exceptions.DuplicateRecordException("A station with this name");
        }
        Station s = byName(name.trim());
        for (int i = 1; i <= fast; i++)
            addCharger(s.id(), "F" + i, "FAST", 60, fastRate, actor);
        for (int i = 1; i <= slow; i++)
            addCharger(s.id(), "S" + i, "SLOW", 7.4, slowRate, actor);
        audit.log(actor, "Added station " + name + " (" + fast + " fast, " + slow + " slow chargers)");
        return s;
    }

    public void updateStation(int id, String name, String area, String address, String status, String actor) {
        if (db.update("UPDATE stations SET name=?, area=?, address=?, status=? WHERE id=?",
                name.trim(), area, address, status, id) == 0) throw new Exceptions.NotFoundException("Station");
        audit.log(actor, "Updated station " + name);
    }

    public void deleteStation(int id, String actor) {
        Station s = byId(id);
        db.update("DELETE FROM chargers WHERE station_id=?", id);
        db.update("DELETE FROM stations WHERE id=?", id);
        audit.log(actor, "Deleted station " + s.name());
    }

    public Charger addCharger(int stationId, String code, String type, double powerKw, double rate, String actor) {
        Validators.requirePositive(powerKw, "Power");
        Validators.requirePositive(rate, "Rate");
        try {
            db.update("INSERT INTO chargers(station_id,code,type,power_kw,rate_per_kwh) VALUES(?,?,?,?,?)",
                    stationId, code, type, powerKw, rate);
        } catch (DuplicateKeyException e) {
            throw new Exceptions.DuplicateRecordException("Charger code " + code + " at this station");
        }
        audit.log(actor, "Added charger " + code + " (" + type + ") to station #" + stationId);
        return db.query("SELECT * FROM chargers WHERE station_id=? AND code=?", CHARGER, stationId, code).get(0);
    }

    public void setChargerStatus(int chargerId, String status, String actor) {
        if (db.update("UPDATE chargers SET status=? WHERE id=?", status, chargerId) == 0)
            throw new Exceptions.NotFoundException("Charger");
        audit.log(actor, "Set charger #" + chargerId + " to " + status);
    }

    public Station byId(int id) {
        List<Station> s = db.query("SELECT * FROM stations WHERE id=?", STATION, id);
        if (s.isEmpty()) throw new Exceptions.NotFoundException("Station");
        return s.get(0);
    }

    public Station byName(String name) {
        List<Station> s = db.query("SELECT * FROM stations WHERE name=?", STATION, name);
        if (s.isEmpty()) throw new Exceptions.NotFoundException("Station");
        return s.get(0);
    }

    public List<Station> all() { return db.query("SELECT * FROM stations ORDER BY name", STATION); }

    public List<Charger> chargersOf(int stationId) {
        return db.query("SELECT * FROM chargers WHERE station_id=? ORDER BY code", CHARGER, stationId);
    }

    public List<Charger> allChargers() { return db.query("SELECT * FROM chargers ORDER BY station_id, code", CHARGER); }

    public Charger chargerById(int id) {
        List<Charger> c = db.query("SELECT * FROM chargers WHERE id=?", CHARGER, id);
        if (c.isEmpty()) throw new Exceptions.NotFoundException("Charger");
        return c.get(0);
    }

    /** station id -> [free, occupied, faulty, maintenance] (Module IV: HashMap). */
    public Map<Integer, int[]> availability() {
        Map<Integer, int[]> map = new HashMap<>();
        db.query("SELECT station_id, status, COUNT(*) c FROM chargers GROUP BY station_id, status", (RowCallbackHandler) rs -> {
            int[] counts = map.computeIfAbsent(rs.getInt("station_id"), k -> new int[4]);
            switch (rs.getString("status")) {
                case "FREE" -> counts[0] = rs.getInt("c");
                case "OCCUPIED" -> counts[1] = rs.getInt("c");
                case "FAULTY" -> counts[2] = rs.getInt("c");
                case "MAINTENANCE" -> counts[3] = rs.getInt("c");
            }
        });
        return map;
    }

    public List<Charger> freeChargers(int stationId, String type) {
        return db.query("SELECT * FROM chargers WHERE station_id=? AND type=? AND status='FREE' ORDER BY code",
                CHARGER, stationId, type);
    }
}
