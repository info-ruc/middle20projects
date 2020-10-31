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
					<a href="index.html"><img
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
					<%@ include file="top.jsp"%>
				</div>
			</div>
		</div>
	</div>
	<!---->
	<div class="contact-bg2">
		<div class="container">
			<div class="booking">
				<h3>${room.roomName}(${room.roomNumber })</h3>
				<div class="det_pic">
					<img src="<%=request.getContextPath() %>/upload/${room.roomImg}"
						alt="" />
				</div>
				<div class="col-md-8 booking-form">
					<form id="orderRoom" action="#">
						<input type="hidden" name="roomNumber" value="${room.roomNumber }">
						<input type="hidden" name="roomName" value="${room.roomName }">
						<input type="hidden" name="roomPrice" value="${room.roomPrice }">
						<input type="hidden" name="userId"
							value="${sessionScope.user.userId }">
						<h5>姓名</h5>
						<input type="text" id="username" name="username">
						<h5>身份证号</h5>
						<input type="text" id="idCard" name="idCard" maxlength="18">
						<h5>联系电话</h5>
						<input type="text" id="phoneNumber" name="phoneNumber"
							maxlength="11"> <input type="button"
							onclick="orderRoom();" value="确认订购"> <input type="reset"
							value="取消">
					</form>
				</div>
				<div class="clearfix"></div>

			</div>
		</div>
	</div>
	<%@ include file="footer.jsp"%>
</body>
<script type="text/javascript">
	function orderRoom() {
		var username = $('#username').val();
		var idCard = $('#idCard').val();
		var phoneNumber = $('#phoneNumber').val();
		var roomNumber = $('#roomNumber').val();
		var roomName = $('#roomName').val();
		var roomPrice = $('#roomPrice').val();
		var userId = $('#userId').val();
		if (username == null || username == '') {
			alert('姓名不能为空');
			return false;
		}
		if (idCard == null || idCard == '') {
			alert('身份证不能为空');
			return false;
		}
		if (phoneNumber == null || phoneNumber == '') {
			alert('电话不能为空');
			return false;
		}
		$.ajax({
			url:'<%=request.getContextPath()%>/generationOrder',
			type:'POST',
			dataType:'json',
			data:$('#orderRoom').serialize(),
			success:function(result){
				if(result.code == 200){
					alert(result.msg);
				}else{
					alert(result.msg);
				}
			},
			error:function(){
				alert('生成订单异常')
			}
		})
	}
</script>
</html>
