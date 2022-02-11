package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "DTOsForAdminServlet", urlPatterns = {"/get-dtos-for-admin"})
public class DTOsForAdminServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        String allTargetInfo =
                SessionUtils.GSON.toJson(
                        ServletUtils.getGPUPManager(request).getInfoAboutAllTargets(
                                request.getParameter("graph-name"))
                );

        String summaryInfo = SessionUtils.GSON.toJson(
                ServletUtils.getGPUPManager(request).getGraphPeek(
                        request.getParameter("graph-name"))
        );

        String bothJson = summaryInfo + "~~~" + allTargetInfo;
        response.getWriter().write(bothJson);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
