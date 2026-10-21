package com.abcgreen.evnet.service;

import com.abcgreen.evnet.model.Models.User;
import static com.abcgreen.evnet.model.Models.USER;
import com.abcgreen.evnet.util.Validators;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

/** FR-01: authentication, roles, admin bootstrap. */
@Service
public class AuthService {
    private final JdbcTemplate db;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final AuditService audit;

    public AuthService(JdbcTemplate db, AuditService audit) {
        this.db = db; this.audit = audit;
    }

    public synchronized void bootstrapAdmin() {
        Integer count = db.queryForObject("SELECT COUNT(*) FROM users WHERE role='ADMIN'", Integer.class);
        if (count != null && count > 0) return;
        String pw = System.getenv("ADMIN_PASSWORD");
        if (pw == null || pw.isBlank()) {
            pw = generatePassword();
            System.out.println("=== EV ChargeNet: generated admin password (set ADMIN_PASSWORD to override): " + pw);
        }
        db.update("INSERT INTO users(name,email,phone,password_hash,role) VALUES(?,?,?,?,?)",
                "Administrator", "admin@abcgreen.in", "", encoder.encode(pw), "ADMIN");
        System.out.println("=== EV ChargeNet: admin account ready (admin@abcgreen.in)");
    }

    private static String generatePassword() {
        String chars = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }

    public User register(String name, String email, String phone, String password) {
        Validators.requireNonBlank(name, "Name");
        Validators.requireEmail(email);
        Validators.requirePassword(password);
        try {
            db.update("INSERT INTO users(name,email,phone,password_hash,role) VALUES(?,?,?,?,?)",
                    name.trim(), email.trim().toLowerCase(), phone == null ? "" : phone.trim(),
                    encoder.encode(password), "CUSTOMER");
        } catch (DuplicateKeyException e) {
            throw new Exceptions.DuplicateRecordException("An account with this email");
        }
        audit.log(email, "Registered customer account");
        return login(email, password);
    }

    public User login(String email, String password) {
        List<User> users = db.query("SELECT * FROM users WHERE email=?", USER, email.trim().toLowerCase());
        if (users.isEmpty() || !encoder.matches(password, users.get(0).passwordHash()))
            throw new Exceptions.AuthException("Invalid email or password");
        return users.get(0);
    }

    public User byId(int id) {
        List<User> users = db.query("SELECT * FROM users WHERE id=?", USER, id);
        if (users.isEmpty()) throw new Exceptions.NotFoundException("User");
        return users.get(0);
    }

    public List<User> customers() {
        return db.query("SELECT * FROM users WHERE role='CUSTOMER' ORDER BY created_at DESC", USER);
    }
}
