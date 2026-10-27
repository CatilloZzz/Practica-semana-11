package com.pos.demo.config;


public class Prueba {
    public static void main(String[] args) {
        HibernateUtil.getSessionFactory();
        System.out.println("Conexion OK");
        System.exit(0);
    }
}