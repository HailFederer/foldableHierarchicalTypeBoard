<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	request.setCharacterEncoding("utf-8");
	String cp = request.getContextPath();
	int restDiv = (Integer)request.getAttribute("restDiv");
%>

<link rel="stylesheet" href="<%=cp%>/board/css/list.css" type="text/css"/>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js"></script>
<script type="text/javascript">

	function searchData() {
		
		var f = document.searchForm;
		
		f.action = "<%=cp%>/board/list.action";
		f.submit();
	}
	
	$(document).ready(function(){
		
		$('[id^="fold-"]').click(function(){
			
			var src = ($(this).attr('src')=='<%=cp%>/board/image/nonFolded-minus.png') ?'<%=cp%>/board/image/folded-plus.png':'<%=cp%>/board/image/nonFolded-minus.png';
		    $(this).attr('src',src);
			
			var obj = $('.'+ this.id);
			
			if(obj.css('display')=='none')
				obj.show();
			else
				obj.hide();
		});
	});

</script>


<div id="bbsList">
	<div id="bbsList_title">
	게 시 판 (Struts2)
	</div>

	<div id="bbsList_header">
		<div id="leftHeader">
		  <form name="searchForm" method="post" action="">
			<select name="searchKey" class="selectFiled">
				<option value="subject">제목</option>
				<option value="name">작성자</option>
				<option value="content">내용</option>
			</select>
			<input type="text" name="searchValue" class="textFiled"/>
			<input type="button" value=" 검 색 " class="btn2" 
				onclick="searchData();"/>
		  </form>
		</div>
		<div id="rightHeader">
			<input type="button" value=" 글올리기 " class="btn2" 
				onclick="javascript:location.href='<%=cp%>/board/created.action';"/>
		</div>
	</div>
	<div id="bbsList_list">
		<div id="title">
			<dl>
				<dt class="num">번호</dt>
				<dt class="subject">제목</dt>
				<dt class="name">작성자</dt>
				<dt class="created">작성일</dt>
				<dt class="hitCount">조회수</dt>
			</dl>
		</div>
		
		<c:forEach var="dto" items="${lists }">
			<c:forEach begin="1" end="${dto.depthGap }" step="1">
			</div>
			<% restDiv = restDiv - 1;%>
			</c:forEach>
		<div class="fold-${dto.parent}" id="lists">
			<dl>
				<dd class="num">${dto.boardNum}</dd>
				<dd class="subject">

					<c:forEach items="${dto.verticalList }" var="verticalList">
							<c:if test="${verticalList != 0}"><img src="<%=cp%>/board/image/vertical.png"/></c:if>
							<c:if test="${verticalList == 0}"><img src="<%=cp%>/board/image/blank_nonTop.png"/></c:if>
					</c:forEach>
					
					<c:if test="${dto.depth == 0 && dto.replyNum == 0}">
						<img src="<%=cp%>/board/image/blank_top.png"/>
					</c:if>
					<c:if test="${dto.depth == 0 && dto.replyNum != 0}">
						<img id="fold-${dto.boardNum}" src="<%=cp%>/board/image/nonFolded-minus.png"/>
					</c:if>
						
					<c:if test="${dto.depth != 0 && dto.siblingNum != 0 && dto.replyNum == 0}">
						<img src="<%=cp%>/board/image/middle.png"/>
						<img src="<%=cp%>/board/image/horizontal.png"/>
					</c:if>
					<c:if test="${dto.depth != 0 && dto.siblingNum != 0 && dto.replyNum != 0}">
						<img src="<%=cp%>/board/image/middle.png"/>
						<img id="fold-${dto.boardNum}" src="<%=cp%>/board/image/nonFolded-minus.png"/>
					</c:if>
						
					<c:if test="${dto.depth != 0 && dto.siblingNum == 0 && dto.replyNum == 0}">
						<img src="<%=cp%>/board/image/bottom.png"/>
						<img src="<%=cp%>/board/image/horizontal.png"/>
					</c:if>
					<c:if test="${dto.depth != 0 && dto.siblingNum == 0 && dto.replyNum != 0}">
						<img src="<%=cp%>/board/image/bottom.png"/>
						<img id="fold-${dto.boardNum}" src="<%=cp%>/board/image/nonFolded-minus.png"/>
					</c:if>
					
					<a href="${urlArticle}&boardNum=${dto.boardNum}">
					${dto.subject }</a>
				</dd>
					
				<dd class="name">${dto.name }</dd>
				<dd class="created">${dto.created }</dd>
				<dd class="hitCount">${dto.hitCount }</dd>
			</dl>
		</c:forEach>
			<% for(int i=0; i<restDiv; i++){ %>
			</div>
			<%} %>
		
		<div id="footer">
			<p>
				<c:if test="${totalDataCount != 0 }">
					${pageIndexList }
				</c:if>
				<c:if test="${totalDataCount == 0 }">
					등록된 게시물이 없습니다.
				</c:if>
			</p>
		</div>
	</div>
</div>