package servlets;

import argumentsDTO.TaskTarget;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.print.attribute.standard.Severity;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetTargetsServlet", urlPatterns = "/fetch-work")
public class GetTargetsServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        int availableResources = Integer.parseInt(request.getParameter("available-resources"));

        if (availableResources > 0 && availableResources < 6 &&
                ServletUtils.getGPUPManager(request).userExists(username, "worker")) {
            response.getWriter().write(
                    SessionUtils.GSON.toJson(
                            ServletUtils.getGPUPManager(request).getWorkForWorker(username, availableResources),
                            new TypeToken<List<TaskTarget>>() {
                            }.getType()
                    )
            );
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid arguments: " + username + "\n" + availableResources);
        }

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
