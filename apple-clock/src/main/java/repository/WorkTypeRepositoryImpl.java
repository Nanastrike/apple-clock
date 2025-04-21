package repository;

import jakarta.persistence.EntityManager;
import model.WorkType;
import util.JpaUtil;

import java.util.List;

public class WorkTypeRepositoryImpl implements WorkTypeRepository {

    /* ★ 用同一个 EntityManager，这里千万不要再用 HibernateUtil */
    private final EntityManager em = JpaUtil.getEntityManager();

    @Override
    public WorkType save(WorkType wt) {
        em.getTransaction().begin();
        if (wt.getId() == null) {
            em.persist(wt);           // 新增
        } else {
            wt = em.merge(wt);        // 更新
        }
        em.getTransaction().commit();
        return wt;
    }

    @Override
    public List<WorkType> findAll() {
        return em.createQuery("FROM WorkType", WorkType.class).getResultList();
    }

    @Override
    public void deleteById(Long id) {
        em.getTransaction().begin();
        WorkType wt = em.find(WorkType.class, id);
        if (wt != null) em.remove(wt);
        em.getTransaction().commit();
    }

    @Override
    public WorkType findById(Long id) {
        return em.find(WorkType.class, id);
    }
}
