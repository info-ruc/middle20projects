package com.lgy.hotel.service;

import java.util.List;

import com.lgy.hotel.pojo.RoomType;

public interface RoomTypeService {

	List<RoomType> findAllType();

	RoomType findTypeNameById(Integer roomTypeId);

}
