<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>管理员列表</title>
<link href="<%=request.getContextPath()%>/css/bootstrap.css"
	rel='stylesheet' type='text/css' />
<link href="<%=request.getContextPath()%>/css/style.css"
	rel="stylesheet" type="text/css" media="all" />
<meta name="viewport" content="width=device-width, initial-scale=1">
<script src="<%=request.getContextPath()%>/js/jquery.min.js"></script>
<script src="<%=request.getContextPath()%>/js/layer/layer.js"></script>
</head>
<body>
	<div style="padding-top: 50px"></div>
	<button type="button" class="btn btn-primary" onclick="addAdmin()">添加管理员</button>
	<div style="padding-top: 10px"></div>
	<table class="table table-hover">
		<tr>
			<td>ID</td>
			<td>用户名</td>
			<td>昵称</td>
			<td>角色</td>
			<td>操作</td>
		</tr>
		<c:forEach items="${adminList}" var="al">
		<tr>
			<td>${al.userId }</td>		
			<td>${al.username }</td>		
			<td>${al.nickName }</td>		
			<td><c:if test="${al.roleId == 0 }">管理员</c:if></td>
			<td><a href="javascript:void(0)" onclick="deleteUser('${al.userId}','${totalAdmin }')">删除用户</a></td>	
		</tr>
		</c:forEach>
		<c:if test="${empty adminList }">
			<tr style="text-align:center">
				<td colspan="5">暂无记录</td>
			</tr>
		</c:if>
	</table>
</body>

<script type="text/javascript">
		function addAdmin(){
			layer.open({
			      type: 1,
			      area: ['260px', '232px'],
			      title:'添加管理员',
			      shadeClose: true, //点击遮罩关闭
			      content: '<form action="<%=request.getContextPath()%>/addAdmin" method="post">用户名：<input type="text" name="username" width="100%"/><br/>'+
			     				' 昵&nbsp; &nbsp;称：<input type="text" name="nickName" width="100%"/><br/>'+
			      			'密&nbsp; &nbsp;码：<input type="password" name="password" width="100%"/><br/>'+
			      			'<div style="padding-left:100px;padding-top:20px"><input type="submit" value="确认添加" /></div>'+
			      '</form>'
			    });
		}
	function deleteUser(userId,totalAdmin) {
		var con;
		con=confirm("确认删除吗?"); //在页面上弹出对话框
		if(con==true){
			if(totalAdmin == 1){
				alert("系统必须留至少一个管理员");
				return false;
			}
			$.ajax({
				url:'<%=request.getContextPath()%>/deleteUser',
				type:'POST',
				dataType:'json',
				data:{"userId":userId},
				success:function(result){
					if(result.code == 200){
						//删除成功
						window.location.href="findAllAdmin"
					}else{
						//删除失败失败
						alert("删除失败，稍后重试")
					}
				},
				error:function(){
					alert('删除异常')
				}
			})
		}
	}
</script>
</html>