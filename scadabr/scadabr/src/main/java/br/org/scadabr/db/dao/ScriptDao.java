package br.org.scadabr.db.dao;

import br.org.scadabr.vo.scripting.ScriptVO;
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
import com.serotonin.util.SerializationHelper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public class ScriptDao extends BaseDao {

    private static final String SCRIPT_SELECT = "select id, xid, name, script, userId, data from scripts ";

    public void saveScript(final ScriptVO<?> vo) {
        // Decide whether to insert or update.
        if (vo.getId() == Common.NEW_ID) {
            insertScript(vo);
        } else {
            updateScript(vo);
        }
    }

    private void insertScript(final ScriptVO<?> vo) {
        SimpleJdbcInsert insertActor = new SimpleJdbcInsert(getDataSource()).withTableName("scripts").usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xid", vo.getXid());
        params.put("name", vo.getName());
        params.put("script", vo.getScript());
        params.put("userId", vo.getUserId());
        params.put("data", SerializationHelper.writeObjectToArray(vo));

        Number id = insertActor.executeAndReturnKey(params);
        vo.setId(id.intValue());
    }

    @SuppressWarnings("unchecked")
    private void updateScript(final ScriptVO<?> vo) {
        ScriptVO<?> old = getScript(vo.getId());
        getSimpleJdbcTemplate().update("update scripts set xid=?, name=?, script=?, userId=?, data=? where id=?",
                vo.getXid(), vo.getName(), vo.getScript(), vo.getUserId(), SerializationHelper.writeObjectToArray(vo), vo.getId());
    }

    public void deleteScript(final int scriptId) {
        ScriptVO<?> vo = getScript(scriptId);
        if (vo != null) {
            new TransactionTemplate(getTransactionManager()).execute(
                    new TransactionCallbackWithoutResult() {

                        @Override
                        protected void doInTransactionWithoutResult(
                                TransactionStatus status) {
                            getSimpleJdbcTemplate().update("delete from scripts where id=?", scriptId);
                        }
                    });
        }
    }

    public ScriptVO<?> getScript(int id) {
        return getSimpleJdbcTemplate().queryForObject(SCRIPT_SELECT + " where id=?", new ScriptRowMapper(), id);
    }

    public List<ScriptVO<?>> getScripts() {
        List<ScriptVO<?>> scripts = getSimpleJdbcTemplate().query(SCRIPT_SELECT, new ScriptRowMapper());
        return scripts;
    }

    class ScriptRowMapper implements ParameterizedRowMapper<ScriptVO<?>> {

        @Override
        public ScriptVO<?> mapRow(ResultSet rs, int rowNum) throws SQLException {
            ScriptVO<?> script = (ScriptVO<?>) SerializationHelper.readObject(rs.getBlob(6).getBinaryStream());
            script.setId(rs.getInt(1));
            script.setXid(rs.getString(2));
            script.setName(rs.getString(3));
            script.setScript(rs.getString(4));
            script.setUserId(rs.getInt(5));
            return script;
        }
    }

    public String generateUniqueXid() {
        return generateUniqueXid(ScriptVO.XID_PREFIX, "scripts");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "scripts");
    }

    public ScriptVO<?> getScript(String xid) {
        return getSimpleJdbcTemplate().queryForObject(SCRIPT_SELECT + " where xid=?", new ScriptRowMapper(), xid);
    }
}
