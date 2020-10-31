package com.lgy.hotel.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lgy.hotel.dao.OrderMapper;
import com.lgy.hotel.pojo.Order;

/**
 * @author xlisteven
 *	订单相关
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	OrderMapper orderMapper;
	@Override
	public int saveOrder(Order order) {
		return orderMapper.saveOrder(order);
	}
	@Override
	public List<Order> findOrderByUserId(Integer userId) {
		return orderMapper.findOrderByUserId(userId);
	}
	@Override
	public int updateOrderStatus(String orderId, String status, Integer userId) {
		return orderMapper.updateOrderStatus(orderId,status,userId);
	}
	@Override
	public List<Order> findAllOrder() {
		return orderMapper.findAllOrder();
	}
	@Override
	public List<Order> findAllUnsubscribe() {
		return orderMapper.findAllUnsubscribe();
	}
	@Override
	public List<Order> findRevenueStatistics() {
		return orderMapper.findRevenueStatistics();
	}
	@Override
	public double findAllMoney() {
		return orderMapper.findAllMoney();
	}
	@Override
	public List<Order> findAllCheckIn() {
		return orderMapper.findAllCheckIn();
	}

}
