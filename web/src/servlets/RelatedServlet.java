package servlets;

import com.google.gson.reflect.TypeToken;
import dataTransferObjects.WhatIfDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "RelatedServlet", urlPatterns = "/get-related")
public class RelatedServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String engineName = req.getParameter("engine-name");
        String targetName = req.getParameter("target-name");
        String related = req.getParameter("relation-type");

        if (assetParameters(req, engineName, targetName, related)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json");
            res.getWriter().write(
                    SessionUtils.GSON.toJson(
                            ServletUtils.getGPUPManager(req).getRelated(engineName, targetName, related),
                            new TypeToken<WhatIfDTO>() {
                            }.getType()
                    )
            );
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Input provided was not valid, Please try again");
        }
    }

    private boolean assetParameters(HttpServletRequest req, String engineName, String targetName, String related) {
        return engineName != null && targetName != null && !targetName.isEmpty() &&
                ("DEPENDS_ON".equals(related) || "REQUIRED_FOR".equals(related)) &&
                (ServletUtils.getGPUPManager(req)).engineExists(engineName);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        doGet(req, res);
    }
}
