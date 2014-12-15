/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.dao;

import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.UserComment;
import java.util.Collection;
import javax.inject.Named;

/**
 *
 * @author aploese
 */
@Named
public interface UserDao {

    void insertUserComment(int TYPE_EVENT, int eventId, UserComment comment);

    User getUser(int userId);

    /**
     * Returns all users
     * @return 
     */
    Collection<User> getUsers();

    Collection<User> getActiveUsers();

    void saveHomeUrl(int id, String url);

    User getUser(String username);

    void recordLogin(int id);
    
}
