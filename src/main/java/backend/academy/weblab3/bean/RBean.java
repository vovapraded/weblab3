package backend.academy.weblab3.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Named("rBean")  // Имя бина
@SessionScoped
@Getter
@Setter
public class RBean implements Serializable {
    private double r = 1.0;
}
