package com.pos.demo.config;

// TODO (Nazario): descomentar cuando exista la entidad Estudiante
// import edu.gestionacademica.model.Estudiante;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

public final class HibernateUtil {

    private static final SessionFactory FACTORY = crear();

    private HibernateUtil() {}

    private static SessionFactory crear() {
        try (var in = HibernateUtil.class.getClassLoader()
                .getResourceAsStream("hibernate.properties")) {

            if (in == null) throw new IllegalStateException("Falta configuración");

            Properties p = new Properties();
            p.load(in);

            return new Configuration().addProperties(p)
                    // TODO (Nazario): descomentar la siguiente línea
                    // .addAnnotatedClass(Estudiante.class)
                    .buildSessionFactory();

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() { return FACTORY; }
}