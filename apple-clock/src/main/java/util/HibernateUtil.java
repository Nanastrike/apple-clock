package util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import java.sql.Connection;

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

    public static void printCurrentDatabaseURL() {
        try (Session session = sessionFactory.openSession()) {
            session.doWork(connection -> {
                try {
                    String url = connection.getMetaData().getURL();
                    System.out.println("🔍 当前数据库连接地址：" + url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
