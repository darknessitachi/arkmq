package com.abigdreamer.arkmq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.abigdreamer.arkmq.bio.ArkServer;

public class ServerUI {

	private JFrame frame;
	public JTextArea contentArea;
	private JTextField txtMessage;
	private JTextField txtMax;
	private JTextField txtPort;
	private JButton btnStart;
	private JButton btnStop;
	private JButton btnSend;
	private JPanel northPanel;
	private JPanel southPanel;
	private JScrollPane rightPanel;
	private JScrollPane leftPanel;
	private JSplitPane centerSplit;
	private JList userList;
	public DefaultListModel listModel;

	// 执行消息发送
	public void send() {
		if (!this.arkServer.isStart) {
			JOptionPane.showMessageDialog(frame, "服务器还未启动,不能发送消息！", "错误",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (this.arkServer.clients.size() == 0) {
			JOptionPane.showMessageDialog(frame, "没有用户在线,不能发送消息！", "错误",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		String message = txtMessage.getText().trim();
		if (message == null || message.equals("")) {
			JOptionPane.showMessageDialog(frame, "消息不能为空！", "错误",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		sendServerMessage(message);// 群发服务器消息
		contentArea.append("服务器说：" + txtMessage.getText() + "\r\n");
		txtMessage.setText(null);
	}

	// 构造放法
	public ServerUI() {
		frame = new JFrame("服务器");
		// 更改JFrame的图标：
		//frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Client.class.getResource("qq.png")));
//		frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Server.class.getResource("qq.png")));
		contentArea = new JTextArea();
		contentArea.setEditable(false);
		contentArea.setForeground(Color.blue);
		txtMessage = new JTextField();
		txtMax = new JTextField("30");
		txtPort = new JTextField("6666");
		btnStart = new JButton("启动");
		btnStop = new JButton("停止");
		btnSend = new JButton("发送");
		btnStop.setEnabled(false);
		listModel = new DefaultListModel();
		userList = new JList(listModel);

		southPanel = new JPanel(new BorderLayout());
		southPanel.setBorder(new TitledBorder("写消息"));
		southPanel.add(txtMessage, "Center");
		southPanel.add(btnSend, "East");
		leftPanel = new JScrollPane(userList);
		leftPanel.setBorder(new TitledBorder("在线用户"));

		rightPanel = new JScrollPane(contentArea);
		rightPanel.setBorder(new TitledBorder("消息显示区"));

		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		centerSplit.setDividerLocation(100);
		northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(1, 6));
		northPanel.add(new JLabel("人数上限"));
		northPanel.add(txtMax);
		northPanel.add(new JLabel("端口"));
		northPanel.add(txtPort);
		northPanel.add(btnStart);
		northPanel.add(btnStop);
		northPanel.setBorder(new TitledBorder("配置信息"));

		frame.setLayout(new BorderLayout());
		frame.add(northPanel, "North");
		frame.add(centerSplit, "Center");
		frame.add(southPanel, "South");
		frame.setSize(600, 400);
		//frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());//设置全屏
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2, (screen_height - frame.getHeight()) / 2);
		frame.setVisible(true);

		// 关闭窗口时事件
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (arkServer.isStart) {
					closeServer();// 关闭服务器
				}
				System.exit(0);// 退出程序
			}
		});

		// 文本框按回车键时事件
		txtMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});

		// 单击发送按钮时事件
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				send();
			}
		});

		// 单击启动服务器按钮时事件
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (arkServer.isStart) {
					JOptionPane.showMessageDialog(frame, "服务器已处于启动状态，不要重复启动！", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int max;
				int port;
				try {
					try {
						max = Integer.parseInt(txtMax.getText());
					} catch (Exception e1) {
						throw new Exception("人数上限为正整数！");
					}
					if (max <= 0) {
						throw new Exception("人数上限为正整数！");
					}
					try {
						port = Integer.parseInt(txtPort.getText());
					} catch (Exception e1) {
						throw new Exception("端口号为正整数！");
					}
					if (port <= 0) {
						throw new Exception("端口号 为正整数！");
					}
					serverStart(max, port);
					contentArea.append("服务器已成功启动!人数上限：" + max + ",端口：" + port + "\r\n");
					JOptionPane.showMessageDialog(frame, "服务器成功启动!");
					btnStart.setEnabled(false);
					txtMax.setEnabled(false);
					txtPort.setEnabled(false);
					btnStop.setEnabled(true);
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, exc.getMessage(),
							"错误", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// 单击停止服务器按钮时事件
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!arkServer.isStart) {
					JOptionPane.showMessageDialog(frame, "服务器还未启动，无需停止！", "错误",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					closeServer();
					btnStart.setEnabled(true);
					txtMax.setEnabled(true);
					txtPort.setEnabled(true);
					btnStop.setEnabled(false);
					contentArea.append("服务器成功停止!\r\n");
					JOptionPane.showMessageDialog(frame, "服务器成功停止！");
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, "停止服务器发生异常！", "错误",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	ArkServer arkServer;
	
	public void init() {
		arkServer = new ArkServer(this);
	}
	
	// 启动服务器
	public void serverStart(int max, int port) throws java.net.BindException {
		
		arkServer.start(max, port);
	}

	// 关闭服务器
	public void closeServer() {
		this.arkServer.closeServer();
		listModel.removeAllElements();// 清空用户列表
	}

	// 群发服务器消息
	public void sendServerMessage(String message) {
		String msg = "服务器：" + message + "(多人发送)";
		this.arkServer.sendServerMessage(msg);
	}
	
}

