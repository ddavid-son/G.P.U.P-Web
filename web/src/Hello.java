import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class Hello extends HttpServlet {
    @WebServlet(name = "Hello", urlPatterns = "/hello")
    public static class HelloServlet extends HttpServlet {
        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

            response.getWriter().write("Hello, Wor  ld!");
        }
    }
}
