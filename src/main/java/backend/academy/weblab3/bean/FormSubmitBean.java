package backend.academy.weblab3.bean;

import backend.academy.weblab3.Checker;
import backend.academy.weblab3.Validator;
import backend.academy.weblab3.hibernate.Point;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.Locale;

@Named("formSubmitBean")  // Имя бина
@SessionScoped
@Getter
@Setter
public class FormSubmitBean implements Serializable {

    @Inject
    private XCoordinateBean xBean;

    @Inject
    private YCoordinateBean yBean;

    @Inject
    private RBean rBean;

    @Inject
    private ErrorBean errorBean;

    @Inject
    private PointSaverBean pointSaverBean;


    private final Validator validator = new Validator();
    private final Checker checker = new Checker();

    public String submit() {
        String xStr = xBean.getX();
        double y = yBean.getY();
        double r = rBean.getR();

        var errors = validator.validate(xStr,y,r);
        // Логика при отправке формы, например, обработка выбранного значения
        if (errors.isEmpty()) {
            errorBean.setErrors("");
            var x = Double.parseDouble(xStr);
            pointSaverBean.savePointFromForm(x,y,r);
        }else{
            errorBean.setErrors(errors);
        }



        return null;
    }


}