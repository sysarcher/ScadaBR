/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.timer.cron;

import br.org.scadabr.ImplementMeException;

/**
 *
 * @author aploese
 */
public class CronParser {

    enum ParseState {

        NONE,
        COLLECTING,
        ANY,
        WAIT_FOR_RANGE_END,
        WAIT_FOR_INCREMENT;
    }

    private CronExpression result;
    private StringBuilder sb;
    private CronFieldType currentField;
    private ParseState state;
    private CombinedCronField currentCombinedField;

    public CronExpression parse(final String cron) {
        result = new CronExpression();
        sb = new StringBuilder();
        currentField = CronFieldType.MILLIS;
        state = ParseState.NONE;
        CronRangeField currentBasicField = null;
        currentCombinedField = null;
        int pos = -1;
        for (char c : cron.toCharArray()) {
            pos++;
            switch (Character.toUpperCase(c)) {
                case '*':
                    state = ParseState.ANY;
                    break;
                case ',':
                    switch (state) {
                        case WAIT_FOR_RANGE_END:
                            currentBasicField.setEndRange(currentField, sb.toString());
                            sb.setLength(0);
                            if (currentCombinedField == null) {
                                currentCombinedField = new CombinedCronField(currentField, currentBasicField);
                            } else {
                                currentCombinedField.addField(currentBasicField);
                            }
                            currentBasicField = null;
                            break;
                        case WAIT_FOR_INCREMENT:
                            currentBasicField.setIncrement(currentField, sb.toString());
                            sb.setLength(0);
                            sb.setLength(0);
                            if (currentCombinedField == null) {
                                currentCombinedField = new CombinedCronField(currentField, currentBasicField);
                            } else {
                                currentCombinedField.addField(currentBasicField);
                            }
                            currentBasicField = null;
                            break;
                        case COLLECTING:
                            currentCombinedField = new CombinedCronField(currentField, new CronValueField(currentField, sb.toString()));
                            sb.setLength(0);
                            break;
                        default:
                            throw new RuntimeException();
                    }
                    state = ParseState.NONE;
                    break;
                case '-':
                    if (state != ParseState.COLLECTING) {
                        throw new RuntimeException();
                    }
                    if (currentBasicField != null) {
                        throw new RuntimeException();
                    }
                    currentBasicField = new CronRangeField(currentField);
                    currentBasicField.setStartRange(currentField, sb.toString());
                    sb.setLength(0);
                    state = ParseState.WAIT_FOR_RANGE_END;
                    break;
                case '/':
                    switch (state) {
                        case ANY:
                            currentBasicField = new CronRangeField(currentField);
                            state = ParseState.WAIT_FOR_INCREMENT;
                            break;
                        case WAIT_FOR_RANGE_END:
                            currentBasicField.setEndRange(currentField, sb.toString());
                            sb.setLength(0);
                            state = ParseState.WAIT_FOR_INCREMENT;
                            break;
                        default:
                            throw new RuntimeException();
                    }
                    break;
                case ' ':
                case '\t':
                    fieldParsed(currentBasicField);
                    currentBasicField = null;
                    switch (currentField) {
                        case MILLIS:
                            currentField = CronFieldType.SEC;
                            break;
                        case SEC:
                            currentField = CronFieldType.MIN;
                            break;
                        case MIN:
                            currentField = CronFieldType.HOUR;
                            break;
                        case HOUR:
                            currentField = CronFieldType.DAY_OF_MONTH;
                            break;
                        case DAY_OF_MONTH:
                            currentField = CronFieldType.MONTH_OF_YEAR;
                            break;
                        case MONTH_OF_YEAR:
                            currentField = CronFieldType.DAY_OF_WEEK;
                            break;
                        case DAY_OF_WEEK:
                            currentField = CronFieldType.YEAR;
                            break;
                        case YEAR:
                            throw new RuntimeException("Cron expression too long");
                        default:
                            throw new RuntimeException();
                    }
                    break;
                default:
                    if (state == ParseState.NONE) {
                        state = ParseState.COLLECTING;
                    }
                    sb.append(c);

            }
        }

        if (currentField != CronFieldType.YEAR) {
            throw new RuntimeException("Expression too short, last field: " + currentField);
        }
        fieldParsed(currentBasicField);
        currentBasicField = null;
        return result;
    }

    private void fieldParsed(CronField cronField) throws RuntimeException {
        switch (state) {
            case WAIT_FOR_RANGE_END:
                ((CronRangeField) cronField).setEndRange(currentField, sb.toString());
                sb.setLength(0);
                addToResult(cronField);
                break;
            case WAIT_FOR_INCREMENT:
                ((CronRangeField) cronField).setIncrement(currentField, sb.toString());
                sb.setLength(0);
                addToResult(cronField);
                break;
            case COLLECTING:
                CronValueField cvf = new CronValueField(currentField, sb.toString());
                sb.setLength(0);
                addToResult(cvf);
                break;
            case ANY:
                if ((currentCombinedField != null) || (cronField != null)) {
                    throw new RuntimeException();
                }
                result.setField(new AnyField(currentField));
                break;
            default:
                throw new RuntimeException("Field parsed, and state = " + state + " Dont know where to go");
        }
        state = ParseState.NONE;
    }

    private void addToResult(CronField cronField) {
        if (currentCombinedField != null) {
            currentCombinedField.addField(cronField);
            result.setField(currentCombinedField);
            currentCombinedField = null;
        } else {
            result.setField(cronField);
        }
    }

}
