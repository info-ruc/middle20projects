package com.lgy.hotel.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lgy.hotel.pojo.Order;
import com.lgy.hotel.pojo.User;
import com.lgy.hotel.service.OrderService;
import com.lgy.hotel.service.UserService;

/**
 * 用户相关
 * 
 * @author xlisteven
 *
 */
@Controller
public class UserController {

	@Autowired
	OrderService orderService;
	@Autowired
	UserService userService;

	/**
	 * 订单中心
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping("/personalCenter")
	public String personalCenter(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			List<Order> orderList = orderService.findOrderByUserId(user.getUserId());
			model.addAttribute("orderList", orderList);
		}
		return "orderList";
	}

	/**
	 * 查询所有房客
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/findAllTenant")
	public String findAllTenant(Model model) {
		List<User> userList = userService.findAllTenant();
		model.addAttribute("userList", userList);
		return "admin/userList";
	}

	/**
	 * 删除房客
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping("/deleteUser")
	@ResponseBody
	public Map<String, Object> deleteUser(String userId) {
		Map<String, Object> map = new HashMap<String, Object>();
		int result = userService.deleteUser(Integer.parseInt(userId));
		if (result > 0) {
			map.put("code", 200);
			map.put("msg", "删除成功");
		} else {
			map.put("code", 500);
			map.put("msg", "删除失败");
		}
		return map;
	}
	
	/**
	 * 	查询所有管理员
	 * @param model
	 * @return
	 */
	@RequestMapping("/findAllAdmin")
	public String findAllAdmin(Model model) {
		List<User> adminList = userService.findAllAdmin();
		if (adminList != null) {
			model.addAttribute("adminList",adminList);
			model.addAttribute("totalAdmin", adminList.size());
		}
		return "admin/adminList";
	}
	
	/**
	 * 添加管理员
	 * @param user
	 * @return
	 */
	@RequestMapping("/addAdmin")
	public String addAdmin(User user) {
		user.setRoleId("0");
		int result = userService.saveUser(user);
		if(result > 0) {
			//添加成功
			return "redirect:/findAllAdmin";
		}
		return "";
	}
	/**
	 * 添加用户
	 * @param user
	 * @return
	 */
	@RequestMapping("/addUser")
	public String addUser(User user) {
		user.setRoleId("1");
		int result = userService.saveUser(user);
		if(result > 0) {
			//添加成功
			return "redirect:/findAllTenant";
		}
		return "";
	}
}
