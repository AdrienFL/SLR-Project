package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientProcessor implements Runnable {

	@Override
	public void run() {
		InetAddress netAddr;

		try {
			netAddr = InetAddress.getLocalHost();
			InetSocketAddress sockAddr = new InetSocketAddress(netAddr, 80);
			Socket socket = new Socket();
			
			

			try {
				socket.connect(sockAddr, 1000);
				
				OutputStream outStr = socket.getOutputStream();
				PrintWriter writer = new PrintWriter(outStr, false);
				String message = "coucou";
				
				
				
				writer.write("a\n");
				writer.flush();
				
				
				
				InputStream inpStr = socket.getInputStream();
				InputStreamReader strReader = new InputStreamReader(inpStr);
				BufferedReader buffReader = new BufferedReader(strReader);
				

				message = buffReader.readLine();
				System.out.println(message + " : client");
				inpStr.close();
				outStr.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
