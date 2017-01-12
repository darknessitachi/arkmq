package com.abigdreamer.arkmq.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.abigdreamer.arkmq.ServerUI;
import com.abigdreamer.arkmq.User;

public class ArkServer {

	public ArrayList<ClientThread> clients;
	
	ServerUI serverUI;
	public boolean isStart = false;
	public ServerSocket serverSocket;
	public Thread serverThread;
	
	private int max;// 人数上限
	
	public ArkServer(ServerUI serverUI) {
		this.serverUI = serverUI;
	}

	public void start(int max, int port) throws BindException {
		try {
			this.max = max;
			
			this.clients = new ArrayList<>();
			this.serverSocket = new ServerSocket(port);
			
			serverThread = new Thread(new Runnable() {
				@Override
				public void run() {
					runServer();
				}
			});
			serverThread.start();
			
			this.isStart = true;
		} catch (BindException e) {
			this.isStart = false;
			throw new BindException("端口号已被占用，请换一个！");
		} catch (Exception e1) {
			e1.printStackTrace();
			this.isStart = false;
			throw new BindException("启动服务器异常！");
		}
	}
	
	public void runServer() {
		while (true) {// 不停的等待客户端的链接
			try {
				Socket socket = serverSocket.accept();
				if (this.clients.size() == max) {// 如果已达人数上限
					BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter w = new PrintWriter(socket.getOutputStream());
					// 接收客户端的基本用户信息
					String inf = r.readLine();
					StringTokenizer st = new StringTokenizer(inf, "@");
					User user = new User(st.nextToken(), st.nextToken());
					// 反馈连接成功信息
					w.println("MAX@服务器：对不起，" + user.getName() + user.getIp() + "，服务器在线人数已达上限，请稍后尝试连接！");
					w.flush();
					// 释放资源
					r.close();
					w.close();
					socket.close();
					continue;
				}
				ClientThread client = new ClientThread(this, this.serverUI, socket);
				client.start();// 开启对此客户端服务的线程
				this.clients.add(client);
				
				this.serverUI.listModel.addElement(client.getUser().getName());// 更新在线列表
				this.serverUI.contentArea.append(client.getUser().getName() + client.getUser().getIp() + "上线!\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 关闭服务器
	public void closeServer() {
		try {
			if (this.serverThread != null)
				this.serverThread.stop();// 停止服务器线程

			for (int i = this.clients.size() - 1; i >= 0; i--) {
				// 给所有在线用户发送关闭命令
				this.clients.get(i).getWriter().println("CLOSE");
				this.clients.get(i).getWriter().flush();
				// 释放资源
				this.clients.get(i).stop();// 停止此条为客户端服务的线程
				this.clients.get(i).reader.close();
				this.clients.get(i).writer.close();
				this.clients.get(i).socket.close();
				this.clients.remove(i);
			}
			if (this.serverSocket != null) {
				this.serverSocket.close();// 关闭服务器端连接
			}
			
			this.isStart = false;
		} catch (IOException e) {
			e.printStackTrace();
			this.isStart = true;
		}
	}
	
	// 群发服务器消息
	public void sendServerMessage(String msg) {
		for (int i = this.clients.size() - 1; i >= 0; i--) {
			this.clients.get(i).getWriter().println(msg);
			this.clients.get(i).getWriter().flush();
		}
	}

}
