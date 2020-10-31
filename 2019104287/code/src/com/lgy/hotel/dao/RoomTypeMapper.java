package com.lgy.hotel.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.lgy.hotel.pojo.RoomType;

@Repository
public interface RoomTypeMapper {

	/**
	 * 查询所有分类
	 * @return
	 */
	List<RoomType> findAllType();

	/**
	 * 根据房间类型id查询房间类型名称
	 * @param roomTypeId
	 * @return
	 */
	RoomType findTypeNameById(Integer roomTypeId);

}
