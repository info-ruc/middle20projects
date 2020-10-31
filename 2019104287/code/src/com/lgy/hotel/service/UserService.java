package com.lgy.hotel.service;

import java.util.List;

import com.lgy.hotel.pojo.User;

public interface UserService {

	int saveUser(User user);

	User findUser(User user);

	List<User> findAllTenant();

	int deleteUser(int userId);

	List<User> findAllAdmin();

	void updatePwd(String newPwd, Integer userId);

}
