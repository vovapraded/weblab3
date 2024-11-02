package backend.academy.weblab3.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Named("errorBean")  // Имя бина
@SessionScoped
@Getter
@Setter
public class ErrorBean implements Serializable {
    private String errors;
}
