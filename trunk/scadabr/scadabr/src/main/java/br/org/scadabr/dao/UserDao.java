/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.dao;

import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.UserComment;
import javax.inject.Named;

/**
 *
 * @author aploese
 */
@Named
public interface UserDao {

    public void insertUserComment(int TYPE_EVENT, int eventId, UserComment comment);

    public User getUser(int userId);

    public Iterable<User> getActiveUsers();

    public void saveHomeUrl(int id, String url);

    public User getUser(String username);

    public void recordLogin(int id);
    
}
