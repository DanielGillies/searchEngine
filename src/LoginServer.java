import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;


public class LoginServer {
	private static final Logger log = LogManager.getLogger();
	private static final int PORT = 8080;

	public static void main(String[] args) {
		Server server = new Server(PORT);

		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);

		handler.addServletWithMapping(LoginUserServlet.class,     "/login");
		handler.addServletWithMapping(LoginRegisterServlet.class, "/register");
		handler.addServletWithMapping(LoginWelcomeServlet.class,  "/welcome");
		handler.addServletWithMapping(SearchServlet.class, 		  "/");
		handler.addServletWithMapping(SearchResultServlet.class,  "/results");
		handler.addServletWithMapping(UserServlet.class,		  "/user");


		log.info("Starting server on port " + PORT + "...");

		try {
			server.start();
			server.join();

			log.info("Exiting...");
		}
		catch (Exception ex) {
			log.fatal("Interrupted while running server.", ex);
			System.exit(-1);
		}
	}
}