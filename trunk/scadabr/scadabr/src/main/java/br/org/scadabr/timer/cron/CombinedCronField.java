/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.timer.cron;

import java.util.Arrays;

/**
 *
 * @author aploese
 */
public class CombinedCronField extends CronField {

    private CronField[] fields;

    CombinedCronField(CronFieldType fieldType, final CronField... fields) {
        super(fieldType);
        if (fields == null) {
            throw new IllegalArgumentException("Expect at least one field");
        }
        switch (fields.length) {
            case 0:
                throw new IllegalArgumentException("Expect at least one field");
            case 1:
                this.fields = new CronField[2];
                this.fields[0] = fields[0];
                break;
            default:
                this.fields = fields;
        }
    }

    void addField(CronField field) {
        if (fields[1] == null) {
            fields[1] = field;
        } else {
            fields = Arrays.copyOf(fields, fields.length + 1);
            fields[fields.length - 1] = field;
        }
    }

    
    @Override
    protected void toString(StringBuilder sb) {
        super.toString(sb);
        sb.append(", fields=").append(fields);
    }
    
    @Override
    protected int hashCode(int hash, int multiplyer) {
        return  multiplyer * super.hashCode(hash, multiplyer) + Arrays.hashCode(fields);
    }

    @Override
    protected boolean equals(CronField obj) {
        final CombinedCronField ccf = (CombinedCronField) obj;
        if (!Arrays.equals(fields, ccf.fields)) {
            return false;
        }
        return super.equals(obj);
    }


}
