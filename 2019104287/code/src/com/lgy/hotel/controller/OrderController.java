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
import com.lgy.hotel.service.RoomService;

/**
 * @author xlisteven 订单相关
 */
@Controller
public class OrderController {

	@Autowired
	OrderService orderService;
	@Autowired
	RoomService roomService;

	@RequestMapping("/unsubscribe")
	@ResponseBody
	public Map<String, Object> unsubscribe(String orderId, HttpSession session, String roomNumber) {
		User user = (User) session.getAttribute("user");
		Map<String, Object> map = new HashMap<String, Object>();
		String status = "已退订";
		int result = orderService.updateOrderStatus(orderId, status, user.getUserId());
		if (result > 0) {

			// 退订成功
			map.put("code", 200);
			map.put("msg", "退订成功");
			// 退订成功后，修改房间状态为可入住
			status = "可入住";
			roomService.updateRoomStatus(roomNumber, status);
		} else {
			// 退订失败
			map.put("code", 500);
			map.put("msg", "退订失败");
		}
		return map;
	}

	/**
	 * 查看所有订单
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/findAllOrder")
	public String findAllOrder(Model model) {
		List<Order> orderList = orderService.findAllOrder();
		if (orderList != null) {
			model.addAttribute("orderList", orderList);
		}
		return "admin/orderList";
	}

	/**
	 * 入住
	 * @param orderId
	 * @param roomNumber
	 * @param userId
	 * @return
	 */
	@RequestMapping("/checkIn")
	@ResponseBody
	public Map<String, Object> checkIn(String orderId, String roomNumber, String userId) {
		Map<String, Object> map = new HashMap<String, Object>();
		String status = "已入住";
		int result = orderService.updateOrderStatus(orderId, status, Integer.parseInt(userId));
		if (result > 0) {
			// 退订成功
			map.put("code", 200);
			map.put("msg", "入住成功");
			// 退订成功后，修改房间状态为可入住
			status = "已入住";
			roomService.updateRoomStatus(roomNumber, status);
		} else {
			map.put("code", 5500);
			map.put("msg", "入住失败");
		}
		return map;
	}
	/**
	 * 	退房
	 * @param orderId
	 * @param roomNumber
	 * @param userId
	 * @return
	 */
	@RequestMapping("/checkOut")
	@ResponseBody
	public Map<String, Object> checkOut(String orderId, String roomNumber, String userId) {
		Map<String, Object> map = new HashMap<String, Object>();
		String status = "已完成";
		int result = orderService.updateOrderStatus(orderId, status, Integer.parseInt(userId));
		if (result > 0) {
			// 退订成功
			map.put("code", 200);
			map.put("msg", "退房成功");
			// 退订成功后，修改房间状态为可入住
			status = "可入住";
			roomService.updateRoomStatus(roomNumber, status);
		} else {
			map.put("code", 5500);
			map.put("msg", "退房失败");
		}
		return map;
	}
	
	/**
	 *	 查询所有退订的信息
	 * @param model
	 * @return
	 */
	@RequestMapping("/findAllUnsubscribe")
	public String findAllUnsubscribe(Model model) {
		List<Order> orderList = orderService.findAllUnsubscribe();
		if(orderList != null) {
			model.addAttribute("orderList",orderList);
		}
		return "admin/unsubscribeList";
	}
	
	/**
	 * 	收益统计
	 * @return
	 */
	@RequestMapping("/revenueStatistics")
	public String revenueStatistics(Model model) {
		//获取所有已完成的订单
		List<Order> orderList = orderService.findRevenueStatistics();
		//获取所有已完成订单的总和
		double money = orderService.findAllMoney();
		if (orderList != null) {
			model.addAttribute("orderList",orderList);
			model.addAttribute("money",money);
		}
		return "admin/revenueStatisticsList";
	}
	
	/**
	 * 入住统计
	 * @return
	 */
	@RequestMapping("/findAllCheckIn")
	public String findAllCheckIn(Model model) {
		List<Order> orderList = orderService.findAllCheckIn();
		if(orderList != null) {
			model.addAttribute("orderList",orderList);
		}
		return "admin/checkInList";
	}
}
