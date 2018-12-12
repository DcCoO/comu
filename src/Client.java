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
	
	public Client(String fileName, int windowSize, int percentage) throws Exception {
		this.fileName = fileName;
		this.windowSize = windowSize;
		this.module = new Module[windowSize];
		this.percentage = percentage;
		this.index = 0;
		this.file = new File(this.fileName);
		this.os = new FileOutputStream(file); 
	}
	
	public void Receive() throws Exception {
		
		this.fileSize = GetFileSize();
		
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
		
		//while(!TransferOver()) {}
		
		//WriteFile(this.arq);
		
	}
	
	
	public void AddPacket(byte[] packet) {
		try {   
            //os.write(packet, 100 * this.index - 1, packet.length);
            os.write(packet);
            this.index++;
            if(this.index == 33) {
            	System.out.println("ACABOU O WRITE");
            	os.close();
            }
        }   
        catch (Exception e) { 
            System.out.println("Exception: " + e); 
        } 
	}
	
	/*
    public void WriteFile(byte[] bytes) { 
        try { 
        	File file = new File(this.fileName);
            OutputStream os = new FileOutputStream(file); 
  
            os.write(bytes); 
            System.out.println("Successfully" + " byte inserted"); 
  
            os.close(); 
        } 
  
        catch (Exception e) { 
            System.out.println("Exception: " + e); 
        } 
    }*/
	
	public int GetFileSize() throws Exception {
		DatagramSocket socket = new DatagramSocket(Port.CLIENT);
		byte[] ans = new byte[4];
		DatagramPacket receivePacket = new DatagramPacket(ans, ans.length);
		socket.receive(receivePacket);
		ByteBuffer wrapped = ByteBuffer.wrap(ans); 
		return wrapped.getInt();
	}
	
	//public boolean TransferOver() {
	//	for(int i = 0; i < this.packets.length; i++) if(!this.packets[i]) return false;
	//	return true;
	//}
	
    public static void main(String[] args) throws Exception {
		
    	int windowSize = 8;
    	
    	Client client = new Client("output.txt", windowSize, 100);
    	
    	client.Receive();
		
	}

    
	
}
