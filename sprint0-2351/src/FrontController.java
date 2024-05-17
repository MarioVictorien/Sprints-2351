package controller;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
public class FrontController extends HttpServlet
{
    protected void processRequest (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<HTML>");
        out.println("<BODY>");
        out.println("<BIG>"+request.getRequestURI()+"</BIG>");
        out.println("</BODY></HTML>");        
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
	}

    protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}