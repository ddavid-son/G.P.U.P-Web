package servlets;

import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "TaskStatuseSetterServlet", urlPatterns = "/change-task-statuse")
public class TaskStatuseSetterServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String statuse = req.getParameter("task-statuse");
        String taskName = req.getParameter("task-name");

        if (ServletUtils.getGPUPManager(req).taskExists(taskName)) {
            ServletUtils.getGPUPManager(req).setNewStatuse(statuse, taskName);
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("Successfully changed statuse");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("task not fond - could not complete operation");
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        doGet(req, res);
    }
}
