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
						<span style="color: white">欢迎您，${sessionScope.user.username},<a style="color: pink" href="logout">点击退出</a>
						</span><br/>
						<span style="padding-left: 244px"><a href="<%=request.getContextPath()%>/personalCenter">订单查看 </a></span>
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
	<div class="package text-center">
		<div class="container">
			<h3>酒店预订</h3>
			<p>宾至如归、古色古香、 清静幽雅 、桃李周庭、 宽敞明亮；
			美酒佳肴迎挚友、名楼雅座待高朋、美味招徕云外客、清香引出月中仙。</p>
			<!-- requried-jsfiles-for owl -->
			<link href="<%=request.getContextPath()%>/css/owl.carousel.css"
				rel="stylesheet">
			<script src="<%=request.getContextPath()%>/js/owl.carousel.js"></script>
			<script>
				$(document).ready(function() {
					$("#owl-demo").owlCarousel({
						items : 1,
						lazyLoad : true,
						autoPlay : true,
						navigation : true,
						navigationText : false,
						pagination : false,
					});
				});
			</script>
			<!-- //requried-jsfiles-for owl -->
			<div id="owl-demo" class="owl-carousel">
				<div class="item text-center image-grid">
					<ul>
						<li><img src="<%=request.getContextPath()%>/images/1.jpg"
							alt=""></li>
						<li><img src="<%=request.getContextPath()%>/images/2.jpg"
							alt=""></li>
						<li><img src="<%=request.getContextPath()%>/images/3.jpg"
							alt=""></li>
					</ul>
				</div>
				<div class="item text-center image-grid">
					<ul>
						<li><img src="<%=request.getContextPath()%>/images/3.jpg"
							alt=""></li>
						<li><img src="<%=request.getContextPath()%>/images/4.jpg"
							alt=""></li>
						<li><img src="<%=request.getContextPath()%>/images/5.jpg"
							alt=""></li>
					</ul>
				</div>
				<div class="item text-center image-grid">
					<ul>
						<li><img src="<%=request.getContextPath()%>/images/6.jpg"
							alt=""></li>
						<li><img src="<%=request.getContextPath()%>/images/2.jpg"
							alt=""></li>
						<li><img src="<%=request.getContextPath()%>/images/8.jpg"
							alt=""></li>
					</ul>
				</div>
			</div>
		</div>
	</div>
	<!---->
	<!---->
	<div class="rooms text-center">
		<div class="container">
			<h3>房间类型</h3>
			<div class="room-grids">
			<c:forEach items="${roomList }" var="rl">
				<div class="col-md-4 room-sec">
					<img src="<%=request.getContextPath()%>/upload/${rl.roomImg}" style="width:350px;height:160px" alt="" />
					<h4>${rl.roomName }</h4>
					<p>¥${rl.roomPrice}</p>
					<div class="items">
						<li><a href="#">${rl.specail1 }</span></a></li>
						<li><a href="#">${rl.specail2 }</span></a></li>
						<li><a href="#">${rl.specail3 }</span></a></li>
						<li><a href="#">${rl.specail4 }</span></a></li>
					</div>
				</div>
				</c:forEach>
				<div class="col-md-4 room-sec">
					<img src="<%=request.getContextPath()%>/images/pic4.jpg" alt="" />
					<h4>占位房间</h4>
					<p>这是占位房间</p>
					<div class="items">
						<li><a href="#"><span class="img1"> </span></a></li>
						<li><a href="#"><span class="img2"> </span></a></li>
						<li><a href="#"><span class="img3"> </span></a></li>
						<li><a href="#"><span class="img4"> </span></a></li>
						<li><a href="#"><span class="img5"> </span></a></li>
						<li><a href="#"><span class="img6"> </span></a></li>
					</div>
				</div> 
				<div class="clearfix"></div>
			</div>
		</div>
	</div>
	<%@include file="footer.jsp"%>
</body>
</html>