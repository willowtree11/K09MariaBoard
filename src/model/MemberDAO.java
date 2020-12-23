package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/*
	기본세팅:
	1. Dynamic Web Project 폴더 생성
	2. WEB-INF - lib 경로에 maria~~~.jar 파일 넣기
	3. web.xml 파일에 context-param 추가
*/
public class MemberDAO {
	
	Connection con; //커넥션 객체를 멤버변수로 설정하여 공유
	PreparedStatement psmt;
	ResultSet rs;
	
	//기본생성자를 통한 DB연결
	public MemberDAO() {
		String driver = "org.mariadb.jdbc.Driver"; //바뀜
		String url = "jdbc:mariadb://127.0.0.1:3306/kosmo_db"; //바뀜
		try {
			Class.forName(driver);
			String id = "kosmo_user"; //바뀜
			String pw = "1234";
			con = DriverManager.getConnection(url, id, pw);
			System.out.println("DB연결성공(디폴트생성자)");
		}
		catch (Exception e) {
			System.out.println("DB연결실패(디폴트생성자)");
			e.printStackTrace();
		}
	}
	
	//JSP에서 컨텍스트 초기화 파라미터를 인자로 전달하여 DB연결
	public MemberDAO(String driver, String url, String id, String pw) {		
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
	
	//로그인 방법3: DTO객체 대신 Map컬렉션에 회원정보를 저장 후 반환한다.
	public Map<String, String> getMemberMap(String id, String pwd){
		//회원정보를 저장할 Map컬렉션 생송
		Map<String, String> maps=new HashMap<String, String>();
		
		String query="SELECT id, pass, name FROM "
				+ " member WHERE id=? AND pass=?";
		try{
			psmt=con.prepareStatement(query);
			psmt.setString(1, id);
			psmt.setString(2, pwd);
			rs=psmt.executeQuery();

			if(rs.next()){
				maps.put("id", rs.getString(1));
				maps.put("pass", rs.getString("pass"));
				maps.put("name", rs.getString("name"));
			}
			else{
				System.out.println("결과셋이 없습니다.");
			}
			return maps;
		}
		catch(Exception e){
			System.out.println("getMemberDTO오류");
			e.printStackTrace();
		}
		return maps;
	}
	
	public static void main(String[] args) {
		new MemberDAO();
	}
}
