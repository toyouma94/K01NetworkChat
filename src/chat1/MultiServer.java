package chat1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {

	public static void main(String[] args) {

		ServerSocket serverSocket=null;
		Socket socket=null;
		PrintWriter out=null;
		BufferedReader in=null;
		String s="";
		
		try {
			/*
			 9999번으로 포트번호를 설정하여 서버객체를 생성하고
			 클라이언트의 접속을 기다린다.
			 */
			serverSocket=new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			
			////...접속대기중...
			
			/*
			 클라이언트가 접속요청을 하면 accept()를 통해 허가한다.
			 */
			socket=serverSocket.accept();
			/*
			 getInetAddress():소켓이 연결되어 있는 원격 IP주소를얻어옴
			 getPort():원격 포트번호를 얻어옴.
			 	즉 클라이언트의 IP와 포트번호를 얻어와서 출력함.
			 */
			System.out.println(socket.getInetAddress()+":"+socket.getPort());
			
			//서버->클라이언트 측으로 메세지를 전송하기위한 스트림 생성
			out=new PrintWriter(socket.getOutputStream(),true);
			//클라이언트로부터 메세지를 받기위한 스트림 생성
			in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			//클라이언트가 보낸 메세지를 라인단위로 읽어옴
			s=in.readLine();
			//읽어온 내용을 콘솔에 출력
			System.out.println("Client에서 읽어옴:"+s);
			//클라이언트로 응답메세지(Echo)를 보냄
			out.println("Server에서응답:"+s);
			//콘솔에 종료메세지 출력
			System.out.println("Bye...!!!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				//입출력 스트림 종료(자원해제)
				in.close();
				out.close();
				//소켓 종료(자원해제)
				socket.close();
				serverSocket.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
