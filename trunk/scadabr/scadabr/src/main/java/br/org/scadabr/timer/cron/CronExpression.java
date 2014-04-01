/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.timer.cron;

import br.org.scadabr.ImplementMeException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aploese
 */
public class CronExpression {

    private boolean doIncrementTime;
    private CronCalendar nextTimeStamp;
    private CronField millisecond;
    private CronField second;
    private CronField minute;
    private CronField hourOfDay;
    private CronField dayOfMonth;
    private CronField month;
    private CronField dayOfWeek;
    private CronField year;

    public CronExpression(String cronPattern) throws ParseException {
        throw new ImplementMeException();
    }

    CronExpression() {
        nextTimeStamp = new CronCalendar();
    }

    CronExpression(CronCalendar nextTimeStamp) {
        this.nextTimeStamp = nextTimeStamp;
    }

    private void calcNextValidTime() {
        //TODO store cal and check day of month and day of week the smaller will win dito range...
        calcNextMillisecond();
        calcNextSecond();
        calcNextMinute();
        calcNextHourOfDay();
        // find ranges and unwind them
        calcNextDayOfMonth();
        calcNextMonth();
        calcNextYear();
        
    }

    public void calcNextValidTimeAfter() {
        doIncrementTime = true;
        calcNextValidTime();
        
    }

    public void calcNextValidTimeIncludingNow() {
        doIncrementTime = false;
        calcNextValidTime();
    }

    public CronField setMilliSecond(CronField field) {
        return second = field;
    }

    public CronField setSecond(CronField field) {
        return second = field;
    }

    public Date getNextValidTimeAfter(Date date) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setCurrentTime(Calendar c) {
        nextTimeStamp.setCurrentTime(c);
    }

    public void setNextTimestampTo(GregorianCalendar c) {
        nextTimeStamp.setCalendarTo(c);
    }

