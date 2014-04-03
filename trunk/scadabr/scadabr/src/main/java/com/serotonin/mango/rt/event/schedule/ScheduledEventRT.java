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
package com.serotonin.mango.rt.event.schedule;

import br.org.scadabr.ImplementMeException;
import br.org.scadabr.ShouldNeverHappenException;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.event.SimpleEventDetector;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.rt.event.type.ScheduledEventType;
import com.serotonin.mango.util.timeout.RunWithArgClient;
import com.serotonin.mango.vo.event.ScheduledEventVO;
import br.org.scadabr.timer.cron.CronExpression;
import br.org.scadabr.timer.cron.CronParser;
import br.org.scadabr.web.i18n.LocalizableMessage;
import br.org.scadabr.web.i18n.LocalizableMessageImpl;
import com.serotonin.mango.util.timeout.EventRunWithArgTask;
import java.text.ParseException;

/**
 * @author Matthew Lohbihler
 *
 */
public class ScheduledEventRT extends SimpleEventDetector implements RunWithArgClient<Boolean> {

    private final ScheduledEventVO vo;
    private ScheduledEventType eventType;
    private boolean eventActive;
    private EventRunWithArgTask<Boolean> activeTask;
    private EventRunWithArgTask<Boolean> inactiveTask;

    public ScheduledEventRT(ScheduledEventVO vo) {
        this.vo = vo;
    }

    public ScheduledEventVO getVo() {
        return vo;
    }

    private void raiseEvent(long time) {
        Common.ctx.getEventManager().raiseEvent(eventType, time, vo.isReturnToNormal(), vo.getAlarmLevel(),
                getMessage(), null);
        eventActive = true;
        fireEventDetectorStateChanged(time);
    }

    private void returnToNormal(long time) {
        Common.ctx.getEventManager().returnToNormal(eventType, time);
        eventActive = false;
        fireEventDetectorStateChanged(time);
    }

    public LocalizableMessage getMessage() {
        return new LocalizableMessageImpl("event.schedule.active", vo.getDescription());
    }

    @Override
    public boolean isEventActive() {
        return eventActive;
    }

    @Override
    synchronized public void run(Boolean active, long fireTime) {
        if (active) {
            raiseEvent(fireTime);
        } else {
            returnToNormal(fireTime);
        }
    }

    //
    //
    // /
    // / Lifecycle interface
    // /
    //
    //
    @Override
    public void initialize() {
        eventType = new ScheduledEventType(vo.getId());
        if (!vo.isReturnToNormal()) {
            eventType.setDuplicateHandling(EventType.DuplicateHandling.ALLOW);
        }

        // Schedule the active event.
        CronExpression activeTrigger = createTrigger(true);
        activeTask = new EventRunWithArgTask<>(activeTrigger, this, true);
            Common.eventCronPool.schedule(activeTask);

            if (vo.isReturnToNormal()) {
            CronExpression inactiveTrigger = createTrigger(false);
            inactiveTask = new EventRunWithArgTask<>(inactiveTrigger, this, false);
            Common.eventCronPool.schedule(inactiveTask);

            if (vo.getScheduleType() != ScheduledEventVO.TYPE_ONCE) {
                // Check if we are currently active.
                if (inactiveTask.getNextScheduledExecutionTime()< activeTask.getNextScheduledExecutionTime()) {
                    raiseEvent(System.currentTimeMillis());
                }
            }
        }
    }

    @Override
    public void terminate() {
        fireEventDetectorTerminated();
        if (activeTask != null) {
            activeTask.cancel();
        }
        if (inactiveTask != null) {
            inactiveTask.cancel();
        }
        returnToNormal(System.currentTimeMillis());
    }

    @Override
    public void joinTermination() {
        // no op
    }

    public CronExpression createTrigger(boolean activeTrigger) {
        if (!activeTrigger && !vo.isReturnToNormal()) {
            return null;
        }

        final int month = activeTrigger ? vo.getActiveMonth() : vo.getInactiveMonth();
        final int day = activeTrigger ? vo.getActiveDay() : vo.getInactiveDay();
        final int hour = activeTrigger ? vo.getActiveHour() : vo.getInactiveHour();
        final int minute = activeTrigger ? vo.getActiveMinute() : vo.getInactiveMinute();
        final int second = activeTrigger ? vo.getActiveSecond() : vo.getInactiveSecond();
        switch (vo.getScheduleType()) {

            case ScheduledEventVO.TYPE_CRON:
                try {
                    if (activeTrigger) {
                        return new CronParser().parse(vo.getActiveCron(), CronExpression.TIMEZONE_UTC);
                    }
                    return new CronParser().parse(vo.getInactiveCron(), CronExpression.TIMEZONE_UTC);
                } catch (ParseException e) {
                    // Should never happen, so wrap and rethrow
                    throw new ShouldNeverHappenException(e);
                }

            case ScheduledEventVO.TYPE_ONCE:
                if (activeTrigger) {
                    return new CronExpression(vo.getActiveYear(), vo.getActiveMonth(), vo.getActiveDay(), vo.getActiveHour(),
                            vo.getActiveMinute(), vo.getActiveSecond(), 0, CronExpression.TIMEZONE_UTC);
                } else {
                    return new CronExpression(vo.getInactiveYear(), vo.getInactiveMonth(), vo.getInactiveDay(),
                            vo.getInactiveHour(), vo.getInactiveMinute(), vo.getInactiveSecond(), 0, CronExpression.TIMEZONE_UTC);
                }
            case ScheduledEventVO.TYPE_HOURLY:
                return CronExpression.createPeriodByHour(1, minute, second, 0);
            case ScheduledEventVO.TYPE_DAILY:
                return CronExpression.createDaily(hour, minute, second, 0);
            case ScheduledEventVO.TYPE_WEEKLY:
                throw new ImplementMeException();
            case ScheduledEventVO.TYPE_MONTHLY:
                throw new ImplementMeException();
            default:
                throw new RuntimeException();
        }
    }
}
