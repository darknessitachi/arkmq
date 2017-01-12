package com.abigdreamer.arkmq.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import com.abigdreamer.arkmq.ServerUI;
import com.abigdreamer.arkmq.User;

//为一个客户端服务的线程
public class ClientThread extends Thread {
	public Socket socket;
	public BufferedReader reader;
	public PrintWriter writer;
	public User user;
	
	ServerUI serverUI;
	ArkServer arkServer;
	
	// 客户端线程的构造方法
	public ClientThread(ArkServer arkServer, ServerUI serverUI, Socket socket) {
		this.arkServer = arkServer;
		this.serverUI = serverUI;
		
		try {
			this.socket = socket;
			reader = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream());
			// 接收客户端的基本用户信息
			String inf = reader.readLine();
			StringTokenizer st = new StringTokenizer(inf, "@");
			user = new User(st.nextToken(), st.nextToken());
			// 反馈连接成功信息
			writer.println(user.getName() + user.getIp() + "与服务器连接成功!");
			writer.flush();
			// 反馈当前在线用户信息
			if (this.arkServer.clients.size() > 0) {
				String temp = "";
				for (int i = this.arkServer.clients.size() - 1; i >= 0; i--) {
					temp += (this.arkServer.clients.get(i).getUser().getName() + "/" + this.arkServer.clients
							.get(i).getUser().getIp())
							+ "@";
				}
				writer.println("USERLIST@" + this.arkServer.clients.size() + "@" + temp);
				writer.flush();
			}
			// 向所有在线用户发送该用户上线命令
			for (int i = this.arkServer.clients.size() - 1; i >= 0; i--) {
				this.arkServer.clients.get(i).getWriter().println(
						"ADD@" + user.getName() + user.getIp());
				this.arkServer.clients.get(i).getWriter().flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BufferedReader getReader() {
		return reader;
	}

	public PrintWriter getWriter() {
		return writer;
	}

	public User getUser() {
		return user;
	}

	@SuppressWarnings("deprecation")
	public void run() {// 不断接收客户端的消息，进行处理。
		String message = null;
		while (true) {
			try {
				message = reader.readLine();// 接收客户端消息
				if (message.equals("CLOSE"))// 下线命令
				{
					this.serverUI.contentArea.append(this.getUser().getName()
							+ this.getUser().getIp() + "下线!\r\n");
					// 断开连接释放资源
					reader.close();
					writer.close();
					socket.close();

					// 向所有在线用户发送该用户的下线命令
					for (int i = this.arkServer.clients.size() - 1; i >= 0; i--) {
						this.arkServer.clients.get(i).getWriter().println(
								"DELETE@" + user.getName());
						this.arkServer.clients.get(i).getWriter().flush();
					}

					this.serverUI.listModel.removeElement(user.getName());// 更新在线列表

					// 删除此条客户端服务线程
					for (int i = this.arkServer.clients.size() - 1; i >= 0; i--) {
						if (this.arkServer.clients.get(i).getUser() == user) {
							ClientThread temp = this.arkServer.clients.get(i);
							this.arkServer.clients.remove(i);// 删除此用户的服务线程
							temp.stop();// 停止这条服务线程
							return;
						}
					}
				} else {
					dispatcherMessage(message);// 转发消息
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 转发消息
	public void dispatcherMessage(String message) {
		StringTokenizer stringTokenizer = new StringTokenizer(message, "@");
		String source = stringTokenizer.nextToken();
		String owner = stringTokenizer.nextToken();
		String content = stringTokenizer.nextToken();
		message = source + "说：" + content;
		this.serverUI.contentArea.append(message + "\r\n");
		if (owner.equals("ALL")) {// 群发
			for (int i = this.arkServer.clients.size() - 1; i >= 0; i--) {
				this.arkServer.clients.get(i).getWriter().println(message + "(多人发送)");
				this.arkServer.clients.get(i).getWriter().flush();
			}
		}
	}
}
