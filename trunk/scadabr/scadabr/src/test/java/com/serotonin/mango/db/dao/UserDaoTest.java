package com.serotonin.mango.db.dao;

import com.serotonin.mango.Common;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;

import com.serotonin.mango.vo.User;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Before;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Integration tests for the {@link UserDao} implementation.
 * </p>
 * <p>
 * "UserDao-context.xml" determines the actual beans to test.
 * </p>
 *
 * @author Arne Plöse
 */
@ContextConfiguration
public class UserDaoTest extends AbstractDaoTests {

    public User createUser(int index) {
        User result = new User();
        result.setUsername("UserName" + index);
        result.setPassword("UserPassword" + index);
        result.setEmail("Email@" + index);
        return result;
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    private void setUpTableUsers() {
        executeSqlScript("classpath:db/setUp-Table-Users.sql", false);
    }
    private UserDao userDao;

    @Before
    public void setUp() {
        userDao = super.applicationContext.getBean(UserDao.class);
    }

    @Test
    public void getUsers() {
        setUpTableUsers();
        List<User> users = userDao.getUsers();
        if (users.isEmpty()) {
            fail("Setup not correct expected more than 0 users");
        }
        assertEquals(countRowsInTable("users"), users.size());
    }

    @Test
    public void saveUser() {
        User user = createUser(1);

        //test insert
        userDao.saveUser(user);
        User userById = userDao.getUser(user.getId());
        assertEquals(user, userById);
        assertEquals(user.getPassword(), userById.getPassword());

        //test update
        user.setPassword("PASSWORD");
        userDao.saveUser(user);
        userById = userDao.getUser(user.getId());
        assertEquals(user, userById);
        assertEquals(user.getPassword(), userById.getPassword());
    }
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.GERMANY);

    @Test
    public void testAPL() {
        test_UTC_DaylightSaving(userDao.getJdbcTemplate());
    }
    
    @Test
    public void testDate_Summer_Winter_Time() {
        setUpTableUsers();
        final Calendar c_cest = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c_cest.set(2011, 9, 30, 0, 59, 59);
        c_cest.set(Calendar.MILLISECOND, 0);
        
        final Calendar c_cet = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c_cet.set(2011, 9, 30, 01, 00, 00);
        c_cet.set(Calendar.MILLISECOND, 0);
        
        final Calendar c_now = Calendar.getInstance();
        User user;
        
        //Test write
        user = userDao.getUser(-11);
        user.setLastLogin(c_cest.getTime());
        userDao.saveLastLogin(user);
        User userById = userDao.getUser(user.getId());
        assertEquals(c_cest.getTime(), userById.getLastLogin());

        user.setLastLogin(c_cet.getTime());
        userDao.saveLastLogin(user);
        userById = userDao.getUser(user.getId());
        assertEquals(c_cet.getTime(), userById.getLastLogin());
        
        user.setLastLogin(c_now.getTime());
        userDao.saveLastLogin(user);
        userById = userDao.getUser(user.getId());
        assertEquals(c_now.getTime(), userById.getLastLogin());
        
    }

    @Test(expected = org.springframework.dao.DuplicateKeyException.class)
    public void chekUsernameUnique() {
        setUpTableUsers();
        User user = userDao.getUser(-10);
        user.setId(Common.NEW_ID);
        userDao.saveUser(user);
    }

    @Test
    public void getUser_Int() {
        setUpTableUsers();
        User user = userDao.getUser(-10);
        user = userDao.getUser(-11);
        assertNotNull(user);
        user = userDao.getUser(0);
        assertNull(user);
    }

    @Test
    public void deleteUser() {
        setUpTableUsers();
        User user = userDao.getUser(-10);
        userDao.deleteUser(user);
        user = userDao.getUser(user.getId());
        assertNull(user);
    }

    @Test
    public void getActiveUsers() {
        setUpTableUsers();
        List<User> users = userDao.getActiveUsers();
        assertEquals(1, users.size());
        assertEquals(-12, users.get(0).getId());

    }
}
