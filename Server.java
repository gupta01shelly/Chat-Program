package com.chatapplication;

import java.io.*;
import java.util.*;
import java.net.*;

public class Server {

	// Vector to store active clients
	static Vector<ClientHandler> activeClients = new Vector<>();

	// counter for clients
	static int index = 0;

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		// InetAddress address = InetAddress.getByName(ipAddress);

		// server is listening on port 3300
		ServerSocket serverSocket = new ServerSocket(3300);

		Socket clientSocket;

		// running infinite loop for getting client request
		while (true) {
			// Accept the incoming request
			clientSocket = serverSocket.accept();

			System.out.println("New client request received : " + clientSocket);

			// obtain input and output streams
			DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

			System.out.println("Creating a new handler for this client...");

			// Create a new handler object for handling this request.
			ClientHandler clientHandler = new ClientHandler(clientSocket, "chat " + index, dis, dos);

			// Create a new Thread with this object.
			Thread thread = new Thread(clientHandler);

			System.out.println("Adding this client to active client list.");

			// add this client to active clients list
			activeClients.add(clientHandler);
			printActiveClients();
			// start the thread.
			thread.start();

			// increment index for new client.
			// index is used for naming only, and can be replaced by any naming scheme
			index++;

		}
	}

	public static void printActiveClients() {
		System.out.println("\n------------------");
		System.out.println("Active clients :");
		for (ClientHandler clientHandler : activeClients) {
			System.out.println(clientHandler.getName());
		}
		System.out.println("------------------\n");
	}
}

class ClientHandler implements Runnable {
	Scanner scan = new Scanner(System.in);
	private String name;
	final DataInputStream dataInputStream;
	final DataOutputStream dataOutputStream;
	Socket socket;
	boolean isLoggedIn;

	// constructor
	public ClientHandler(Socket socket, String name, DataInputStream dis, DataOutputStream dos) {
		this.dataInputStream = dis;
		this.dataOutputStream = dos;
		this.name = name;
		this.socket = socket;
		this.isLoggedIn = true;
	}

	public String getName() {
		return name;
	}

	@Override
	public void run() {

		String received;
		while (true) {
			try {
				// receive the string
				received = dataInputStream.readUTF();

				System.out.println(received);

				if (received.equals("quit")) {
					this.isLoggedIn = false;
					Server.activeClients.remove(this);
					break;
				}

				String MsgToSend = received;
				String recipient = getOtherClientName(this.name);

				// search for the recipient in the active clients list.
				for (ClientHandler client : Server.activeClients) {
					// if the recipient is found, write on its output stream
					if (client.name.equals(recipient) && client.isLoggedIn == true) {
						client.dataOutputStream.writeUTF(this.name + " : " + MsgToSend);
						break;
					}
				}
			} catch (EOFException eof) {
				System.out.println(name + " closed the connection.");
				Server.printActiveClients();
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		try {
			// closing resources
			this.dataInputStream.close();
			this.dataOutputStream.close();

		} catch (EOFException eof) {
			System.out.println(name + " closed the connection.");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getOtherClientName(String myName) {
		String otherClientName = "";
		
		for (ClientHandler client : Server.activeClients) {
			if(!client.name.equals(myName)) {
				otherClientName = client.name;
			}
		}
		
		return otherClientName;
	}
}