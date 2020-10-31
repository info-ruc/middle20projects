package com.lgy.hotel.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lgy.hotel.pojo.User;
import com.lgy.hotel.service.UserService;

/**
 * 	后台
 * @author xlisteven
 *
 */
@Controller
public class AdminIndexController {

	@Autowired
	UserService userService;
	/**
	 * 跳转后台首页
	 * @return
	 */
	@RequestMapping("/adminIndex")
	public String adminIndex() {
		return "admin/index";
	}
	
	@RequestMapping("/toAdminIndex")
	public String toAdminIndex(){
		
		return "admin/welcome";
	}
	
	@RequestMapping("/toUpdateAdminPwd")
	public String toUpdateAdminPwd(HttpSession session ,Model model) {
		User user = (User)session.getAttribute("user");
		if(user != null) {
			String oldPwd = user.getPassword();
			model.addAttribute("oldPwd",oldPwd);
		}
		return "admin/updatePwd";
	}
	
	@RequestMapping("/updatePwd")
	@ResponseBody
	public Map<String,Object> updateAdminPwd(String newPwd,HttpSession session,HttpServletResponse response) throws Exception{
		User user = (User)session.getAttribute("user");
		userService.updatePwd(newPwd,user.getUserId());
		HashMap<String,Object> map = new HashMap<String, Object>();
		map.put("code", 200);
		map.put("msg","修改成功");
		session.invalidate();
		return map;
		
	}
}
