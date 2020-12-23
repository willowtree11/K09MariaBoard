<%@page import="model.MemberDAO"%>
<%@page import="model.MemberDTO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%-- LoginProcess.jsp --%>
<%
	//폼 값 받기
	String id=request.getParameter("user_id");
	String pw=request.getParameter("user_pw");
	
	//web.xml에 저장된 컨텍스트 초기화  파라미터 가져옴
	String drv=application.getInitParameter("JDBCDriver");
	String url=application.getInitParameter("ConnectionURL");
	
	//DAO객체 생성 및 DB연결
	MemberDAO dao=new MemberDAO(drv, url);
	
	/*
		연습문제: 작성용 교안에 있는 getMembeerDTO()를 MemberDAO클래스에 작성한 후
		아래 코드를 수정하여 로그인 페이지에 회원의 이름이 출력되도록 구현하시오
	*/
	
	//1. 타입변경(boolean 아님)
	MemberDTO memberDTO=dao.getMemberDTO(id, pw); 
	if(memberDTO.getId()!=null){
		//2. 로그인 성송 시 세션영역에 아래 속성을 저장한다.(입력시 받은게 아니라 DTO에 저장된 정보를 불러옴)
		session.setAttribute("USER_ID", memberDTO.getId());
		session.setAttribute("USER_PW", memberDTO.getPass());
		session.setAttribute("USER_NAME", memberDTO.getName());
		
		//로그인 페이지로 이동
		response.sendRedirect("Login.jsp");
	}
	else{
		//로그인 실패시 리퀘스트 영역에 속성을 저장 후 로그인 페이지로 포워드
		request.setAttribute("ERROR_MSG", "아이디가 없습니다. 가입이 필요합니다.");
		request.getRequestDispatcher("Login.jsp").forward(request, response);
	}
%>