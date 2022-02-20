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
import java.util.List;

@WebServlet(name = "TaskPeekServlet", urlPatterns = "/get-task-peek")
public class TaskPeekServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //String taskName = request.getParameter("task-name");
        String userName = request.getParameter("user-name");

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(
                SessionUtils.GSON.toJson(
                        ServletUtils.getGPUPManager(request).getTaskInfo(userName),
                        new TypeToken<List<TaskInfoDTO>>() {
                        }.getType()
                )
        );

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
