import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class LoginRegisterServlet extends LoginBaseServlet {

	@Override
	public void doGet(
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		prepareResponse("Register New User", response);
		Map<String, String> cookies = getCookieMap(request);

		PrintWriter out = response.getWriter();
		printCSS(out);
		printHeader(out, cookies, request);
		String error = request.getParameter("error");

		if(error != null) {
			String errorMessage = StringUtilities.getStatus(error).message();
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

		printForm(out);
		finishResponse(request, response);
	}

	@Override
	public void doPost(
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		prepareResponse("Register New User", response);

		String newuser = request.getParameter("user");
		String newpass = request.getParameter("pass");
		Status status = db.registerUser(newuser, newpass);

		if(status == Status.OK) {
			response.sendRedirect(response.encodeRedirectURL("/login?newuser=true"));
		}
		else {
			String url = "/register?error=" + status.name();
			response.sendRedirect(response.encodeRedirectURL(url));
		}
	}

	private void printForm(PrintWriter out) {
		out.println("<form action=\"/register\" method=\"post\">");
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Usename:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"user\" size=\"30\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"pass\" size=\"30\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Register\"></p>");
		out.println("</form>");
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
}