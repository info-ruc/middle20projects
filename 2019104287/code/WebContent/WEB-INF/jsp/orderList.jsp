<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<title>Home</title>
<link href="<%=request.getContextPath()%>/css/bootstrap.css"
	rel='stylesheet' type='text/css' />
<link href="<%=request.getContextPath()%>/css/style.css"
	rel="stylesheet" type="text/css" media="all" />
<meta name="viewport" content="width=device-width, initial-scale=1">
<script src="<%=request.getContextPath()%>/js/jquery.min.js"></script>
</head>
<body>
	<!--header starts-->
	<div class="header">
		<div class="top-header">
			<div class="container">
				<div class="logo">
					<a href="#"><img
						src="<%=request.getContextPath()%>/images/logo.png" /></a>
					<c:if test="${empty sessionScope.user }">
						<a href="<%=request.getContextPath()%>/toLogin"><span
							style="color: white">登录/注册</span></a>
					</c:if>
					<c:if test="${not empty sessionScope }">
						<span style="color: white">欢迎您，${sessionScope.user.username}
						</span>
						<br />
						<span style="padding-left: 244px"><a
							href="<%=request.getContextPath()%>/personalCenter">订单查看 </a></span>
					</c:if>
				</div>
				<span class="menu"></span>
				<div class="m-clear"></div>
				<div class="top-menu">
					<%@include file="top.jsp"%>
				</div>
				<div class="clearfix"></div>
			</div>
		</div>
	</div>
	<script type="text/javascript"
		src="<%=request.getContextPath()%>/js/JFCore.js"></script>
	<script type="text/javascript"
		src="<%=request.getContextPath()%>/js/JFForms.js"></script>
	<!-- Set here the key for your domain in order to hide the watermark on the web server -->
	<script type="text/javascript">
		(function() {
			JC.init({
				domainKey : ''
			});
		})();
	</script>
	<!---->
	<!---->
	<div class="rooms text-center">
		<div class="container">
			<h3>我的订单</h3>
			<table class="table table-hover">
				<tr>
					<td>订单编号</td>
					<td>房间号</td>
					<td>房间名</td>
					<td>房间价格</td>
					<td>用户名</td>
					<td>身份证</td>
					<td>联系电话</td>
					<td>生成日期</td>
					<td>订单状态</td>
					<td>操作</td>
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
						<td><a href="javascript:void(0)"
							onclick="unsubscribe('${ol.orderId }','${ol.roomNumber }')"><c:if
									test="${ol.orderStatus == '已预订' }">退订</c:if></a></td>
					</tr>
				</c:forEach>
				<c:if test="${empty orderList }">
					<tr style="text-align: center;">
						<td colspan="10">暂无数据</td>
					</tr>
				</c:if>



			</table>
			<div class="clearfix"></div>
		</div>
	</div>
	<%@include file="footer.jsp"%>
</body>
<script>
	//退订
	function unsubscribe(orderId,roomNumber){
		var con;
		con=confirm("确认退订吗?"); //在页面上弹出对话框
		if(con==true){
			$.ajax({
				url:'<%=request.getContextPath()%>/unsubscribe',
				type:'POST',
				dataType:'json',
				data:{"orderId":orderId,"roomNumber":roomNumber},
				success:function(result){
					if(result.code == 200){
						//退订成功
						parent.location.reload();
					}else{
						//退订失败
					}
				},
				error:function(){
					alert('退订异常')
				}
			})
		}
	}	
</script>
</html>