package servlets;

import com.google.gson.reflect.TypeToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.Request;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import argumentsDTO.CommonEnums.*;

@WebServlet(name = "WhatIfServlet", urlPatterns = {"/get-what-if"})
public class WhatIfServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String engineName = request.getParameter("engine-name");
        String relationType = request.getParameter("relation-type");
        String selectedTargetsJson = request.getReader().lines().collect(Collectors.joining());
        List<String> targets = SessionUtils.GSON.fromJson(selectedTargetsJson,
                new TypeToken<List<String>>() {
                }.getType());

        boolean b = assertArguments(engineName, relationType, targets, request);
        if (b) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(
                    SessionUtils.GSON.toJson(
                            ServletUtils.getGPUPManager(request).getEngineByName(engineName)
                                    .getWhatIf(
                                            targets,
                                            "DEPENDS_ON".equals(relationType) ?
                                                    RelationType.DEPENDS_ON :
                                                    RelationType.REQUIRED_FOR
                                    )
                            , new TypeToken<List<String>>() {
                            }.getType()
                    )
            );
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("error, invalid arguments: " + engineName + relationType + targets);
        }

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

    private boolean assertArguments(String engineName, String relationType, List<String> selectedTargets, HttpServletRequest request) {
        return selectedTargets != null &&
                selectedTargets.size() > 0 &&
                ServletUtils.getGPUPManager(request).engineExists(engineName) &&
                ("DEPENDS_ON".equals(relationType) || "REQUIRED_FOR".equals(relationType));
    }
}
