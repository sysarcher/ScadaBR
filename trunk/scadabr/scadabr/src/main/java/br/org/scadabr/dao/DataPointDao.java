/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.dao;

import br.org.scadabr.web.LazyTreeNode;
import com.serotonin.mango.vo.DataPointVO;
import java.util.Collection;
import java.util.List;
import javax.inject.Named;

/**
 *
 * @author aploese
 */
@Named
public interface DataPointDao {

    Iterable<DataPointVO> getDataPoints(int dsId);

    void updateDataPoint(DataPointVO dp);

    void deleteDataPoints(int dataSourceId);

    String generateUniqueXid();

    void saveDataPoint(DataPointVO vo);

    void copyPermissions(int id, int id0);

    DataPointVO getDataPoint(int pointId);

    DataPointVO getDataPoint(String xid);

    void deleteDataPoint(int id);

    String getExtendedPointName(int targetPointId);

    Iterable<DataPointVO> getDataPoints(boolean includeRelationalData);

    String getCanonicalPointName(DataPointVO dp);

    Collection<LazyTreeNode> getFoldersAndDpByParentId(int parentId);

    boolean isXidUnique(String xid, int id);

    public void addPointToHierarchy(DataPointVO dp, String[] pathToPoint);

    public LazyTreeNode getRootFolder();

    public LazyTreeNode getFolderById(int id);

    public void savePointFolder(LazyTreeNode folder);
    
}
