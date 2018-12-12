import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class Window extends Thread {

	public int index;
	public volatile Status status;
	byte[] data;
	public DatagramSocket socket;

	public boolean isTarget;

	public Window() {
		this.status = Status.WAITING;
		this.isTarget = false;
	}

	public void Init(byte[] data, int index, DatagramSocket socket) {
		this.index = index;
		this.data = data;
		this.socket = socket;
	}

	@Override
	public void run() {
		this.status = Status.RUNNING;

		while (this.status == Status.RUNNING) {
			try {

				this.socket.setSoTimeout(5000);
				int modulePort = Port.MODULE + (this.socket.getLocalPort() % 1000);
				InetAddress IPAddress = InetAddress.getByName("localhost");

				// mandando index do packet
				SendInt(this.index);
				System.out.println(me() + ": enviei index " + this.index);
				sleep(500);

				// enviando dados para o modulo especial
				DatagramPacket sendPacket = new DatagramPacket(this.data, this.data.length, IPAddress, modulePort);
				this.socket.send(sendPacket);
				System.out.println(me() + ": enviei pacote " + this.index);

				// recebendo ack de entrega
				this.socket.setSoTimeout(0);
				int ack = GetInt();
				if (ack == -1) {
					System.err.println(me() + "Receive timed out");
				} else {
					System.out.println(me() + ": recebi ack " + ack);
					this.status = Status.FINISHED;
				}

			} catch (Exception e) {
				System.err.println("Window[" + this.socket.getLocalPort() % 1000 + "] " + e.getLocalizedMessage());
			}
		}

	}

	public String me() {
		return "Window[" + this.socket.getLocalPort() % 1000 + "] ";
	}

	public void SendInt(int x) throws Exception {
		int modulePort = Port.MODULE + (this.socket.getLocalPort() % 1000);
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

	public Window clone() {
		Window w = new Window();
		w.Init(this.data, this.index, this.socket);
		return w;
	}

}
