/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.vo.datasource;

import br.org.scadabr.ScadaBrConstants;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import java.io.Serializable;

/**
 *
 * @author aploese
 */
public class PointLocatorFolderVO implements Serializable {
    
    private int dataSourceId = ScadaBrConstants.NEW_ID;
    private int id = ScadaBrConstants.NEW_ID;
    private Integer parentFolderId;
    private String name;

    public PointLocatorFolderVO() {
        
    }
    
    public PointLocatorFolderVO(DataSourceVO dataSource, String name) {
        this.dataSourceId = dataSource.getId();
        this.name = name;
    }

    /**
     * @return the dataSourceId
     */
    public int getDataSourceId() {
        return dataSourceId;
    }

    /**
     * @param dataSourceId the dataSourceId to set
     */
    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the parentFolderId
     */
    public Integer getParentFolderId() {
        return parentFolderId;
    }

    /**
     * @param parentFolderId the parentFolderId to set
     */
    public void setParentFolderId(Integer parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public boolean isNew() {
        return id == ScadaBrConstants.NEW_ID;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
}
