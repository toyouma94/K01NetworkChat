package chat6;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultiServer {

	//클라이언트 정보 저장을 위한 Map컬렉션 생성
	static ServerSocket serverSocket=null;
	static Socket socket=null;
	Map<String,PrintWriter>clientMap;
	
	//생성자
	public MultiServer() {
		//클라이언트의 이름과 출력스트림을 저장할 HashMap컬렉션 생성
		clientMap=new HashMap<String, PrintWriter>();
		//HashMap 동기화 설정. 쓰레드가 사용자 정보에 동시에 접근하는 것을 차단 
		Collections.synchronizedMap(clientMap);
	}
	//채팅서버 초기화
	public void init() {
		//클라이언트의 이름을 저장
		try {
			/*
			 9999번 포트를 성정하여 서버객체를 생성하고 클라이언트의 
			 접속을 대기한다.
			 */
			serverSocket=new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			
			/*
			 1명의 클라이언트가 접속할때마다 접속을 허용(accept())해주고
			 동시에 MultiServerT 쓰레드를 생성한다.
			 해당 쓰레드는 1명의 클라이언트가 전송하는 메세지를 읽어서 Echo
			 해주는 역할을 담당한다.
			 */
			while(true) {
				socket=serverSocket.accept();
				System.out.println(
						socket.getInetAddress()+"(클라이언트)의"
						+socket.getPort()+"포트를통해"
						+socket.getLocalAddress()+"(서버)의"
						+socket.getLocalPort()+"포트로 연결됨");
				/*
				클라이언트의 메세지를 모든 클라이언트에게 전달하기 위한
				쓰레드 생성 및 시작. 한명당 하나씩의 쓰레드가 생성된다.
				 */
				Thread mst=new MultiServerT(socket);
				mst.start();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				serverSocket.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		MultiServer ms=new MultiServer();
		ms.init();
	}
	//접선된 모든 클라이언트에게 서버의 메세지를 전달하는 메소드
	public void sendAllMsg(String name,String msg) {
		
		//Map에 저장된 객체에 기값(접속자명을)을 먼저 얻어온다.
		Iterator<String>it=clientMap.keySet().iterator();
		//저장된 객체(클라이언트)의 갯수만큼 반복한다.
		while(it.hasNext()) {
			try {
				//각 클라이언트의 PrintWriter객체를 얻어온다
				PrintWriter it_out=(PrintWriter)clientMap.get(it.next());
				/*
				 클라이언트에게 메세지를 전달한다.
				 매개변수로 name이 있는경우와 없는 경우를 구분해서
				 메세지를 전달하게된다.
				 */
				if(name.equals("")) {
					//접속,퇴장에서 사용되는 부분
					it_out.println(msg);
				}
				else {
					//메세지를 보낼떄 사용되는 부분
					it_out.println("["+name+"]:"+msg);
				}
			}
			catch (Exception e) {
				System.out.println("예외:"+e);
			}
		}
	}
	/*
	 init()에 기술되었던 스트림을 생성후 메세지를 읽기/쓰기 하던 부분이
	 해당 내부클래스로 이동되었다.
	 */
	class MultiServerT extends Thread{
		
		//멤버변수
		Socket socket=null;
		PrintWriter out=null;
		BufferedReader in=null;
		
		/*
		 생성자:Socket을 기반으로 입출력 스트림 생성
		 */
		public MultiServerT(Socket socket) {
			this.socket=socket;
			try {
				out=new PrintWriter(socket.getOutputStream(),true);
				in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			}
			catch (Exception e) {
				System.out.println("예외:"+e);
			}
		}
		/*
		 쓰레드로 동작할 run()에서는 클라이언트의
		 접속자명과 메세지를 지속적으로 읽어 Echo 해주는 역할을
		 하고있다.
		 */
		@Override
		public void run() {
			
			String name="";
			String s="";
			try {
				//클라이언트의 이름을 읽어와서 저장
				name=in.readLine();
				/*
				 방금 접속한 클라이언트를 제외한 나머지에게 사용자의
				 입장을 알려준다.
				 */
				sendAllMsg("",name+"님이 입장하셨습니다.");
				//현재 접속자의 정보를 HashMap에 저장한다
				clientMap.put(name, out);
				
				//HashMap에 저장된 객체의 수로 접속자수를 파악할 수 있다.
				System.out.println(name+" 접속");
				System.out.println("현재 접속자 수는"+clientMap.size()+"명 입니다.");
				
				while(in!=null) {
					s=in.readLine();
					if(s==null)
						break;
					System.out.println(name+">>"+s);
					sendAllMsg(name, s);
				}
			}
			catch (Exception e) {
				System.out.println("예외:"+e);
			}
			finally {
				/*
				 클라이언트가 접속을 종료하면 Socket예외가 발생하게 되어
				 finally절로 진입하게된다. 이떄"대화명"을 통해 정보를
				 삭제한다.
				 */
				clientMap.remove(name);
				sendAllMsg("",name+"님이 퇴장하셨습니다.");
				//퇴장하는 클라이언트의 쓰레드명을 보여준다
				System.out.println(name+"["+Thread.currentThread().getName()+"] 퇴장");
				System.out.println("현재 접속자수는"+clientMap.size()+"명 입니다.");
				try {
					in.close();
					out.close();
					socket.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
