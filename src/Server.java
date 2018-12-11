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
import java.util.Queue;

public class Server {
	public String fileName;
	public int fileSize;
	public Queue<Window> windows;
	public int windowSize;
	
	public Server(String fileName, int windowSize) {
		this.fileName = fileName;
		File f = new File(fileName);
		this.fileSize = (int) f.length();
		this.windows = new LinkedList<Window>();
		this.windowSize = windowSize;
	}
	
	public void Send() throws Exception {
		
		SendFileSize(this.fileSize);
		System.out.println("SERVIDOR: arquivo tem " + this.fileSize + " bytes");
		
		FileInputStream fis = new FileInputStream(fileName);
		byte[] buffer = new byte[100];
		
		//criando sockets para cada janela
		DatagramSocket[] sockets = new DatagramSocket[this.windowSize];
		for(int i = 0; i < this.windowSize; i++) {
			sockets[i] = new DatagramSocket(1000 + i);
		}
		
		int index = 0;
		int fileParts = this.fileSize % 100 == 0? (this.fileSize / 100) : (this.fileSize / 100) + 1;
		
		for(int i = 0; i < this.windowSize; i++) {
			if(fis.read(buffer) > 0) {
				Window w = new Window();
				w.Init(buffer, index++, sockets[i]);
				w.start();
				this.windows.add(w);
			}
			else break;
		}
		
		while(index < fileParts) {
			while(this.windows.peek().status == Status.RUNNING) {}
			System.out.println("Index " + this.windows.peek().index + " enviado!");
			
			//TODO problema do ultimo packet nao necessariamente ter tamanho 100
			//filesize mod 100
			while(this.windows.peek().status == Status.FINISHED) {
				Window w = this.windows.poll();
				if(fis.read(buffer) > 0) {
					w.Init(buffer, index++, w.socket);
					w.start();
					this.windows.add(w);
				}
				else break;
			}
		}
		
		fis.close();
	}
	
	public void SendFileSize(int size) throws Exception {
		int clientPort = 3000;	
		InetAddress IPAddress = InetAddress.getByName("localhost");	
		byte[] buf = ByteBuffer.allocate(Integer.BYTES).putInt(size).array();
		DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, IPAddress, clientPort);
		DatagramSocket serverSocket = new DatagramSocket();
		serverSocket.send(sendPacket);
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		int windowSize = 5;		
		Server server = new Server("input.zip", windowSize);
		
		server.Send();
		
		
	}
}
