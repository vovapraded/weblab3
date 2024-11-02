package backend.academy.weblab3.bean;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResetButtonBeanTest {
    @Test
 void  resetButton() {

     var res=new ResetButtonBean();
     res.reset();
 }
}