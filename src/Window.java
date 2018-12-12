import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Window extends Thread {
	
	public int index;
	public volatile Status status;
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
				
				//this.socket.setSoTimeout(5000);			
				int modulePort = Port.MODULE + (this.socket.getLocalPort() % 1000);	
				InetAddress IPAddress = InetAddress.getByName("localhost");	
				
				//mandando index do packet
				SendInt(this.index);
				
				sleep(500);		
				
				//enviando dados para o modulo especial
				//System.out.println("Window[" + this.socket.getLocalPort() + "," + index + "] = " + barr(this.data));
				DatagramPacket sendPacket = new DatagramPacket(this.data, this.data.length, IPAddress, modulePort);
				this.socket.send(sendPacket);
				
				//sleep(1000);
				
				//recebendo ack de entrega
				int ack = GetInt();		
				//System.out.println("Window[" + this.socket.getLocalPort() + "," + index + "] recebeu ack: " + ack);
				this.status = Status.FINISHED;				
				
			}
			catch(Exception e) {
				System.err.println("Window[" + this.socket.getLocalPort() + "] deu " + e.getLocalizedMessage());
			}
		}
		
		
	}
	
	public String barr(byte[] array) {
		return Arrays.toString(array);
	}
	
	public void SendInt(int x) throws Exception {
		int modulePort = Port.MODULE + (this.socket.getLocalPort() % 1000);	
		InetAddress IPAddress = InetAddress.getByName("localhost");	
		byte[] buf = ByteBuffer.allocate(Integer.BYTES).putInt(x).array();
		DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, IPAddress, modulePort);
		//System.out.println("Window[" + this.socket.getLocalPort() + "] enviando para Modulo[" + modulePort + "]");
		this.socket.send(sendPacket);
	}
	
	public int GetInt() throws Exception {
		byte[] ans = new byte[4];
		DatagramPacket receivePacket = new DatagramPacket(ans, ans.length);
		this.socket.receive(receivePacket);
		ByteBuffer wrapped = ByteBuffer.wrap(ans); // big-endian by default
		return wrapped.getInt();
	}
	
	public Window clone() {
		Window w = new Window();
		w.Init(this.data, this.index, this.socket);
		return w;
	}
	
}


