package edu.asu.commons.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Jetty embedded server for SESEF experiments.
 */
public class SesefExperimentHandler extends AbstractHandler {


    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        // FIXME: figure out how to best support custom experiments 
        response.getWriter().println("<html><body><h1>Sesef Web Experiment</h1></body></html>");
    }
    
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new SesefExperimentHandler());
        server.start();
        server.join();
    }

}
