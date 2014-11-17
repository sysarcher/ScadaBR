/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.vo.dataSource;

import com.serotonin.mango.db.dao.DataPointDao;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author aploese
 */
@Configurable //TODO why is this not autowiring???
public class PointLocatorValidator implements Validator {
    
    @Autowired
    private DataPointDao dataPointDao;
    
    public PointLocatorValidator() {
        
    }

    public PointLocatorValidator(DataPointDao dao) {
        this.dataPointDao = dao;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return PointLocatorVO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        final PointLocatorVO vo = (PointLocatorVO) target;
        if (vo.getName().isEmpty()) {
            errors.rejectValue("name", "validate.nameRequired");
        }
        if (vo.getName().length() > 40) {
            errors.rejectValue("name", "validate.nameTooLong");
        }
    }
    
}
