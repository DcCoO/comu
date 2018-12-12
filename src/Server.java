import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Deque;

public class Server {
	public String fileName;
	public int fileSize;
	public volatile Deque<Window> windows;
	public int windowSize;
	public volatile int index;
	
	public Server(String fileName, int windowSize) {
		this.fileName = fileName;
		File f = new File(fileName);
		this.fileSize = (int) f.length();
		this.windows = new LinkedList<Window>();
		this.windowSize = windowSize; this.index = 0;
	}
	
	public void Send() throws Exception {
		
		SendFileSize(this.fileSize);
		System.out.println("SERVIDOR: arquivo tem " + this.fileSize + " bytes");
		
		FileInputStream fis = new FileInputStream(fileName);
		byte[] buffer = new byte[100];
		byte[] lastBuffer = new byte[ this.fileSize % 100 == 0 ? 100 : this.fileSize % 100];
		
		//criando sockets para cada janela
		DatagramSocket[] sockets = new DatagramSocket[this.windowSize];
		for(int i = 0; i < this.windowSize; i++) {
			sockets[i] = new DatagramSocket(Port.WINDOW + i);
		}
		
		int fileParts = this.fileSize % 100 == 0? (this.fileSize / 100) : (this.fileSize / 100) + 1;
		System.out.println("FILE PARTS = " + fileParts);
		for(int i = 0; i < this.windowSize; i++) {
			if(fis.read(index < fileParts - 1? buffer : lastBuffer) > 0) {
				Window w = new Window();
				w.Init(buffer, index++, sockets[i]);
				w.start();
				this.windows.addLast(w);
			}
			else break;
		}
		
		while(index < fileParts - 1) {
			
			try {
				System.out.println("Window[" + this.windows.getFirst().socket.getLocalPort() % 1000 +
						"] esperando inserir " + this.windows.getFirst().index);
				
				while(this.windows.getFirst().status == Status.RUNNING) {}
				//System.out.println("Index " + this.windows.peek().index + " enviado!");
				
				while(this.windows.getFirst().status == Status.FINISHED) {
					Window aux = this.windows.poll();
					System.out.println("Window[" + aux.socket.getLocalPort() % 1000 + "] recebeu " + aux.index);
					
					//System.out.println("PACKET " + aux.index + " ENVIADO: " + aux.barr(aux.data));
					
					Window w = aux.clone();
					if(fis.read(index < fileParts - 1? buffer : lastBuffer) > 0) {
						index = this.windows.getLast().index + 1;
						w.Init(buffer, index, w.socket);
						//System.out.println("Window[" + w.socket.getLocalPort() + "]");
						w.start();
						this.windows.addLast(w);
					}
					//else {
					//	System.out.println("ACABOU EM " + index);
					//	break;
					//}
				}
			
			}
			catch(Exception e) {
				System.out.println("INDEX = " + index);
				break;
			}
		}
		
		System.out.println("SERVER: acabou!");
		
		fis.close();
	}
	
	public void SendFileSize(int size) throws Exception {
		InetAddress IPAddress = InetAddress.getByName("localhost");	
		byte[] buf = ByteBuffer.allocate(Integer.BYTES).putInt(size).array();
		DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, IPAddress, Port.CLIENT);
		DatagramSocket serverSocket = new DatagramSocket();
		serverSocket.send(sendPacket);
	}
	
	
	
	public static void main(String[] args) throws Exception {	
		int windowSize = 8;		
		Server server = new Server("input.txt", windowSize);
		server.Send();
	}
}
