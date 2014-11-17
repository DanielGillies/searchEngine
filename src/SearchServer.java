import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * Demonstrates how to create, use, and clear cookies. Vulnerable to attack
 * since cookie values are not sanitized prior to use!
 *
 * @author Sophie Engle
 * @author CS 212 Software Development
 * @author University of San Francisco
 *
 * @see BaseServlet
 * @see CookieIndexServlet
 * @see CookieConfigServlet
 */
public class SearchServer {

	public static void main(String[] args) throws Exception {
		Server server = new Server(BaseServlet.PORT);

		ServletHandler handler = new ServletHandler();
		handler.addServletWithMapping(SearchServlet.class, "/");

		server.setHandler(handler);
		server.start();
		server.join();
	}
}