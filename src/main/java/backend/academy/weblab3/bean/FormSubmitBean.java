package backend.academy.weblab3.bean;

import backend.academy.weblab3.Checker;
import backend.academy.weblab3.Validator1;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

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


    private final Validator1 validator1 = new Validator1();
    private final Checker checker = new Checker();

    public String submit() {
        String xStr = xBean.getX();
        double y = yBean.getY();
        double r = rBean.getR();

        var errors = validator1.validate(xStr,y,r);
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