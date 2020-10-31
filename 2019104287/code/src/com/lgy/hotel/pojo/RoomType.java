package com.lgy.hotel.pojo;

import java.io.Serializable;

/**
 * 房间类型
 * @author xlisteven
 *
 */
public class RoomType implements Serializable {

	private Integer typeId;//类型iD
	private String typeName;//房间类型名称
	public Integer getTypeId() {
		return typeId;
	}
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	
}
