package repository;

import model.WorkType;
import repository.WorkTypeRepository;
import util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class WorkTypeRepositoryImpl implements WorkTypeRepository {

    @Override
    public WorkType save(WorkType workType) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.saveOrUpdate(workType); // 新增或更新
            tx.commit();
            return workType;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<WorkType> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM WorkType", WorkType.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteById(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            WorkType workType = session.get(WorkType.class, id);
            if (workType != null) {
                session.delete(workType);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    @Override
    public WorkType findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(WorkType.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
