<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>密码修改</title>
<link href="<%=request.getContextPath()%>/css/bootstrap.css"
	rel='stylesheet' type='text/css' />
<link href="<%=request.getContextPath()%>/css/style.css"
	rel="stylesheet" type="text/css" media="all" />
<meta name="viewport" content="width=device-width, initial-scale=1">
<script src="<%=request.getContextPath()%>/js/jquery.min.js"></script>
<script src="<%=request.getContextPath()%>/js/layer/layer.js"></script>
</head>
<body>
	<div style="padding-top: 10px"></div>
	<table class="table table-hover">
		<tr>
			<td>旧密码</td>
			<td>新密码</td>
			<td>确认密码</td>
		</tr>
			<tr>
				<td><input type="password" id="oldPwd" name="oldPwd"></td>
				<td><input type="password" id="newPwd" name="newPwd"></td>
				<td><input type="password" id="rePwd" name="rePwd"></td>
			</tr>
			<tr>
				<td colspan="3" style="text-align: center">
				<button onclick=" return check('${oldPwd}')">确认修改</button>
				</td>
			</tr>
	</table>
</body>
<script type="text/javascript">
function check(oldoldPwd) {
	var oldPwd = document.getElementById('oldPwd').value;
	var newPwd = document.getElementById('newPwd').value;
	var rePwd = document.getElementById('rePwd').value;
	if (oldPwd == null || oldPwd == '') {
		alert('旧密码不能为空');
		return false;
	} else if (oldoldPwd != oldPwd) {
		alert('旧密码输入不正确');
		return false;
	}
	if (newPwd == null || newPwd == '') {
		alert('新密码不能为空');
		return false;
	}
	if (rePwd == null || rePwd == '') {
		alert('确认密码不能为空');
		return false;
	}
	if (rePwd != newPwd) {
		alert('两次密码不一致');
		return false;
	}
	
	$.ajax({ 
		url:'<%=request.getContextPath()%>/updatePwd',
		type:'POST',
		dataType:'json',
		data:{"newPwd":newPwd},
		success:function(result){
			if(result.code == 200){
				alert(result.msg);
				window.location.href="preIndex.jsp"
			}
		},
		error:function(){
							
		}
})
	
	
	
}
</script>
</html>