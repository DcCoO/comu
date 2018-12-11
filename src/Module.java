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
	public Status status;
	
	int lastIndex;
	int lastSize;
	
	public Module(int percentage, DatagramSocket socket, int fileSize) {
		this.percentage = percentage;
		this.socket = socket;
		
		this.lastIndex = fileSize % 100 == 0? (fileSize / 100) - 1 : (fileSize / 100);
		this.lastSize = fileSize % 100 == 0? 100 : fileSize % 100;
	}
	
	public void run() {
		while(true) {
			
			
			try {
				
				System.out.println("Modulo[" + this.socket.getLocalPort() + "] aguardando index");	
				
				//le index do packet da Window
				int index = GetInt();
				System.out.println("Modulo[" + this.socket.getLocalPort() + "] recebendo index " + index);
				
				Random r = new Random();		
		
				//TODO decidir tamanho do ultimo packet pelo index e client.fileSize
				byte[] receiveData = new byte[index == this.lastIndex? this.lastSize : 100];		
					
				//recebe um packet do servidor
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				this.socket.receive(receivePacket);
				
				System.out.println("Modulo[" + this.socket.getLocalPort() + "] recebeu pacote " + index);
				
				int random = r.nextInt(101);
				
				if(random < this.percentage) {
					
					//TODO receivePacket no cliente
				
					//enviando ack para janela
					SendInt(index);
					
				}	
				sleep(1000);
					
			}
			catch(Exception e) {
				System.err.println("Module[" + this.socket.getLocalPort() + "] deu " + e.getLocalizedMessage());
			}
		}
	}
	
	public void SendInt(int x) throws Exception {
		int modulePort = Port.WINDOW + (this.socket.getLocalPort() % 1000);	
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
