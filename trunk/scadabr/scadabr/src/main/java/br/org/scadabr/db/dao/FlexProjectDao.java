package br.org.scadabr.db.dao;

import br.org.scadabr.api.vo.FlexProject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.BaseDao;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class FlexProjectDao extends BaseDao {

    private static final String FLEX_PROJECT_SELECT = "select id, name, description, xmlConfig from flexProjects ";

    public int saveFlexProject(int id, String name, String description,
            String xmlConfig) {
        if (id == Common.NEW_ID) {
            return insertFlexProject(id, name, description, xmlConfig);
        } else {
            return updateFlexProject(id, name, description, xmlConfig);
        }
    }

    private int insertFlexProject(int id, String name, String description,
            String xmlConfig) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("flexProjects").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", name);
        params.put("description", description);
        params.put("xmlConfig", xmlConfig);

        Number result = insertActor.executeAndReturnKey(params);
        return result.intValue();
    }

    private int updateFlexProject(int id, String name, String description,
            String xmlConfig) {
        getSimpleJdbcTemplate().update(
                "update flexProjects set name=?, description=?, xmlConfig=? where id=?",
                name, description, xmlConfig, id);
        return id;

    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void deleteFlexProject(final int flexProjectId) {
        getSimpleJdbcTemplate().update("delete from flexProjects where id=?",
                flexProjectId);
    }

    public FlexProject getFlexProject(int id) {
        return getJdbcTemplate().queryForObject(FLEX_PROJECT_SELECT + " where id=?", new FlexProjectRowMapper(), id);
    }

    public List<FlexProject> getFlexProjects() {
        List<FlexProject> flexProjects = getJdbcTemplate().query(FLEX_PROJECT_SELECT,
                new FlexProjectRowMapper());
        return flexProjects;
    }

    class FlexProjectRowMapper implements ParameterizedRowMapper<FlexProject> {

        @Override
        public FlexProject mapRow(ResultSet rs, int rowNum) throws SQLException {
            FlexProject project = new FlexProject();
            project.setId(rs.getInt(1));
            project.setName(rs.getString(2));
            project.setDescription(rs.getString(3));
            project.setXmlConfig(rs.getString(4));
            return project;
        }
    }
}
