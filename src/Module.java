import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;

public class Module extends Thread {
	
	public Client client;
	public int percentage;
	public DatagramSocket socket;
	
	public Module(int percentage, DatagramSocket socket) {
		this.percentage = percentage;
		this.socket = socket;
	}
	
	public void run() {
		while(true) {
			System.out.println("Modulo[" + this.socket.getLocalPort() + "] recebendo");
			
			try {
				//le index do packet da Window
				int index = GetInt();
				
				Random r = new Random();		
		
				//TODO decidir tamanho do ultimo packet pelo index e client.fileSize
				byte[] receiveData = new byte[100];
		
				while (true) {			
					
					//recebe um packet do servidor
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					this.socket.receive(receivePacket);
					
					int random = r.nextInt(101);
					
					if(random < this.percentage) {
						
						//TODO receivePacket no cliente
					
						//enviando ack para janela
						SendInt(1);
						break;
					}			
				}	
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void SendInt(int x) throws Exception {
		int modulePort = 2000 + this.socket.getLocalPort();	
		InetAddress IPAddress = InetAddress.getByName("localhost");	
		byte[] buf = ByteBuffer.allocate(Integer.BYTES).putInt(x).array();
		DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, IPAddress, modulePort);
		this.socket.send(sendPacket);
	}
	
	public int GetInt() throws Exception {
		byte[] ans = new byte[4];
		DatagramPacket receivePacket = new DatagramPacket(ans, ans.length);
		this.socket.receive(receivePacket);
		ByteBuffer wrapped = ByteBuffer.wrap(ans); 
		return wrapped.getInt();
	}
}
