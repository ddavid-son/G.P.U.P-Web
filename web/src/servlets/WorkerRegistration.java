package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "WorkerRegistration", urlPatterns = "/task-registration")
public class WorkerRegistration extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String joinOrLeave = request.getParameter("join");
        String taskName = request.getParameter("task-name");
        String workerName = request.getParameter("user-name");

        boolean parametersAreValid = assertParameters(taskName, joinOrLeave, workerName, request);

        if (parametersAreValid) {
            if ("join".equals(joinOrLeave)) {
                ServletUtils.getGPUPManager(request).joinTask(taskName, workerName);
            } else {
                ServletUtils.getGPUPManager(request).leaveTask(taskName, workerName);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Operation successful");
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid parameters");
        }
    }

    private boolean assertParameters(String taskName, String joinOrLeave, String workerName, HttpServletRequest request) {
        return ServletUtils.getGPUPManager(request).taskExists(taskName) &&
                ServletUtils.getGPUPManager(request).userInRole(workerName, "worker") &&
                ("join".equals(joinOrLeave) || "leave".equals(joinOrLeave) &&
                        ServletUtils.getGPUPManager(request).taskInCompatibleState(taskName, joinOrLeave));
    }
}
