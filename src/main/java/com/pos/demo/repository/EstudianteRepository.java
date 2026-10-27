
package com.pos.demo.repository;

import com.pos.demo.config.HibernateUtil;
import com.pos.demo.model.Estudiante;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class EstudianteRepository {

    // Fase V: guardar con transacción y rollback
    public void guardar(Estudiante estudiante) {
        Transaction tx = null;

        try (Session session = HibernateUtil
                .getSessionFactory().openSession()) {

            tx = session.beginTransaction();

            // RF05: validar que el correo no esté repetido
            if (existePorCorreo(session, estudiante.getCorreo())) {
                throw new IllegalArgumentException(
                        "El correo ya está registrado");
            }

            session.persist(estudiante);
            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    // Fase V: listar todos los estudiantes
    public List<Estudiante> listar() {
        try (Session session = HibernateUtil
                .getSessionFactory().openSession()) {

            return session.createQuery(
                            "FROM Estudiante", Estudiante.class)
                    .getResultList();
        }
    }

    // Reto 2: verificar si ya existe un correo
    public boolean existePorCorreo(String correo) {
        try (Session session = HibernateUtil
                .getSessionFactory().openSession()) {

            return existePorCorreo(session, correo);
        }
    }

    private boolean existePorCorreo(
            Session session, String correo) {

        Long cantidad = session.createQuery(
                        "SELECT COUNT(e) FROM Estudiante e " +
                                "WHERE e.correo = :correo",
                        Long.class)
                .setParameter("correo", correo)
                .getSingleResult();

        return cantidad > 0;
    }

    // Reto 3: buscar estudiantes por carrera
    public List<Estudiante> listarPorCarrera(String carrera) {
        try (Session session = HibernateUtil
                .getSessionFactory().openSession()) {

            return session.createQuery(
                            "FROM Estudiante e " +
                                    "WHERE e.carrera = :carrera",
                            Estudiante.class)
                    .setParameter("carrera", carrera)
                    .getResultList();
        }
    }
}
