package it.unimib.sd2024.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DatabaseConnector {
	private static final int MAX_POOL_SIZE = 15;
	private static final BlockingQueue<Socket> connectionPool = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
	
	static {
		// Initialize the connection pool with socket instances
		for (int i = 0; i < MAX_POOL_SIZE; i++) {
			try {
				Socket socket = new Socket("localhost", 3030);
				connectionPool.add(socket);
			} catch (IOException e) {
				System.err.println("[ERROR] Error while creating the connection pool: " + e.getMessage());
			}
		}
	}
	
	private static Socket getConnection() throws InterruptedException {
		return connectionPool.take();
	}
	
	private static void releaseConnection(Socket socket) {
		connectionPool.offer(socket);
	}

	private static String readFromServer(BufferedReader reader) throws IOException {
		StringBuilder responseBuilder = new StringBuilder();
		String outputLine;
		while ((outputLine = reader.readLine()) != null) {
			responseBuilder.append(outputLine);
		}
		return responseBuilder.toString();
	}
	
	public static String Communicate(String message) throws IOException {
		System.out.println("start communicating with query: \"" + message + "\"");
		
		// Get a connection from the database conections pool
		Socket socket;
		try {
			socket = getConnection();
		} catch (InterruptedException e) {
			System.err.println("[ERROR] Error while getting a connection from the pool: " + e.getMessage());
			return "";
		}

		System.out.println("Socket obtained: " + socket.toString());
		
		// Prepare the input and output streams
		DataOutputStream toServer = new DataOutputStream(socket.getOutputStream());
		BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		// Send the query to the database and read the response
		String response = "";
		try {
			toServer.writeBytes(message+ "\n");
			response = readFromServer(fromServer);
		} catch (Exception e) {
			System.err.println("[ERROR] Error while communicating with the database: " + e.getMessage());
		} finally {
			releaseConnection(socket);
		}
		return response; 
	}
}