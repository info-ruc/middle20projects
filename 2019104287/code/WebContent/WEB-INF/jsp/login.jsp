<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!--Author: W3layouts
Author URL: http://w3layouts.com
License: Creative Commons Attribution 3.0 Unported
License URL: http://creativecommons.org/licenses/by/3.0/
-->
<!DOCTYPE HTML>
<html>
<head>
<title>登录注册</title>
<!-- Custom Theme files -->
<link href="<%=request.getContextPath()%>/css/login/style.css"
	rel="stylesheet" type="text/css" media="all" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="keywords"
	content="Flat Tab Forms Responsive, Login form web template, Sign up Web Templates, Flat Web Templates, Login signup Responsive web template, Smartphone Compatible web template, free webdesigns for Nokia, Samsung, LG, SonyEricsson, Motorola web design" />
<!--Google Fonts-->
<link
	href='//fonts.googleapis.com/css?family=Raleway:400,400italic,500,500italic,600,600italic,700,700italic,800,800italic,900,900italic'
	rel='stylesheet' type='text/css'>
<link href='//fonts.googleapis.com/css?family=Federo' rel='stylesheet'
	type='text/css'>
<!--google fonts-->
<!--remove-->
<script src="<%=request.getContextPath()%>/js/jquery.min.js"></script>
<script>
	$(document).ready(function(c) {
		$('.close').on('click', function(c) {
			$('.header').fadeOut('slow', function(c) {
				$('.header').remove();
			});
		});
	});
</script>
<!--remove-->
<script src="<%=request.getContextPath()%>/js/easyResponsiveTabs.js"
	type="text/javascript"></script>
<script type="text/javascript">
	$(document).ready(function() {
		$('#horizontalTab').easyResponsiveTabs({
			type : 'default', //Types: default, vertical, accordion           
			width : 'auto', //auto or any width like 600px
			fit : true
		// 100% fit in a container
		});
	});
</script>
</head>
<body>
	<!--header start here-->
	<h1>登录/注册</h1>
	<div class="header agile">
		<div class="headder-main w3layouts">
			<div class="login agileinfo">
				<div class="sap_tabs">
					<div id="horizontalTab"
						style="display: block; width: 100%; margin: 0px;">
						<ul class="resp-tabs-list w3">
							<li class="resp-tab-item" aria-controls="tab_item-0"  role="tab"><span>登录</span></li>
							<li class="resp-tab-item" id="create" aria-controls="tab_item-1"
								role="tab"><span>注册</span></li>
							<div class="clearfix"></div>
						</ul>
						<div class="resp-tabs-container w3-agile">
							<div class="tab-1 resp-tab-content" aria-labelledby="tab_item-0">
								<div class="login-top">
									<form id="loginForm" action="#" method="post">
										<h6>用户名</h6>
										<input type="text" placeholder="用户名" id="login_username"
											name="username" required="required">
										<h6>密码</h6>
										<input type="password" id="login_password" class="password"
											placeholder="密码" name="password" required="required">
										<div class="login-bottom login-bottom1 w3ls">
											<input type="button" onclick="login()" value="登录" />
										</div>
									</form>
								</div>
							</div>
							<div class="tab-1 resp-tab-content" aria-labelledby="tab_item-1">
								<div class="login-top wthree">
									<form id="registerForm" action="#" method="post">
										<h6>用户名</h6>
										<input type="text" id="username" placeholder="用户名"
											name="username" required="required">
										<h6>昵称</h6>
										<input type="text" id="nickname" placeholder="昵称"
											name="nickName">
										<h6>密码</h6>
										<input type="password" id="password" class="password"
											placeholder="密码" name="password">
										<h6>确认密码</h6>
										<input type="password" id="repassword"
											class="password confirm_password" placeholder="确认密码"
											name="repassword">
										<div class="login-bottom">
											<input type="button" onclick="register()" value="注册">
											<div class="clear"></div>
										</div>
									</form>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="close">
			<img src="<%=request.getContextPath()%>/images/cancel.png" alt="">
		</div>
	</div>
	<!--header end here-->
	<!--copy rights end here-->
	<div class="copy-rights agileits">
		<p>
			© 2016 Flat Tab Forms. All Rights Reserved | Design by <a
				href="http://w3layouts.com/" target="_blank">W3layouts</a>
		</p>
	</div>
	<!--copyrights start here-->
</body>
<script>
function register() {
	//registerForm
	var password  = $('#password').val();
	var repassword  = $('#repassword').val();
	var username  = $('#username').val();
	var nickname  = $('#nickname').val();
	if(username == null || username == ''){
		alert("用户名不能为空");
		return false;
	}
	if(nickname == null || nickname == ''){
		alert("昵称不能为空");
		return false;
	}
	
	if(password == null || password == ''){
		alert("密码不能为空");
		return false;
	}
	if(repassword == null || repassword == ''){
		alert("确认密码不能为空");
		return false;
	}
	if (password != repassword) {
		alert("两次密码输入不一致")
		return false;
	}
	$.ajax({
		url:'<%=request.getContextPath()%>/register',
		type:'POST',
		dataType:'json',
		data:$('#registerForm').serialize(),
		success:function(result){
			if (result > 0) {
				alert("注册成功")
				top.location.href = "toLogin";
			}
		},
		error:function(){
			alert('注册异常')
		}
	})
}


function login(){
	var username = $('#login_username').val();
	var password = $('#login_password').val();
	if(username == null || username == ''){
		alert('用户名不能为空');
		return false;
	}
	if(password == null || password == ''){
		alert('密码不能为空');
		return false;
	}
	$.ajax({
		url:'<%=request.getContextPath()%>/login',
			type : 'POST',
			dataType : 'json',
			data : $('#loginForm').serialize(),
			success : function(result) {
				console.log(result)
				if (result.code == 500) {
					alert('用户名或者密码错误')
				}
				if (result.code == 200 && result.user.roleId == 1) {
					//跳转用户看到的界面
					alert('登录成功 我是普通用户')
					top.location.href = "preIndex";
				}
				if (result.code == 200 && result.user.roleId == 0) {
					//跳转管理员看到的界面
					alert('登录成功 我是管理员')
					top.location.href = "adminIndex"
				}
			},
			error : function() {
				alert('注册异常')
			}
		})
	}
</script>
</html>