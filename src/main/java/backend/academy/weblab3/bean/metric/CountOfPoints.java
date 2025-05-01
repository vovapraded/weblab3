package backend.academy.weblab3.bean.metric;

import jakarta.enterprise.context.ApplicationScoped;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class CountOfPoints extends NotificationBroadcasterSupport implements CountOfPointsMBean, NotificationEmitter, Serializable {
    private final AtomicInteger countOfPoints = new AtomicInteger();
    private final AtomicInteger countOfHitPoints = new AtomicInteger();

    private final AtomicLong sequenceNumber = new AtomicLong(1);

    public void increment(boolean gotIt) {
        int current = countOfPoints.incrementAndGet();
        if (gotIt) {
            countOfHitPoints.incrementAndGet();
        }
        if (current % 5 == 0) {
            Notification notification = new Notification(
                    "count.updated",
                    this.getClass().getSimpleName(),
                    sequenceNumber.getAndIncrement(),
                    System.currentTimeMillis(),
                    "Количество точек достигло " + current
            );
            sendNotification(notification);

        }
    }

    @Override
    public int getCountOfHitPoints() {
        return countOfHitPoints.get();
    }

    @Override
    public int getCommonCountOfPoints() {
        return countOfPoints.get();
    }

    @Override
    public void reset() {
        countOfPoints.set(0);
        countOfHitPoints.set(0);
    }
}
