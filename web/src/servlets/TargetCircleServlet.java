package servlets;

import backend.GPUPManager;
import com.google.gson.reflect.TypeToken;
import com.sun.deploy.net.HttpUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "TargetCircleServlet", urlPatterns = "/find-circle")
public class TargetCircleServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String engineName = req.getParameter("engine-name");
        String targetName = req.getParameter("target-name");

        if (engineName != null &&
                targetName != null && !targetName.isEmpty() &&
                (ServletUtils.getGPUPManager(req)).engineExists(engineName)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json");
            res.getWriter().write(
                    targetName + "~~~" + SessionUtils.GSON.toJson(
                            ServletUtils.getGPUPManager(req).getCircle(engineName, targetName), new TypeToken<List<String>>() {
                            }.getType()
                    )
            );
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(targetName == null ? "BAD_REQUEST" : targetName);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        doGet(req, res);
    }

}
