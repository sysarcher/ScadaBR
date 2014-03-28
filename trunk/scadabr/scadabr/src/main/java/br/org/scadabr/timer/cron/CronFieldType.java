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
public enum CronFieldType {
    MILLIS(0, 999), 
    SEC(0, 59), 
    MIN(0, 59), 
    HOUR(0, 23), 
    DAY_OF_MONTH(1, 31), 
    MONTH_OF_YEAR(1, 12), 
    DAY_OF_WEEK(0, 6), 
    YEAR(1970, 2099);
    
    public final int floor;
    public final int ceil;
    
    private CronFieldType(int floor, int ceil) {
        this.floor = floor;
        this.ceil = ceil;
    }
    
    public boolean isValid(int value) {
        return value >= floor ? value <= ceil : false;
    }

}
