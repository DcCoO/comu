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
	public int percentage;
	
	public Client(String fileName, int windowSize, int percentage) throws Exception {
		this.fileName = fileName;
		this.windowSize = windowSize;
		this.module = new Module[windowSize];
		this.percentage = percentage;
	}
	
	public void Receive() throws Exception {
		
		
		this.fileSize = GetFileSize();
		System.out.println("CLIENTE: arquivo tem " + this.fileSize + " bytes");
		
		for(int i = 0; i < windowSize; i++) {
			this.module[i] = new Module(this.percentage, new DatagramSocket(2000 + i), this.fileSize);
			this.module[i].client = this;
		}
		
		for(int i = 0; i < module.length; i++) {
			module[i].start();
		}
		
		
	}
	
	public int GetFileSize() throws Exception {
		DatagramSocket socket = new DatagramSocket(Port.CLIENT);
		byte[] ans = new byte[4];
		DatagramPacket receivePacket = new DatagramPacket(ans, ans.length);
		socket.receive(receivePacket);
		ByteBuffer wrapped = ByteBuffer.wrap(ans); 
		return wrapped.getInt();
	}
	
    public static void main(String[] args) throws Exception {
		
    	int windowSize = 2;
    	
    	Client client = new Client("output.zip", windowSize, 100);
    	
    	client.Receive();
		
	}

    
	
}
