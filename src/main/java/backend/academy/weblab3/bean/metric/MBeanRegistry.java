package backend.academy.weblab3.bean.metric;

import jakarta.servlet.ServletContextListener;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class MBeanRegistry implements ServletContextListener {
    private static final Map<Class<?>, ObjectName> beans = new HashMap<>();

    public static <T> void registerBean(T bean, Class<T> mbeanInterface, String name) {
        try {
            var domain = mbeanInterface.getPackageName();
            var type = mbeanInterface.getSimpleName().replace("MBean", "");
            var objectName = new ObjectName(String.format("%s:type=%s,name=%s", domain, type, name));

            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

            if (mbs.isRegistered(objectName)) {
                mbs.unregisterMBean(objectName); // удаляем, если уже есть
            }

            mbs.registerMBean(bean, objectName);

            beans.put(mbeanInterface, objectName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static void unregisterBean(Object bean) {
        if (!beans.containsKey(bean.getClass())) {
            throw new IllegalArgumentException("Specified bean is not registered.");
        }

        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(beans.get(bean.getClass()));
        } catch (InstanceNotFoundException | MBeanRegistrationException ex) {
            ex.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> T getBean(Class<T> beanClass) {
        ObjectName objectName = beans.get(beanClass);
        if (objectName == null) {
            throw new IllegalStateException("Bean of class " + beanClass.getName() + " is not registered.");
        }

        return (T) ManagementFactory.getPlatformMBeanServer().getObjectInstance(objectName);
    }
}