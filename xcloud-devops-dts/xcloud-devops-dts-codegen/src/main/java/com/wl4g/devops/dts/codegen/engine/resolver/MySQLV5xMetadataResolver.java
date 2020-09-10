package com.wl4g.devops.dts.codegen.engine.resolver;

import com.wl4g.devops.dts.codegen.bean.GenDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.wl4g.components.common.lang.Assert2.notNullOf;

/**
 * {@link MySQLV5xMetadataResolver}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @version 2020-09-08
 * @sine v1.0.0
 * @see
 */
public class MySQLV5xMetadataResolver extends AbstractMetadataResolver {

	@Override
	public List<String> loadTable(GenDatabase genDatabase) {
		String databaseUrl = "jdbc:mysql://" + genDatabase.getHost() + ":" + genDatabase.getPort() + "/"
				+ genDatabase.getDatabase();
		return getTables(databaseUrl, genDatabase.getUsername(), genDatabase.getPassword());
	}

	@Override
	public TableMetadata loadTable(GenDatabase genDatabase, String tableName) {
		String databaseUrl = "jdbc:mysql://" + genDatabase.getHost() + ":" + genDatabase.getPort() + "/"
				+ genDatabase.getDatabase();
		return getTable(databaseUrl, genDatabase.getUsername(), genDatabase.getPassword(), tableName);
	}

	@Override
	public void loadForeign(String databaseName, String tableName) throws Exception {
		String sql = loadResolvingSql(SQL_TYPE, SQL_QUERY_FOREIGN, databaseName, tableName);
		// TODO
	}

	private List<String> getTables(String databaseUrl, String user, String password) {
		List<String> tables = new ArrayList<String>();
		Connection connect = null;
		try {
			connect = openConnection(databaseUrl, user, password, JDBC_CLASS_NAME);
			DatabaseMetaData dbmd = connect.getMetaData();

			ResultSet rs = dbmd.getTables(null, null, null, new String[] { "TABLE" });
			while (rs.next()) {
				tables.add(rs.getString("TABLE_NAME"));
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (null != connect) {
				try {
					connect.close();
				} catch (SQLException e) {
					log.error("close connect fail", e);
				}
			}
		}
		return tables;
	}

	/**
	 * Get Table Info
	 */
	private TableMetadata getTable(String databaseUrl, String user, String password, String tableName) {
		Connection connect = null;
		try {
			connect = openConnection(databaseUrl, user, password, JDBC_CLASS_NAME);
			TableMetadata table = getTableInfo(connect, tableName);
			notNullOf(table, "table");
			List<TableMetadata.ColumnMetadata> tableCloumns = getTableCloumns(connect, tableName);
			table.setColumns(tableCloumns);
			return table;
		} catch (Exception e) {
			log.error("get Table Info error", e);
		} finally {
			if (connect != null) {
				try {
					connect.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 获取表字段信息
	 */
	private List<TableMetadata.ColumnMetadata> getTableCloumns(Connection connect, String tableName) throws Exception {
		List<TableMetadata.ColumnMetadata> columns = new ArrayList<>();
		Statement stmt = connect.createStatement();
		String sql = loadResolvingSql(SQL_TYPE, SQL_QUERY_COLUMNS, tableName);
		ResultSet rs = stmt.executeQuery(sql);
		while (rs.next()) {
			TableMetadata.ColumnMetadata column = new TableMetadata.ColumnMetadata();
			column.setColumnName(rs.getString("columnName"));
			column.setColumnType(rs.getString("columnType"));
			column.setDataType(rs.getString("dataType"));
			column.setComments(rs.getString("columnComment"));
			column.setColumnKey(rs.getString("columnKey"));
			column.setExtra(rs.getString("extra"));
			columns.add(column);
		}
		return columns;
	}

	/**
	 * 获取表字段信息
	 */
	private TableMetadata getTableInfo(Connection connect, String tableName) throws Exception {
		TableMetadata table = new TableMetadata();
		Statement stmt = connect.createStatement();
		String sql = loadResolvingSql(SQL_TYPE, SQL_QUERY_TABLE, tableName);
		ResultSet rs = stmt.executeQuery(sql);
		if (rs.next()) {// just one
			HashMap<String, String> map = new HashMap<String, String>();
			table.setTableName(rs.getString("tableName"));
			table.setComments(rs.getString("tableComment"));
			return table;
		}
		return null;
	}

	// MySQL jdbc
	final private static String SQL_TYPE = "mysql";
	final private static String JDBC_CLASS_NAME = "com.mysql.jdbc.Driver";

}