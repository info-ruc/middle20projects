package com.lgy.hotel.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.lgy.hotel.pojo.Order;

@Repository
public interface OrderMapper {

	/**
	 * 生成订单
	 * 
	 * @param order
	 * @return
	 */
	int saveOrder(Order order);

	/**
	 * 通过用户ID查询订单
	 * 
	 * @param userId
	 * @return
	 */
	List<Order> findOrderByUserId(Integer userId);

	/**
	 * 更新订单状态
	 * @param orderId
	 * @param status
	 * @param userId
	 * @return
	 */
	int updateOrderStatus(@Param("orderId")String orderId, @Param("status")String status, @Param("userId")Integer userId);

	/**
	 * 管理员查询所有订单
	 * @return
	 */
	List<Order> findAllOrder();

	/**
	 * 退订统计
	 * @return
	 */
	List<Order> findAllUnsubscribe();

	/**
	 * 	收益统计
	 * @return
	 */
	List<Order> findRevenueStatistics();

	/**
	 * 查询所有已完成订单的总和
	 * @return
	 */
	double findAllMoney();

	/**
	 * 查询所有已入住的订单
	 * @return
	 */
	List<Order> findAllCheckIn();

}
