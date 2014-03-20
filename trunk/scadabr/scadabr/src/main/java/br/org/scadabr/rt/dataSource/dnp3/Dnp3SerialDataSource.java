package br.org.scadabr.rt.dataSource.dnp3;

import gnu.io.NoSuchPortException;

import java.util.Date;

import br.org.scadabr.vo.dataSource.dnp3.Dnp3SerialDataSourceVO;
import br.org.scadabr.web.i18n.LocalizableException;

import com.serotonin.mango.rt.dataSource.DataSourceRT;
import br.org.scadabr.web.i18n.LocalizableMessage;
import br.org.scadabr.web.i18n.LocalizableMessageImpl;

public class Dnp3SerialDataSource extends Dnp3DataSource {

    private final Dnp3SerialDataSourceVO configuration;

    public Dnp3SerialDataSource(Dnp3SerialDataSourceVO configuration) {
        super(configuration);
        this.configuration = configuration;
    }

    @Override
    public void initialize() {
        // inicializa DnpMaster com os parametros seriais.
        DNP3Master dnp3Master = new DNP3Master();
        try {
            dnp3Master.initSerial(configuration.getSourceAddress(),
                    configuration.getSlaveAddress(), configuration
                    .getCommPortId(), configuration.getBaudRate(),
                    configuration.getStaticPollPeriods());
        } catch (Exception e) {
            e.printStackTrace();
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, new Date().getTime(), true,
                    new LocalizableMessageImpl("event.exception2", configuration
                            .getName(), e.getMessage()));
        }

        super.initialize(dnp3Master);
    }

}
