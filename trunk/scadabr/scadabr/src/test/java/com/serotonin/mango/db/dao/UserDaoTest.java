package com.serotonin.mango.db.dao;

import com.serotonin.mango.Common;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import com.serotonin.mango.vo.User;
import org.junit.Before;
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
    
    @Transactional(readOnly=false, propagation= Propagation.REQUIRES_NEW)
    private void setUpUsers() {
        executeSqlScript("classpath:db/setUp-Table-Users.sql", false);
    }

    private UserDao userDao;

    @Before
    public void setUp() {
        userDao = super.applicationContext.getBean(UserDao.class);
    }
    
    @Test
    public void getUsers() {
        setUpUsers();
        List<User> users = this.userDao.getUsers();
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
    
    @Test(expected=org.springframework.dao.DuplicateKeyException.class)
    public void chekUsernameUnique() {
        setUpUsers();
        User user = userDao.getUser(-10);
        user.setId(Common.NEW_ID);
        userDao.saveUser(user);
    }

    @Test
    public void getUser_Int() {
        setUpUsers();
        User user = userDao.getUser(-10);
        assertNotNull(user);
        user = userDao.getUser(0);
        assertNull(user);
    }
    
    @Test
    public void deleteUser() {
        setUpUsers();
        User user = userDao.getUser(-10);
        userDao.deleteUser(user);
        user = userDao.getUser(user.getId());
        assertNull(user);
    }
            
    @Test
    public void getActiveUsers() {
        setUpUsers();
        List<User> users = userDao.getActiveUsers();
        assertEquals(1, users.size());
        assertEquals(-12, users.get(0).getId());
        
    }
    
}
