/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.dao.jdbc;

import br.org.scadabr.ScadaBrVersionBean;
import br.org.scadabr.dao.DataSourceDao;
import br.org.scadabr.dao.EventDao;
import br.org.scadabr.dao.SystemSettingsDao;
import br.org.scadabr.dao.UserDao;
import br.org.scadabr.rt.SchedulerPool;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.utils.i18n.LocalizableMessageImpl;
import br.org.scadabr.vo.event.EventStatus;
import br.org.scadabr.vo.event.type.SystemEventKey;
import com.serotonin.mango.db.DatabaseAccessFactory;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.vo.User;
import java.util.Collection;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 *
 * @author aploese
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {EventsDaoTest.Config.class})
public class EventsDaoTest {

    @Configuration
    public static class Config {

        private final UserDao userDao = new UserDaoImpl();
        
        private final EventDao eventDao = new EventDaoImpl();
        
        private final DatabaseAccessFactory databaseAccessFactory = new DatabaseAccessFactory();
                
        private final ScadaBrVersionBean ScadaBrVersionBean = new ScadaBrVersionBean();
        
        
        

        /**
         * No tests with SchedulerPool so return null.
         * @return 
         */
        @Bean
        public SchedulerPool getSchedulerPool() {
            return null;
        }
        
        /**
         * No tests with DataSourceDao so return null.
         * @return 
         */
        @Bean
        public DataSourceDao getDataSourceDao() {
            return null;
        }
        
        @Bean
        public UserDao getUserDao() {
            return userDao;
        }
        
        @Bean
        public EventDao getEventDao() {
            return eventDao;
        }
        
        @Bean
        public DatabaseAccessFactory getDatabaseAccessFactory() {
            return databaseAccessFactory;
        }

        @Bean
        public ScadaBrVersionBean getScadaBrVersionBean() {
            return ScadaBrVersionBean;
        }
        

    }

    public EventsDaoTest() {
    }

    @Inject
    private UserDao userDao;

    @Inject
    private EventDao eventDao;

    @Before
    public void setUp() {
    }

    /**
     * Test the whole lifecycle of a statefull event
     */
    @Test
    public void testUserLoginEvent() {
        User user = userDao.getUser("admin");
        SystemEventType systemEventType = new SystemEventType(SystemEventKey.USER_LOGIN);
        long fireTs = System.currentTimeMillis();
        EventInstance eventInstance = new EventInstance(systemEventType, fireTs, new LocalizableMessageImpl("user.login"), null);
        eventDao.saveEvent(eventInstance);
        
        Collection<EventInstance> events = eventDao.getActiveEvents();
        assertEquals(1, events.size());
        assertEquals(eventInstance, events.iterator().next());

        events = eventDao.getPendingEvents(user);
        assertEquals(0, events.size());
        long ackTs = System.currentTimeMillis();
        eventDao.ackEvent(eventInstance.getId(), ackTs, user, null);
        events = eventDao.getActiveEvents();
        assertEquals(1, events.size());
        events = eventDao.getPendingEvents(user);
        assertEquals(0, events.size());

        long goneTs = System.currentTimeMillis();
        eventInstance = eventDao.getEventInstance(eventInstance.getId());
        eventInstance.setAlarmGone(goneTs);
        eventDao.saveEvent(eventInstance);
        events = eventDao.getActiveEvents();
        assertEquals(0, events.size());
        EventInstance event = eventDao.getEventInstance(eventInstance.getId());
        assertEquals(EventStatus.GONE, event.getEventState());
        assertEquals(fireTs, event.getFireTimestamp());
        assertEquals(ackTs, event.getAcknowledgedTimestamp());
        assertEquals(goneTs, event.getGoneTimestamp());
        
    }

}
