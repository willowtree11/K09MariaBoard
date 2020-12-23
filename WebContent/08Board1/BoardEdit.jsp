<%@page import="util.JavascriptUtil"%>
<%@page import="model.BbsDAO"%>
<%@page import="model.BbsDTO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%-- 글 작성 페이지 진입 전 로그인 체크 --%>
<%@ include file="../common/isLogin.jsp" %>
<%
	//파라미터로 전송된 게시물의 일련번호를 받음
	String num=request.getParameter("num");
	BbsDAO dao=new BbsDAO(application);
	
	//본인이 작성한 게시물이므로 조회수 증가는 의미없음.
	//dao.updateVisitCount(num);
	
	//일련번호에 해당하는 게시물을 DTO객체로 반환함
	BbsDTO dto=dao.selectView(num);
	
	//본인확인 후 작성자가 아니면 뒤로 보내기(url 페이지 번호 조작해서 수정 시도하려는 유저 행동 차단하기 위함)
	if(!session.getAttribute("USER_ID").toString().equals(dto.getId())){
		JavascriptUtil.jsAlertBack("작성자 본인만 수정 가능합니다.", out);
		return;
	}
	
	dao.close();
%>
<!DOCTYPE html>
<html lang="ko">
<jsp:include page="../common/boardHead.jsp"/>
<body>
<div class="container">
	<div class="row">		
		<jsp:include page="../common/boardTop.jsp" />
	</div>
	<div class="row">		
		<jsp:include page="../common/boardLeft.jsp" />
		<div class="col-9 pt-3">
			<!-- ### 게시판의 body 부분 start ### -->
			<h3>게시판 - <small>Edit(글 수정하기)</small></h3>
			<script>
				function checkValidate(fm){
					if(fm.title.value==""){
						alert("제목을 입력하세요."); 
						fm.title.focus(); 
						return false; 
					}
					if(fm.content.value==""){
						alert("내용을 입력하세요."); 
						fm.content.focus(); 
						return false;
					}
				}
			</script>
			<div class="row mt-3 mr-1">
				<table class="table table-bordered table-striped">
					<form name="writeFrm" method="post" action="EditProc.jsp" 
						onsubmit="return checkValidate(this);">
						<!-- 
							특정 게시물(ex. 10번 게시물)을 수정한다는 근거가 없는 상태이므로
							해당 게시물의 일련번호를 전송해야 수정이 가능하다.
							hidden 속성으로 form을 만들어 처리하면 화면에서는 숨김처리되지만,
							서버로는 값을 전송할 수 있다.
							
							update 테이블 set 컬럼=값 
							-> where절이 없다면 모든 게시물에 대한 수정이 된다. 
						 -->
					<input type="hidden" name="num" value="<%=num %>" />
						<colgroup>
							<col width="20%"/>
							<col width="*"/>
						</colgroup>
						<tbody>
	<!-- 						<tr> -->
	<!-- 							<th class="text-center align-middle">작성자</th> -->
	<!-- 							<td> -->
	<!-- 								<input type="text" class="form-control"	style="width:100px;"/> -->
	<!-- 							</td> -->
	<!-- 						</tr> -->
	<!-- 						<tr> -->
	<!-- 							<th class="text-center"  -->
	<!-- 								style="vertical-align:middle;">패스워드</th> -->
	<!-- 							<td> -->
	<!-- 								<input type="password" class="form-control" -->
	<!-- 									style="width:200px;"/> -->
	<!-- 							</td> -->
	<!-- 						</tr> -->
							<tr>
								<th class="text-center"
									style="vertical-align:middle;">제목</th>
								<td>
									<input type="text" class="form-control" 
										name="title" value="<%=dto.getTitle()%>"/>
								</td>
							</tr>
							<tr>
								<th class="text-center"
									style="vertical-align:middle;">내용</th>
								<td>
									<textarea rows="10" 
										class="form-control" name="content"><%=dto.getContent()%></textarea>
								</td>
							</tr>
	<!-- 						<tr> -->
	<!-- 							<th class="text-center" -->
	<!-- 								style="vertical-align:middle;">첨부파일</th> -->
	<!-- 							<td> -->
	<!-- 								<input type="file" class="form-control" /> -->
	<!-- 							</td> -->
	<!-- 						</tr> -->
						</tbody>
					</table>
					</div>
					<div class="row mb-3">
					<div class="col text-right">					
						<button type="submit" class="btn btn-danger">전송하기</button>
						<button type="reset" class="btn btn-dark">Reset</button>
						<button type="button" class="btn btn-warning" onclick="location.href='BoardList.jsp';">리스트보기</button>
					</div>
				</form>
			</div>
		<!-- ### 게시판의 body 부분 end ### -->
		</div>
	</div>
	<div class="row border border-dark border-bottom-0 border-right-0 border-left-0"></div>
	<jsp:include page="../common/boardBottom.jsp"/>
</div>
</body>
</html>

 
