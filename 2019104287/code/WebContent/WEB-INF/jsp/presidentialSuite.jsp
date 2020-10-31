<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<title>总统套房</title>
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
					<a href="preIndex"><img
						src="<%=request.getContextPath()%>/images/logo.png" /></a>
					<c:if test="${empty sessionScope.user }">
						<a href="<%=request.getContextPath()%>/toLogin"><span
							style="color: white">登录/注册</span></a>
					</c:if>
					<c:if test="${not empty sessionScope }">
						<span style="color: white">欢迎您，${sessionScope.user.username}</span>
						<br/>
						<span style="padding-left: 244px"><a href="<%=request.getContextPath()%>/personalCenter">订单查看 </a></span>
					</c:if>
					<div class="clearfix"></div>
				</div>
				<span class="menu"> </span>
				<div class="m-clear"></div>
				<div class="top-menu">
					<%@include file="top.jsp"%>
				</div>
			</div>
		</div>
	</div>
	<!---->
	<div class="main_bg">
		<div class="container">
			<div class="main">
				<ul class="service_list">
					<c:forEach items="${roomList }" var="rl">
						<li>
							<div class="ser_img">
								<a
									href="<%=request.getContextPath()%>/findRoomDetailsByRoomId?roomId=${rl.roomId}">
									<img src="<%=request.getContextPath()%>/upload/${rl.roomImg}"
									alt="" style="width: 244px; height: 209px" /> <span
									class="next"></span>
								</a>
							</div>
							<h3>${rl.roomName }</h3>
							<span style="color: pink">(${rl.roomStatus})</span>
							<p class="para">${rl.roomDesc }</p>
							<h4>
								<a href="javascript:void(0)">¥${rl.roomPrice }</a>
							</h4>
						</li>
					</c:forEach>
				</ul>
			</div>
		</div>
	</div>
	<%@include file="footer.jsp"%>
</body>
</html>