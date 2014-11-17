import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;


public class Driver {

	private static final Logger log = LogManager.getLogger();
	public static InvertedIndex index = new InvertedIndex();

	/**
	 * @param args arguments that the user is passing to the program through the command line
	 */
	public static void main(String[] args) {

		try {
			String mapPath = null;
			int PORT = 8080;
			int tFlag = 0;

			// Uses the argument parser class to parse the command line arguments
			ArgumentParser argParser = new ArgumentParser(args);
			if (argParser.hasFlag("-t") && (argParser.getValue("-t") != null)){
				tFlag = Integer.parseInt(argParser.getValue("-t"));
				if (tFlag < 1) {
					tFlag = 5;
				}
			}

			if (argParser.hasFlag("-p") && (argParser.getValue("-p") != null)){
				PORT = Integer.parseInt(argParser.getValue("-p"));
				if (PORT < 1) {
					PORT = 8080;
				}
			}

			if (argParser.hasFlag("-u") && (argParser.getValue("-u") != null)) {
				mapPath = argParser.getValue("-u");
				WebCrawler webCrawler = new WebCrawler(index, tFlag);
				webCrawler.crawlWeb(mapPath);
				webCrawler.shutdown();

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

				server.start();
				server.join();

				log.info("Exiting...");


			} else {
				System.out.println("Please specify the seed url");
			}
		} catch (NumberFormatException ex) {
			System.out.println("Please input a number for the -t flag");
		} catch (Exception ex) {
			log.fatal("Interrupted while running server.", ex);
			System.exit(-1);
		}
	}
}