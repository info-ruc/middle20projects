<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<ul>
	<li><a href="<%=request.getContextPath() %>/preIndex">首页</a></li>
	<li><a class="scroll" href="<%=request.getContextPath() %>/businessRoom">商务套房</a></li>
	<li><a class="scroll" href="<%=request.getContextPath() %>/mySky">亲子房</a></li>
	<li><a class="scroll" href="<%=request.getContextPath() %>/presidentialSuite">总统套房</a></li>
	<li><a class="scroll" href="<%=request.getContextPath() %>/loversRoom">情侣房</a></li>
	<li><a class="scroll" href="<%=request.getContextPath() %>/thematicRoom">主题房</a></li>
</ul>
<script>
	$("span.menu").click(function(){
		$(".top-menu ul").slideToggle(200);
	});
</script>