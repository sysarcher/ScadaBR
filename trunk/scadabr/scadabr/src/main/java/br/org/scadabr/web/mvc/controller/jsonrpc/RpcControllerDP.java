/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc.controller.jsonrpc;

import br.org.scadabr.DataType;
import br.org.scadabr.dao.DataPointDao;
import br.org.scadabr.dao.LazyTreePointFolder;
import br.org.scadabr.logger.LogUtils;
import com.googlecode.jsonrpc4j.JsonRpcService;
import com.serotonin.mango.vo.DataPointVO;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 *
 * @author aploese
 */
@Named
@JsonRpcService("/dataPoints/rpc/")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RpcControllerDP {

    private final static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private DataPointDao dataPointDao;

    public DataPointVO addDataPoint(int parentFolderId, String dataType, String name) {
        DataPointVO dpvo =  DataPointVO.create(DataType.valueOf(dataType));
        dpvo.setPointFolderId(parentFolderId);
        dpvo.setName(name);
        dataPointDao.saveDataPoint(dpvo);
        return dpvo;
    }

    public DataPointVO renameDataPoint(int id, String name) {
        final DataPointVO dpvo = dataPointDao.getDataPoint(id);
        dpvo.setName(name);
        return dataPointDao.saveDataPoint(dpvo);
    }

    public DataPointVO deleteDataPoint(int id) {
        return dataPointDao.deleteDataPoint(id);
    }

    public LazyTreePointFolder addPointFolder(int parentFolderId, String name) {
        LazyTreePointFolder result = new LazyTreePointFolder(parentFolderId, name);
        dataPointDao.savePointFolder(result);
        return result;
    }

    public LazyTreePointFolder renamePointFolder(int id, String name) {
        final LazyTreePointFolder ltpf = dataPointDao.getPointFolderById(id);
        return dataPointDao.savePointFolder(ltpf);
    }

    public LazyTreePointFolder deletePointFolder(int id) {
        return dataPointDao.deletePointFolder(id);
    }


}
