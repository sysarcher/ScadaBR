/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.vo.dataSource.vmstat;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.mango.i18n.LocalizableEnum;
import com.serotonin.web.i18n.LocalizableMessage;
import java.util.ResourceBundle;
import org.directwebremoting.annotations.DataTransferObject;

/**
 *
 * @author aploese
 */
//@DataTransferObject(type="enum") 
public enum VMStatAttributes implements LocalizableEnum<VMStatAttributes> {

    PROCS_R(1, "dsEdit.vmstat.attr.procsR"),
    PROCS_B(2, "dsEdit.vmstat.attr.procsB"),
    MEMORY_SWPD(3, "dsEdit.vmstat.attr.memorySwpd"),
    MEMORY_FREE(4, "dsEdit.vmstat.attr.memoryFree"),
    MEMORY_BUFF(5, "dsEdit.vmstat.attr.memoryBuff"),
    MEMORY_CACHE(6, "dsEdit.vmstat.attr.memoryCache"),
    SWAP_SI(7, "dsEdit.vmstat.attr.swapSi"),
    SWAP_SO(8, "dsEdit.vmstat.attr.swapSo"),
    IO_BI(9, "dsEdit.vmstat.attr.ioBi"),
    IO_BO(10, "dsEdit.vmstat.attr.ioBo"),
    SYSTEM_IN(11, "dsEdit.vmstat.attr.systemIn"),
    SYSTEM_CS(12, "dsEdit.vmstat.attr.systemCs"),
    CPU_US(13, "dsEdit.vmstat.attr.cpuUs"),
    CPU_SY(14, "dsEdit.vmstat.attr.cpuSy"),
    CPU_ID(15, "dsEdit.vmstat.attr.cpuId"),
    CPU_WA(16, "dsEdit.vmstat.attr.cpuWa"),
    CPU_ST(17, "dsEdit.vmstat.attr.cpuSt");

    static VMStatAttributes fromMangoId(int mangoId) {
        for (VMStatAttributes v : values()) {
            if (v.mangoId == mangoId) {
                return v;
            }
        }
        throw new ShouldNeverHappenException("Cant find VmStatAttribute of mangoId: " + mangoId);
    }
    public final int mangoId;
    public final LocalizableMessage messageI18n;

    private VMStatAttributes(int mangoId, String message) {
        this.mangoId = mangoId;
        this.messageI18n = new LocalizableMessage(message);
    }

    @Override
    public LocalizableMessage getMessageI18n() {
        return messageI18n;
    }

    @Override
    public String getLocalizedMessage(ResourceBundle bundle) {
        return messageI18n.getLocalizedMessage(bundle);
    }

    @Override
    public String getI18nMessageKey() {
        return messageI18n.getKey();
    }

    @Override
    public Class<VMStatAttributes> getEnum() {
        return VMStatAttributes.class;
    }

    @Override
    public String getName() {
        return name();
    }
}
