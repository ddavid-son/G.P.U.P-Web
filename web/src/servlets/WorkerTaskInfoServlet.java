package servlets;

import com.google.gson.reflect.TypeToken;
import dataTransferObjects.WorkerTaskInfoDto;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "WorkerTaskInfoServlet", urlPatterns = "/worker-task-info")
public class WorkerTaskInfoServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userName = request.getParameter("username");

        if (ServletUtils.getGPUPManager(request).userExists(userName)) {
            ServletUtils.getGPUPManager(request).getWorkerTaskInfo(userName);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(
                    SessionUtils.GSON.toJson(
                            ServletUtils.getGPUPManager(request).getWorkerTaskInfo(userName),
                            new TypeToken<List<WorkerTaskInfoDto>>() {
                            }.getType())
            );
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("User not found");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
