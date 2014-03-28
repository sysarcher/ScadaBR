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
public abstract class CronField {

    final CronFieldType fieldType;

    protected CronField(CronFieldType fieldType) {
        this.fieldType = fieldType;
    }

    protected boolean equals(CronField other) {
        return this.fieldType == other.fieldType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return equals((CronField) obj);
    }

    protected int hashCode(int hash, int multiplyer) {
        return multiplyer * hash + Objects.hashCode(this.fieldType);
    }

    @Override
    public int hashCode() {
        return hashCode(3, 83);
    }

    protected void toString(StringBuilder sb) {
        sb.append(" fieldType=").append(fieldType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append('{');
        toString(sb);
        sb.append('}');
        return sb.toString();
    }

}
