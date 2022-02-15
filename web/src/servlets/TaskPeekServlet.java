package servlets;

import com.google.gson.reflect.TypeToken;
import dataTransferObjects.TaskInfoDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sun.reflect.generics.tree.TypeTree;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "TaskPeekServlet", urlPatterns = "/get-task-peek")
public class TaskPeekServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String taskName = request.getParameter("task-name");

        if (ServletUtils.getGPUPManager(request).taskExists(taskName)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(
                    SessionUtils.GSON.toJson(
                            ServletUtils.getGPUPManager(request).getTaskInfo(taskName),
                            new TypeToken<TaskInfoDTO>() {
                            }.getType()
                    )
            );
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Task with the name \"" + taskName + "\" does not exists");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
