package backend.academy.weblab3.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Named("xBean")  // Имя бина
@SessionScoped
@Getter
@Setter
public class XCoordinateBean implements Serializable {
    private String x;

}
