package com.serotonin.mango.db.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Base class for {@link Clinic} integration tests.
 * </p>
 * <p>
 * &quot;AbstractClinicTests-context.xml&quot; declares a common
 * {@link javax.sql.DataSource DataSource}. Subclasses should specify
 * additional context locations which declare a
 * {@link org.springframework.transaction.PlatformTransactionManager PlatformTransactionManager}
 * and a concrete implementation of {@link Clinic}.
 * </p>
 * <p>
 * This class extends {@link AbstractTransactionalJUnit4SpringContextTests},
 * one of the valuable testing support classes provided by the
 * <em>Spring TestContext Framework</em> found in the
 * <code>org.springframework.test.context</code> package. The
 * annotation-driven configuration used here represents best practice for
 * integration tests with Spring. Note, however, that
 * AbstractTransactionalJUnit4SpringContextTests serves only as a convenience
 * for extension. For example, if you do not wish for your test classes to be
 * tied to a Spring-specific class hierarchy, you may configure your tests with
 * annotations such as {@link ContextConfiguration @ContextConfiguration},
 * {@link org.springframework.test.context.TestExecutionListeners @TestExecutionListeners},
 * {@link org.springframework.transaction.annotation.Transactional @Transactional},
 * etc.
 * </p>
 * <p>
 * AbstractClinicTests and its subclasses benefit from the following services
 * provided by the Spring TestContext Framework:
 * </p>
 * <ul>
 * <li><strong>Spring IoC container caching</strong> which spares us
 * unnecessary set up time between test execution.</li>
 * <li><strong>Dependency Injection</strong> of test fixture instances,
 * meaning that we don't need to perform application context lookups. See the
 * use of {@link Autowired @Autowired} on the <code>clinic</code> instance
 * variable, which uses autowiring <em>by type</em>. As an alternative, we
 * could annotate <code>clinic</code> with
 * {@link javax.annotation.Resource @Resource} to achieve dependency injection
 * <em>by name</em>.
 * <em>(see: {@link ContextConfiguration @ContextConfiguration},
 * {@link org.springframework.test.context.support.DependencyInjectionTestExecutionListener DependencyInjectionTestExecutionListener})</em></li>
 * <li><strong>Transaction management</strong>, meaning each test method is
 * executed in its own transaction, which is automatically rolled back by
 * default. Thus, even if tests insert or otherwise change database state, there
 * is no need for a teardown or cleanup script.
 * <em>(see: {@link org.springframework.test.context.transaction.TransactionConfiguration @TransactionConfiguration},
 * {@link org.springframework.transaction.annotation.Transactional @Transactional},
 * {@link org.springframework.test.context.transaction.TransactionalTestExecutionListener TransactionalTestExecutionListener})</em></li>
 * <li><strong>Useful inherited protected fields</strong>, such as a
 * {@link org.springframework.jdbc.core.simple.SimpleJdbcTemplate SimpleJdbcTemplate}
 * that can be used to verify database state after test operations or to verify
 * the results of queries performed by application code. An
 * {@link org.springframework.context.ApplicationContext ApplicationContext} is
 * also inherited and can be used for explicit bean lookup if necessary.
 * <em>(see: {@link org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests AbstractJUnit4SpringContextTests},
 * {@link AbstractTransactionalJUnit4SpringContextTests})</em></li>
 * </ul>
 * <p>
 * The Spring TestContext Framework and related unit and integration testing
 * support classes are shipped in <code>spring-test.jar</code>.
 * </p>
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@ContextConfiguration
public abstract class AbstractDaoTests extends AbstractTransactionalJUnit4SpringContextTests {

    @After
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void tearDown() {
//TODO no need to clean the tables after commit???
//        executeSqlScript("classpath:db/cleanAllTables.sql", false);
    }
    /** calendar to write and read timestamps in utc rather local time,
     * many driverser have problems with '2011-10-30 00:59:59.0000+0:00' which is '2011-10-30 02:59:59.0000 CEST' this timestamp plus 1 second is:
     * '2011-10-30 01:00:00.0000+0:00' or '2011-10-30 02:00:00.0000 CET'
     */
    private final static Calendar CALENDAR_UTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private final static Calendar C_CEST = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private final static Calendar C_CET = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    static {
        //1ms befor turning back the clock 1 hour most often this is interpreded 1 hour later ...
        C_CEST.set(2011, 9, 30, 0, 59, 59);
        C_CEST.set(Calendar.MILLISECOND, 999);

        C_CET.set(2011, 9, 30, 01, 00, 00);
        C_CET.set(Calendar.MILLISECOND, 0);

    }

