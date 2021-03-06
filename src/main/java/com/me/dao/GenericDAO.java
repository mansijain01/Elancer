package com.me.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Query;

public interface GenericDAO<T, ID extends Serializable> { 
    public void save(T entity) throws Exception; 
    public void merge(T entity); 
    public void delete(T entity); 
    public List<T> findMany(Query query); 
    public T findOne(Query query); 
    public List findAll(Class clazz); 
    public T findByID(Class clazz, BigDecimal id);
}
