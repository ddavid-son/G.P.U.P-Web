package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "GraphPeekServlet", urlPatterns = {"/graph-peek"})
public class GraphPeekServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //todo: add validation for graph existence on all servlets
        String graphName = request.getParameter("graph-name");
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(
                SessionUtils.GSON.toJson(
                        ServletUtils.getGPUPManager(request).getGraphPeek(graphName))
        );
    }
}
