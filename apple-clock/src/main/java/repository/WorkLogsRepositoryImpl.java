package repository;

import model.WorkLogs;
import model.WorkType;
import util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class WorkLogsRepositoryImpl implements repository.WorkLogsRepository {

    private final EntityManager em = JpaUtil.getEntityManager();

    @Override
    public List<WorkLogs> findTop3OrderByBeginDesc() {
        TypedQuery<WorkLogs> query = em.createQuery(
                "SELECT wl FROM WorkLogs wl ORDER BY wl.begin DESC",
                WorkLogs.class
        );
        query.setMaxResults(3);
        return query.getResultList();
    }

    @Override
    public WorkLogs save(WorkLogs workLogs) {
        try {
            em.getTransaction().begin();

            if (workLogs.getId() == null) {
                em.persist(workLogs);  // 新纪录
            } else {
                workLogs = em.merge(workLogs); // 更新已有纪录
            }

            em.getTransaction().commit();

            System.out.println("[保存日志] ID=" + workLogs.getId()
                    + "，类型=" + (workLogs.getWorkType() != null ? workLogs.getWorkType().getName() : "未知")
                    + "，开始=" + workLogs.getBegin()
                    + "，结束=" + workLogs.getEnd()
                    + "，时长=" + workLogs.getDuration() + "秒");

            return workLogs;
        } catch (Exception e) {
            em.getTransaction().rollback(); // 保存失败时回滚事务
            System.err.println("保存日志失败：" + e.getMessage());
            throw e; // 抛出去给上层处理
        }
    }


    @Override
    public void delete(WorkLogs workLogs) {
        em.getTransaction().begin();
        if (!em.contains(workLogs)) {
            workLogs = em.merge(workLogs);
        }
        em.remove(workLogs);
        em.getTransaction().commit();
    }

    @Override
    public List<WorkLogs> findAll() {
        TypedQuery<WorkLogs> query = em.createQuery(
                "SELECT wl FROM WorkLogs wl",
                WorkLogs.class
        );
        return query.getResultList();
    }

    @Override
    public List<WorkLogs> findByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        TypedQuery<WorkLogs> query = em.createQuery(
                "SELECT wl FROM WorkLogs wl WHERE wl.begin >= :start AND wl.begin < :end",
                WorkLogs.class
        );
        query.setParameter("start", start);
        query.setParameter("end", end);

        return query.getResultList();
    }

    @Override
    public List<WorkLogs> findByWorkType(WorkType workType) {
        TypedQuery<WorkLogs> query = em.createQuery(
                "SELECT wl FROM WorkLogs wl WHERE wl.workType = :workType",
                WorkLogs.class
        );
        query.setParameter("workType", workType);
        return query.getResultList();
    }

    @Override
    public List<WorkLogs> findByDateRange(LocalDate begin, LocalDate end) {
        LocalDateTime beginDateTime = begin.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay(); // 包含end当天

        TypedQuery<WorkLogs> query = em.createQuery(
                "SELECT wl FROM WorkLogs wl WHERE wl.begin >= :start AND wl.begin < :end",
                WorkLogs.class
        );
        query.setParameter("start", beginDateTime);
        query.setParameter("end", endDateTime);

        return query.getResultList();
    }

    @Override
    public List<WorkLogs> findByDateAndWorkType(LocalDate date, List<WorkType> workTypes) {
        if (workTypes == null || workTypes.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end   = date.plusDays(1).atStartOfDay();

        TypedQuery<WorkLogs> query = em.createQuery(
                "SELECT wl " +
                        "FROM   WorkLogs wl " +
                        "WHERE  wl.begin >= :start " +
                        "  AND  wl.begin <  :end " +
                        "  AND  wl.workType IN :workTypes",   // ★ 用 IN
                WorkLogs.class
        );
        query.setParameter("start", start);
        query.setParameter("end",   end);
        query.setParameter("workTypes", workTypes);   // ★ 直接传 List

        return query.getResultList();
    }


    @Override
    public List<WorkLogs> findByDurationGreaterThanEqual(int durationMinutes) {
        TypedQuery<WorkLogs> query = em.createQuery(
                "SELECT wl FROM WorkLogs wl WHERE wl.duration >= :duration",
                WorkLogs.class
        );
        query.setParameter("duration", durationMinutes);
        return query.getResultList();
    }



    @Override
    public List<WorkLogs> findByDateRangeAndWorkType(LocalDate begin, LocalDate end,
                                                     List<WorkType> workTypes) {
        if (workTypes == null || workTypes.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime beginDateTime = begin.atStartOfDay();
        LocalDateTime endDateTime   = end.plusDays(1).atStartOfDay(); // 包含 end 当天

        TypedQuery<WorkLogs> query = em.createQuery(
                "SELECT wl " +
                        "FROM   WorkLogs wl " +
                        "WHERE  wl.begin >= :start " +
                        "  AND  wl.begin <  :end " +
                        "  AND  wl.workType IN :workTypes",
                WorkLogs.class
        );
        query.setParameter("start", beginDateTime);
        query.setParameter("end",   endDateTime);
        query.setParameter("workTypes", workTypes);

        return query.getResultList();
    }

    @Override
    public WorkLogs findById(Long id) {
        return em.find(WorkLogs.class, id);
    }

}
