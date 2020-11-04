package chat3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MultiClient {

	public static void main(String[] args) {

		//클라이언트의 접속자명을 입력
		System.out.println("이름을 입력하세요:");
		Scanner scanner=new Scanner(System.in);
		String s_name=scanner.nextLine();
		
		PrintWriter out=null;
		//서버의 메세지를 읽어오는 기능을 Receiver 클래스로 옮김
		//BufferedReader in=null;
		
		try {
			/*
			 C:\bin>java chat3.MultiClient 접속할 IP주소
			 	=>위와같이 접속하면 로컬이 아닌 내가원하는 서버로 
			 	접속할 수 있다
			 */
			String ServerIP="localhost";
			if(args.length>0) {
				ServerIP=args[0];
			}
			Socket socket=new Socket(ServerIP,9999);
			System.out.println("서버와 연결되었습니다...");
			
			//서버에서 보내는 메세지를 읽어올 Receiver 쓰레드 객체생성 및 시작
			Thread receiver=new Receiver(socket);
			//setDaemon(true) 선언이 없으므로 독립쓰레드로 생성된다.
			receiver.start();
			
			out=new PrintWriter(socket.getOutputStream(),true);
			out.println(s_name);//최초로 접속자의 이름을 전송
			
			//이름 전송이후에는 메세지를 지속적으로 전송함.
			while(out!=null) {
				try {
					String s2=scanner.nextLine();
					if(s2.equals("q")||s2.equals("Q")) {
						break;
					}
					else {
						out.println(s2);
					}
				}
				catch (Exception e) {
					System.out.println("예외:"+e);
				}
			}
			//while 루프를 탈출하면 소켓을 즉시 종료한다ㅏ.
			out.close();
			socket.close();
		}
		catch (Exception e) {
			System.out.println("예외[MultiClient]"+e);
		}
	}
}
