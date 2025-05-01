package backend.academy.weblab3.bean.metric;

import jakarta.enterprise.context.ApplicationScoped;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class Area extends NotificationBroadcasterSupport implements AreaMBean {
    private final List<Point> convexHull = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public void addPoint(double x, double y) {
        List<Point> allPoints = new ArrayList<>(convexHull);
        allPoints.add(new Point(x, y));
        List<Point> newHull = buildConvexHull(allPoints);
        convexHull.clear();
        convexHull.addAll(newHull);

        sendNotification(new Notification(
                "point.added",
                this.getClass().getSimpleName(),
                sequence.incrementAndGet(),
                System.currentTimeMillis(),
                "Added point: (" + x + ", " + y + ")"
        ));
    }

    @Override
    public void clearPoints() {
        convexHull.clear();
        sendNotification(new Notification(
                "points.cleared",
                this.getClass().getSimpleName(),
                sequence.incrementAndGet(),
                System.currentTimeMillis(),
                "Cleared all points"
        ));
    }

    @Override
    public double getArea() {
        if (convexHull.size() < 3) return 0.0;

        double area = 0.0;
        int n = convexHull.size();
        for (int i = 0; i < n; i++) {
            Point a = convexHull.get(i);
            Point b = convexHull.get((i + 1) % n);
            area += (a.x * b.y - b.x * a.y);
        }
        return Math.abs(area) / 2.0;
    }

    @Override
    public List<String> getPoints() {
        List<String> result = new ArrayList<>();
        for (Point p : convexHull) {
            result.add("(" + p.x + ", " + p.y + ")");
        }
        return result;
    }

    // --- Вспомогательные методы ---

    private List<Point> buildConvexHull(List<Point> points) {
        if (points.size() <= 1) return new ArrayList<>(points);

        points.sort(Comparator.comparingDouble((Point p) -> p.x).thenComparingDouble(p -> p.y));
        List<Point> lower = new ArrayList<>();
        for (Point p : points) {
            while (lower.size() >= 2 && cross(lower.get(lower.size()-2), lower.get(lower.size()-1), p) <= 0) {
                lower.remove(lower.size() - 1);
            }
            lower.add(p);
        }

        List<Point> upper = new ArrayList<>();
        for (int i = points.size() - 1; i >= 0; i--) {
            Point p = points.get(i);
            while (upper.size() >= 2 && cross(upper.get(upper.size()-2), upper.get(upper.size()-1), p) <= 0) {
                upper.remove(upper.size() - 1);
            }
            upper.add(p);
        }

        lower.remove(lower.size() - 1);
        upper.remove(upper.size() - 1);
        lower.addAll(upper);
        return lower;
    }

    private double cross(Point a, Point b, Point c) {
        return (b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x);
    }

    private record Point(double x, double y) implements Serializable {
    }
}
