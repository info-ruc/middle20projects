<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>客房列表</title>
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
		<a class="btn btn-primary" href="<%=request.getContextPath()%>/toAddRoom">添加房间</a>
	<div style="padding-top: 10px"></div>
	<table class="table table-hover">
		<tr>
			<td>房间号</td>
			<td>房间名称</td>
			<td>房间面积</td>
			<td>房间价格</td>
			<td>房间描述</td>
			<td>房间图片</td>
			<td>房间状态</td>
			<td>房间类型</td>
			<td>操作</td>
		</tr>
		<c:forEach items="${roomList}" var="rl" varStatus="status">
		<tr>
			<td>${rl.roomNumber }</td>
			<td>${rl.roomName }</td>
			<td>${rl.roomArea }</td>
			<td>${rl.roomPrice }元</td>
			<td>${rl.roomDesc }</td>
			<td>
			<input type="hidden" id="image${status.index+1 }" value="<%=request.getContextPath()%>/upload/${rl.roomImg }"/>
			<img onclick="seeImage(${status.index+1})"  src="<%=request.getContextPath() %>/upload/${rl.roomImg }" style="width:50px;height:30px"/></td>
			<td>${rl.roomStatus}</td>
			<td>${rl.typeName}</td>
			<td>
				<a onclick="deleteRoom('${rl.roomId}','${rl.roomStatus }')">删除</a>
				<a href="toEditRoom?roomId=${rl.roomId}">编辑</a>
			</td>
		</tr>
		</c:forEach>
		<c:if test="${empty roomList }">
			<tr style="text-align:center">
				<td colspan="9">暂无记录</td>
			</tr>
		</c:if>
	</table>
</body>

<script type="text/javascript">
function seeImage(time){
	var img_infor = "<img src='" + $("#image"+time).val() + "' />";
	layer.open({
	      type: 1,
	      title: false, //不显示标题
	      area:['auto','auto'],  
	      shadeClose: true, //点击遮罩关闭
	      content:img_infor
	    }); 
}

function deleteRoom(roomId,roomStatus){
	var con;
	con=confirm("确认删除吗?"); //在页面上弹出对话框
	if(con==true){
		if(roomStatus != '可入住'){
			alert("当前房间不可删除");
			return false;
		}
		$.ajax({
			url:'<%=request.getContextPath()%>/deleteRoom',
			type:'POST',
			dataType:'json',
			data:{"roomId":roomId},
			success:function(result){
				if(result.code == 200){
					//删除成功
					window.location.href="findAllRoom"
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