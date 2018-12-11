import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Window extends Thread {
	
	public int index;
	public Status status;
	byte[] data;
	public DatagramSocket socket;
	
	public Window() {
		status = Status.WAITING;
	}
	
	public void Init(byte[] data, int index, DatagramSocket socket) {
		this.index = index;
		this.data = data;
		this.socket = socket;
	}
	
	@Override
	public void run() {
		this.status = Status.RUNNING;
		
		while(this.status == Status.RUNNING) {
			try {
				
				this.socket.setSoTimeout(1000000);			
				int modulePort = 1000 + this.socket.getLocalPort();	
				InetAddress IPAddress = InetAddress.getByName("localhost");	
				
				//mandando index do packet
				SendInt(this.index);
				
				byte[] receiveData = new byte[1024];
		
				//enviando dados para o modulo especial
				DatagramPacket sendPacket = new DatagramPacket(this.data, this.data.length, IPAddress, modulePort);
				this.socket.send(sendPacket);
		
				//recebendo confirmacao de entrega
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				this.socket.receive(receivePacket);				
		
				String ack = new String(receivePacket.getData());
		
				System.out.println("Ack: " + ack);
				
				this.status = Status.FINISHED;				
				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void SendInt(int x) throws Exception {
		int modulePort = 1000 + this.socket.getLocalPort();	
		InetAddress IPAddress = InetAddress.getByName("localhost");	
		byte[] buf = ByteBuffer.allocate(Integer.BYTES).putInt(x).array();
		DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, IPAddress, modulePort);
		this.socket.send(sendPacket);
	}
	
	public int GetInt() throws Exception {
		byte[] ans = new byte[4];
		DatagramPacket receivePacket = new DatagramPacket(ans, ans.length);
		this.socket.receive(receivePacket);
		ByteBuffer wrapped = ByteBuffer.wrap(ans); // big-endian by default
		return wrapped.getInt();
	}
	
}


