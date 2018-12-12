import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;

public class Module extends Thread {

	public Client client;
	public int percentage;
	public DatagramSocket socket;
	public Status status;

	int lastIndex;
	int lastSize;

	public boolean over;

	public Module(int percentage, DatagramSocket socket, int fileSize) {
		this.percentage = percentage;
		this.socket = socket;

		this.lastIndex = fileSize % 100 == 0 ? (fileSize / 100) - 1 : (fileSize / 100);
		this.lastSize = fileSize % 100 == 0 ? 100 : fileSize % 100;

		this.over = false;
	}

	public void run() {
		while (true) {

			if (this.over)
				break;

			try {
				this.socket.setSoTimeout(5000);

				int index = GetInt();

				Random r = new Random();

				byte[] receiveData = new byte[index == this.lastIndex ? this.lastSize : 100];

				// recebe um pacote do servidor
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				this.socket.receive(receivePacket);

				int random = r.nextInt(101);

				if (random < this.percentage) {

					System.out.println(me() + "em " + index + ", aguardando cliente em " + this.client.index);

					while (this.client.index != index) {
					}

					System.out.println(me() + "em " + index + ", cliente pode receber!");

					this.client.AddPacket(receiveData);

					SendInt(index);

					System.out.println(me() + "enviou " + index + ", cliente em " + (this.client.index + 1));
					this.client.index++;

					// }
				} else {
					SendInt(-1);
					System.err.println(me() + " falhou em enviar " + index + ", cliente em " + (this.client.index));
				}
			} catch (Exception e) {
				System.err.println(me() + e.getLocalizedMessage());
			}
		}
	}

	public String me() {
		return "Modulo[" + this.socket.getLocalPort() % 1000 + "] ";
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
