package fibServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

class ServerTests {
	static final String url= "http://localhost:9000/fibGet";
	static HttpServer server;
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		server = Main.startServer(9000);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		server.stop(0);
	}

	HttpURLConnection createConnection(String query) throws IOException {
		URL u = new URL(url + (query!=null? "?"+ query:""));
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.setRequestMethod("GET");
		return c;
	}
	String readInputStream(InputStream s) {
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(s).useDelimiter("//A");
		String st = sc.next();
		sc.close();
		return st;
	}
	@Test
	void testCorrectResponse() {
		String response;
		try {
			HttpURLConnection c = createConnection("num=5&full=true");
			c.connect();
			response = readInputStream(c.getInputStream());
		} catch (IOException e) {
			fail("Error when connection to server", e);
			return;
		}
		long[] values = Calculations.getFibNums(5);
		try {
			JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
			for(int i = 0; i< values.length; i++) {
				int j = i;
				assertEquals(values[i], jsonObject.get(""+i).getAsLong(),()->{
					return j + " index of the sequence sent to the client mismatches server side!";
				});
			}
		}
		catch(Exception e) {
			fail("Error parsing server's response", e);
		}
	}
	@Test
	void testSingleResponse() {
		String response;
		try {
			HttpURLConnection c = createConnection("num=6");
			c.connect();
			response = readInputStream(c.getInputStream());
		} catch (IOException e) {
			fail("Error when connection to server", e);
			return;
		}
		try {
			JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
			assertEquals(jsonObject.keySet().size(), 1, 
					"Single value response returned more or less than one value!");
			assertTrue(jsonObject.keySet().contains("6"), 
					"Single value respone returned wrong index!");
			assertEquals(jsonObject.get("6").getAsLong(), Calculations.getFibNums(6)[6],
					"Single value response returned incorrect value!");
		}
		catch(Exception e) {
			fail("Error parsing server's response", e);
		}
	}
	@ParameterizedTest
	@ValueSource(strings = {"" , "num=1.4","num=abh"})
	void testMalformat(String value) {
		String response;
		try {
			HttpURLConnection c = createConnection(value);
			c.connect();
			assertEquals(c.getResponseCode(), 400);
			response = readInputStream(c.getErrorStream());
			assertEquals(response, "missing/malformatted number");
		} catch (IOException e) {
			fail("Error when connection to server", e);
			return;
		}
	}
	@ParameterizedTest
	@ValueSource(strings = {"num=-100" , "num=1000"})
	void testInvalid(String value) {
		String response;
		try {
			HttpURLConnection c = createConnection(value);
			c.connect();
			assertEquals(c.getResponseCode(), 400);
			response = readInputStream(c.getErrorStream());
			assertEquals(response, "invalid number");
		} catch (IOException e) {
			fail("Error when connection to server", e);
			return;
		}
	}
}
