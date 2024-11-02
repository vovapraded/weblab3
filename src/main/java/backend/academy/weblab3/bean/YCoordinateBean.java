package backend.academy.weblab3.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Named("yBean")  // Имя бина
@SessionScoped
@Getter
@Setter
public class YCoordinateBean implements Serializable {
    private double y;

}
