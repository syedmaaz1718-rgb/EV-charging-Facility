package com.abcgreen.evnet.web;

import com.abcgreen.evnet.model.Models.Station;
import com.abcgreen.evnet.service.StationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class PublicApiController {
    private final StationService stations;
    public PublicApiController(StationService stations) { this.stations = stations; }

    /** Public live availability for the landing page (no auth). */
    @GetMapping("/api/public/stations")
    public List<Map<String, Object>> stations() {
        Map<Integer, int[]> av = stations.availability();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Station s : stations.all()) {
            int fast = 0, slow = 0;
            var counts = av.getOrDefault(s.id(), new int[4]);
            for (var c : stations.chargersOf(s.id())) {
                if ("FAST".equals(c.type())) fast++; else slow++;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", s.name());
            m.put("area", s.area());
            m.put("free", counts[0]);
            m.put("total", counts[0] + counts[1] + counts[2] + counts[3]);
            m.put("fast", fast);
            m.put("slow", slow);
            out.add(m);
        }
        return out;
    }
}
