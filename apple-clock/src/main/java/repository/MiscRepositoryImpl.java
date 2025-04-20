package repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.Misc;
import util.JpaUtil;

import java.util.List;

public class MiscRepositoryImpl implements MiscRepository {

    private final EntityManager entityManager = JpaUtil.getEntityManager();

    @Override
    public Misc save(Misc misc) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Misc saved = entityManager.merge(misc);
            transaction.commit();
            return saved;
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException("保存 Misc 失败", e);
        }
    }

    @Override
    public List<Misc> findAll() {
        TypedQuery<Misc> query = entityManager.createQuery("SELECT m FROM Misc m", Misc.class);
        return query.getResultList();
    }

    @Override
    public Misc findById(long id) {
        return entityManager.find(Misc.class, id);
    }
}
