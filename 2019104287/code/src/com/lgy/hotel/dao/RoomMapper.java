package com.lgy.hotel.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.lgy.hotel.pojo.Room;

@Repository
public interface RoomMapper {

	/**
	 * 查询所有商务套房
	 * 
	 * @param type
	 * @return
	 */
	List<Room> findAllBusinessRoom(int type);

	/**
	 * 通过房间Id查询房间详细信息
	 * 
	 * @param roomId
	 * @return
	 */
	Room findRoomDetailsByRoomId(int roomId);

	/**
	 * 更新客房状态
	 * 
	 * @param roomNumber
	 * @param status
	 */
	void updateRoomStatus(@Param("roomNumber") String roomNumber, @Param("status") String status);

	/**
	 * 查询所有客房
	 * 
	 * @return
	 */
	List<Room> findAllRoom();

	/**
	 * 查询五种分类的房间各一条
	 * @return
	 */
	List<Room> findFiveRoom();

	/**
	 * 添加房间
	 * @param room
	 */
	void saveRoom(Room room);

	/**
	 * 删除房间
	 * @param roomId
	 * @return
	 */
	int deleteRoom(int roomId);

	/**
	 * 通过房间iD查询房间信息
	 * @param roomId
	 * @return
	 */
	Room findRoomByRoomId(int roomId);

	/**
	 * 更新房间信息
	 * @param room
	 */
	void updateRoomInfo(Room room);

}
