package it.unimib.sd2024;

import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;


/**
 * Classe principale in cui parte il database.
 */
public class Main {
	/**
	 * Porta di ascolto.
	 */
	public static final int PORT = 3030;

	/**
	 * Avvia il database e l'ascolto di nuove connessioni.
	 */
	public static void startServer() throws IOException {
		var server = new ServerSocket(PORT);

		System.out.println("Database listening at localhost:" + PORT);

		try {
			while (true)
				new Handler(server.accept()).start();
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			server.close();
		}
	}

	/**
	 * Handler di una connessione del client.
	 */
	private static class Handler extends Thread {
		private Socket client;

		public Handler(Socket client) {
			this.client = client;
		}

		public void run() {
			try {
				var out = new PrintWriter(client.getOutputStream(), true);
				var in = new BufferedReader(new InputStreamReader(client.getInputStream()));

				String inputLine;
				List<String> queryStrings = new ArrayList<String>();

				System.out.println("New connection from " + client.getInetAddress().getHostAddress());

				while((inputLine = in.readLine()) != null) {
					//System.out.println("Received: " + inputLine);
					if (inputLine.equals("COMMIT")) {
						break;
					}
					queryStrings.add(inputLine);
				}

				System.out.println("Received query: " + String.join("\n", queryStrings));

				try {
					Query query = new Query(queryStrings);
					query.execute(out);
				} catch (Exception e) {
					String errorMsg = "Error query: " + e.getMessage();
					System.err.println("Query execution failed: " + errorMsg); // ← LOG SUL SERVER
					e.printStackTrace(); // ← per vedere stack trace completo
					out.println(errorMsg);
				}
				

				in.close();
				out.close();
				client.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	/**
	 * Metodo principale di avvio del database.
	 *
	 * @param args argomenti passati a riga di comando.
	 *
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		startServer();
	}
}