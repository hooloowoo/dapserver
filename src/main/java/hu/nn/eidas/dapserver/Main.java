package hu.nn.eidas.dapserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Main {

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/authorize", new AuthHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
		System.out.println("Server started on port 8080");
	}

// GET /authorize?client_id=x509_san_dns:kliens.pelda.hu&request_uri=https%3A%2F%2Fkliens.pelda.hu%2Frequest%2Fvapof4ql2i7m41m68uep&request_uri_method=post HTTP/1.1

	static class AuthHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			String response = "code=SplxlOBeZQQYbYS6WxSbIA&state=af0ifjsldkj";
			t.getResponseHeaders().add("Location", "https://client.example.org/cb?");
			t.sendResponseHeaders(302, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
			Map<String,String> query = parseQuery(t.getRequestURI().getQuery());
			String uri = query.get("request_uri");
			sendMetadataRequest(uri);
		}

		private void sendMetadataRequest(String httpsURL) {
			try {
				URL url = new URL(httpsURL);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Accept", "application/json");
				int responseCode = connection.getResponseCode();
				System.out.println("Response Code: " + responseCode);
				if (responseCode == HttpURLConnection.HTTP_OK) {
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String inputLine;
					StringBuilder response = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					System.out.println("Response: " + response.toString());
				} else {
					System.out.println("GET request failed");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		private Map<String, String> parseQuery(String query) {
			Map<String, String> queryPairs = new HashMap<>();
			String[] pairs = query.split("&");
			for (String pair : pairs) {
				int idx = pair.indexOf("=");
				queryPairs.put(pair.substring(0, idx), pair.substring(idx + 1));
			}
			return queryPairs;
		}
	}
}
