import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws Exception {
		
		Scanner in = new Scanner(System.in);
		
		System.out.println("Digite o tamanho da janela.\nCada janela e uma thread, entao nao eh recomendavel colocar muitas janelas, ate 10 e um bom valor");
		System.out.print("Numero de janelas: ");
		
		Config.WINDOW_SIZE = in.nextInt();
		
		System.out.println("Digite a porcentagem de negacao do pacote pelo modulo especial.\nEssa porcentagem deve ser um int entre 0 e 100, onde 100 aceita tudo e 0 reprova tudo");
		System.out.print("Valor da porcentagem: ");
		
		Config.PERCENTAGE = in.nextInt();
		
		Thread clientThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Client client = new Client("output.zip", Config.WINDOW_SIZE, Config.PERCENTAGE);
					client.Receive();
				} catch (Exception e) {
					e.printStackTrace();
				}
								
			}
		});
				
		clientThread.start();
		
		Thread.sleep(1000);
		
		Thread serverThread = new Thread(new Runnable() {
			@Override
			public void run() {		
				try {
					Server server = new Server("input.zip", Config.WINDOW_SIZE);
					server.Process();
					server.Send();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		serverThread.start();
		
		in.close();
		
	}
}
