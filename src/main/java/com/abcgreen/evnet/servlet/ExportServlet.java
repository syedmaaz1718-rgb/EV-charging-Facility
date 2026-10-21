package com.abcgreen.evnet.servlet;

import com.abcgreen.evnet.config.WebConfig;
import com.abcgreen.evnet.model.Models.User;
import com.abcgreen.evnet.service.ReportService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/** Raw servlet for CSV export + DB backup (Module V: Servlets, HTTP response). */
@WebServlet(name = "exportServlet", urlPatterns = {"/export/sessions.csv", "/admin/backup.sql"})
public class ExportServlet extends HttpServlet {
    private final ReportService reports;

    public ExportServlet(ReportService reports) { this.reports = reports; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        User u = WebConfig.currentUser(req);
        if (u == null) { res.sendRedirect("/login"); return; }
        String path = req.getRequestURI();
        if (path.endsWith("sessions.csv")) {
            res.setContentType("text/csv");
            res.setHeader("Content-Disposition", "attachment; filename=sessions-" + LocalDate.now() + ".csv");
            res.getWriter().write(reports.sessionsCsv());
        } else {
            if (!u.isAdmin()) { res.sendError(403); return; }
            res.setContentType("application/sql");
            res.setHeader("Content-Disposition", "attachment; filename=evnet-backup-" + LocalDate.now() + ".sql");
            res.getWriter().write(reports.backupSql());
        }
    }
}
