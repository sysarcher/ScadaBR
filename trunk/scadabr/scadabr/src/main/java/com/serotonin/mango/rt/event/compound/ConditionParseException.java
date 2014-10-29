/*
 Mango - Open Source M2M - http://mango.serotoninsoftware.com
 Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
 @author Matthew Lohbihler
    
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.rt.event.compound;

import br.org.scadabr.i18n.LocalizableException;

/**
 * @author Matthew Lohbihler
 */
public class ConditionParseException extends LocalizableException {

    private static final long serialVersionUID = -1;

    private final int from;
    private final int to;

    public ConditionParseException(String i18nKey, Object... args) {
        super(i18nKey, args);
        from = -1;
        to = -1;
    }

    /**
     * @param message the human-readable error message
     * @param from inclusive index of the start of the offending part of the
     * statement
     * @param to exclusive index of the end of the offending part of the
     * statement
     */
    public ConditionParseException(int from, int to, String i18nKey, Object... args) {
        super(i18nKey, args);
        this.from = from;
        this.to = to;
    }

    public boolean isRange() {
        return from != -1;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}
