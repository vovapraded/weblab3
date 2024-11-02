package backend.academy.weblab3.bean;

import backend.academy.weblab3.hibernate.HibernateUtil;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PF;
import org.primefaces.PrimeFaces;

import java.io.Serializable;

@Named("resetBean")  // Имя бина
@SessionScoped
@Getter
@Setter
public class ResetButtonBean implements Serializable {
    public void reset() {
        @Cleanup
       var session=  HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.createNativeQuery("TRUNCATE TABLE point").executeUpdate();
        session.getTransaction().commit();
        PrimeFaces.current().executeScript("updatePlot()");
        PF.current().executeScript("document.getElementById(\"tableUpdater\").click()");



    }
}
