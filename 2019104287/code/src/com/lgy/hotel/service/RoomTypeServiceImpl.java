package com.lgy.hotel.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lgy.hotel.dao.RoomTypeMapper;
import com.lgy.hotel.pojo.RoomType;

/**
 * 房间类型
 * @author xlisteven
 *
 */
@Service
public class RoomTypeServiceImpl implements RoomTypeService {

	@Autowired
	RoomTypeMapper roomTypeMapper;
	@Override
	public List<RoomType> findAllType() {
		return roomTypeMapper.findAllType();
	}
	@Override
	public RoomType findTypeNameById(Integer roomTypeId) {
		return roomTypeMapper.findTypeNameById(roomTypeId);
	}

}
