package com.serotonin.mango.rt.event.maintenance;

import br.org.scadabr.ImplementMeException;
import java.text.ParseException;

import br.org.scadabr.ShouldNeverHappenException;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.rt.event.type.MaintenanceEventType;
import com.serotonin.mango.util.timeout.RunWithArgClient;
import com.serotonin.mango.util.timeout.SystemRunWithArgTask;
import com.serotonin.mango.vo.event.MaintenanceEventVO;
import br.org.scadabr.timer.cron.CronExpression;
import br.org.scadabr.timer.cron.CronParser;
import br.org.scadabr.timer.cron.SystemCronTask;
import br.org.scadabr.web.i18n.LocalizableMessage;
import br.org.scadabr.web.i18n.LocalizableMessageImpl;

public class MaintenanceEventRT implements RunWithArgClient<Boolean> {

    private final MaintenanceEventVO vo;
    private MaintenanceEventType eventType;
    private boolean eventActive;
    private SystemRunWithArgTask<Boolean> activeTask;
    private SystemRunWithArgTask<Boolean> inactiveTask;

    public MaintenanceEventRT(MaintenanceEventVO vo) {
        this.vo = vo;
    }

    public MaintenanceEventVO getVo() {
        return vo;
    }

    private void raiseEvent(long time) {
        if (!eventActive) {
            Common.ctx.getEventManager().raiseEvent(eventType, time, true, vo.getAlarmLevel(), getMessage(), null);
            eventActive = true;
        }
    }

    private void returnToNormal(long time) {
        if (eventActive) {
            Common.ctx.getEventManager().returnToNormal(eventType, time);
            eventActive = false;
        }
    }

    public LocalizableMessage getMessage() {
        return new LocalizableMessageImpl("event.maintenance.active", vo.getDescription());
    }

    public boolean isEventActive() {
        return eventActive;
    }

    public boolean toggle() {
        run(!eventActive, System.currentTimeMillis());
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
    // Lifecycle interface
    //
    public void initialize() {
        eventType = new MaintenanceEventType(vo.getId());

        if (vo.getScheduleType() != MaintenanceEventVO.TYPE_MANUAL) {
            // Schedule the active event.
            final CronExpression activeTrigger = createTrigger(true);
            activeTask = new SystemRunWithArgTask<>(activeTrigger, this, true);
            Common.systemCronPool.schedule(activeTask);

            // Schedule the inactive event
            final CronExpression inactiveTrigger = createTrigger(false);
            inactiveTask = new SystemRunWithArgTask<>(inactiveTrigger, this, false);
            Common.systemCronPool.schedule(inactiveTask);

            if (vo.getScheduleType() != MaintenanceEventVO.TYPE_ONCE) {
                // Check if we are currently active.
                if (inactiveTask.getNextScheduledExecutionTime() < activeTask.getNextScheduledExecutionTime()) {
                    raiseEvent(System.currentTimeMillis());
                }
            }
        }
    }

    public void terminate() {
        if (activeTask != null) {
            activeTask.cancel();
        }
        if (inactiveTask != null) {
            inactiveTask.cancel();
        }

        if (eventActive) {
            Common.ctx.getEventManager().returnToNormal(eventType, System.currentTimeMillis(),
                    EventInstance.RtnCauses.SOURCE_DISABLED);
        }
    }

    public void joinTermination() {
        // no op
    }

    public CronExpression createTrigger(boolean activeTrigger) {
        final int month = activeTrigger ? vo.getActiveMonth() : vo.getInactiveMonth();
        final int day = activeTrigger ? vo.getActiveDay() : vo.getInactiveDay();
        final int hour = activeTrigger ? vo.getActiveHour() : vo.getInactiveHour();
        final int minute = activeTrigger ? vo.getActiveMinute() : vo.getInactiveMinute();
        final int second = activeTrigger ? vo.getActiveSecond() : vo.getInactiveSecond();
        switch (vo.getScheduleType()) {
            case MaintenanceEventVO.TYPE_MANUAL:
                return null;

            case MaintenanceEventVO.TYPE_CRON:
                try {
                    if (activeTrigger) {
                        return new CronParser().parse(vo.getActiveCron(), CronExpression.TIMEZONE_UTC);
                    }
                    return new CronParser().parse(vo.getInactiveCron(), CronExpression.TIMEZONE_UTC);
                } catch (ParseException e) {
                    // Should never happen, so wrap and rethrow
                    throw new ShouldNeverHappenException(e);
                }

            case MaintenanceEventVO.TYPE_ONCE:
                if (activeTrigger) {
                    return new CronExpression(vo.getActiveYear(), vo.getActiveMonth(), vo.getActiveDay(), vo.getActiveHour(),
                            vo.getActiveMinute(), vo.getActiveSecond(), 0, CronExpression.TIMEZONE_UTC);
                } else {
                    return new CronExpression(vo.getInactiveYear(), vo.getInactiveMonth(), vo.getInactiveDay(),
                            vo.getInactiveHour(), vo.getInactiveMinute(), vo.getInactiveSecond(), 0, CronExpression.TIMEZONE_UTC);
                }
            case MaintenanceEventVO.TYPE_HOURLY:
                return CronExpression.createPeriodByHour(1, minute, second, 0);
            case MaintenanceEventVO.TYPE_DAILY:
                throw new ImplementMeException();
            case MaintenanceEventVO.TYPE_WEEKLY:
                throw new ImplementMeException();
            case MaintenanceEventVO.TYPE_MONTHLY:
                throw new ImplementMeException();
            default:
                throw new RuntimeException();
        }
    }
}
