/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.dao.jdbc;

import br.org.scadabr.ScadaBrVersionBean;
import br.org.scadabr.dao.UserDao;
import com.serotonin.mango.db.DatabaseAccessFactory;
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
@ContextConfiguration(classes = {BaseJdbcDaoTest.Config.class})
public class BaseJdbcDaoTest {

    @Configuration
    public static class Config {

        private final UserDao userDao = new UserDaoImpl();
        
        private final DatabaseAccessFactory databaseAccessFactory = new DatabaseAccessFactory();
                
        private final ScadaBrVersionBean ScadaBrVersionBean = new ScadaBrVersionBean();
        

        @Bean
        public UserDao getUserDao() {
            return userDao;
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

    public BaseJdbcDaoTest() {
    }

    @Inject
    private UserDao userDao;

    @Before
    public void setUp() {
    }

    @Test
    public void testSetup() {
        Collection<User> users = userDao.getUsers();
        assertEquals(1, users.size());
    }

}
