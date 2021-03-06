/**
 * Copyright (c) 2011-2016, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.plugin.activerecord;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.eova.common.utils.xx;
import com.eova.common.utils.db.DbUtil;

/**
 * RecordBuilder.
 */
public class RecordBuilder {
	
	@SuppressWarnings("unchecked")
	public static final List<Record> build(Config config, ResultSet rs) throws SQLException {
		List<Record> result = new ArrayList<Record>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		String[] labelNames = new String[columnCount + 1];
		int[] types = new int[columnCount + 1];
		buildLabelNamesAndTypes(rsmd, labelNames, types);
		while (rs.next()) {
			Record record = new Record();
			record.setColumnsMap(config.containerFactory.getColumnsMap());
			Map<String, Object> columns = record.getColumns();
			for (int i=1; i<=columnCount; i++) {
				Object value;
				
				if(rs.getObject(i) == null){//edit by jzhao，对于INTEGER，BIGINT，FLOAT，DOUBLE 等类型，判断值是否为空，至于是否需要转换成对象待测验
					value = null;
				}else if (types[i] == Types.INTEGER || types[i] == Types.TINYINT || types[i] == Types.SMALLINT){
					value = rs.getInt(i);
					
				}else if (types[i] == Types.BIGINT)
					value = rs.getLong(i);
				else if (types[i] == Types.FLOAT)
					value = rs.getFloat(i);
				else if (types[i] == Types.DOUBLE)
					value = rs.getDouble(i);
				else if (types[i] == Types.TIMESTAMP || (xx.isOracle() && types[i] == Types.DATE ))
					value = rs.getTimestamp(i);
				else if (types[i] == Types.DATE)
					value = rs.getDate(i);
				else if (types[i] < Types.BLOB)
					value = rs.getObject(i);
				else if (types[i] == Types.CLOB)
					value = ModelBuilder.handleClob(rs.getClob(i));
				else if (types[i] == Types.NCLOB)
					value = ModelBuilder.handleClob(rs.getNClob(i));
				else if (types[i] == Types.BLOB)
					value = ModelBuilder.handleBlob(rs.getBlob(i));
				else
					value = rs.getObject(i);
				
				// Oracle强制类型转换
				if (value != null && xx.isOracle()) {
//					System.out.println("convert before:" + value.getClass());
					value = DbUtil.convertOracleValue(value, types[i]);
//					System.out.println("convert after:" + value.getClass());
				}

				columns.put(labelNames[i], value);
			}
			result.add(record);
		}
		return result;
	}
	
	private static final void buildLabelNamesAndTypes(ResultSetMetaData rsmd, String[] labelNames, int[] types) throws SQLException {
		for (int i=1; i<labelNames.length; i++) {
			labelNames[i] = rsmd.getColumnLabel(i);
			types[i] = rsmd.getColumnType(i);
		}
	}
	
	/* backup before use columnType
	static final List<Record> build(ResultSet rs) throws SQLException {
		List<Record> result = new ArrayList<Record>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		String[] labelNames = getLabelNames(rsmd, columnCount);
		while (rs.next()) {
			Record record = new Record();
			Map<String, Object> columns = record.getColumns();
			for (int i=1; i<=columnCount; i++) {
				Object value = rs.getObject(i);
				columns.put(labelNames[i], value);
			}
			result.add(record);
		}
		return result;
	}
	
	private static final String[] getLabelNames(ResultSetMetaData rsmd, int columnCount) throws SQLException {
		String[] result = new String[columnCount + 1];
		for (int i=1; i<=columnCount; i++)
			result[i] = rsmd.getColumnLabel(i);
		return result;
	}
	*/
	
	/* backup
	static final List<Record> build(ResultSet rs) throws SQLException {
		List<Record> result = new ArrayList<Record>();
		ResultSetMetaData rsmd = rs.getMetaData();
		List<String> labelNames = getLabelNames(rsmd);
		while (rs.next()) {
			Record record = new Record();
			Map<String, Object> columns = record.getColumns();
			for (String lableName : labelNames) {
				Object value = rs.getObject(lableName);
				columns.put(lableName, value);
			}
			result.add(record);
		}
		return result;
	}
	
	private static final List<String> getLabelNames(ResultSetMetaData rsmd) throws SQLException {
		int columCount = rsmd.getColumnCount();
		List<String> result = new ArrayList<String>();
		for (int i=1; i<=columCount; i++) {
			result.add(rsmd.getColumnLabel(i));
		}
		return result;
	}
	*/
}





