package com.lgy.hotel.pojo;

import java.io.Serializable;

/**
 * 房间实体类
 * 
 * @author xlisteven
 *
 */
public class Room implements Serializable {
	private Integer roomId;// 房间ID
	private String roomNumber;// 房间号
	private String roomName;// 房间名称
	private Double roomArea;// 房间面积
	private String roomPrice;// 房间价格
	private String roomDesc;// 房间描述
	private String roomImg;// 房间图片
	private String specail1;// 房间特点1
	private String specail2;// 房间特点2
	private String specail3;// 房间特点3
	private String specail4;// 房间特点4
	private String specail5;// 房间特点5
	private String roomStatus;// 房间状态
	private Integer roomTypeId;// 房间类型ID
	private String typeName;// 房间类型名称

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public Double getRoomArea() {
		return roomArea;
	}

	public void setRoomArea(Double roomArea) {
		this.roomArea = roomArea;
	}

	public String getRoomPrice() {
		return roomPrice;
	}

	public void setRoomPrice(String roomPrice) {
		this.roomPrice = roomPrice;
	}

	public String getRoomDesc() {
		return roomDesc;
	}

	public void setRoomDesc(String roomDesc) {
		this.roomDesc = roomDesc;
	}

	public String getRoomImg() {
		return roomImg;
	}

	public void setRoomImg(String roomImg) {
		this.roomImg = roomImg;
	}

	public String getSpecail1() {
		return specail1;
	}

	public void setSpecail1(String specail1) {
		this.specail1 = specail1;
	}

	public String getSpecail2() {
		return specail2;
	}

	public void setSpecail2(String specail2) {
		this.specail2 = specail2;
	}

	public String getSpecail3() {
		return specail3;
	}

	public void setSpecail3(String specail3) {
		this.specail3 = specail3;
	}

	public String getSpecail4() {
		return specail4;
	}

	public void setSpecail4(String specail4) {
		this.specail4 = specail4;
	}

	public String getSpecail5() {
		return specail5;
	}

	public void setSpecail5(String specail5) {
		this.specail5 = specail5;
	}

	public String getRoomStatus() {
		return roomStatus;
	}

	public void setRoomStatus(String roomStatus) {
		this.roomStatus = roomStatus;
	}

	public Integer getRoomTypeId() {
		return roomTypeId;
	}

	public void setRoomTypeId(Integer roomTypeId) {
		this.roomTypeId = roomTypeId;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

}
