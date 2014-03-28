/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.timer.cron;

import br.org.scadabr.ImplementMeException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author aploese
 */
public class CronExpression {
    
    private Calendar nextTimeStamp = Calendar.getInstance();
    private final Map<CronFieldType, CronField> fields = new EnumMap(CronFieldType.class);
    
    public CronExpression(String cronPattern) throws ParseException {
        throw new ImplementMeException();
    }

    CronExpression() {
    }

    public Date getNextValidTimeAfter(Date date) {
        throw new ImplementMeException();
    }

    public CronField setField(CronField field) {
        return fields.put(field.fieldType, field);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.fields);
        return hash;
    }

    @Override
    public String toString() {
        return "CronExpression{" + "nextTimeStamp=" + nextTimeStamp.getTimeInMillis() + ", fields=" + fields + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CronExpression other = (CronExpression) obj;
        if (!Objects.equals(this.fields, other.fields)) {
            return false;
        }
        return true;
    }
    
}
