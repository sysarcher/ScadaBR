package com.serotonin.mango.db.dao;


public interface RowCallback<T> {

    void row(T obj, int rowNumber);
}