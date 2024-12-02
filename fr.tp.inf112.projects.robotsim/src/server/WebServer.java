package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import fr.tp.inf112.projects.robotsim.app.RemoteSimulatorController;

public class WebServer {
	public static void main(String args[]) {
		
		final Logger LOGGER = Logger.getLogger(WebServer.class.getName());

		
		try(ServerSocket serverSocket = new ServerSocket(80);) {
			do {
				try {	
					Socket socket = serverSocket.accept();
					Runnable reqProcessor = new RequestProcessor(socket);					
					Thread reqThread = new Thread(reqProcessor);
					reqThread.start();
				}
				catch (IOException ex) {
					LOGGER.severe(ex.getMessage());
				}
			} while (true);
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
}
