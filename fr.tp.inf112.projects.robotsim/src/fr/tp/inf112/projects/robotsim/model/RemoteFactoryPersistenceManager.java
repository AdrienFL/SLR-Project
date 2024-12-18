package fr.tp.inf112.projects.robotsim.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;

public class RemoteFactoryPersistenceManager extends AbstractCanvasPersistenceManager {
	
	public RemoteFactoryPersistenceManager(CanvasChooser canvasChooser) {
		super(canvasChooser);
	}

	public RemoteFactoryPersistenceManager() {
		this(null);
	}
	
	@Override
	public Canvas read(final String canvasId)
	throws IOException {
		
		InetAddress netAddr;
		netAddr = InetAddress.getLocalHost();
		InetSocketAddress sockAddr = new InetSocketAddress(netAddr, 80);
		Socket socket = new Socket();
		socket.connect(sockAddr, 1000);
		try {
			OutputStream outStr = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(outStr);
				
			
							
			InputStream inpStr = socket.getInputStream();
			BufferedInputStream bufInputStream = new BufferedInputStream(inpStr);
			ObjectInputStream objectInputStream = new ObjectInputStream(bufInputStream);
			
			Object obj = objectInputStream.readObject();
			
			Canvas canvas = (Canvas) obj;
			return canvas;
		
	}
			
	
		catch (IOException ex) {
			throw new IOException(ex);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void persist(Canvas canvasModel)
	throws IOException {
		InetAddress netAddr;
		netAddr = InetAddress.getLocalHost();
		InetSocketAddress sockAddr = new InetSocketAddress(netAddr, 80);
		Socket socket = new Socket();
		socket.connect(sockAddr, 1000);
		try {
			OutputStream outStr = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(outStr);
				
			oos.writeObject(canvasModel);
						
	}
		catch (IOException ex) {
			throw new IOException(ex);
		} 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete(final Canvas canvasModel)
	throws IOException {
		final File canvasFile = new File(canvasModel.getId());
		
		return canvasFile.delete();
	}


}
