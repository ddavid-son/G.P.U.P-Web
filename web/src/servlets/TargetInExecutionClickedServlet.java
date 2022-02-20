package servlets;

import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "TargetInExecutionClickedServlet", urlPatterns = "/target-clicked-info")
public class TargetInExecutionClickedServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String taskName = request.getParameter("task-name");
        String targetName = request.getParameter("target-name");
        String targetState = request.getParameter("target-state");

        if (assertParameters(request, taskName, targetName, targetState)) {
            response.setContentType("application/json");
            response.getWriter().write(
                    SessionUtils.GSON.toJson(
                            ServletUtils.getGPUPManager(request)
                                    .getInfoAboutClockedTarget(taskName, targetName, targetState),
                            new TypeToken<List<String>>() {
                            }.getType())
            );
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.getWriter().write("Task " + taskName + " does not exist, your request wasn't processed");
        }
    }

    private boolean assertParameters(HttpServletRequest request, String taskName, String targetName, String targetState) {
        return ServletUtils.getGPUPManager(request).taskExists(taskName) &&
                targetName != null && !targetName.isEmpty() && targetState != null && !targetState.isEmpty();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
