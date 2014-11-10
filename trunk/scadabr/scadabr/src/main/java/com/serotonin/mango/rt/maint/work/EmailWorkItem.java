/*
 Mango - Open Source M2M - http://mango.serotoninsoftware.com
 Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
 @author Matthew Lohbihler
    
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.rt.maint.work;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.SystemSettingsDao;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.web.email.MangoEmailContent;
import br.org.scadabr.web.email.EmailContent;
import br.org.scadabr.web.email.EmailSender;
import br.org.scadabr.utils.i18n.LocalizableMessageImpl;
import br.org.scadabr.vo.event.type.SystemEventSource;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Matthew Lohbihler
 *
 */
@Configurable
public class EmailWorkItem implements WorkItem {

    @Autowired
    private SystemSettingsDao  systemSettingsDao;
    
    @Override
    public int getPriority() {
        return WorkItem.PRIORITY_MEDIUM;
    }

    public static void queueEmail(String toAddr, MangoEmailContent content) throws AddressException {
        queueEmail(new String[]{toAddr}, content);
    }

    public static void queueEmail(String[] toAddrs, MangoEmailContent content) throws AddressException {
        queueEmail(toAddrs, content, null);
    }

    public static void queueEmail(String[] toAddrs, MangoEmailContent content, Runnable[] postSendExecution)
            throws AddressException {
        queueEmail(toAddrs, content.getSubject(), content, postSendExecution);
    }

    public static void queueEmail(String[] toAddrs, String subject, EmailContent content, Runnable[] postSendExecution)
            throws AddressException {
        EmailWorkItem item = new EmailWorkItem();

        item.toAddresses = new InternetAddress[toAddrs.length];
        for (int i = 0; i < toAddrs.length; i++) {
            item.toAddresses[i] = new InternetAddress(toAddrs[i]);
        }

        item.subject = subject;
        item.content = content;
        item.postSendExecution = postSendExecution;

        Common.ctx.getBackgroundProcessing().addWorkItem(item);
    }

    private InternetAddress fromAddress;
    private InternetAddress[] toAddresses;
    private String subject;
    private EmailContent content;
    private Runnable[] postSendExecution;

    @Override
    public void execute() {
        try {
            if (fromAddress == null) {
                String addr = systemSettingsDao.getValue(SystemSettingsDao.EMAIL_FROM_ADDRESS);
                String pretty = systemSettingsDao.getValue(SystemSettingsDao.EMAIL_FROM_NAME);
                fromAddress = new InternetAddress(addr, pretty);
            }

            EmailSender emailSender = new EmailSender(systemSettingsDao.getValue(SystemSettingsDao.EMAIL_SMTP_HOST),
                    systemSettingsDao.getIntValue(SystemSettingsDao.EMAIL_SMTP_PORT),
                    systemSettingsDao.getBooleanValue(SystemSettingsDao.EMAIL_AUTHORIZATION),
                    systemSettingsDao.getValue(SystemSettingsDao.EMAIL_SMTP_USERNAME),
                    systemSettingsDao.getValue(SystemSettingsDao.EMAIL_SMTP_PASSWORD),
                    systemSettingsDao.getBooleanValue(SystemSettingsDao.EMAIL_TLS));

            emailSender.send(fromAddress, toAddresses, subject, content);
        } catch (UnsupportedEncodingException e) {
            String to = "";
            for (InternetAddress addr : toAddresses) {
                if (to.length() > 0) {
                    to += ", ";
                }
                to += addr.getAddress();
            }
            new SystemEventType(SystemEventSource.EMAIL_SEND_FAILURE).fire("event.email.failure", subject, to, e.getMessage());
        } finally {
            if (postSendExecution != null) {
                for (Runnable runnable : postSendExecution) {
                    runnable.run();
                }
            }
        }
    }
}
