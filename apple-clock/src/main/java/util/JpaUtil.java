package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("apple-clock-unit");

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
