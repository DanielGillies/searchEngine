import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class SearchResultServlet extends BaseServlet {

	@Override
	public void doGet(
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		prepareResponse("Results", response);

		PrintWriter out = response.getWriter();
		NumberFormat formatter = new DecimalFormat("#0.000");

		Map<String, String> cookies = getCookieMap(request);
		long startTime = System.currentTimeMillis();
		ArrayList<SearchResult> result = new ArrayList<>();
		String query = cookies.get("query");

		String[] queryList = query.split("\\s+");

		result = Driver.index.search(queryList);

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;


		printCSS(out);
		printHeader(out, cookies, request);

		out.printf("<div id=\"query\">%n");
		out.printf("%d results for \"%s\" (%s seconds)%n", result.size(), query, formatter.format(totalTime/1000d));
		out.printf("<br>%n");
		out.printf("</div>");

		out.printf("<div id=\"results\">%n");
		for (SearchResult swag : result) {
			out.printf("<a href=\"" + swag.getPath() + "\">" + swag.getPath() + "</a>%n");
			out.printf("<br>%n");
		}
		out.printf("</div>%n");

		finishResponse(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (request.getParameter("logout") != null) {
			clearCookies(request, response);
			response.sendRedirect(response.encodeRedirectURL("/"));
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
		//RESULTS LIST
		out.printf("#results {%n");
		out.printf("\tfont-size: 140%%;%n");
		out.printf("\tmargin: auto;%n");
		out.printf("\twidth: 40%%;%n");
		out.printf("}%n");
		//LOGFORM
		out.printf("#logForm {%n");
		out.printf("\tfloat: right;%n");
		out.printf("}%n");
		//QUERY
		out.printf("#query {%n");
		out.printf("\tfont-size: 160%%;%n");
		out.printf("\tmargin: auto;%n");
		out.printf("\twidth: 40%%;%n");
		out.printf("\tcolor:#3366AA;%n");
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