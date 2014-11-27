/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.dao;

import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.event.EventHandlerVO;
import java.util.Collection;
import java.util.List;
import javax.inject.Named;

/**
 *
 * @author aploese
 */
@Named
public interface EventDao {

    public void saveEvent(EventInstance evt);

    public void insertUserEvents(int id, List<Integer> eventUserIds, boolean alarm);

    public void ackEvent(int id, long timestamp, int i, int MAINTENANCE_MODE);

    public Collection<? extends EventInstance> getActiveEvents();

    public List<EventHandlerVO> getEventHandlers(EventType eventType);

    public Iterable<EventInstance> getPendingEvents(User user);

    public int purgeEventsBefore(long millis);

    public EventInstance getEventInstance(int id);
    
}
