package com.serotonin.mango.vo.dataSource.persistent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.mango.MangoDataType;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.rt.dataSource.persistent.PersistentPointLocatorRT;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;


//TODO apl add datatTyoe to JSON export ???
@JsonRemoteEntity
public class PersistentPointLocatorVO extends AbstractPointLocatorVO {
    public PointLocatorRT createRuntime() {
        return new PersistentPointLocatorRT(this);
    }

    public LocalizableMessage getConfigurationDescription() {
        return new LocalizableMessage("common.noMessage");
    }

    @JsonRemoteProperty(alias=MangoDataType.ALIAS_DATA_TYPE)
    private MangoDataType mangoDataType = MangoDataType.UNKNOWN;

    @Override
    public MangoDataType getMangoDataType() {
        return mangoDataType;
    }

    public void setMangoDataType(MangoDataType mangoDataType) {
        this.mangoDataType = mangoDataType;
    }

    public boolean isSettable() {
        return false;
    }

    public void validate(DwrResponseI18n response) {
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
        out.writeInt(mangoDataType.mangoId);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            mangoDataType = MangoDataType.fromMangoId(in.readInt());
        }
    }
}
