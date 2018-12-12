import java.io.DataInputStream;
import java.io.File;
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
	public volatile byte[] arq;
	public volatile int index;
	public File file;
	public FileOutputStream os; 
	public int writtenPackets;
	public int fileParts;
	
	public Client(String fileName, int windowSize, int percentage) throws Exception {
		this.fileName = fileName;
		this.windowSize = windowSize;
		this.module = new Module[windowSize];
		this.percentage = percentage;
		this.index = 0;
		this.file = new File(this.fileName);
		this.os = new FileOutputStream(file); 
		this.writtenPackets = 0;
		this.fileParts = 0;
	}
	
	public void Receive() throws Exception {
		
		this.fileSize = GetFileSize();
		
		this.fileParts = this.fileSize % 100 == 0? (this.fileSize / 100) : (this.fileSize / 100) + 1;
		
		//calcula numero de pacotes
		this.arq = new byte[this.fileSize];
		
		System.out.println("CLIENTE: arquivo tem " + this.fileSize + " bytes");
		
		for(int i = 0; i < windowSize; i++) {
			this.module[i] = new Module(this.percentage, new DatagramSocket(2000 + i), this.fileSize);
			this.module[i].client = this;
		}
		
		for(int i = 0; i < module.length; i++) {
			module[i].start();
		}
		
	}
	
	int soma = 0;
	
	
	public void AddPacket(byte[] packet) {
		try {   
            System.out.println("escrevendo parte " + index + " com " + packet.length + " bytes");
            soma += packet.length; 
            this.os.write(packet);
            this.writtenPackets++;
            if(this.writtenPackets == this.fileParts) {
            	System.out.println("ACABOU O WRITE, SOMA DEU " + soma + " BYTES");
            	os.close();
            	//for(int i = 0; i < this.module.length; i++) {
            	//	this.module[i].interrupt();
            	//}
            }
        }   
        catch (Exception e) { 
            System.out.println("Exception: " + e); 
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
		
    	Client client = new Client("output.txt", Config.WINDOW_SIZE, Config.PERCENTAGE);
    	
    	client.Receive();
		
	}

    
	
}
