///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.apache.log4j.db;
//
//import org.apache.log4j.AppenderSkeleton;
//import org.apache.log4j.db.dialect.SQLDialect;
//import org.apache.log4j.db.dialect.Util;
//import org.apache.log4j.helpers.LogLog;
//import org.apache.log4j.spi.LocationInfo;
//import org.apache.log4j.spi.LoggingEvent;
//import org.apache.log4j.xml.DOMConfigurator;
//import org.apache.log4j.xml.UnrecognizedElementHandler;
//import org.w3c.dom.Element;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.Properties;
//import java.util.Set;
//
//
///**
// * The DBAppender inserts loggin events into three database tables in a format
// * independent of the Java programming language. The three tables that
// * DBAppender inserts to must exists before DBAppender can be used. These tables
// * may be created with the help of SQL scripts found in the
// * <em>src/java/org/apache/log4j/db/dialect</em> directory. There is a
// * specific script for each of the most popular database systems. If the script
// * for your particular type of database system is missing, it should be quite
// * easy to write one, taking example on the already existing scripts. If you
// * send them to us, we will gladly include missing scripts in future releases.
// * <p>
// * <p>
// * If the JDBC driver you are using supports the
// * {@link java.sql.Statement#getGeneratedKeys}method introduced in JDBC 3.0
// * specification, then you are all set. Otherwise, there must be an
// * {@link SQLDialect}appropriate for your database system. Currently, we have
// * dialects for PostgreSQL, MySQL, Oracle and MsSQL. As mentioed previously, an
// * SQLDialect is required only if the JDBC driver for your database system does
// * not support the {@link java.sql.Statement#getGeneratedKeys getGeneratedKeys}
// * method.
// * </p>
// * <p>
// * <table border="1" cellpadding="4">
// * <caption>supported dialects</caption>
// * <tr>
// * <th>RDBMS</th>
// * <th>supports <br><code>getGeneratedKeys()</code> method</th>
// * <th>specific <br>SQLDialect support</th>
// * <tr>
// * <tr>
// * <td>PostgreSQL</td>
// * <td align="center">NO</td>
// * <td>present and used</td>
// * <tr>
// * <tr>
// * <td>MySQL</td>
// * <td align="center">YES</td>
// * <td>present, but not actually needed or used</td>
// * <tr>
// * <tr>
// * <td>Oracle</td>
// * <td align="center">YES</td>
// * <td>present, but not actually needed or used</td>
// * <tr>
// * <tr>
// * <td>DB2</td>
// * <td align="center">YES</td>
// * <td>not present, and not needed or used</td>
// * <tr>
// * <tr>
// * <td>MsSQL</td>
// * <td align="center">YES</td>
// * <td>not present, and not needed or used</td>
// * <tr>
// * <tr>
// * <td>HSQL</td>
// * <td align="center">NO</td>
// * <td>present and used</td>
// * <tr>
// * <p>
// * </table>
// * <p>
// * <b>Performance: </b> Experiments show that writing a single event into the
// * database takes approximately 50 milliseconds, on a "standard" PC. If pooled
// * connections are used, this figure drops to under 10 milliseconds. Note that
// * most JDBC drivers already ship with connection pooling support.
// * </p>
// * <p>
// * <p>
// * <p>
// * <p>
// * <b>Configuration </b> DBAppender can be configured programmatically, or using
// * {@link org.apache.log4j.xml.DOMConfigurator JoranConfigurator}. Example
// * scripts can be found in the <em>tests/input/db</em> directory.
// *
// * @author Ceki G&uuml;lc&uuml;
// * @author Ray DeCampo
// */
//public class DBAppender extends AppenderSkeleton implements UnrecognizedElementHandler {
//    static final String insertPropertiesSQL =
//        "INSERT INTO  logging_event_property (event_id, mapped_key, mapped_value) VALUES (?, ?, ?)";
//    static final String insertExceptionSQL =
//        "INSERT INTO  logging_event_exception (event_id, i, trace_line) VALUES (?, ?, ?)";
//    static final String insertSQL;
//
//
//    static {
//        String sql = "INSERT INTO logging_event (" +
//            "sequence_number, " +
//            "timestamp, " +
//            "rendered_message, " +
//            "logger_name, " +
//            "level_string, " +
//            "ndc, " +
//            "thread_name, " +
//            "reference_flag, " +
//            "caller_filename, " +
//            "caller_class, " +
//            "caller_method, " +
//            "caller_line) " +
//            " VALUES (?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        insertSQL = sql;
//    }
//
//    ConnectionSource connectionSource;
//    boolean cnxSupportsGetGeneratedKeys = false;
//    boolean cnxSupportsBatchUpdates = false;
//    SQLDialect sqlDialect;
//    boolean locationInfo = false;
//
//
//    public DBAppender() {
//        super(false);
//    }
//
//    public void activateOptions() {
//        LogLog.debug("DBAppender.activateOptions called");
//
//        if (connectionSource == null) {
//            throw new IllegalStateException(
//                "DBAppender cannot function without a connection source");
//        }
//
//        sqlDialect = Util.getDialectFromCode(connectionSource.getSQLDialectCode());
//        cnxSupportsGetGeneratedKeys = connectionSource.supportsGetGeneratedKeys();
//        cnxSupportsBatchUpdates = connectionSource.supportsBatchUpdates();
//        if (!cnxSupportsGetGeneratedKeys && (sqlDialect == null)) {
//            throw new IllegalStateException(
//                "DBAppender cannot function if the JDBC driver does not support getGeneratedKeys method *and* without a specific SQL dialect");
//        }
//
//        // all nice and dandy on the eastern front
//        super.activateOptions();
//    }
//
//    /**
//     * @return Returns the connectionSource.
//     */
//    public ConnectionSource getConnectionSource() {
//        return connectionSource;
//    }
//
//    /**
//     * @param connectionSource The connectionSource to set.
//     */
//    public void setConnectionSource(ConnectionSource connectionSource) {
//        LogLog.debug("setConnectionSource called for DBAppender");
//        this.connectionSource = connectionSource;
//    }
//
//    protected void append(LoggingEvent event) {
//        Connection connection = null;
//        try {
//            connection = connectionSource.getConnection();
//            connection.setAutoCommit(false);
//
//            PreparedStatement insertStatement;
//            if (cnxSupportsGetGeneratedKeys) {
//                insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
//            } else {
//                insertStatement = connection.prepareStatement(insertSQL);
//            }
//
//            /*          insertStatement.setLong(1, event.getSequenceNumber());*/
//            insertStatement.setLong(1, 0);
//
//            insertStatement.setLong(2, event.getTimeStamp());
//            insertStatement.setString(3, event.getRenderedMessage());
//            insertStatement.setString(4, event.getLoggerName());
//            insertStatement.setString(5, event.getLevel().toString());
//            insertStatement.setString(6, event.getNDC());
//            insertStatement.setString(7, event.getThreadName());
//            insertStatement.setShort(8, DBHelper.computeReferenceMask(event));
//
//            LocationInfo li;
//
//            if (event.locationInformationExists() || locationInfo) {
//                li = event.getLocationInformation();
//            } else {
//                li = LocationInfo.NA_LOCATION_INFO;
//            }
//
//            insertStatement.setString(9, li.getFileName());
//            insertStatement.setString(10, li.getClassName());
//            insertStatement.setString(11, li.getMethodName());
//            insertStatement.setString(12, li.getLineNumber());
//
//            int updateCount = insertStatement.executeUpdate();
//            if (updateCount != 1) {
//                LogLog.warn("Failed to insert loggingEvent");
//            }
//
//            ResultSet rs = null;
//            Statement idStatement = null;
//            boolean gotGeneratedKeys = false;
//            if (cnxSupportsGetGeneratedKeys) {
//                rs = insertStatement.getGeneratedKeys();
//                gotGeneratedKeys = true;
//            }
//
//            if (!gotGeneratedKeys) {
//                insertStatement.close();
//                insertStatement = null;
//
//                idStatement = connection.createStatement();
//                idStatement.setMaxRows(1);
//                rs = idStatement.executeQuery(sqlDialect.getSelectInsertId());
//            }
//
//            // A ResultSet cursor is initially positioned before the first row; the
//            // first call to the method next makes the first row the current row
//            rs.next();
//            int eventId = rs.getInt(1);
//
//            rs.close();
//
//            // we no longer need the insertStatement
//            if (insertStatement != null) {
//                insertStatement.close();
//            }
//
//            if (idStatement != null) {
//                idStatement.close();
//            }
//
//            Set propertiesKeys = event.getPropertyKeySet();
//
//            if (propertiesKeys.size() > 0) {
//                PreparedStatement insertPropertiesStatement =
//                    connection.prepareStatement(insertPropertiesSQL);
//
//                for (Object propertiesKey : propertiesKeys) {
//                    String key = (String) propertiesKey;
//                    String value = event.getProperty(key);
//
//                    //LogLog.info("id " + eventId + ", key " + key + ", value " + value);
//                    insertPropertiesStatement.setInt(1, eventId);
//                    insertPropertiesStatement.setString(2, key);
//                    insertPropertiesStatement.setString(3, value);
//
//                    if (cnxSupportsBatchUpdates) {
//                        insertPropertiesStatement.addBatch();
//                    } else {
//                        insertPropertiesStatement.execute();
//                    }
//                }
//
//                if (cnxSupportsBatchUpdates) {
//                    insertPropertiesStatement.executeBatch();
//                }
//
//                insertPropertiesStatement.close();
//            }
//
//            String[] strRep = event.getThrowableStrRep();
//
//            if (strRep != null) {
//                LogLog.debug("Logging an exception");
//
//                PreparedStatement insertExceptionStatement =
//                    connection.prepareStatement(insertExceptionSQL);
//
//                for (short i = 0; i < strRep.length; i++) {
//                    insertExceptionStatement.setInt(1, eventId);
//                    insertExceptionStatement.setShort(2, i);
//                    insertExceptionStatement.setString(3, strRep[i]);
//                    if (cnxSupportsBatchUpdates) {
//                        insertExceptionStatement.addBatch();
//                    } else {
//                        insertExceptionStatement.execute();
//                    }
//                }
//                if (cnxSupportsBatchUpdates) {
//                    insertExceptionStatement.executeBatch();
//                }
//                insertExceptionStatement.close();
//            }
//
//            connection.commit();
//        } catch (Throwable sqle) {
//            LogLog.error("problem appending event", sqle);
//        } finally {
//            DBHelper.closeConnection(connection);
//        }
//    }
//
//    public void close() {
//        closed = true;
//    }
//
//    /**
//     * Returns value of the <b>LocationInfo </b> property which determines whether
//     * caller's location info is written to the database.
//     */
//    public boolean getLocationInfo() {
//        return locationInfo;
//    }
//
//    /**
//     * If true, the information written to the database will include caller's
//     * location information. Due to performance concerns, by default no location
//     * information is written to the database.
//     */
//    public void setLocationInfo(boolean locationInfo) {
//        this.locationInfo = locationInfo;
//    }
//
//    /**
//     * Gets whether appender requires a layout.
//     *
//     * @return false
//     */
//    public boolean requiresLayout() {
//        return false;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public boolean parseUnrecognizedElement(Element element, Properties props) throws Exception {
//        if ("connectionSource".equals(element.getNodeName())) {
//            Object instance =
//                DOMConfigurator.parseElement(element, props, ConnectionSource.class);
//            if (instance instanceof ConnectionSource) {
//                ConnectionSource source = (ConnectionSource) instance;
//                source.activateOptions();
//                setConnectionSource(source);
//            }
//            return true;
//        }
//        return false;
//    }
//}
