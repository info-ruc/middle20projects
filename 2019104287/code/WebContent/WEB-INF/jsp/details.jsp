<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
					<a href="<%=request.getContextPath()%>/preIndex"> <img
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
				<div class="details">
					<a href="javascript:void(0)"><h2>${room.roomName }</h2></a>
					<div class="det_pic">
						<img src="<%=request.getContextPath() %>/upload/${room.roomImg}"
							alt="" />
					</div>
					<div class="det_text">
						<p class="para">${room.roomDesc }.</p>
						<a href="javascript:void(0)"><h2>房间配置</h2></a>
						<p class="para">${room.specail1 }</p>
						<p class="para">${room.specail2 }</p>
						<p class="para">${room.specail3 }</p>
						<p class="para">${room.specail4 }</p>
						<p class="para">${room.specail5 }</p>

						<div class="read_more">
							<c:if test="${room.roomStatus == '可入住' }">
								<button onclick="orderRoom('${room.roomId}','${user}')"
									class="btn btn-success">预订该房间</button>
							</c:if>
							<c:if test="${room.roomStatus != '可入住' }">
								<button class="btn btn-warning">该房间不可预订</button>
							</c:if>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%@include file="footer.jsp"%>
</body>
<script>
	function orderRoom(roomId, user) {
		if (user == '' || user == null) {
			alert("请先登录系统");
			top.location.href = "toLogin";
		} else {
			top.location.href = "orderRoom?roomId=" + roomId;
		}
	}
</script>
</html>
