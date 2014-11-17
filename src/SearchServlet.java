import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
@SuppressWarnings("serial")
public class SearchServlet extends LoginBaseServlet {

	@Override
	protected void doGet(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		log.info("GET " + request.getRequestURL().toString());

		if (request.getRequestURI().endsWith("favicon.ico")) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		prepareResponse("Danny Boy Search", response);
		Map<String, String> cookies = getCookieMap(request);

		PrintWriter out = response.getWriter();
		// Printing out css
		printCSS(out);

		printHeader(out, cookies, request);

		String error = request.getParameter("error");
		if(error != null) {
			String errorMessage = StringUtilities.getStatus(error).message();
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

			out.printf("<form method=\"post\" action=\"%s\">%n", request.getRequestURI());
			out.printf("<table border=\"0\"%n");
			out.printf("\t<tr>%n");
			out.printf("\t\t<td><input type=\"text\" name=\"query\"></td>%n");
			out.printf("\t<tr>%n");
			out.printf("\t\t<td><input type=\"submit\" name=\"search\" value=\"Danny Boy Search\"></td>%n");
			out.printf("</table>%n");
			out.printf("</form>%n");
			out.printf("</p>%n");



		finishResponse(request, response);
	}

	@Override
	protected void doPost(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		if (request.getParameter("logout") != null) {
			clearCookies(request, response);
			response.sendRedirect(response.encodeRedirectURL("/"));
		}

		Map<String, String> cookies = getCookieMap(request);

		String query = request.getParameter("query");

		log.info("POST " + request.getRequestURL().toString());

		response.addCookie(new Cookie("query", query));

		if (cookies.containsKey("name")) {
			String username = cookies.get("name");

			Status status = db.addQueryHistory(username, query);

			if(status == Status.OK) {
				response.sendRedirect(response.encodeRedirectURL("/results?query=" + query));
			}
			else {
				String url = "/?error=" + status.name();
				response.sendRedirect(response.encodeRedirectURL(url));
			}
		} else {
			response.setStatus(HttpServletResponse.SC_OK);
			response.sendRedirect(response.encodeRedirectURL("/result?query=" + query));
		}
	}


	private void printHeader(PrintWriter out, Map<String, String> cookies, HttpServletRequest request) {
		if (cookies.containsKey("login") && cookies.get("login").equals("true")){
			out.printf("<p align=\"right\">Logged in as <a href=\"/user\">%s</a>%n", cookies.get("name"));
			out.printf("<div id=\"logForm\">%n");
			out.printf("<form id=\"logForm\" align=\"right\" method=\"post\" action=\"%s\">%n", request.getRequestURI());
			out.printf("<input type=\"submit\" name=\"logout\" value=\"Logout\">%n");
			out.printf("</form>%n");
			out.printf("</div>");
			out.printf("</p>%n");
		} else {
			out.printf("<p align=\"right\">(<a href=\"/login\">Login here</a>)%n");
			out.printf("(<a href=\"/register\">new user? register here.</a>)</p>%n");
		}
		out.printf("<br>%n");



		out.printf("<h1><a href=\"/\"><img style=\"border:0;\" src=\"http://i.imgur.com/0uiCevh.png\" alt=\"SearchLogo\"></a></h1>%n");


	}
	private void printCSS(PrintWriter out) {
		out.printf("<style>%n");
		//WHOLE PAGE
		out.printf("html {%n");
		out.printf("\tbackground: url('http://bia4game.persiangig.com/image/watercolor-grunge-000002-gray-black.jpg') no-repeat center center fixed;%n");
		out.printf("\t-webkit-background-size: cover;%n");
		out.printf("\t-moz-background-size: cover;%n");
		out.printf("\t-o-background-size: cover;%n");
		out.printf("\tbackground-size: cover;%n");
		out.printf("\tcolor: white;%n");
		out.printf("}%n");
		//LOGO
		out.printf("h1 {%n");
		out.printf("\ttext-align:center%n");
		out.printf("}%n");
		//FORM
		out.printf("form {%n");
		out.printf("\twidth: 300px;%n");
		out.printf("\tmargin: 0 auto;%n");
		out.printf("}%n");
		//LOGFORM
		out.printf("#logForm {%n");
		out.printf("\tfloat: right;%n");
		out.printf("}%n");
		//LINKS
		out.printf("a:link {color:#FFFFFF;}      /* unvisited link */");
		out.printf("a:visited {color:#9B9B9B;}      /* visited link */");
		out.printf("a:hover {color:#3366AA;}      /* hovered link */");
		out.printf("a:active {color:#FF0000;}      /* selected link */");
		//TEXTBOX
		out.printf("input[type=\"text\"] {%n");
		out.printf("\twidth: 300px;%n");
		out.printf("\tpadding: 5px 0;%n");
		out.printf("\tbackground-color:#696969;%n");
		out.printf("\tcolor:#FFFFFF;%n");
		out.printf("}%n");
		out.printf("</style>%n");

	}
}