package com.lgy.hotel.pojo;

import java.io.Serializable;

/**
 * 用户实体类
 * 
 * @author xlisteven
 *
 */
public class User implements Serializable {
	private Integer userId;// 用户Id
	private String username;// 用户名称
	private String nickName;// 用户昵称
	private String password;// 用户密码
	private String roleId;// 用户角色（因为两个角色就不单独分表了1、前台用户；0、管理员）

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

}
