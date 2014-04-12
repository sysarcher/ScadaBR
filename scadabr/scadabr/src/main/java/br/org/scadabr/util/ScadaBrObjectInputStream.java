/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.util;

import br.org.scadabr.vo.datasource.vmstat.VMStatDataSourceVO;
import br.org.scadabr.vo.datasource.vmstat.VMStatPointLocatorVO;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 *
 * @author aploese
 */
class ScadaBrObjectInputStream extends ObjectInputStream {

    public ScadaBrObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        final ObjectStreamClass osc = super.readClassDescriptor();
        switch (osc.getName()) {
            case "com.serotonin.mango.vo.datasource.mbus.VMStatDataSourceVO":
                return ObjectStreamClass.lookup(VMStatDataSourceVO.class);
            case "com.serotonin.mango.vo.datasource.mbus.VMStatPointLocatorVO":
                return ObjectStreamClass.lookup(VMStatPointLocatorVO.class);
            default:
                return osc;
        }
    }
}
