<%
//可能不需要隐藏配置模式  force
if(isEmpty(force!))
	force = 0;

//debug(f.en+','+force);
if(!isEmpty(data!)){
										// 有数据为更新模式
										if(force == 1 ||f.update_status < 50){		 	// 未禁用,以及强制显示的
	
											var value = @data.get(f.en);	// 获取当前字段的值
											var isReadOnly = false;		// 当前字段是否只读
											if(f.update_status == 10){
												isReadOnly = true;
											}
											
											// 获取固定值(必须是JSON)
											if(!isEmpty(fixed)){
												// JSON对象化
												var fix = parseJson(fixed);
												var fixedValue = fix[f.en];
												// 固定值覆盖动态值并且只读
												if(!isEmpty(fixedValue)){
													value = fixedValue;
													isReadOnly = true;
												}
											}
											
											
											
											
											
												%>
												     <#field_lay item="${f}" value="${value!f.defaulter!}" readnoly="${isReadOnly}" />
												<%
											
										}
									} else {
										// 无数据为新增模式
										if(force == 1 || f.add_status < 50){			// 未禁用,以及强制显示的
											var value = null;
											var isReadOnly = false;		// 当前字段是否只读
											if(f.add_status == 10){
												isReadOnly = true;
											}
											
											// 获取固定值(必须是JSON)
											if(!isEmpty(fixed)){
												// JSON对象化
												var fix = parseJson(fixed);
												var fixedValue = fix[f.en];
												// 固定值覆盖动态值并且只读
												if(!isEmpty(fixedValue)){
													value = fixedValue;
													isReadOnly = true;
												}
											}
											
											
											
											
												%>

												    <#field_lay item="${f}" value="${value!f.defaulter!}" readnoly="${isReadOnly}" />
												<%
											
										}
									}