/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.db.dao;

import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.UserComment;
import java.util.List;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.*;

/**
 *
 * @author aploese
 */
public class UserCommentDaoTest extends AbstractDaoTests {
    
    private UserCommentDao userCommentDao = new UserCommentDao();
    
    @Before
    public void setUp() {
        userCommentDao.setDataSource(super.applicationContext.getBean(DataSource.class));
    }
    
    @Transactional(readOnly=false, propagation= Propagation.REQUIRES_NEW)
    private void setUpTableDataSources() {
        executeSqlScript("classpath:db/setUp-Tables_Users_UserComments.sql", false);
    }

    private int countUserCommentRows() {
        return countRowsInTable("userComments");
    }
    
    /**
     * Test of addUserComment method, of class UserCommentDao.
     */
    @Ignore
    @Test
    public void testAddComment_EventComment() {
        System.out.println("addUserComment");
        executeSqlScript("classpath:db/setUp-Table-Users.sql", false);
//        UserComment result = userCommentDao.addComment(UserComment.TYPE_POINT, -1, user, "Test Comment");
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addUserComment method, of class UserCommentDao.
     */
    @Ignore
    @Test
    public void testAddComment_DataPointComment() {
        System.out.println("addUserComment");
        executeSqlScript("classpath:db/setUp-Table-Users.sql", false);
//        UserComment result = userCommentDao.addComment(UserComment.TYPE_POINT, -1, user, "Test Comment");
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of insertUserComment method, of class UserCommentDao.
     */
    @Ignore
    @Test
    public void testInsertUserComment() {
        System.out.println("insertUserComment");
        int typeId = 0;
        int referenceId = 0;
        UserComment comment = null;
//        userCommentDao.insertComment(typeId, referenceId, comment);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of populateComments method, of class UserCommentDao.
     */
    @Ignore
    @Test
    public void testPopulateComments_List() {
        System.out.println("populateComments");
        List<EventInstance> list = null;
        userCommentDao.populateComments(list);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of populateComments method, of class UserCommentDao.
     */
    @Ignore
    @Test
    public void testPopulateComments_DataPointVO() {
        System.out.println("populateComments");
        DataPointVO dp = null;
        userCommentDao.populateComments(dp);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of populateComments method, of class UserCommentDao.
     */
    @Ignore
    @Test
    public void testPopulateComments_EventInstance() {
        System.out.println("populateComments");
        EventInstance event = null;
        userCommentDao.populateComments(event);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
