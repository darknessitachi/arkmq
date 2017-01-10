package com.abigdreamer.arkmq;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author lilinfeng
 * @date 2014年2月14日
 * @version 1.0
 */
public class TimeServer {
	
	private int port;
	
	public TimeServer(int port) {
		this.port = port;
	}
	
	public void start() throws IOException {
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("The time server is start in port : " + port);
			Socket socket = null;
			while (true) {
				socket = server.accept();
				new Thread(new TimeServerHandler(socket)).start();
			}
		} finally {
			if (server != null) {
				System.out.println("The time server close");
				server.close();
				server = null;
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		int port = 8080;

		TimeServer server = new TimeServer(port);
		
		server.start();
		
		
	}
}
