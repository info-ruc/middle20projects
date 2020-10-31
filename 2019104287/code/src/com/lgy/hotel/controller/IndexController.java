package com.lgy.hotel.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lgy.hotel.pojo.Room;
import com.lgy.hotel.service.RoomService;

/**
 *  首页相关
 * @author xlisteven
 *
 */
@Controller
public class IndexController {

	@Autowired
	RoomService roomService;
	@RequestMapping("/preIndex")
	public String preIndex(Model model) {
		//查询五种房间分类的6条数据
		List<Room> roomList = roomService.findFiveRoom();
		if (roomList != null) {
			model.addAttribute("roomList",roomList);
		}
		//跳转到首页
		return "index";
	}
	
	/**
	 * 商务房
	 * @param model
	 * @return
	 */
	@RequestMapping("/businessRoom")
	public String businessRoom(Model model) {
		
		int type = 1;//商务房默认类型为1
		//查询所有商务房
		List<Room> roomList = roomService.findAllBusinessRoom(type);
		model.addAttribute("roomList",roomList);
		//跳转到商务房分类界面
		return "business_room";
	}
	/**
	 * 	亲子房
	 * @param model
	 * @return
	 */
	@RequestMapping("/mySky")
	public String mySky(Model model) {
		
		int type = 2;//亲子房房默认类型为2
		//查询所有亲子房
		List<Room> roomList = roomService.findAllBusinessRoom(type);
		model.addAttribute("roomList",roomList);
		//跳转到亲子房分类界面
		return "mysky";
	}
	/**
	 * 	总统套房
	 * @param model
	 * @return
	 */
	@RequestMapping("/presidentialSuite")
	public String presidentialSuite(Model model) {
		
		int type = 3;//总统套房默认类型为3
		//查询所有总统套房
		List<Room> roomList = roomService.findAllBusinessRoom(type);
		model.addAttribute("roomList",roomList);
		//跳转到总统套房分类界面
		return "presidentialSuite";
	}
	/**
	 * 	情侣房
	 * @param model
	 * @return
	 */
	@RequestMapping("/loversRoom")
	public String loversRoom(Model model) {
		
		int type = 4;//情侣房默认类型为4
		//查询所有情侣房
		List<Room> roomList = roomService.findAllBusinessRoom(type);
		model.addAttribute("roomList",roomList);
		//跳转到情侣房分类界面
		return "presidentialSuite";
	}
	/**
	 * 	主题房
	 * @param model
	 * @return
	 */
	@RequestMapping("/thematicRoom")
	public String thematicRoom(Model model) {
		
		int type = 5;//主题房默认类型为5
		//查询所有主题房
		List<Room> roomList = roomService.findAllBusinessRoom(type);
		model.addAttribute("roomList",roomList);
		//跳转到主题房分类界面
		return "presidentialSuite";
	}
	
	@RequestMapping("/details")
	public String details() {
		//跳转到详情页面
		return "details";
	}
}
