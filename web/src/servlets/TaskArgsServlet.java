package servlets;

import argumentsDTO.CompilationArgs;
import argumentsDTO.SimulationArgs;
import argumentsDTO.TaskArgs;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.lang.reflect.Type;

@WebServlet(name = "TaskArgsServlet", urlPatterns = "/task-view-data")
public class TaskArgsServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String taskName = req.getParameter("task-name");
        if (taskName != null && ServletUtils.getGPUPManager(req).taskExists(taskName)) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            Type type;
            TaskArgs taskArgs = ServletUtils.getGPUPManager(req).getTaskArgs(taskName);

            String s;
            if (taskArgs instanceof CompilationArgs) {
                s = "COMPILATION";
                type = new TypeToken<CompilationArgs>() {
                }.getType();
            } else {
                s = "SIMULATION";
                type = new TypeToken<SimulationArgs>() {
                }.getType();
            }

            resp.getWriter().write(s + "~~~" + SessionUtils.GSON.toJson(taskArgs, type));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Task not found");
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }
}
