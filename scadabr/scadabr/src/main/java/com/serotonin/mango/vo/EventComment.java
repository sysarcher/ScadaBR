/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.vo;

/**
 *
 * @author aploese
 */
public class EventComment extends UserComment {
    private int eventId;

    public EventComment() {
        super();
    }
    
    public EventComment(User user, String comment, int eventId) {
        super(user, comment);
        this.eventId = eventId;
    }

    /**
     * @return the eventId
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * @param eventId the eventId to set
     */
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
    
}
