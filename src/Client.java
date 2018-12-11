import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class Client {
	
	public String fileName;
	public int fileSize;
	public int windowSize;
	public Module[] module;
	
	public Client(String fileName, int windowSize, int percentage) throws Exception {
		this.fileName = fileName;
		this.windowSize = windowSize;
		this.module = new Module[windowSize];
		for(int i = 0; i < windowSize; i++) {
			this.module[i] = new Module(percentage, new DatagramSocket(2000 + i));
		}
	}
	
	public void Receive() throws Exception {
		
			
		for(int i = 0; i < module.length; i++) {
			module[i].start();
		}
		
		this.fileSize = GetFileSize();
		System.out.println("CLIENTE: arquivo tem " + this.fileSize + " bytes");
	}
	
	public int GetFileSize() throws Exception {
		DatagramSocket socket = new DatagramSocket(3000);
		byte[] ans = new byte[4];
		DatagramPacket receivePacket = new DatagramPacket(ans, ans.length);
		socket.receive(receivePacket);
		ByteBuffer wrapped = ByteBuffer.wrap(ans); 
		return wrapped.getInt();
	}
	
    public static void main(String[] args) throws Exception {
		
    	int windowSize = 5;
    	
    	Client client = new Client("output.zip", windowSize, 100);
    	
    	client.Receive();
		
	}

    
	
}
