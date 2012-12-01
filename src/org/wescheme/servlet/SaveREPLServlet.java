package org.wescheme.servlet;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wescheme.project.Program;
import org.wescheme.user.Session;
import org.wescheme.user.SessionManager;
import org.wescheme.util.PMF;
import org.wescheme.util.Queries;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SaveREPLServlet extends HttpServlet {

    /**
     * Returns program XML if either pid or publicId is provided.
     */
    private static final long serialVersionUID = 1165047992267892812L;
    private static final Logger log = Logger.getLogger(SaveREPLServlet.class.getName());
	
    private boolean isOwner(Session userSession, Program prog) {
        return (userSession != null && 
                prog != null && 
                prog.getOwner().equals(userSession.getName()));
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
   	 	log.log(Level.INFO, "Got a request."); 
    	log.log(Level.INFO, req.getParameter("data"));
    	
        resp.setContentType("text/json");
        resp.getWriter().print("{}");
    }   
    
    private Program getProgramByPid(PersistenceManager pm, String pid) {
        Long id = (Long) Long.parseLong(pid);
        Key k = KeyFactory.createKey("Program", id);
        Program prog = pm.getObjectById(Program.class, k);
        return prog;
    }

	
    private Program getProgramByPublicId(PersistenceManager pm, String publicId) {
        Program program = Queries.getProgramByPublicId(pm, publicId);
        if (program == null) {
            throw new RuntimeException("Could not find unique program with publicId=" + publicId);
        }
        return program;
    }	
}
