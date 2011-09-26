/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serotonin.mango.vo.dataSource;

import com.serotonin.ShouldNeverHappenException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aploese
 */
public enum DataSourceRegistry {

    EBI25(16, "dsEdit.ebi25", false, "com.serotonin.mango.vo.dataSource.ebro.EBI25DataSourceVO"),
    VICONICS(18, "dsEdit.viconics", false, "com.serotonin.mango.vo.dataSource.viconics.ViconicsDataSourceVO"),
    BACNET(10, "dsEdit.bacnetIp", true, "com.serotonin.mango.vo.dataSource.bacnet.BACnetIPDataSourceVO"),
    DNP3_IP(21, "dsEdit.dnp3Ip", true, "br.org.scadabr.vo.dataSource.dnp3.Dnp3IpDataSourceVO"),
    DNP3_SERIAL(22, "dsEdit.dnp3Serial", true, "br.org.scadabr.vo.dataSource.dnp3.Dnp3SerialDataSourceVO"),
    GALIL(14, "dsEdit.galil", true, "com.serotonin.mango.vo.dataSource.galil.GalilDataSourceVO"),
    HTTP_RECEIVER(7, "dsEdit.httpReceiver", true, "com.serotonin.mango.vo.dataSource.http.HttpReceiverDataSourceVO"),
    HTTP_RETRIEVER(11, "dsEdit.httpRetriever", true, "com.serotonin.mango.vo.dataSource.http.HttpRetrieverDataSourceVO"),
    HTTP_IMAGE(15, "dsEdit.httpImage", true, "com.serotonin.mango.vo.dataSource.http.HttpImageDataSourceVO"),
    INTERNAL(27, "dsEdit.internal", true, "com.serotonin.mango.vo.dataSource.internal.InternalDataSourceVO"),
    JMX(26, "dsEdit.jmx", true, "com.serotonin.mango.vo.dataSource.jmx.JmxDataSourceVO"),
    M_BUS(20, "dsEdit.mbus", true, "com.serotonin.mango.vo.dataSource.mbus.MBusDataSourceVO"),
    META(9, "dsEdit.meta", true, "com.serotonin.mango.vo.dataSource.meta.MetaDataSourceVO"),
    MODBUS_IP(3, "dsEdit.modbusIp", true, "com.serotonin.mango.vo.dataSource.modbus.ModbusIpDataSourceVO"),
    MODBUS_SERIAL(2, "dsEdit.modbusSerial", true, "com.serotonin.mango.vo.dataSource.modbus.ModbusSerialDataSourceVO"),
    NMEA(13, "dsEdit.nmea", true, "com.serotonin.mango.vo.dataSource.nmea.NmeaDataSourceVO"),
    ONE_WIRE(8, "dsEdit.1wire", true, "com.serotonin.mango.vo.dataSource.onewire.OneWireDataSourceVO"),
    OPEN_V_4_J(19, "dsEdit.openv4j", true, "com.serotonin.mango.vo.dataSource.openv4j.OpenV4JDataSourceVO"),
    FHZ_4_J(40, "dsEdit.fhz4j", true, "com.serotonin.mango.vo.dataSource.fhz4j.Fhz4JDataSourceVO"),
    PACHUBE(23, "dsEdit.pachube", true, "com.serotonin.mango.vo.dataSource.pachube.PachubeDataSourceVO"),
    PERSISTENT(24, "dsEdit.persistent", true, "com.serotonin.mango.vo.dataSource.persistent.PersistentDataSourceVO"),
    POP3(12, "dsEdit.pop3", true, "com.serotonin.mango.vo.dataSource.pop3.Pop3DataSourceVO"),
    SNMP(5, "dsEdit.snmp", true, "com.serotonin.mango.vo.dataSource.snmp.SnmpDataSourceVO"),
    SPINWAVE(4, "dsEdit.spinwave", true, "com.serotonin.mango.vo.dataSource.spinwave.SpinwaveDataSourceVO"),
    SQL(6, "dsEdit.sql", true, "com.serotonin.mango.vo.dataSource.sql.SqlDataSourceVO"),
    VIRTUAL(1, "dsEdit.virtual", true, "com.serotonin.mango.vo.dataSource.virtual.VirtualDataSourceVO"),
    VMSTAT(17, "dsEdit.vmstat", true, "com.serotonin.mango.vo.dataSource.vmstat.VMStatDataSourceVO"),
    OPC(32, "dsEdit.opc", true, "br.org.scadabr.vo.dataSource.opc.OPCDataSourceVO"),
    ASCII_FILE(33, "dsEdit.asciiFile", true, "br.org.scadabr.vo.dataSource.asciiFile.ASCIIFileDataSourceVO"),
    ASCII_SERIAL(34, "dsEdit.asciiSerial", true, "br.org.scadabr.vo.dataSource.asciiSerial.ASCIISerialDataSourceVO"),
    IEC101_SERIAL(35, "dsEdit.iec101Serial", true, "br.org.scadabr.vo.dataSource.iec101.IEC101SerialDataSourceVO"),
    IEC101_ETHERNET(36, "dsEdit.iec101Ethernet", true, "br.org.scadabr.vo.dataSource.iec101.IEC101EthernetDataSourceVO"),
    NODAVE_S7(37, "dsEdit.nodaves7", false, "br.org.scadabr.vo.dataSource.nodaves7.NodaveS7DataSourceVO"),
    DR_STORAGE_HT5B(38, "dsEdit.drStorageHt5b", true, "br.org.scadabr.vo.dataSource.drStorageHt5b.DrStorageHt5bDataSourceVO"),
    ALPHA_2(39, "dsEdit.alpha2", true, "br.org.scadabr.vo.dataSource.alpha2.Alpha2DataSourceVO");
    public final int mangoId;
    private final String key;
    private final boolean display;
    private Class<? extends DataSourceVO<?>> clazz;

    private DataSourceRegistry(int mangoId, String key, boolean display, String className) {
        this.mangoId = mangoId;
        this.key = key;
        this.display = display;
        try {
            this.clazz = (Class<? extends DataSourceVO<?>>) DataSourceRegistry.class.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException ex) {
            this.clazz = null;
        }
    }

    public String getKey() {
        return key;
    }

    public boolean isDisplay() {
        return display;
    }

    public DataSourceVO<?> createDataSourceVO() {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new ShouldNeverHappenException(
                    "Error finding component with name '" + key + "': "
                    + e.getMessage());
        }
    }

    public static List<String> getTypeList() {
        List<String> result = new ArrayList<String>();
        for (DataSourceRegistry type : values()) {
            result.add(type.name());
        }
        return result;
    }
}
