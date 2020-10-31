package com.lgy.hotel.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.lgy.hotel.pojo.User;

public interface UserMapper {

	/**
	 * 注册用户
	 * 
	 * @param user
	 * @return
	 */
	int saveUser(User user);

	/**
	 * 登录
	 * 
	 * @param user
	 * @return
	 */
	User findUser(User user);

	/**
	 * 查找所有房客
	 * 
	 * @return
	 */
	List<User> findAllTenant();

	/**
	 * 删除用户
	 * 
	 * @param userId
	 * @return
	 */
	int deleteUser(int userId);

	/**
	 * 查询所有管理员
	 * 
	 * @return
	 */
	List<User> findAllAdmin();

	void updatePwd(@Param("newPwd")String newPwd, @Param("userId")Integer userId);

}
