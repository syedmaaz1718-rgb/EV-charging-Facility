package com.abcgreen.evnet.web;

import com.abcgreen.evnet.config.WebConfig;
import com.abcgreen.evnet.model.Models.*;
import com.abcgreen.evnet.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Controller
public class CustomerController {
    private final StationService stations;
    private final BookingService bookings;
    private final SessionService sessions;

    public CustomerController(StationService stations, BookingService bookings, SessionService sessions) {
        this.stations = stations; this.bookings = bookings; this.sessions = sessions;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest req, Model m) {
        User u = WebConfig.currentUser(req);
        m.addAttribute("user", u);
        m.addAttribute("stations", stations.all());
        m.addAttribute("availability", stations.availability());
        m.addAttribute("vehicles", bookings.vehiclesOf(u.id()));
        m.addAttribute("myBookings", bookings.mine(u.id()));
        return "dashboard";
    }

    @PostMapping("/book/vehicle")
    public String addVehicle(@RequestParam String regNo, @RequestParam("model") String model_,
                             @RequestParam(defaultValue = "40") double batteryKwh,
                             HttpServletRequest req, RedirectAttributes ra) {
        User u = WebConfig.currentUser(req);
        try {
            bookings.addVehicle(u.id(), regNo, model_, batteryKwh, u.email());
            ra.addFlashAttribute("ok", "Vehicle added");
        } catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/dashboard";
    }

    @PostMapping("/book")
    public String book(@RequestParam int vehicleId, @RequestParam int stationId,
                       @RequestParam String chargerType, @RequestParam String slotTime,
                       HttpServletRequest req, RedirectAttributes ra) {
        User u = WebConfig.currentUser(req);
        try {
            Timestamp ts = Timestamp.valueOf(slotTime.replace("T", " ") + ":00");
            Booking b = bookings.book(u.id(), vehicleId, stationId, chargerType, ts, u.email());
            ra.addFlashAttribute("ok", "ALLOCATED".equals(b.status())
                    ? "Slot booked - charger allocated" : "All chargers busy - you are in the queue, est. wait " + b.estWaitMin() + " min");
        } catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/dashboard";
    }

    @PostMapping("/book/{id}/cancel")
    public String cancel(@PathVariable int id, HttpServletRequest req, RedirectAttributes ra) {
        User u = WebConfig.currentUser(req);
        try {
            bookings.cancel(id, u.id(), u.isAdmin(), u.email());
            ra.addFlashAttribute("ok", "Booking cancelled");
        } catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/dashboard";
    }

    @PostMapping("/session/start/{bookingId}")
    public String start(@PathVariable int bookingId, HttpServletRequest req, RedirectAttributes ra) {
        User u = WebConfig.currentUser(req);
        try {
            sessions.start(bookingId, u.id(), u.isAdmin(), u.email());
            ra.addFlashAttribute("ok", "Charging started");
        } catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/dashboard";
    }

    @PostMapping("/session/stop/{sessionId}")
    public String stop(@PathVariable int sessionId, HttpServletRequest req, RedirectAttributes ra) {
        User u = WebConfig.currentUser(req);
        try {
            Session s = sessions.stop(sessionId, u.id(), u.isAdmin(), u.email());
            ra.addFlashAttribute("ok", "Session complete - " + s.energyKwh() + " kWh, Rs " + s.amount());
        } catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/dashboard";
    }

    @PostMapping("/invoice/pay/{id}")
    public String pay(@PathVariable int id, HttpServletRequest req, RedirectAttributes ra) {
        User u = WebConfig.currentUser(req);
        try {
            sessions.payInvoice(id, u.id(), u.isAdmin(), u.email());
            ra.addFlashAttribute("ok", "Invoice marked paid");
        } catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/history";
    }

    @GetMapping("/history")
    public String history(HttpServletRequest req, Model m) {
        User u = WebConfig.currentUser(req);
        m.addAttribute("user", u);
        m.addAttribute("rows", sessions.historyFor(u.id()));
        return "history";
    }

    @GetMapping("/invoice/{id}")
    public String invoice(@PathVariable int id, HttpServletRequest req, Model m) {
        User u = WebConfig.currentUser(req);
        Invoice inv = sessions.invoiceById(id);
        Session s = sessions.byId(inv.sessionId());
        Booking b = bookings.byId(s.bookingId());
        if (!u.isAdmin() && b.userId() != u.id()) return "redirect:/history";
        m.addAttribute("user", u);
        m.addAttribute("inv", inv);
        m.addAttribute("s", s);
        m.addAttribute("b", b);
        m.addAttribute("station", stations.byId(b.stationId()));
        m.addAttribute("charger", stations.chargerById(s.chargerId()));
        m.addAttribute("customer", b.userId() == u.id() ? u : null);
        return "invoice";
    }
}
