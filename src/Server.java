import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Deque;

public class Server {
	public String fileName;
	public int fileSize;
	public volatile Deque<Window> windows;
	public int windowSize;
	public AtomicInteger index;
	public Packet[] packets;

	public Server(String fileName, int windowSize) {
		this.fileName = fileName;
		File f = new File(fileName);
		this.fileSize = (int) f.length();
		this.windows = new LinkedList<Window>();
		this.windowSize = windowSize;
		this.index = new AtomicInteger(0);
		this.packets = new Packet[this.fileSize % 100 == 0 ? (this.fileSize / 100) : (this.fileSize / 100) + 1];
	}

	public void Process() throws Exception {
		byte[] buffer = new byte[100];
		byte[] lastBuffer = new byte[this.fileSize % 100 == 0 ? 100 : this.fileSize % 100];

		FileInputStream fis = new FileInputStream(this.fileName);
		int p = 0;
		while (true) {
			int len = fis.read(buffer);
			if (len != 100) {
				for (int i = 0; i < len; i++)
					lastBuffer[i] = buffer[i];
				this.packets[p] = new Packet(p, lastBuffer.clone());
				fis.close();
				break;
			} else
				this.packets[p] = new Packet(p, buffer.clone());
			p++;
		}
	}

	public void Send() throws Exception {

		SendFileSize(this.fileSize);
		System.out.println(
				"SERVIDOR: arquivo tem " + this.fileSize + " bytes divididos em " + this.packets.length + " partes");

		// criando sockets para cada janela
		DatagramSocket[] sockets = new DatagramSocket[this.windowSize];
		for (int i = 0; i < this.windowSize; i++) {
			sockets[i] = new DatagramSocket(Port.WINDOW + i);
		}

		for (int i = 0; i < Math.min(this.windowSize, this.packets.length); i++) {

			Window w = new Window();
			w.Init(this.packets[i].data, i, sockets[i]);
			w.start();
			this.windows.addLast(w);
			Thread.sleep(1000);
		}

		index.set(Math.min(this.windowSize, this.packets.length));

		while (this.index.get() < this.packets.length) {

			try {
				System.out.println("SERVER: " + this.windows.getFirst().me() + "esperando inserir "
						+ this.windows.getFirst().index);

				while (this.windows.getFirst().status == Status.RUNNING) {
				}

				while (this.windows.getFirst().status == Status.FINISHED) {
					Window aux = this.windows.poll();

					Window w = aux.clone();

					if (index.get() >= this.packets.length)
						continue;

					synchronized (index) {
						if (this.windows.size() == 0)
							index.set(aux.index + 1);
						else
							index.set(this.windows.getLast().index + 1);

						if (index.get() >= this.packets.length)
							continue;

						w.Init(this.packets[index.get()].data, index.get(), w.socket);
						w.start();

						this.windows.addLast(w);

					}
				}
				if (this.windows.size() > 0) {
					this.windows.getFirst().isTarget = true;
				}

			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

		System.out.println("SERVER: acabou!");

	}

	public void SendFileSize(int size) throws Exception {
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] buf = ByteBuffer.allocate(Integer.BYTES).putInt(size).array();
		DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, IPAddress, Port.CLIENT);
		DatagramSocket serverSocket = new DatagramSocket();
		serverSocket.send(sendPacket);
	}

	public static void main(String[] args) throws Exception {

		Server server = new Server("input.zip", Config.WINDOW_SIZE);
		server.Process();
		server.Send();
	}
}
