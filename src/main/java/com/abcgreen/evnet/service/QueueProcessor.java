package com.abcgreen.evnet.service;

import com.abcgreen.evnet.model.Models.Booking;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;

/** FR-06: background thread that keeps the queue moving (Module III: multithreading). */
@Component
@org.springframework.context.annotation.DependsOn("schemaInitializer")
public class QueueProcessor implements Runnable {
    private final BookingService bookings;
    private final Thread thread = new Thread(this, "queue-processor");
    private volatile boolean running = true;

    public QueueProcessor(BookingService bookings) {
        this.bookings = bookings;
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        while (running) {
            try {
                List<Booking> queued = bookings.allQueued();
                for (Booking b : queued)
                    bookings.tryAllocateNext(b.stationId(), b.chargerType());
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            } catch (Exception e) {
                System.err.println("Queue processor error: " + e.getMessage());
            }
        }
    }

    @PreDestroy
    public void stop() { running = false; thread.interrupt(); }
}
