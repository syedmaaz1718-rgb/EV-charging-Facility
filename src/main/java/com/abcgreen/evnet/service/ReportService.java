package com.abcgreen.evnet.service;

import com.abcgreen.evnet.util.CsvUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

import java.util.*;

/** FR-08, FR-09, FR-11, FR-12: dashboards, demand, reports, CSV, backup/restore. */
@Service
public class ReportService {
    private final JdbcTemplate db;

    public ReportService(JdbcTemplate db) { this.db = db; }

    public Map<String, Object> dashboardStats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("activeSessions", db.queryForObject("SELECT COUNT(*) FROM sessions WHERE status='RUNNING'", Integer.class));
        m.put("chargersTotal", db.queryForObject("SELECT COUNT(*) FROM chargers", Integer.class));
        m.put("chargersFree", db.queryForObject("SELECT COUNT(*) FROM chargers WHERE status='FREE'", Integer.class));
        m.put("chargersFaulty", db.queryForObject("SELECT COUNT(*) FROM chargers WHERE status='FAULTY'", Integer.class));
        m.put("chargersMaint", db.queryForObject("SELECT COUNT(*) FROM chargers WHERE status='MAINTENANCE'", Integer.class));
        m.put("inQueue", db.queryForObject("SELECT COUNT(*) FROM bookings WHERE status='QUEUED'", Integer.class));
        m.put("revenueToday", db.queryForObject(
                "SELECT COALESCE(SUM(amount),0) FROM invoices WHERE DATE(issued_at)=CURRENT_DATE", Double.class));
        m.put("customers", db.queryForObject("SELECT COUNT(*) FROM users WHERE role='CUSTOMER'", Integer.class));
        return m;
    }

    /** kWh per hour of day for the last 24h of completed sessions: 24 values for the bar chart. */
    public List<Double> demandByHour() {
        Map<Integer, Double> byHour = new HashMap<>();
        db.query("SELECT HOUR(start_time) h, SUM(energy_kwh) e FROM sessions WHERE status='COMPLETED' GROUP BY HOUR(start_time)", (RowCallbackHandler) rs -> byHour.put(rs.getInt("h"), rs.getDouble("e")));
        List<Double> out = new ArrayList<>(24);
        for (int h = 0; h < 24; h++) out.add(byHour.getOrDefault(h, 0.0));
        return out;
    }

    public List<Map<String, Object>> stationUtilization() {
        return db.queryForList("""
            SELECT st.name,
                   COUNT(s.id) sessions,
                   COALESCE(SUM(s.energy_kwh),0) energy,
                   COALESCE(SUM(s.amount),0) revenue
            FROM stations st
            LEFT JOIN bookings b ON b.station_id = st.id
            LEFT JOIN sessions s ON s.booking_id = b.id AND s.status='COMPLETED'
            GROUP BY st.id, st.name ORDER BY energy DESC""");
    }

    public List<Map<String, Object>> monthlyDemand() {
        return db.queryForList("""
            SELECT CONCAT(YEAR(start_time),'-',LPAD(MONTH(start_time),2,'0')) ym,
                   COUNT(*) sessions, SUM(energy_kwh) energy, SUM(amount) revenue
            FROM sessions WHERE status='COMPLETED'
            GROUP BY CONCAT(YEAR(start_time),'-',LPAD(MONTH(start_time),2,'0')) ORDER BY ym""");
    }

    public List<Map<String, Object>> dailyReport() {
        return db.queryForList("""
            SELECT CAST(start_time AS DATE) report_day, COUNT(*) sessions,
                   COALESCE(SUM(energy_kwh),0) energy, COALESCE(SUM(amount),0) revenue
            FROM sessions WHERE status='COMPLETED'
            GROUP BY CAST(start_time AS DATE) ORDER BY report_day DESC LIMIT 31""");
    }

    public String sessionsCsv() {
        List<Map<String, Object>> rows = db.queryForList("""
            SELECT s.id, st.name station, c.code charger, c.type, s.start_time, s.end_time,
                   s.energy_kwh, s.amount, s.status
            FROM sessions s JOIN bookings b ON b.id=s.booking_id
            JOIN stations st ON st.id=b.station_id JOIN chargers c ON c.id=s.charger_id
            ORDER BY s.id DESC""");
        List<String[]> data = rows.stream().map(r -> new String[]{
                str(r.get("id")), str(r.get("station")), str(r.get("charger")), str(r.get("type")),
                str(r.get("start_time")), str(r.get("end_time")), str(r.get("energy_kwh")),
                str(r.get("amount")), str(r.get("status"))}).toList();
        return CsvUtil.toCsv(new String[]{"id","station","charger","type","start","end","energy_kwh","amount","status"}, data);
    }

    /** Logical SQL backup generated through JDBC (FR-12). */
    public String backupSql() {
        StringBuilder sb = new StringBuilder("-- EV ChargeNet backup\n");
        for (String table : List.of("users","vehicles","stations","chargers","bookings","sessions","invoices","maintenance","audit_log")) {
            List<Map<String, Object>> rows = db.queryForList("SELECT * FROM " + table);
            for (Map<String, Object> row : rows) {
                StringJoiner cols = new StringJoiner(",");
                StringJoiner vals = new StringJoiner(",");
                row.forEach((k, v) -> {
                    cols.add(k);
                    vals.add(v == null ? "NULL" : "'" + v.toString().replace("'", "''") + "'");
                });
                sb.append("INSERT INTO ").append(table).append(" (").append(cols).append(") VALUES (").append(vals).append(");\n");
            }
        }
        return sb.toString();
    }

    private static String str(Object o) { return o == null ? "" : o.toString(); }
}
