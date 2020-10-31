package com.lgy.hotel.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lgy.hotel.pojo.User;
import com.lgy.hotel.service.UserService;

/**
 * @author xlisteven 登录注册相关
 */
@Controller
public class LoginController {

	@Autowired
	UserService userService;
	@RequestMapping("/toLogin")
	public String toLogin() {
		// 跳转到登录注册页面
		return "login";
	}
	
	@RequestMapping("/register")
	@ResponseBody
	public String register(User user) {
		user.setRoleId("1");
		int result = userService.saveUser(user);
		return String.valueOf(result);
	}
	
	/**
	 * 登录
	 * @param user
	 * @param session
	 * @return
	 */
	@RequestMapping("/login")
	@ResponseBody
	public Map<String, Object> login(User user,HttpSession session) {
		Map<String, Object> map = new HashMap<String, Object>();
		User user2 = userService.findUser(user);
		if (user2 != null) {
			session.setAttribute("user", user2);
			map.put("code", 200);
			map.put("user",user2);
		}else {
			map.put("code", 500);
			map.put("user", null);
		}
		return map;
	}
	
	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "login";
	}
}
