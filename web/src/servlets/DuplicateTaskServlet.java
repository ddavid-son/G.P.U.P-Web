package servlets;

import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "DuplicateTaskServlet", urlPatterns = "/duplicate-task")
public class DuplicateTaskServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String taskName = request.getParameter("task-name");
        boolean isIncremental = request.getParameter("incremental").equals("true");

        if (taskName != null && !taskName.isEmpty() && ServletUtils.getGPUPManager(request).taskExists(taskName)) {
            if (ServletUtils.getGPUPManager(request).createTaskFromExistingTask(taskName, isIncremental)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Task " + taskName + " duplicated successfully");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Task can only be duplicated when in finished state");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Task " + taskName + " does not exist");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }


}
