package com.serotonin.mango.vo.dataSource.persistent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import br.org.scadabr.json.JsonRemoteEntity;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.rt.dataSource.persistent.PersistentPointLocatorRT;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import br.org.scadabr.web.dwr.DwrResponseI18n;
import br.org.scadabr.web.i18n.LocalizableMessage;
import br.org.scadabr.web.i18n.LocalizableMessageImpl;

@JsonRemoteEntity
public class PersistentPointLocatorVO extends AbstractPointLocatorVO {

    @Override
    public PointLocatorRT createRuntime() {
        return new PersistentPointLocatorRT(this);
    }

    @Override
    public LocalizableMessage getConfigurationDescription() {
        return new LocalizableMessageImpl("common.noMessage");
    }

    private int dataTypeId;

    @Override
    public int getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(int dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public void validate(DwrResponseI18n response) {
        if (!DataTypes.CODES.isValidId(dataTypeId)) {
            response.addContextualMessage("dataTypeId", "validate.invalidValue");
        }
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        // no op
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        // no op
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(dataTypeId);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            dataTypeId = in.readInt();
        }
    }
}
