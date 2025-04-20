package util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // 创建 Hibernate 的 SessionFactory
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("初始SessionFactory创建失败：" + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        // 关闭缓存和连接池
        getSessionFactory().close();
    }
}
