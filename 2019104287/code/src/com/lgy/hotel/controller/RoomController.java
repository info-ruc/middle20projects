package com.lgy.hotel.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.lgy.hotel.pojo.Order;
import com.lgy.hotel.pojo.Room;
import com.lgy.hotel.pojo.RoomType;
import com.lgy.hotel.pojo.User;
import com.lgy.hotel.service.OrderService;
import com.lgy.hotel.service.RoomService;
import com.lgy.hotel.service.RoomTypeService;
import com.lgy.hotel.utils.DateUtils;

/**
 * @author xlisteven 房间相关
 */
@Controller
public class RoomController {

	@Autowired
	RoomService roomService;
	@Autowired
	OrderService orderService;
	@Autowired
	RoomTypeService roomTypeService;

	/**
	 * 通过房间ID查询房间详细信息
	 * 
	 * @param roomId
	 * @param model
	 * @param session
	 * @return
	 */
	@RequestMapping("/findRoomDetailsByRoomId")
	public String findRoomDetailsByRoomId(String roomId, Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");
		Room room = roomService.findRoomDetailsByRoomId(Integer.parseInt(roomId));
		model.addAttribute("room", room);
		model.addAttribute("user", user);
		return "details";
	}

	/**
	 * 跳转订房填写基本信息页面
	 * 
	 * @param roomId
	 * @param model
	 * @return
	 */
	@RequestMapping("/orderRoom")
	public String orderRoom(String roomId, Model model) {
		Room room = roomService.findRoomDetailsByRoomId(Integer.parseInt(roomId));
		model.addAttribute("room", room);
		return "fill_information";
	}

	/**
	 * 预订房间,生成订单
	 * @param order
	 * @return
	 */
	@RequestMapping("/generationOrder")
	@ResponseBody
	public Map<String,Object> generationOrder(Order order) {
		Map<String,Object> map = new HashMap<String, Object>();
		order.setGenerationTime(DateUtils.getNow());
		order.setOrderStatus("已预订");
		int result = orderService.saveOrder(order);
		if(result > 0) {
			//生成订单成功
			//订单生成成功后，需要修改房间的状态为已预订
			String status = "已预订";
			roomService.updateRoomStatus(order.getRoomNumber(),status);
			map.put("code", 200);
			map.put("msg", "预订成功，前往个人中心查看");
		}else {
			map.put("code", 500);
			map.put("msg","预订失败，稍后重试");
		}
		return map;
		
	}
	
	/**
	 * 查询所有客房
	 * @return
	 */
	@RequestMapping("/findAllRoom")
	public String findAllRoom(Model model) {
		List<Room> roomList = roomService.findAllRoom();
		if(roomList != null) {
			model.addAttribute("roomList",roomList);
		}
		return "admin/roomList";
	}
	
	/**
	 * 跳转添加房间的页面
	 * @param model
	 * @return
	 */
	@RequestMapping("/toAddRoom")
	public String toAddRoom(Model model) {
		List<RoomType> roomTypeList = roomTypeService.findAllType();
		if (roomTypeList != null) {
			model.addAttribute("roomTypeList",roomTypeList);
		}
		return "admin/addRoom";
	}
	
	@RequestMapping("/addRoom")
	public String addRoom(Room room ,String[] specail,MultipartFile roomImgs, HttpServletRequest request) {
		RoomType roomType = roomTypeService.findTypeNameById(room.getRoomTypeId());
		if(specail.length == 0) {
		}else if(specail.length == 1) {
			room.setSpecail1(specail[0]);
		}else if(specail.length == 2) {
			room.setSpecail1(specail[0]);
			room.setSpecail2(specail[1]);
		}else if(specail.length == 3) {
			room.setSpecail1(specail[0]);
			room.setSpecail2(specail[1]);
			room.setSpecail3(specail[2]);
		}else if(specail.length == 4) {
			room.setSpecail1(specail[0]);
			room.setSpecail2(specail[1]);
			room.setSpecail3(specail[2]);
			room.setSpecail4(specail[3]);
		}else if(specail.length == 5) {
			room.setSpecail1(specail[0]);
			room.setSpecail2(specail[1]);
			room.setSpecail3(specail[2]);
			room.setSpecail4(specail[3]);
			room.setSpecail5(specail[4]);
		}
		//上传目录地址
        String uploadDir = "D:\\eclipse2018\\workspace\\HotelManagement\\WebContent\\upload\\";
        String filename = "";
        //如果目录不存在，自动创建文件夹
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
			//文件后缀名
			String suffix = roomImgs.getOriginalFilename().substring(roomImgs.getOriginalFilename().lastIndexOf("."));
			filename = UUID.randomUUID() + suffix;
			//服务器端保存的文件对象
			File serverFile = new File(uploadDir + filename);
			//将上传的文件写入到服务器端文件内
			roomImgs.transferTo(serverFile);
			room.setRoomImg(filename);
			room.setRoomStatus("可入住");
			room.setTypeName(roomType.getTypeName());
			roomService.saveRoom(room);
		}  catch (Exception e) {
			e.printStackTrace();
		}
        return "redirect:/findAllRoom";
	}
	
	@RequestMapping("/deleteRoom")
	@ResponseBody
	public Map<String, Object> deleteRoom(String roomId){
		Map<String, Object> map = new HashMap<String, Object>();
		int result = roomService.deleteRoom(Integer.parseInt(roomId));
		if(result > 0) {
			//删除成功
			map.put("code", 200);
			map.put("msg", "删除成功");
		}else {
			//删除失败
			map.put("code", 500);
			map.put("msg", "删除失败");
		}
		return  map;
	}
	
	/**
	 * 编辑房间信息
	 * @param roomId
	 * @param model
	 * @return
	 */
	@RequestMapping("/toEditRoom")
	public String toEditRoom(String roomId,Model model) {
		Room room = roomService.findRoomByRoomId(Integer.parseInt(roomId));
		List<RoomType> roomTypeList = roomTypeService.findAllType();
		model.addAttribute("room",room);
		model.addAttribute("roomTypeList",roomTypeList);
		return "admin/editRoom";
	}
	
	@RequestMapping("/editRoom")
	public String editRoom(Room room ,String[] specail) {
		RoomType roomType = roomTypeService.findTypeNameById(room.getRoomTypeId());
		if(specail.length == 0) {
		}else if(specail.length == 1) {
			room.setSpecail1(specail[0]);
		}else if(specail.length == 2) {
			room.setSpecail1(specail[0]);
			room.setSpecail2(specail[1]);
		}else if(specail.length == 3) {
			room.setSpecail1(specail[0]);
			room.setSpecail2(specail[1]);
			room.setSpecail3(specail[2]);
		}else if(specail.length == 4) {
			room.setSpecail1(specail[0]);
			room.setSpecail2(specail[1]);
			room.setSpecail3(specail[2]);
			room.setSpecail4(specail[3]);
		}else if(specail.length == 5) {
			room.setSpecail1(specail[0]);
			room.setSpecail2(specail[1]);
			room.setSpecail3(specail[2]);
			room.setSpecail4(specail[3]);
			room.setSpecail5(specail[4]);
		}
		roomService.updateRoomInfo(room);
		return "redirect:/findAllRoom";
	}
}
