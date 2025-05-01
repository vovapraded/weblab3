package backend.academy.weblab3.bean.metric;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.io.Serializable;

@ApplicationScoped
public class MBeanBootstrap implements Serializable {

    @Inject
    CountOfPoints countOfPoints;
    @Inject Area area;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        MBeanRegistry.registerBean(countOfPoints, CountOfPointsMBean.class, "CountOfPoints");
        MBeanRegistry.registerBean(area, AreaMBean.class, "Area");
    }
}
