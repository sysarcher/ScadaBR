/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.scadabr.timer.cron;

/**
 *
 * @author aploese
 */
public class CronRangeField extends CronField {
    int start;
    int end;
    int increment;

    public CronRangeField(CronFieldType fieldType) {
        super(fieldType);
        start = fieldType.floor;
        end = fieldType.ceil;
        increment = 1;
    }

    CronRangeField(CronFieldType fieldType, int start, int end, int increment) {
        this(fieldType);
        this.start = start;
        this.end = end;
        this.increment = increment;
    }

    public void setStartRange(CronFieldType state, String s) {
        int value = Integer.valueOf(s);
        if (!fieldType.isValid(value)) {
            throw new IllegalArgumentException();
        }
        start = value;
    }

    public  void setEndRange(CronFieldType state, String s) {
        int value = Integer.valueOf(s);
        if (!fieldType.isValid(value)) {
            throw new IllegalArgumentException();
        }
        end = value;
    }

    void setIncrement(CronFieldType currentField, String value) {
        increment = Integer.valueOf(value);
    }
    
    @Override
    protected void toString(StringBuilder sb) {
        super.toString(sb);
        sb.append(", start=").append(start);
        sb.append(", end=").append(end);
        sb.append(", increment=").append(increment);
    }
    
    @Override
    protected int hashCode(int hash, int multiplyer) {
        hash = multiplyer * super.hashCode(hash, multiplyer) + this.start;
        hash = multiplyer * hash + this.end;
        return multiplyer * hash + this.increment; 
    }

    @Override
    protected boolean equals(CronField obj) {
        final CronRangeField crf = (CronRangeField) obj;
        if (this.start != crf.start) {
            return false;
        }
        if (this.end != crf.end) {
            return false;
        }
        if (this.increment != crf.increment) {
            return false;
        }
        return super.equals(obj);
    }


}
