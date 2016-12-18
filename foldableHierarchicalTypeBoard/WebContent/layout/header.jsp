<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	request.setCharacterEncoding("UTF-8");
	String cp = request.getContextPath();
%>
<p align="center">
<a href="<%= cp %>/board/main.action">메인</a>
<a href="<%= cp %>/board/list.action">게시판</a>
</p>