package servlets;

import backend.GPUPManager;
import constants.Constants;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.File;
import java.io.IOException;

@WebListener
@WebServlet(name = "ClientLogin", urlPatterns = "/client-login")
public class ClientLogin extends HttpServlet implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        File gpupDir = new File("c:\\gpup-working-dir");
        if (!gpupDir.exists()) {
            try {
                if (!gpupDir.mkdirs()) throw new IOException("Could not create directory");
            } catch (SecurityException | IOException se) {
                throw new IllegalArgumentException("could not find the folder and creating it failed too" +
                        "(could be due to permission issues), please choose another directory");
            }
        }
    }

    @Override
    public void init() {
        if (getServletContext().getAttribute(Constants.GPUP_MANAGER) == null) {
            getServletContext().setAttribute(Constants.GPUP_MANAGER, new GPUPManager());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");

        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        if (usernameFromSession == null) {
            String usernameFromParameter = request.getParameter(Constants.USERNAME);
            String usernameRoleParameter = request.getParameter(Constants.USER_ROLE);
            String numberOfThreadsAvailableToWorker = request.getParameter(Constants.NUMBER_OF_THREADS);

            if (usernameFromParameter == null || usernameFromParameter.isEmpty() ||
                    usernameRoleParameter == null || usernameRoleParameter.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
            } else {
                usernameFromParameter = usernameFromParameter.trim();
                usernameRoleParameter = usernameRoleParameter.trim();
                synchronized (this) {
                    if (userManager.isUserExists(usernameFromParameter)) {
                        String errorMessage = "Username " + usernameFromParameter +
                                " already exists. Please enter a different username.";
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getOutputStream().print(errorMessage);
                    } else {
                        userManager.addUser(usernameFromParameter);
                        request.getSession(true).setAttribute(Constants.USERNAME, usernameFromParameter);
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.addCookie(new Cookie("username", usernameFromParameter));
                        saveUserInEngine(request, usernameFromParameter, usernameRoleParameter);
                    }
                }
            }
        } else {
            // todo: figure out why this is here
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    private void saveUserInEngine(HttpServletRequest request, String usernameFromParameter, String role) {
        ((GPUPManager) getServletContext()
                .getAttribute(Constants.GPUP_MANAGER))
                .addUser(
                        request.getSession().getId(),
                        usernameFromParameter,
                        role
                );
    }
}