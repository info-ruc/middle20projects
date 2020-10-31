package com.lgy.hotel.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lgy.hotel.dao.RoomMapper;
import com.lgy.hotel.pojo.Room;

/**
 * @author xlisteven 
 *      房间相关
 */
@Service
public class RoomServiceImpl implements RoomService {

	@Autowired
	RoomMapper roomMapper;

	@Override
	public List<Room> findAllBusinessRoom(int type) {
		return roomMapper.findAllBusinessRoom(type);
	}

	@Override
	public Room findRoomDetailsByRoomId(int roomId) {
		return roomMapper.findRoomDetailsByRoomId(roomId);
	}

	@Override
	public void updateRoomStatus(String roomNumber, String status) {
		roomMapper.updateRoomStatus(roomNumber,status);
	}

	@Override
	public List<Room> findAllRoom() {
		return roomMapper.findAllRoom();
	}

	@Override
	public List<Room> findFiveRoom() {
		return roomMapper.findFiveRoom();
	}

	@Override
	public void saveRoom(Room room) {
		roomMapper.saveRoom(room);
	}

	@Override
	public int deleteRoom(int roomId) {
		return roomMapper.deleteRoom(roomId);
	}

	@Override
	public Room findRoomByRoomId(int roomId) {
		return roomMapper.findRoomByRoomId(roomId);
	}

	@Override
	public void updateRoomInfo(Room room) {
		roomMapper.updateRoomInfo(room);
	}
}
