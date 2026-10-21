package com.abcgreen.evnet.web;

import com.abcgreen.evnet.config.WebConfig;
import com.abcgreen.evnet.model.Models.User;
import com.abcgreen.evnet.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final StationService stations;
    private final BookingService bookings;
    private final SessionService sessions;
    private final ReportService reports;
    private final AuditService audit;
    private final AuthService auth;

    public AdminController(StationService stations, BookingService bookings, SessionService sessions,
                           ReportService reports, AuditService audit, AuthService auth) {
        this.stations = stations; this.bookings = bookings; this.sessions = sessions;
        this.reports = reports; this.audit = audit; this.auth = auth;
    }

    @GetMapping
    public String dashboard(HttpServletRequest req, Model m) {
        m.addAttribute("user", WebConfig.currentUser(req));
        m.addAttribute("stats", reports.dashboardStats());
        var byHour = reports.demandByHour();
        double max = byHour.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        m.addAttribute("demandByHour", byHour);
        m.addAttribute("maxDemand", max);
        m.addAttribute("utilization", reports.stationUtilization());
        m.addAttribute("queue", bookings.allQueued());
        m.addAttribute("running", sessions.running());
        m.addAttribute("audit", audit.recent(10));
        return "admin/dashboard";
    }

    @GetMapping("/stations")
    public String stationsPage(HttpServletRequest req, Model m) {
        m.addAttribute("user", WebConfig.currentUser(req));
        m.addAttribute("stations", stations.all());
        m.addAttribute("availability", stations.availability());
        m.addAttribute("chargers", stations.allChargers());
        return "admin/stations";
    }

    @PostMapping("/stations")
    public String addStation(@RequestParam String name, @RequestParam String area,
                             @RequestParam(required = false) String address,
                             @RequestParam(defaultValue = "0") int fast, @RequestParam(defaultValue = "0") int slow,
                             @RequestParam(defaultValue = "18") double fastRate,
                             @RequestParam(defaultValue = "9") double slowRate,
                             HttpServletRequest req, RedirectAttributes ra) {
        User u = WebConfig.currentUser(req);
        try {
            stations.addStation(name, area, address, fast, slow, fastRate, slowRate, u.email());
            ra.addFlashAttribute("ok", "Station added");
        } catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/stations";
    }

    @PostMapping("/stations/{id}/update")
    public String updateStation(@PathVariable int id, @RequestParam String name, @RequestParam String area,
                                @RequestParam(required = false) String address,
                                @RequestParam(defaultValue = "ONLINE") String status,
                                HttpServletRequest req, RedirectAttributes ra) {
        User u = WebConfig.currentUser(req);
        try {
            stations.updateStation(id, name, area, address, status, u.email());
            ra.addFlashAttribute("ok", "Station updated");
        } catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/stations";
    }

    @PostMapping("/stations/{id}/delete")
    public String deleteStation(@PathVariable int id, HttpServletRequest req, RedirectAttributes ra) {
        User u = WebConfig.currentUser(req);
        try {
            stations.deleteStation(id, u.email());
            ra.addFlashAttribute("ok", "Station deleted");
        } catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/stations";
    }

    @PostMapping("/chargers")
    public String addCharger(@RequestParam int stationId, @RequestParam String code,
                             @RequestParam String type, @RequestParam double powerKw,
                             @RequestParam double rate, HttpServletRequest req, RedirectAttributes ra) {
        User u = WebConfig.currentUser(req);
        try {
            stations.addCharger(stationId, code, type, powerKw, rate, u.email());
            ra.addFlashAttribute("ok", "Charger added");
        } catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/stations";
    }

    @PostMapping("/chargers/{id}/status")
    public String chargerStatus(@PathVariable int id, @RequestParam String status,
                                HttpServletRequest req, RedirectAttributes ra) {
        User u = WebConfig.currentUser(req);
        try {
            stations.setChargerStatus(id, status, u.email());
            ra.addFlashAttribute("ok", "Charger updated");
        } catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/stations";
    }

    @GetMapping("/customers")
    public String customers(HttpServletRequest req, Model m) {
        m.addAttribute("user", WebConfig.currentUser(req));
        m.addAttribute("customers", auth.customers());
        return "admin/customers";
    }

    @GetMapping("/reports")
    public String reportsPage(HttpServletRequest req, Model m) {
        m.addAttribute("user", WebConfig.currentUser(req));
        m.addAttribute("daily", reports.dailyReport());
        m.addAttribute("monthly", reports.monthlyDemand());
        m.addAttribute("utilization", reports.stationUtilization());
        return "admin/reports";
    }
}
