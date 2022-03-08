package servlets;

import argumentsDTO.TaskTarget;
import argumentsDTO.accumulatorForWritingToFile;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "SendResultsServlet", urlPatterns = "/finished-work")
public class SendResultsServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userName = request.getParameter("username");
        if (ServletUtils.getGPUPManager(request).userExists(userName)) { // ,maybe check if the task is still active
            String[] jsons = request.getReader().lines().collect(Collectors.joining()).split("~~~");

            List<accumulatorForWritingToFile> accs = SessionUtils.GSON.fromJson(jsons[0], new TypeToken<List<accumulatorForWritingToFile>>() {
            }.getType());
            List<TaskTarget> targets = SessionUtils.GSON.fromJson(jsons[1], new TypeToken<List<TaskTarget>>() {
            }.getType());

            targets.forEach(System.out::println);
            accs.forEach(System.out::println);

            if (targets.size() != accs.size() || targets.size() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("number of work done is inconsistent");
                return;
            }

            int totalEarnings = ServletUtils.getGPUPManager(request).acceptResults(targets, accs);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(String.valueOf(totalEarnings));
        } else {
            response.getWriter().write("I dont know who you are " + userName);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
