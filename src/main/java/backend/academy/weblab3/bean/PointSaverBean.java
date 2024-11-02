package backend.academy.weblab3.bean;

import backend.academy.weblab3.Checker;
import backend.academy.weblab3.hibernate.HibernateUtil;
import backend.academy.weblab3.hibernate.Point;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PF;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.Locale;

@Named("pointSaverBean")  // Имя бина
@ApplicationScoped
@Getter
@Setter
public class PointSaverBean implements Serializable {
    private final Checker checker = new Checker();
    @Inject
    private TableBean tableBean;

    public void savePointFromForm(double x,double y,double r){
       var gotIt=savePoint(x, y,r);
       draw(x,y,r,gotIt);
    }
    private boolean savePoint(double x,double y,double r) {

        var gotIt = checker.check(x,y,r);
        @Cleanup
        var session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.persist(Point.builder().x(x).y(y).r(r).gotIt(gotIt).build());
        session.getTransaction().commit();
        session.close();
        PF.current().executeScript("document.getElementById(\"tableUpdater\").click()");
        return gotIt;
    }
    public void savePointFromJS(){
        String xStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("x");
        String yStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("y");
        String rStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("r");
        try {
            double x = Double.parseDouble(xStr);
            double y = Double.parseDouble(yStr);
            double r = Double.parseDouble(rStr);

            var gotIt=savePoint(x,y,r);
            draw(x,y,r,gotIt);
        }catch (NumberFormatException e){
        }
    }
    void draw( double x,double y,double r,boolean gotIt){
        var label = gotIt ? "got" : "away";
        var query = "createPoint(%.2f, %.2f,\"point-%s\",%.2f)";

        String formatted = String.format(Locale.ENGLISH,query, x, y, label, r);
        System.out.println(formatted);
        PrimeFaces.current().executeScript(formatted);

    }
}
