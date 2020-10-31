package com.lgy.hotel.service;

import java.util.List;

import com.lgy.hotel.pojo.Order;

public interface OrderService {

	int saveOrder(Order order);

	List<Order> findOrderByUserId(Integer userId);

	int updateOrderStatus(String orderId, String status, Integer userId);

	List<Order> findAllOrder();

	List<Order> findAllUnsubscribe();

	List<Order> findRevenueStatistics();

	double findAllMoney();

	List<Order> findAllCheckIn();

}