    public void setTimeZone(TimeZone tz) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //        nextTimeStamp.setTimeZone(tz);
    }

    void setField(CronFieldType ft, CronField f) {
        switch (ft) {
            case MILLISECOND:
                millisecond = f;
                break;
            case SECOND:
                second = f;
                break;
            case MINUTE:
                minute = f;
                break;
            case HOUR_OF_DAY:
                hourOfDay = f;
                break;
            case DAY_OF_MONTH:
                dayOfMonth = f;
                break;
            case MONTH:
                month = f;
                break;
            case DAY_OF_WEEK:
                dayOfWeek = f;
                break;
            case YEAR:
                year = f;
                break;
            default:
                throw new RuntimeException();
        }
    }

    private static void monthToString(StringBuilder sb, CronFieldType cft, CronField cf, String lastChar) {
        switch (cf.type) {
            case ANY:
                sb.append('*').append(lastChar);
                break;
            case VALUE:
                sb.append(cf.getMonthValue()).append(lastChar);
                break;
            case RANGE_INCREMENT:
                if ((cft.floor == cf.getStart()) && (cft.ceil == cf.getEnd())) {
                    if (cf.getIncrement() == 1) {
                        sb.append("* ");
                    } else {
                        sb.append("*/").append(cf.getIncrement()).append(lastChar);
                    }
                } else {
                    sb.append(cf.getMonthStart()).append('-').append(cf.getMonthEnd());
                    if (cf.getIncrement() == 1) {
                        sb.append(lastChar);
                    } else {
                        sb.append('/').append(cf.getIncrement()).append(lastChar);
                    }
                }
                break;
            case CHILD_FIELDS:
                final CronField[] children = cf.getChildren();
                for (int i = 0; i < children.length; i++) {
                    monthToString(sb, cft, children[i], i < children.length - 1 ? "," : " ");
                }
                break;
            default:
                throw new RuntimeException();
        }

    }

    private static void dayOfWeekToString(StringBuilder sb, CronFieldType cft, CronField cf, String lastChar) {
        switch (cf.type) {
            case ANY:
                sb.append('*').append(lastChar);
                break;
            case VALUE:
                sb.append(cf.getDayOfWeekValue()).append(lastChar);
                break;
            case RANGE_INCREMENT:
                if ((cft.floor == cf.getStart()) && (cft.ceil == cf.getEnd())) {
                    if (cf.getIncrement() == 1) {
                        sb.append("* ");
                    } else {
                        sb.append("*/").append(cf.getIncrement()).append(lastChar);
                    }
                } else {
                    sb.append(cf.getDayOfWeekStart()).append('-').append(cf.getDayOfWeekEnd());
                    if (cf.getIncrement() == 1) {
                        sb.append(lastChar);
                    } else {
                        sb.append('/').append(cf.getIncrement()).append(lastChar);
                    }
                }
                break;
            case CHILD_FIELDS:
                final CronField[] children = cf.getChildren();
                for (int i = 0; i < children.length; i++) {
                    dayOfWeekToString(sb, cft, children[i], i < children.length - 1 ? "," : " ");
                }
                break;
            default:
                throw new RuntimeException();
        }

    }

    private static void genericToString(StringBuilder sb, CronFieldType cft, CronField cf, String lastChar) {
        switch (cf.type) {
            case ANY:
                sb.append('*').append(lastChar);
                break;
            case VALUE:
                sb.append(cf.getValue()).append(lastChar);
                break;
            case RANGE_INCREMENT:
                if ((cft.floor == cf.getStart()) && (cft.ceil == cf.getEnd())) {
                    if (cf.getIncrement() == 1) {
                        sb.append("* ");
                    } else {
                        sb.append("*/").append(cf.getIncrement()).append(lastChar);
                    }
                } else {
                    sb.append(cf.getStart()).append('-').append(cf.getEnd());
                    if (cf.getIncrement() == 1) {
                        sb.append(lastChar);
                    } else {
                        sb.append('/').append(cf.getIncrement()).append(lastChar);
                    }
                }
                break;
            case CHILD_FIELDS:
                final CronField[] children = cf.getChildren();
                for (int i = 0; i < children.length; i++) {
                    genericToString(sb, cft, children[i], i < children.length - 1 ? "," : " ");
                }
                break;
            default:
                throw new RuntimeException();
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        genericToString(sb, CronFieldType.MILLISECOND, millisecond, " ");
        genericToString(sb, CronFieldType.SECOND, second, " ");
        genericToString(sb, CronFieldType.MINUTE, minute, " ");
        genericToString(sb, CronFieldType.HOUR_OF_DAY, hourOfDay, " ");
        genericToString(sb, CronFieldType.DAY_OF_MONTH, dayOfMonth, " ");
        monthToString(sb, CronFieldType.MONTH, month, " ");
        dayOfWeekToString(sb, CronFieldType.DAY_OF_WEEK, dayOfWeek, " ");
        genericToString(sb, CronFieldType.YEAR, year, "");

        return sb.toString();
    }

    String getNextTimestampAsString() {
        return String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d", nextTimeStamp.getYear(), nextTimeStamp.getMonth(), nextTimeStamp.getDayOfMonth(), nextTimeStamp.getHourOfDay(), nextTimeStamp.getMinute(), nextTimeStamp.getSecond(), nextTimeStamp.getMillisecond());
    }

    private void calcNextMillisecond() {
        switch (millisecond.type) {
            case ANY:
                if (doIncrementTime) {
                    nextTimeStamp.incMillisecond();
                    doIncrementTime = false;
                }
                break;
            case VALUE:
                doIncrementTime &= !nextTimeStamp.setMilliseconds(millisecond.getValue());
                break;
            case RANGE_INCREMENT:
                doIncrementTime &= !nextTimeStamp.setMilliseconds(!doIncrementTime, millisecond.getStart(), millisecond.getEnd(), millisecond.getIncrement());
                break;
            case CHILD_FIELDS:
                final CronField[] cfs = millisecond.getChildren();
                int selectedValue = Integer.MAX_VALUE;
                for (CronField cf : cfs) {
                    int currentValue;
                    switch (cf.type) {
                        case RANGE_INCREMENT:
                            currentValue = rangeCheck(cf.getStart(), cf.getEnd(), cf.getIncrement(), nextTimeStamp.getMillisecond(), 1000);
                            break;
                        case VALUE:
                            currentValue = cf.getValue() < nextTimeStamp.getMillisecond() ? 1000 + cf.getValue() : cf.getValue();
                            break;
                        default:
                            throw new RuntimeException();
                    }
                    if (currentValue < selectedValue) {
                        selectedValue = currentValue;
                    }
                }
                if (selectedValue >= 1000) {
                    doIncrementTime &= !nextTimeStamp.setMilliseconds(selectedValue - 1000);
                } else {
                    doIncrementTime &= !nextTimeStamp.setMilliseconds(selectedValue);
                }
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void calcNextSecond() {
        switch (second.type) {
            case ANY:
                if (doIncrementTime) {
                    nextTimeStamp.incSecond();
                    doIncrementTime = false;
                }
                break;
            case VALUE:
                doIncrementTime &= !nextTimeStamp.setSeconds(second.getValue());
                break;
            case RANGE_INCREMENT:
                doIncrementTime &= !nextTimeStamp.setSeconds(!doIncrementTime, second.getStart(), second.getEnd(), second.getIncrement());
                break;
            case CHILD_FIELDS:
                final CronField[] cfs = second.getChildren();
                int selectedValue = Integer.MAX_VALUE;
                for (CronField cf : cfs) {
                    int currentValue;
                    switch (cf.type) {
                        case RANGE_INCREMENT:
                            currentValue = rangeCheck(cf.getStart(), cf.getEnd(), cf.getIncrement(), nextTimeStamp.getSecond(), 60);
                            break;
                        case VALUE:
                            currentValue = cf.getValue() < nextTimeStamp.getSecond() ? 60 + cf.getValue() : cf.getValue();
                            break;
                        default:
                            throw new RuntimeException();
                    }
                    if (currentValue < selectedValue) {
                        selectedValue = currentValue;
                    }
                }
                if (selectedValue >= 60) {
                    doIncrementTime &= !nextTimeStamp.setSeconds(selectedValue - 60);
                } else {
                    doIncrementTime &= !nextTimeStamp.setSeconds(selectedValue);
                }
                break;
            default:
                throw new RuntimeException();

        }
    }

    private int rangeCheck(int start, int end, int increment, int value, int overflowValue) {
        if (value < start) {
            value = start;
            return value;
        }

        if (value > end) {
            value = start;
            value += overflowValue;
            return value;
        }

        int modulo = (value - start) % increment;
        if (modulo > 0) {
            value += increment - modulo;
            if (value > end) {
                value = start;
                value += overflowValue;
            }
            return value;
        } else if (doIncrementTime) {
            value += increment;
            if (value > end) {
                value = start;
                value += overflowValue;
            }
            return value;
        } else {
            return value;
        }
    }

    private void calcNextMinute() {
        switch (minute.type) {
            case ANY:
                if (doIncrementTime) {
                    nextTimeStamp.incMinute();
                    doIncrementTime = false;
                }
                break;
            case VALUE:
                doIncrementTime &= !nextTimeStamp.setMinutes(minute.getValue());
                break;
            case RANGE_INCREMENT:
                doIncrementTime &= !nextTimeStamp.setMinutes(!doIncrementTime, minute.getStart(), minute.getEnd(), minute.getIncrement());
                break;
            case CHILD_FIELDS:
                final CronField[] cfs = minute.getChildren();
                int selectedValue = Integer.MAX_VALUE;
                for (CronField cf : cfs) {
                    int currentValue;
                    switch (cf.type) {
                        case RANGE_INCREMENT:
                            currentValue = rangeCheck(cf.getStart(), cf.getEnd(), cf.getIncrement(), nextTimeStamp.getMinute(), 60);
                            break;
                        case VALUE:
                            currentValue = cf.getValue() < nextTimeStamp.getMinute() ? 60 + cf.getValue() : cf.getValue();
                            break;
                        default:
                            throw new RuntimeException();
                    }
                    if (currentValue < selectedValue) {
                        selectedValue = currentValue;
                    }
                }
                if (selectedValue >= 60) {
                    doIncrementTime &= !nextTimeStamp.setMinutes(selectedValue - 60);
                } else {
                    doIncrementTime &= !nextTimeStamp.setMinutes(selectedValue);
                }
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void calcNextHourOfDay() {
        switch (hourOfDay.type) {
            case ANY:
                if (doIncrementTime) {
                    nextTimeStamp.incHourOfDay();
                    doIncrementTime = false;
                }
                break;
            case VALUE:
                doIncrementTime &= !nextTimeStamp.setHourOfDay(hourOfDay.getValue());
                break;
            case RANGE_INCREMENT:
                doIncrementTime &= !nextTimeStamp.setHourOfDay(!doIncrementTime, hourOfDay.getStart(), hourOfDay.getEnd(), hourOfDay.getIncrement());
                break;
            case CHILD_FIELDS:
                final CronField[] cfs = hourOfDay.getChildren();
                int selectedValue = Integer.MAX_VALUE;
                for (CronField cf : cfs) {
                    int currentValue;
                    switch (cf.type) {
                        case RANGE_INCREMENT:
                            currentValue = rangeCheck(cf.getStart(), cf.getEnd(), cf.getIncrement(), nextTimeStamp.getHourOfDay(), 24);
                            break;
                        case VALUE:
                            currentValue = cf.getValue() < nextTimeStamp.getHourOfDay() ? 24 + cf.getValue() : cf.getValue();
                            break;
                        default:
                            throw new RuntimeException();
                    }
                    if (currentValue < selectedValue) {
                        selectedValue = currentValue;
                    }
                }
                if (selectedValue >= 24) {
                    doIncrementTime &= !nextTimeStamp.setHourOfDay(selectedValue - 24);
                } else {
                    doIncrementTime &= !nextTimeStamp.setHourOfDay(selectedValue);
                }
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void calcNextDayOfMonth() {
        switch (dayOfMonth.type) {
            case ANY:
                if (doIncrementTime) {
                    nextTimeStamp.incDayOfMonth();
                    doIncrementTime = false;
                }
                break;
            case VALUE:
                doIncrementTime &= !nextTimeStamp.setDayOfMonth(dayOfMonth.getValue());
                break;
            case RANGE_INCREMENT:
                doIncrementTime &= !nextTimeStamp.setDayOfMonth(!doIncrementTime, dayOfMonth.getStart(), dayOfMonth.getEnd(), dayOfMonth.getIncrement());
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void calcNextMonth() {
        switch (month.type) {
            case ANY:
                if (doIncrementTime) {
                    nextTimeStamp.incMonth();
                    doIncrementTime = false;
                }
                break;
            case VALUE:
                try {
                    doIncrementTime &= !nextTimeStamp.setMonth(month.getValue());
                } catch (CronCalendar.NoLeapYearException ex) {
                    // We tried to set the 29 feb on a year which is no leapyer
                    switch (dayOfMonth.type) {
                        case RANGE_INCREMENT:
                            // OK No leapYear try next year with dayOfMonth start
                            doIncrementTime &= !nextTimeStamp.setDayOfMonth(dayOfMonth.getStart());
                            try {
                                doIncrementTime &= !nextTimeStamp.setMonth(month.getValue());
                            } catch (CronCalendar.AfterLastDayOfMonthException exInner) {
                                throw new RuntimeException(exInner);
                            } catch (CronCalendar.NoLeapYearException exInner) {
                                throw new RuntimeException("Should never happen", exInner);
                            }
                            break;
                        case VALUE:
                            if (dayOfMonth.getValue() == 29) {
                                //find leapYear
                                while (!nextTimeStamp.isLeapYear()) {
                                    nextTimeStamp.incYear();
                                    doIncrementTime = false;
                                }
                            }
                            break;
                        default:
                            throw new RuntimeException("Dont know where to go");
                    }
                } catch (CronCalendar.AfterLastDayOfMonthException ex) {
                    // We tried to set the 30,31 of feb or th 31 of april, june, september, november
                    switch (dayOfMonth.type) {
                        case RANGE_INCREMENT:
                            doIncrementTime &= !nextTimeStamp.setDayOfMonth(dayOfMonth.getStart());
                            try {
                                doIncrementTime &= !nextTimeStamp.setMonth(month.getValue());
                            } catch (CronCalendar.AfterLastDayOfMonthException exInner) {
                                throw new RuntimeException(exInner);
                            } catch (CronCalendar.NoLeapYearException exInner) {
                                throw new RuntimeException("Should never happen", exInner);
                            }
                            break;
                        case VALUE:
                            throw new RuntimeException(ex);
                        default:
                            throw new RuntimeException("Dont know where to go");
                    }
                }
                break;
            case RANGE_INCREMENT:
                doIncrementTime &= !nextTimeStamp.setMonth(!doIncrementTime, month.getStart(), month.getEnd(), month.getIncrement());
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void calcNextYear() {
        switch (year.type) {
            case ANY:
                if (doIncrementTime) {
                    nextTimeStamp.incYear();
                    doIncrementTime = false;
                }
                if (nextTimeStamp.getDayOfMonth() == 29 && nextTimeStamp.getMonth() == 2 && !nextTimeStamp.isLeapYear()) {
                    if (dayOfMonth.type == CronField.Type.VALUE && month.type == CronField.Type.VALUE) {
                        while (!nextTimeStamp.isLeapYear()) {
                            nextTimeStamp.incYear();
                        }
                    } else {
                        // I dont know if we ever reach this ....
                        throw new RuntimeException("Is leap year");
                    }
                }
                break;
            case VALUE:
                try {
                    doIncrementTime &= !nextTimeStamp.setYear(year.getValue());
                } catch (CronCalendar.NoLeapYearException ex) {
                    throw new RuntimeException(ex);
                } catch (CronCalendar.TimeInPastException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            case RANGE_INCREMENT:
                try {
                    doIncrementTime &= !nextTimeStamp.setYear(!doIncrementTime, year.getStart(), year.getEnd(), year.getIncrement());
                } catch (CronCalendar.NoLeapYearException ex) {
                    throw new RuntimeException(ex);
                } catch (CronCalendar.TimeInPastException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            default:
                throw new RuntimeException();
        }
    }

}
