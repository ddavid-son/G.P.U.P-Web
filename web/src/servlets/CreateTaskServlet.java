package servlets;

import argumentsDTO.CompilationArgs;
import argumentsDTO.SimulationArgs;
import argumentsDTO.TaskArgs;
import backend.Task;
import backend.TaskManager;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.nashorn.internal.parser.Token;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.EventListener;
import java.util.stream.Collectors;

@WebServlet(name = "CreateTaskServlet", urlPatterns = {"/create-task"})
public class CreateTaskServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String taskType = request.getParameter("task-type");
        String taskArgsJson = request.getReader().lines().collect(Collectors.joining());
        Type type;

        if ("SIMULATION".equals(taskType)) {
            type = new TypeToken<SimulationArgs>() {
            }.getType();
        } else {
            type = new TypeToken<CompilationArgs>() {
            }.getType();
        }

        TaskArgs taskArgs = SessionUtils.GSON.fromJson(taskArgsJson, type);
        boolean b = assertTaskArgs(taskArgs, request);

        if (b) {
            TaskManager taskManager = ServletUtils.getGPUPManager(request)
                    .getEngineByName(taskArgs.getOriginalGraph())
                    .buildTask(taskArgs, null, null);
            if (ServletUtils.getGPUPManager(request).addTask(taskManager)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("addition was successful");
            } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("Task with the name \"" + taskArgs.getTaskName() + "\" already exists");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Some of the arguments provided aren't valid: " + taskArgs);
        }
    }

    private boolean assertTaskArgs(TaskArgs taskArgs, HttpServletRequest request) {
        return taskArgs.getTaskName() != null && !taskArgs.getTaskName().isEmpty() && // todo add check for task unique name
                ServletUtils.getGPUPManager(request).engineExists(taskArgs.getOriginalGraph());
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
