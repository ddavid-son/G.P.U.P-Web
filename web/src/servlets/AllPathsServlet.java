package servlets;

import com.google.gson.reflect.TypeToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AllPathsServlet", urlPatterns = "/get-all-paths")
public class AllPathsServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String engineName = req.getParameter("engine-name");
        String srcName = req.getParameter("src-target");
        String dstName = req.getParameter("dst-target");

        if (engineName != null && dstName != null &&
                srcName != null && !srcName.isEmpty() && !dstName.isEmpty() &&
                (ServletUtils.getGPUPManager(req)).engineExists(engineName)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json");
            res.getWriter().write(
                    SessionUtils.GSON.toJson(
                            ServletUtils.getGPUPManager(req).getAllPaths(engineName, srcName, dstName),
                            new TypeToken<List<String>>() {
                            }.getType()
                    )
            );
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Request input was invalid");
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        doGet(req, res);
    }
}
