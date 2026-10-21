package com.abcgreen.evnet.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** Audit trail (NFR: transaction logs). */
@Service
public class AuditService {
    private final JdbcTemplate db;
    public AuditService(JdbcTemplate db) { this.db = db; }

    public void log(String actor, String action) {
        db.update("INSERT INTO audit_log(actor, action) VALUES(?,?)", actor, action);
    }

    public List<Map<String, Object>> recent(int limit) {
        return db.queryForList("SELECT actor, action, created_at FROM audit_log ORDER BY id DESC LIMIT ?", limit);
    }
}
