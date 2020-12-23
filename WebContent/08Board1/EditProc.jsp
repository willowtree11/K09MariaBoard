<%@page import="model.BbsDAO"%>
<%@page import="model.BbsDTO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="../common/isLogin.jsp" %>
<%
	//한글처리
	request.setCharacterEncoding("UTF-8");
	
	//폼값받기
	String num=request.getParameter("num");
	String title=request.getParameter("title");
	String content=request.getParameter("content");
	
	//DTO객체 생성
	BbsDTO dto=new BbsDTO();
	dto.setNum(num); //특정 게시물에 대한 수정이므로 일련번호 추가(일련번호로 고유값을 불러오기 위함)
	dto.setTitle(title);
	dto.setContent(content);
	
	//DAO객체 생성
	BbsDAO dao=new BbsDAO(application);
	
	//수정 메소드 호출
	int affected = dao.updateEdit(dto);
	if(affected==1){
		//수정된 내용을 봐야하므로 리스트가 아닌 상세보기 페이지로 가는 것.
		response.sendRedirect("BoardView.jsp?num="+dto.getNum());
	}
	else{
		%>
		<script>
			alert("수정하기에 실패하였습니다.");
			history.go(-1);
		</script>
		<%
		}
	%>