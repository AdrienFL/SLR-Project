package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
	public static void main(String args[]) {
		try(ServerSocket serverSocket = new ServerSocket(80);) {
			do {
				try {	
					Socket socket = serverSocket.accept();
					Runnable reqProcessor = new RequestProcessor(socket);					
					Thread reqThread = new Thread(reqProcessor);
					reqThread.start();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			} while (true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
