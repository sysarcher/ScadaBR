/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.timer.cron;

import java.util.Objects;

/**
 *
 * @author aploese
 */
class CronValueField extends CronField {
    
    private int value;
  
    public CronValueField(CronFieldType fieldType, String value) {
        super(fieldType);
        this.value = Integer.valueOf(value);
    }
    
    public CronValueField(CronFieldType fieldType, int value) {
        super(fieldType);
        this.value = value;
    }

    @Override
    protected void toString(StringBuilder sb) {
        super.toString(sb);
        sb.append(", value=").append(value);
    }
    
    @Override
    protected int hashCode(int hash, int multiplyer) {
        return  multiplyer * super.hashCode(hash, multiplyer) + this.value;
    }

    @Override
    protected boolean equals(CronField obj) {
        final CronValueField cvf = (CronValueField) obj;
        if (this.value != cvf.value) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(int value) {
        this.value = value;
    }
    
    
    
}
