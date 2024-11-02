package backend.academy.weblab3.bean;

import backend.academy.weblab3.hibernate.HibernateUtil;
import backend.academy.weblab3.hibernate.Point;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.List;

@Named("tableBean")  // Имя бина
@ViewScoped
@Getter
@Setter
public class TableBean implements Serializable {
    private List<Point> records;

    @PostConstruct
    public void updateTable() {
        @Cleanup
        var session= HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Query<Point> query = session.createQuery("FROM Point ORDER BY id DESC", Point.class);
        query.setMaxResults(10); // Устанавливаем лимит на 10 записей
        records = query.list();
        System.out.println(records);

        session.getTransaction().commit();
    }

}
