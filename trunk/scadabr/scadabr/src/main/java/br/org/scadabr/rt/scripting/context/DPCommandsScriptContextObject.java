package br.org.scadabr.rt.scripting.context;


import org.springframework.beans.factory.annotation.Autowired;

import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.permission.Permissions;

public class DPCommandsScriptContextObject extends ScriptContextObject {

    public static final Type TYPE = Type.DATAPOINT_COMMANDS;
    @Autowired
    private Permissions permissions;
    @Autowired
    private RuntimeManager runtimeManager;

    @Override
    public Type getType() {
        return TYPE;
    }

    public void writeDataPoint(String xid, String stringValue) {
        DataPointVO dataPoint = new DataPointDao().getDataPoint(xid);
        if (dataPoint != null) {
            permissions.ensureDataPointSetPermission(user, dataPoint);
            MangoValue value = MangoValue.stringToValue(stringValue, dataPoint.getPointLocator().getMangoDataType());
            runtimeManager.setDataPointValue(dataPoint.getId(), value,
                    this.user);
        }
    }

    public void enableDataPoint(String xid) {
        DataPointVO dataPoint = new DataPointDao().getDataPoint(xid);
        if (dataPoint != null) {
            permissions.ensureDataPointReadPermission(user, dataPoint);
            dataPoint.setEnabled(true);
            runtimeManager.saveDataPoint(dataPoint);
        }

    }

    public void disableDataPoint(String xid) {
        DataPointVO dataPoint = new DataPointDao().getDataPoint(xid);
        if (dataPoint != null) {
            permissions.ensureDataPointReadPermission(user, dataPoint);
            dataPoint.setEnabled(false);
            runtimeManager.saveDataPoint(dataPoint);
        }

    }
}
