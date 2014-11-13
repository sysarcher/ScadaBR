/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.vo.dataSource;

import com.serotonin.mango.db.dao.DataSourceDao;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author aploese
 */
@Configurable //TODO why is this not autowiring???
public class DataSourceValidator implements Validator {
    
    @Autowired
    private DataSourceDao dataSourceDao;
    
    public DataSourceValidator() {
        
    }

    public DataSourceValidator(DataSourceDao dao) {
        this.dataSourceDao = dao;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return DataSourceVO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        final DataSourceVO vo = (DataSourceVO) target;
        if (vo.getXid().isEmpty()) {
            errors.rejectValue("xid", "validate.required");
        } else if (!dataSourceDao.isXidUnique(vo.getXid(), vo.getId())) {
            errors.rejectValue("xid", "validate.xidUsed");
        } else if (vo.getXid().length() > 50) {
            errors.rejectValue("xid", "validate.notLongerThan", new Object[]{50}, "validate.notLongerThan");
        }
        if (vo.getName().isEmpty()) {
            errors.reject("name", "validate.nameRequired");
        }
        if (vo.getName().length() > 40) {
            errors.reject("name", "validate.nameTooLong");
        }
    }
    
}
