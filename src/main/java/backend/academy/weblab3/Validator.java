package backend.academy.weblab3;

public class Validator {
    public String validate(String xStr, double y, double r){
        StringBuilder sb = new StringBuilder();
        Double x = null;
        try{
            x = Double.parseDouble(xStr);
        }catch(NumberFormatException e){
            sb.append("X должен быть числом от -3 до 5\n");
        }
        if (x!=null && (x<-3 || x>5)) {
            sb.append("X должен быть числом от -3 до 5\n");
        }
        if (y>1 || y<-2 || y%0.5 != 0){
            sb.append("Невалидное значение y\n");
        }
        if (r<1 || r>4 || r%0.25 != 0){
            sb.append("Невалидное значение R\n");
        }
        return sb.toString();

    }
}
