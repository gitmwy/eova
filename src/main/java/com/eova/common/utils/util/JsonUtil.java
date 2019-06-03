package com.eova.common.utils.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jfinal.plugin.activerecord.Record;

public class JsonUtil {
	
	/**
	 * JSON -> List&lt;Record>
	 * @param json
	 * @return
	 */
	public static List<Record> getRecords(String json) {
		List<Record> records = new ArrayList<Record>();

		List<JSONObject> list = JSON.parseArray(json, JSONObject.class);
		for (JSONObject o : list) {
			Map<String, Object> map = JSON.parseObject(o + "", new TypeReference<Map<String, Object>>() {
			});
			Record re = new Record();
			re.setColumns(map);
			records.add(re);
		}

		return records;
	}
}
