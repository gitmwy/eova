package com.eova.engine;

import com.alibaba.druid.sql.SQLUtils;

public class TestCv {
	public static void main(String[] args) {
		String sql = SQLUtils.translateOracleToMySql("create table a{a};");
		System.out.println(sql);
	}
}
