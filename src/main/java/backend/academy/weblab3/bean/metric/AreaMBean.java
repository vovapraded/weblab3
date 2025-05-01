package backend.academy.weblab3.bean.metric;

import java.util.List;

public interface AreaMBean {
    void addPoint(double x, double y);
    void clearPoints();
    double getArea();
    List<String> getPoints(); // для отображения в VisualVM
}