    /**
     * This test on a System with timezone "Europe/Berlin" the correctness of 
     * the returned data for a critical hour (the clock switches form 3 am summertimne to 2 am wintertime also the hour from 2 to 3 is double
     * most driver will convert C_CEST wrong ... 
     * 
     * Some driver will cut milliseconds ...
     * 
     * Results (Thu Oct 27 15:07:14 CEST 2011):
     *   mysql faild to retrieve corr3ect timestam and has no fraction of second
     *      TIMESTAMP TEST (FORCE UTC): 0 ORIGINAL: Sun Oct 30 02:59:59 CEST 2011 RETRIEVED: Sun Oct 30 02:59:59 CET 2011 DIFF: -3599001
     *      TIMESTAMP TEST:             1 ORIGINAL: Sun Oct 30 02:59:59 CEST 2011 RETRIEVED: Sun Oct 30 02:59:59 CET 2011 DIFF: -3599001
     *      TIMESTAMP TEST (FORCE UTC): 2 ORIGINAL: Sun Oct 30 02:00:00 CET 2011 RETRIEVED: Sun Oct 30 02:00:00 CET 2011 DIFF: 0
     *      TIMESTAMP TEST:             3 ORIGINAL: Sun Oct 30 02:00:00 CET 2011 RETRIEVED: Sun Oct 30 02:00:00 CET 2011 DIFF: 0
     *   hsql correct if forced UTC save/retrieve is in effect also fraction of second
     *   derby correct if forced UTC save/retrieve is in effect also fraction of second
     *      TIMESTAMP TEST (FORCE UTC): 0 ORIGINAL: Sun Oct 30 02:59:59 CEST 2011 RETRIEVED: Sun Oct 30 02:59:59 CEST 2011 DIFF: 0
     *      TIMESTAMP TEST:             1 ORIGINAL: Sun Oct 30 02:59:59 CEST 2011 RETRIEVED: Sun Oct 30 02:59:59 CET 2011 DIFF: -3600000
     *      TIMESTAMP TEST (FORCE UTC): 2 ORIGINAL: Sun Oct 30 02:59:59 CEST 2011 RETRIEVED: Sun Oct 30 02:00:00 CET 2011 DIFF: 0
     *      TIMESTAMP TEST:             3 ORIGINAL: Sun Oct 30 02:00:00 CET 2011 RETRIEVED: Sun Oct 30 02:00:00 CET 2011 DIFF: 0

     * 
     * @param jdbcTemplate 
     */
    public void test_UTC_DaylightSaving(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("create table timestampTest (id int, ts timestamp)");
        try {
            jdbcTemplate.batchUpdate("insert into timestampTest (id, ts) values (?, ?)", new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, i);
                    switch (i) {
                        case 0:
                            ps.setTimestamp(2, new Timestamp(C_CEST.getTimeInMillis()), CALENDAR_UTC);
                            break;
                        case 1:
                            ps.setTimestamp(2, new Timestamp(C_CEST.getTimeInMillis()));
                            break;
                        case 2:
                            ps.setTimestamp(2, new Timestamp(C_CET.getTimeInMillis()), CALENDAR_UTC);
                            break;
                        case 3:
                            ps.setTimestamp(2, new Timestamp(C_CET.getTimeInMillis()));
                            break;
                    }
                }

                @Override
                public int getBatchSize() {
                    return 4;
                }
            });
            final StringBuilder sb = new StringBuilder();
            
            jdbcTemplate.query("select id, ts  from timestampTest", new RowCallbackHandler() {

                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    Date d;
                    long diff;
                    final int i = rs.getInt("id");
                    switch (i) {
                        case 0:
                            sb.append("TIMESTAMP TEST (FORCE UTC): ").append(i).append(" ORIGINAL: ").append(C_CEST.getTime());
                            d = new Date(rs.getTimestamp("ts", C_CET).getTime());
                            diff = C_CEST.getTimeInMillis() - d.getTime();
                            break;
                        case 1:
                            sb.append("TIMESTAMP TEST:             ").append(i).append(" ORIGINAL: ").append(C_CEST.getTime());
                                    d = new Date(rs.getTimestamp("ts").getTime());
                            diff = C_CEST.getTimeInMillis() - d.getTime();
                            break;
                        case 2:
                            sb.append("TIMESTAMP TEST (FORCE UTC): ").append(i).append(" ORIGINAL: ").append(C_CET.getTime());
                            d = new Date(rs.getTimestamp("ts", C_CET).getTime());
                            diff = C_CET.getTimeInMillis() - d.getTime();
                            break;
                        case 3:
                            sb.append("TIMESTAMP TEST:             ").append(i).append(" ORIGINAL: ").append(C_CET.getTime());
                            d = new Date(rs.getTimestamp("ts").getTime());
                            diff = C_CET.getTimeInMillis() - d.getTime();
                            break;
                            default: throw  new RuntimeException("unexpected data");
                    }
                    sb.append(" RETRIEVED: ").append(d).append(" DIFF: ").append(diff);
                    sb.append("\n");
                }
                
            });
            System.err.println(sb.toString());
            System.err.println(Calendar.getInstance().getTime());
        } finally {
            jdbcTemplate.execute("drop table timestampTest");
        }
    }
}
