/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.vo.datasource.meta;

import br.org.scadabr.dao.DataPointDao;
import br.org.scadabr.db.IntValuePair;
import br.org.scadabr.timer.cron.CronExpression;
import br.org.scadabr.timer.cron.CronParser;
import br.org.scadabr.vo.dataSource.PointLocatorValidator;
import com.serotonin.mango.vo.dataSource.meta.MetaPointLocatorVO;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.Errors;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author aploese
 */
@Configurable //TODO why is this not autowiring???
public class MetaPointLocatorValidator extends PointLocatorValidator {

    public MetaPointLocatorValidator() {

    }

    public MetaPointLocatorValidator(DataPointDao dao) {
        super(dao);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MetaPointLocatorVO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        super.validate(target, errors);
        final MetaPointLocatorVO vo = (MetaPointLocatorVO) target;

        if (vo.isScriptEmpty()) {
            errors.rejectValue("script", "validate.required");
        }

        List<String> varNameSpace = new ArrayList<>();
        for (IntValuePair point : vo.getContext()) {
            String varName = point.getValue();
            if (varName.isEmpty()) {
                errors.rejectValue("context", "validate.allVarNames");
                break;
            }

            if (!validateVarName(varName)) {
                errors.rejectValue("context", "validate.invalidVarName", new Object[]{varName}, "validate.invalidVarName");
                break;
            }

            if (varNameSpace.contains(varName)) {
                errors.rejectValue("context", "validate.duplicateVarName", new Object[]{varName}, "validate.duplicateVarName");
                break;
            }

            varNameSpace.add(varName);
        }

        if (vo.getUpdateEvent() == UpdateEvent.CRON) {
            try {
                new CronParser().parse(vo.getUpdateCronPattern(), CronExpression.TIMEZONE_UTC);
            } catch (ParseException e) {
                errors.rejectValue("updateCronPattern", "validate.invalidCron", new Object[]{vo.getUpdateCronPattern()}, "validate.invalidCron");
            }
        }

        if (vo.getExecutionDelaySeconds() < 0) {
            errors.rejectValue("executionDelaySeconds", "validate.cannotBeNegative");
        }

    }

    private boolean validateVarName(String varName) {
        char ch = varName.charAt(0);
        if (!Character.isLetter(ch) && ch != '_') {
            return false;
        }
        for (int i = 1; i < varName.length(); i++) {
            ch = varName.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '_') {
                return false;
            }
        }
        return true;
    }

}
