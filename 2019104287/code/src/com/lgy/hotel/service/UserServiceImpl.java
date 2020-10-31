package com.lgy.hotel.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lgy.hotel.dao.UserMapper;
import com.lgy.hotel.pojo.User;

/**
 * @author xlisteven
 *	用户相关
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserMapper userMapper;

	@Override
	public int saveUser(User user) {
		return userMapper.saveUser(user);
	}

	@Override
	public User findUser(User user) {
		return userMapper.findUser(user);
	}

	@Override
	public List<User> findAllTenant() {
		return userMapper.findAllTenant();
	}

	@Override
	public int deleteUser(int userId) {
		return userMapper.deleteUser(userId);
	}

	@Override
	public List<User> findAllAdmin() {
		return userMapper.findAllAdmin();
	}

	@Override
	public void updatePwd(String newPwd, Integer userId) {
		userMapper.updatePwd(newPwd,userId);
	}
}
