package com.chatapplication;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
	private static int port = 3300;
	//Connects to a server on localhost by default with port number 3300
	private static String ipAddress = "127.0.0.1";
	
	static boolean connectionAlive = false;

	public Client(String serverIp, int port) {
		Client.ipAddress = serverIp;
		Client.port = port;
	}

	@SuppressWarnings("resource")
	public static void main(String args[]) throws UnknownHostException, IOException {
		Scanner scan = new Scanner(System.in);
		
		// getting localhost ip
		InetAddress ip = InetAddress.getByName(ipAddress);

		// establish the connection
		Socket socket = new Socket(ip, port);
		Client.connectionAlive = true;
		
		// obtaining input and out streams
		DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
		DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

		// sendMessage thread
		Thread sendMessage = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {

					// read the message to deliver.
					String msg = scan.nextLine();

					try {
						// write on the output stream
						dataOutputStream.writeUTF(msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		// readMessage thread
		Thread readMessage = new Thread(new Runnable() {
			@Override
			public void run() {

				while (true) {
					try {
						// read the message sent to this client
						String msg = dataInputStream.readUTF();
						System.out.println(msg);
					} catch (EOFException eof) {
						System.out.println("Client closed the connection.");
						closeSocket(socket);
						Client.connectionAlive = false;
						break;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.exit(0);
			}
		});

		if (connectionAlive) {
			sendMessage.start();
			readMessage.start();
		}
		else {
			System.exit(0);
		}

	}

	private static void closeSocket(Socket socket) {
		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("Error occurred while closing socket. " + e.getMessage());
		}
	}
}
