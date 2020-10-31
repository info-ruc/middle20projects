<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>退订列表</title>
<link href="<%=request.getContextPath()%>/css/bootstrap.css"
	rel='stylesheet' type='text/css' />
<link href="<%=request.getContextPath()%>/css/style.css"
	rel="stylesheet" type="text/css" media="all" />
<meta name="viewport" content="width=device-width, initial-scale=1">
<script src="<%=request.getContextPath()%>/js/jquery.min.js"></script>
</head>
<body>
	<div style="padding-top: 50px"></div>
	<table class="table table-hover">
		<tr>
			<td>订单编号</td>
			<td>房间号</td>
			<td>房间名称</td>
			<td>房间价格</td>
			<td>用户名</td>
			<td>身份证</td>
			<td>联系电话</td>
			<td>订单日期</td>
			<td>订单状态</td>
		</tr>
		<c:forEach items="${orderList }" var="ol">
			<tr>
				<td>${ol.orderId }</td>
				<td>${ol.roomNumber }</td>
				<td>${ol.roomName }</td>
				<td>${ol.roomPrice }</td>
				<td>${ol.username }</td>
				<td>${ol.idCard }</td>
				<td>${ol.phoneNumber }</td>
				<td>${ol.generationTime }</td>
				<td>${ol.orderStatus }</td>
			</tr>
		</c:forEach>
		<c:if test="${empty orderList }">
			<tr style="text-align: center;">
				<td colspan="10">暂无数据</td>
			</tr>
		</c:if>
	</table>
</body>
</html>