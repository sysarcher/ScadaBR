/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.web.mvc.controller.datasources;

import br.org.scadabr.web.l10n.RequestContextAwareLocalizer;
import com.googlecode.jsonrpc4j.JsonRpcService;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.dataSource.meta.MetaDataSourceVO;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 *
 * @author aploese
 */
@Named
@JsonRpcService("/dataSources/rpc/")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RpcController {
    @Inject
    private DataSourceDao dataSourceDao;
    @Inject 
    private RequestContextAwareLocalizer localizer;
    
    public JsonDataSource addDataSource(String type) {
        DataSourceVO result = new MetaDataSourceVO();
        dataSourceDao.saveDataSource(result);
        return new JsonDataSource(result, localizer);
    }
    
}
