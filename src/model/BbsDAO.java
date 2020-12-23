package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

public class BbsDAO {
	
	Connection con; //커넥션 객체를 멤버변수로 설정하여 공유
	PreparedStatement psmt;
	ResultSet rs;
	
	/*
		인자생성자1: JSP파일에서 web.xml에 등록된 컨텍스트  초기화 파라미터를
		가져와서 생성자 호출 시 파라미터로 전달한다.
	 */
	public BbsDAO(String driver, String url, String id, String pw) {		
		try {
			Class.forName(driver);
			//DB에 연결된 정보를 멤버변수에 저장
			con = DriverManager.getConnection(url, id, pw);
			System.out.println("DB연결성공(인자생성자)");
		}
		catch (Exception e) {
			System.out.println("DB연결실패(인자생성자)");
			e.printStackTrace();
		}
	}
	
	/*
 	인자생성자2: JSP에서는 application내장객체를 파라미터로 전달하고
 			생성자에서는 web.xml을 직접 접근한다.
 			application 내장객체는 javax.servlet.ServletContext 타입으로
 			정의되었으므로 매개변수를 이와같이 정의해준다.
	 */
	public BbsDAO(ServletContext ctx) {
		try {
			Class.forName(ctx.getInitParameter("MariaJDBCDriver"));
			String id = ctx.getInitParameter("MariaUser");
			String pw = ctx.getInitParameter("MariaPass");
			con = DriverManager.getConnection(
					ctx.getInitParameter("MariaConnectURL"),id,pw);
			System.out.println("DB연결성공");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	생성자3 : 커넥션풀(DBCP)을 이용한 DB연결
	 */
	public BbsDAO() {
		try {
			Context initctx = new InitialContext();
			Context ctx = (Context)initctx.lookup("java:comp/env"); 
			DataSource source = (DataSource)ctx.lookup("jdbc/myoracle"); 
			//Connection 새로 생성하지 않고 전역변수 con 씀
			con = source.getConnection();
			System.out.println("DBCP연결성공");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
		데이터베이스의 연결을 해제할 때 사용.
		컴퓨터는 한정된 자원을 사용하므로 연결했다면 반드시
		연결을 해제해주어야 한다.
	 */
	public void close() {
		try {
			//사용된 자원이 있다면 자원해제 해준다.
			if(rs!=null) rs.close();
			if(psmt!=null) psmt.close();
			if(con!=null) con.close();
		}
		catch (Exception e) {
			System.out.println("자원반납시 예외발생");
		}
	}
	
	/*
		게시판 리스트에서 게시물의 갯수를 count() 그룸함수를 통해 구해 반환
		게시물의 가상번호, 페이지 처리를 위해 사용
	 */
	public int getTotalRecordCount(Map<String, Object> map) {
		
		//게시물의 갯수는 최초 0으로 초기화
		int totalCount = 0;
		
		//기본쿼리문(전체레코드를 대상으로 함)
		String query = "SELECT COUNT(*) FROM board";
		
		//JSP페이지에서 검색어를 입력한 경우 where절이 동적으로 추가된다.
		if(map.get("Word")!=null) {
			query += " WHERE "+ map.get("Column") +" "
					+ " LIKE '%"+ map.get("Word") +"%'";
		}
		System.out.println("query="+query);

		try {
			//쿼리 실행후 결과값 반환
			psmt = con.prepareStatement(query);
			rs = psmt.executeQuery();
			rs.next();
			totalCount = rs.getInt(1);
		}
		catch(Exception e) {}
		
		return totalCount;
	}
	
	//member테이블과 join해서 게시물 갯수를 카운트
	public int getTotalRecordCountSearch(Map<String, Object> map) {
		
		//게시물의 갯수는 최초 0으로 초기화
		int totalCount = 0;
		
		//기본쿼리문(전체레코드를 대상으로 함)
		String query = "SELECT COUNT(*) FROM board B "
				+ " 		INNER JOIN member M "
				+ "				ON B.id=M.id ";
		
		//JSP페이지에서 검색어를 입력한 경우 where절이 동적으로 추가된다.
		if(map.get("Word")!=null) {
			query += " WHERE "+ map.get("Column") +" "
					+ " LIKE '%"+ map.get("Word") +"%'";
		}
		System.out.println("query="+query);

		try {
			//쿼리 실행후 결과값 반환
			psmt = con.prepareStatement(query);
			rs = psmt.executeQuery();
			rs.next();
			totalCount = rs.getInt(1);
		}
		catch(Exception e) {}
		
		return totalCount;
	}
	
	/*
		게시판 리스트에서 조건에 맞는 레코드를 select하여 ResultSet을 
		List컬렉션에 저장한 후 반환하는 메소드
	 */
	public List<BbsDTO> selectList(Map<String, Object> map){
		
		//리스트 컬렉션을 생성
		List<BbsDTO> bbs = new Vector<BbsDTO>();
		
		//기본쿼리문
		String query = "SELECT * FROM board ";
		//검색어가 있을경우  조건절 동적 추가
		if(map.get("Word")!=null) {		
			query += " WHERE "+ map.get("Column") +" "
					+ " LIKE '%"+ map.get("Word") +"%'";			
		}
		//최근게시물을 항상 위로 노출해야하므로 작성된 순서의 역순으로 정렬한다. 
		query +=" ORDER BY num DESC";
		try {
			psmt = con.prepareStatement(query);
			rs = psmt.executeQuery();
			//오라클이 반환해준 ResultSet의 갯수만큼 반복
			while(rs.next()) {
				//하나의 레코드를 DTO객체에 저장하기 위해 새로운 객체생성
				BbsDTO dto = new BbsDTO();
				
				//setter()를 통해 각각의 컬럼에 데이터 저장
				dto.setNum(rs.getString(1));
				dto.setTitle(rs.getString("title"));
				dto.setContent(rs.getString(3));
				dto.setPostdate(rs.getDate("postdate"));
				dto.setId(rs.getString("id"));
				dto.setVisitcount(rs.getString(6));

				//DTO객체를 List컬렉션에 추가
				bbs.add(dto);
			}
		}
		catch(Exception e) {
			System.out.println("Select시 예외발생");
			e.printStackTrace();
		}

		return bbs;
	}
	
	//글쓰기 처리 메소드
	public int insertWrite(BbsDTO dto) {
		int affected = 0;
		try {
		/*
			데이터 입력을 위한 insert문 작성. 
			MariaDB는 sequence를 쓰지 않고 자동증가 컬럼을 씀, 
			해당 컬럼에  auto_increment를 부여하게 되면
			레코드 삽입시 자동으로 증가하는 컬럼이 된다.
			insert문 작성시 해당 컬럼은 명시하지 않는다.
		 */
			String query = "INSERT INTO board ( "
				+ " title,content,id,visitcount) "
				+ " VALUES ( "
				+ " ?, ?, ?, 0)";

			psmt = con.prepareStatement(query);
			psmt.setString(1, dto.getTitle());
			psmt.setString(2, dto.getContent());
			psmt.setString(3, dto.getId());
			
			/*
			쿼리문 실행시 사용하는 메소드
			 	executeQuery() : select계열의 쿼리문을
			 		실행할때 사용한다. 행에 영향을 주지않고
			 		조회를 위해 사용된다. 반환타입은 ResultSet이다.
			 	executeUpdate() : insert,delete,update
			 		쿼리문을 실행할때 사용한다. 행에 영향을 주게되고
			 		반환타입은 쿼리의 영향을 받은 행의 갯수가 반환되므로
			 		int형이 된다. 			 
			 */
			affected = psmt.executeUpdate();
		}
		catch(Exception e) {
			System.out.println("insert중 예외발생");
			e.printStackTrace();
		}
		
		return affected;
	}
	
	//게시물 조회수를 증가시킴
	public void updateVisitCount(String num) {
		
		String query = "UPDATE board SET "
			+ " visitcount=visitcount+1 "
			+ " WHERE num=?";
		System.out.println("조회수증가:"+query);
		try {
			psmt = con.prepareStatement(query);
			psmt.setString(1, num);
			/*
			쿼리 실행시 executeQuery() 혹은 executeUpdate()
			둘다 사용가능하다. 단, 두 메소드의 차이는 반환값이 다르다는 
			점이다. 반환값이 굳이 필요없는 경우라면 어떤것을 사용해도
			무방하다. 
			 */
			psmt.executeQuery();
		}
		catch(Exception e) {
			System.out.println("조회수 증가시 예외발생");
			e.printStackTrace();
		}
	}
	
	//일련번호에 해당하는 게시물 하나를 가져온다.
	public BbsDTO selectView(String num) {

		BbsDTO dto = new BbsDTO();
		
		//게시판 테이블만 사용하여 게시물 조회
		//String query = "SELECT * FROM board WHERE num=?";
		
		//게시판, 회원 테이블을 조인하여 이름까지 가져와서 조회
		String query = ""
			+"SELECT "
			+"    num, title, content, B.id, postdate, visitcount, name "
			+" FROM member M INNER JOIN board B "
			+"    ON M.id=B.id "
			+" WHERE num=? ";

		try {
			psmt = con.prepareStatement(query);
			psmt.setString(1, num);
			rs = psmt.executeQuery();
			if(rs.next()) {
				dto.setNum(rs.getString(1));
				dto.setTitle(rs.getString(2));
				dto.setContent(rs.getString("content"));
				dto.setPostdate(rs.getDate("postdate"));
				dto.setId(rs.getString("id"));
				dto.setVisitcount(rs.getString(6));		
				/*
				member 테이블과 join하여 얻어온 name을 DTO에 추가함.
				 */
				dto.setName(rs.getString(7));
			}
		}
		catch(Exception e) {
			System.out.println("상세보기시 예외발생");
			e.printStackTrace();
		}

		return dto;
	}
	
	//게시물 수정하기
	public int updateEdit(BbsDTO dto) {
		int affected = 0;
		try {
			String query = "UPDATE board SET "
					+ " title=?, content=? "
					+ " WHERE num=?";
			
			psmt = con.prepareStatement(query);
			psmt.setString(1, dto.getTitle());
			psmt.setString(2, dto.getContent());
			psmt.setString(3, dto.getNum());
			
			affected = psmt.executeUpdate();
		} 		
		catch (Exception e) {
			System.out.println("update중 예외 발생");
			e.printStackTrace();
		}
		System.out.println("DAO="+affected);
		return affected;
	}
	
	//게시물 삭제하기
	public int delete(BbsDTO dto) {
		int affected=0;
		try {
			String query = "DELETE FROM board WHERE num=? ";
			
			psmt = con.prepareStatement(query);
			psmt.setString(1, dto.getNum());
			
			affected = psmt.executeUpdate();
		} 		
		catch (Exception e) {
			System.out.println("delete중 예외 발생");
			e.printStackTrace();
		
		}
		return affected;
	}
	
	//게시판 리스트 출력 - 페이지처리 포함
	public List<BbsDTO> selectListPage(Map<String,Object> map){

		List<BbsDTO> bbs = new Vector<BbsDTO>();

		//쿼리문이 아래와같이 페이지처리 쿼리문으로 변경됨.
		String query = " "
			+" SELECT * FROM ( "
			+"	 SELECT Tb.*, ROWNUM rNum FROM ( "
			+"	    SELECT * FROM board ";
		if(map.get("Word")!=null)
		{
			query +=" WHERE "+ map.get("Column") +" "
			  +" LIKE '%"+ map.get("Word") +"%' "; 
		}
		query += " "
		    +"    	ORDER BY num DESC "
		    +"    ) Tb "
		    +" ) "
		    +" WHERE rNum BETWEEN ? AND ?";
		System.out.println("쿼리문:"+ query);
		
		try {
			psmt = con.prepareStatement(query);
				
			//JSP에서 계산한 페이지 범위값을 이용해 인파라미터를 설정함.
			psmt.setString(1, map.get("start").toString());
			psmt.setString(2, map.get("end").toString());
			
			rs = psmt.executeQuery();

			while(rs.next()) {
				BbsDTO dto = new BbsDTO();

				dto.setNum(rs.getString("num"));
				dto.setTitle(rs.getString("title"));
				dto.setContent(rs.getString("content"));
				dto.setPostdate(rs.getDate("postdate"));
				dto.setId(rs.getString("id"));
				dto.setVisitcount(rs.getString("visitcount"));

				bbs.add(dto);
			}
		}
		catch(Exception e) {
			System.out.println("Select시 예외발생");
			e.printStackTrace();
		}

		return bbs;
	}
	
	//게시판 리스트+페이지처리+회원이름으로 검색기능 추가
	public List<BbsDTO> selectListPageSearch(Map<String,Object> map){

		List<BbsDTO> bbs = new Vector<BbsDTO>();

		//쿼리문이 아래와같이 페이지처리 쿼리문으로 변경됨.
		String query = " "						
			+"		SELECT B.*, M.name FROM board B " 
			+"         INNER JOIN member M "
			+"           ON B.id=M.id " ;
		if(map.get("Word")!=null) {
			query +="      WHERE "+map.get("Column")
					+" LIKE '%"+map.get("Word")+"%' ";
		}			
		query +="		ORDER BY num DESC LIMIT ?, ?";				
		System.out.println("쿼리문:"+ query);
		
		try {
			psmt = con.prepareStatement(query);
				
			//JSP에서 계산한 페이지 범위값을 이용해 인파라미터를 설정함.
			//setString()으로 인파라미터를 설정하면 문자형이 되므로
			//양쪽에 싱글 쿼테이션이 자동 기술된다. 여기서는 정수로 입력해야 하므로
			//setInt()를 사용하고, 인자도 전달되는 변수를 정수로 변경해야 한다
			psmt.setInt(1, Integer.parseInt(map.get("start").toString()));
			psmt.setInt(2, Integer.parseInt(map.get("end").toString()));
			
			rs = psmt.executeQuery();

			while(rs.next()) {
				BbsDTO dto = new BbsDTO();

				dto.setNum(rs.getString("num"));
				dto.setTitle(rs.getString("title"));
				dto.setContent(rs.getString("content"));
				dto.setPostdate(rs.getDate("postdate"));
				dto.setId(rs.getString("id"));
				dto.setVisitcount(rs.getString("visitcount"));
				
				//member테이블과의 JOIN으로 이름이 추가됨
				dto.setName(rs.getString("name"));

				bbs.add(dto);
			}
		}
		catch(Exception e) {
			System.out.println("Select시 예외발생");
			e.printStackTrace();
		}

		return bbs;
	}
}	
