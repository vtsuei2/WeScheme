package org.wescheme.servlet;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wescheme.data.DAO;
import org.wescheme.data.Feedback;
import org.wescheme.project.HistoryEntry;
import org.wescheme.project.Program;
import org.wescheme.user.Session;
import org.wescheme.user.SessionManager;
import org.wescheme.util.CacheHelpers;
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
    	/*Long id = request.getParameter("id");
    	Long programID = request.getParameter("programID");
    	String author = request.getParameter("author");
		List<String> history = request.getParameter("history");
		Date date = request.getParameter("date");
		History his = new History(programID, author, history, date);
		new DAO().saveHistory(his);*/
		
//		response.setContentType("text/plain");
//		response.getWriter().write("ok");
    	
    	
   	 	log.log(Level.INFO, "Got a request."); 
    	log.log(Level.INFO, req.getParameter("data"));
    	
    	if (req.getParameter("programID") == null || req.getParameter("data") == null) {
    		resp.sendError(401);
    		return;
    	}
    	
    	PersistenceManager pm = PMF.get().getPersistenceManager();
        SessionManager sm = new SessionManager();
    	
        Long pid = null;
        try {
        	pid = Long.parseLong(req.getParameter("programID"));
        }  catch  (NumberFormatException nfe) { }
        if (pid == null) {
        	resp.setContentType("text/json");
            resp.getWriter().print("{'status':'0'}");
        }
        
    	String command = req.getParameter("data");
    	long time = System.currentTimeMillis(); //TODO: Figure this out
    	
    	try {
    		Session userSession = sm.authenticate(req, resp);
            if( null != userSession ){			
                CacheHelpers.notifyUserProgramsDirtied(userSession.getName());
                    Program prog = pm.getObjectById(Program.class, pid);
                    if (prog != null && prog.getOwner().equals(userSession.getName())) {
                    	prog.addHistoryEntry(new HistoryEntry(command, time));
                    } else {
                    	throw new RuntimeException("Null program or not owner.");
                    }
            } else {
                log.warning("User session can't be retrieved; user appears to be logged out.");
                resp.sendError(401);
                return;
            }
    	} finally {
            pm.close();
        }	
    	
        resp.setContentType("text/json");
        resp.getWriter().print("{'status':'1'}");
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
