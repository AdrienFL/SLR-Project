package server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;

public class RequestProcessor implements Runnable {

	private Socket socket;
	private FactoryPersistenceManager manager;
	public RequestProcessor(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			InputStream inpStr = socket.getInputStream();

			BufferedInputStream bufInputStream = new BufferedInputStream(inpStr);
			ObjectInputStream objectInputStream = new ObjectInputStream(bufInputStream);
			
			Object obj = objectInputStream.readObject();
				
			OutputStream outStr = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(outStr);
			
			if (obj instanceof String) {
				String filename = (String) obj;
				Canvas canvas = manager.read(filename);
				oos.writeObject(canvas);
			}
			
			if(obj instanceof Canvas) {
				Canvas factoryCanvas = (Canvas) obj;
				manager.persist(factoryCanvas);
			}
			
			
			
			outStr.close();
			inpStr.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
