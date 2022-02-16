package servlets;

import com.google.gson.reflect.TypeToken;
import dataTransferObjects.UpdateListsDTO;
import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "StateListsServlet", urlPatterns = "/get-state-lists")
public class StateListsServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String taskName = request.getParameter("task-name");
        boolean taskExist = ServletUtils.getGPUPManager(request).taskExists(taskName);
        if (taskExist) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(
                    SessionUtils.GSON.toJson(
                            ServletUtils.getGPUPManager(request).getUpdateListsDTO(taskName),
                            new TypeToken<UpdateListsDTO>() {
                            }.getType()
                    )
            );
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Task \"" + taskName + "\" not found");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
