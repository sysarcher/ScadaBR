/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.vo.event;

/**
 *
 * @author aploese
 */
public enum EventStatus {
    /**
     * statefull and active event or better alarm
     */
        ACTIVE(0),
        /** 
         * Stateful and gone
         */
        GONE(1),
        SOURCE_DISABLED(4),
        STATELESS(5);
        
        

        public static EventStatus fromId(int id) {
            switch (id) {
                case 0:
                    return ACTIVE;
                case 1:
                    return GONE;
                case 4:
                    return SOURCE_DISABLED;
                case 5:
                    return STATELESS;
                default:
                    throw new IndexOutOfBoundsException("Unknown id: " + id);
            }
        }
        private final int id;

        private EventStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

    
}
