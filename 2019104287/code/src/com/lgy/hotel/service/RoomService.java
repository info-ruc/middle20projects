package com.lgy.hotel.service;

import java.util.List;

import com.lgy.hotel.pojo.Room;

public interface RoomService {

	List<Room> findAllBusinessRoom(int type);

	Room findRoomDetailsByRoomId(int roomId);

	void updateRoomStatus(String roomNumber, String status);

	List<Room> findAllRoom();

	List<Room> findFiveRoom();

	void saveRoom(Room room);

	int deleteRoom(int roomId);

	Room findRoomByRoomId(int roomId);

	void updateRoomInfo(Room room);

}
