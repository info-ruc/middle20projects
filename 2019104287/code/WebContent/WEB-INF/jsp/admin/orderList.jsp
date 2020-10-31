<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>订单列表</title>
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
			<td>用户名称</td>
			<td>身份证</td>
			<td>联系电话</td>
			<td>生成日期</td>
			<td>订单状态</td>
		</tr>
		<c:forEach items="${orderList}" var="ol">
		<tr>
			<td>${ol.orderId }</td>
			<td>${ol.roomNumber}</td>
			<td>${ol.roomName }</td>
			<td>${ol.roomPrice}</td>
			<td>${ol.username }</td>
			<td>${ol.idCard }</td>
			<td>${ol.phoneNumber }</td>
			<td>${ol.generationTime}</td>
			<td>
				<c:if test="${ol.orderStatus == '已预订' }">
					<a href="javascript:void(0)" onclick="checkIn('${ol.orderId }','${ol.roomNumber}','${ol.userId }')">
					<span style="color:red">${ol.orderStatus}</span></a>
				</c:if>
				<c:if test="${ol.orderStatus == '已入住' }">
					<a href="javascript:void(0)" onclick="checkOut('${ol.orderId }','${ol.roomNumber}','${ol.userId }')">
					<span style="color:red">${ol.orderStatus}</span></a>
				</c:if>
				<c:if test="${ol.orderStatus == '可入住' }">
					<span>${ol.orderStatus}</span>
				</c:if>
				<c:if test="${ol.orderStatus == '已完成' }">
					<span>${ol.orderStatus}</span>
				</c:if>
				<c:if test="${ol.orderStatus == '已退订' }">
					<span>${ol.orderStatus}</span>
				</c:if>
			</td>
		</tr>
		</c:forEach>
		<c:if test="${empty orderList }">
			<tr style="text-align:center">
				<td colspan="10">暂无记录</td>
			</tr>
		</c:if>
	</table>
</body>

<script type="text/javascript">
	function checkIn(orderId,roomNumber,userId){
		var con;
		con=confirm("确认入住吗?"); //在页面上弹出对话框
		if(con==true){
			$.ajax({
				url:'<%=request.getContextPath()%>/checkIn',
				type:'POST',
				dataType:'json',
				data:{"orderId":orderId,"roomNumber":roomNumber,"userId":userId},
				success:function(result){
					if(result.code == 200){
						//入住成功
						window.location.href="findAllOrder";
					}else{
						//入住失败
					}
				},
				error:function(){
					alert('入住异常')
				}
			})
		}
	}
	function checkOut(orderId,roomNumber,userId){
		var con;
		con=confirm("确认退房吗?"); //在页面上弹出对话框
		if(con==true){
			$.ajax({
				url:'<%=request.getContextPath()%>/checkOut',
				type:'POST',
				dataType:'json',
				data:{"orderId":orderId,"roomNumber":roomNumber,"userId":userId},
				success:function(result){
					if(result.code == 200){
						//退房成功
						window.location.href="findAllOrder";
					}else{
						//退房失败
					}
				},
				error:function(){
					alert('退房异常')
				}
			})
		}
	}
</script>
</html>