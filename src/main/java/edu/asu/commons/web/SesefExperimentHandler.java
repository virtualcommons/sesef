package edu.asu.commons.web;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.stringtemplate.v4.ST;

import edu.asu.commons.util.ResourceLoader;

/**
 * Jetty embedded server delivering templated client and facilitator JNLPs for SESEF experiments.
 */
public class SesefExperimentHandler extends AbstractHandler {
    
    protected final Logger logger = Logger.getLogger(getClass().getName());
    
    protected void addFacilitatorTemplateVariables(ST template) {
        template.add("frameworkJar", "sesef.jar");
    }
    
    protected void addClientTemplateVariables(ST template) {
    }

    protected ST handleFacilitatorRequest(HttpServletResponse response) throws IOException {
        logger.info("handling facilitator request");
        ST template = getJnlpTemplate(getFacilitatorJnlpTemplate());
        addFacilitatorTemplateVariables(template);
        return template;
    }

    protected String getFacilitatorJnlpTemplate() {
        return "facilitator-template.jnlp";
    }

    protected String getClientJnlpTemplate() {
        return "client-template.jnlp";
    }
    
    protected ST handleClientRequest(HttpServletResponse response) throws IOException {
        logger.info("handling client request");
        ST template = getJnlpTemplate(getClientJnlpTemplate());
        addClientTemplateVariables(template);
        return template;
    }

    
    protected ST getJnlpTemplate(String path) throws IOException {
        return new ST(ResourceLoader.getResourceAsString(path));
    }
    
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        String path = baseRequest.getUri().getPath();
        logger.info(path);
        ST template = (path.contains("facilitator")) ? handleFacilitatorRequest(response) : handleClientRequest(response);
        logger.info("generating template response " + template);
        response.getWriter().println(template.render());
    }
    
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new SesefExperimentHandler());
        server.start();
        server.join();
    }

}
