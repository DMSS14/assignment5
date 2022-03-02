package fibServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.net.httpserver.HttpServer;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class Main {
	public static void main(String[] args) {
		int port = 8081;
		if(args.length>0) {
			port = Integer.parseInt(args[0]);
		}
		try {
			startServer(port);
		} catch (IOException e) {
			System.err.println("Error starting server.");
			e.printStackTrace();
		}
	}
	public static HttpServer startServer(int port) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		server.setExecutor((c)->new Thread(c).start()); //TODO better multi-thread management
		
		server.createContext("/fibGet", (httpEx)->{
			Map<String, String> query = decodeQuery(httpEx.getRequestURI().getQuery());
			int number;
			try {
				number = Integer.parseInt(query.get("num"));
			}
			catch(NumberFormatException e) {
				sendRepsonse(httpEx, 400, "missing/malformatted number", "text/plain");
				return;
			}
			if(number > Calculations.MAX_NUMBER || number < 0) {
				sendRepsonse(httpEx, 400, "invalid number", "text/plain");
				return;
			}
			boolean full = false;
			try {
				full = !query.get("full").isEmpty();
			}
			catch(Exception e) {
			}
			long[] fibs = Calculations.getFibNums(number);
			JsonObject response = new JsonObject();
			for(int i = full?0:number; i<=number; i++) {
				response.addProperty(""+i, fibs[i]);
			}
			sendRepsonse(httpEx, 200, response.toString(), "application/json");
		});
		
		server.start();
		return server;
	}
	private static void sendRepsonse(HttpExchange e, int code, String value, String contentType) throws IOException {
		Headers rh = e.getResponseHeaders();
		rh.set("Content-Type", contentType);
		byte[] stringInfo = value.getBytes();
		e.sendResponseHeaders(code, stringInfo.length);
		OutputStream rb = e.getResponseBody();
		rb.write(stringInfo);
		rb.close();
	}
	public static Map<String, String> decodeQuery(String query){
		if(query == null || query.isEmpty()) {
			return Collections.emptyMap();
		}
		return Stream.of(query.split("&"))
				.filter(a -> !a.isEmpty() && a.contains("="))
				.map(a -> a.split("=",2))
				.collect(Collectors.toMap(as -> as[0], as-> as[1]));
	}

}
