package ds.gae.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import ds.gae.entities.Quote;
import ds.gae.task.ConfirmQuotesTask;
import ds.gae.view.JSPSite;

@SuppressWarnings("serial")
public class ConfirmQuotesServlet extends HttpServlet {
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession();
		
		String key = session.getId();
		HashMap<String, ArrayList<Quote>> allQuotes = (HashMap<String, ArrayList<Quote>>) session.getAttribute("quotes");

		ArrayList<Quote> quotes = new ArrayList<Quote>();
		for (String crcName : allQuotes.keySet()) {
			quotes.addAll(allQuotes.get(crcName));
		}
		
		// get session values
		String sessionId = req.getSession().getId();
		String renter = (String) req.getSession().getAttribute("renter");
		String email = (String) req.getSession().getAttribute("email");
		
		// Queue using Google queue factory
		Queue defaultQueue = QueueFactory.getDefaultQueue();
		
		defaultQueue.add(
				TaskOptions.Builder.withPayload( new ConfirmQuotesTask(renter, email, quotes) )
				);
		
		// Clear the current quotes map
		session.setAttribute("quotes", new HashMap<String, ArrayList<Quote>>());
		
		// Note:
		// If you wish confirmQuotesReply.jsp to be shown to the client as
		// a response of calling this servlet, please replace the following line 
		// with resp.sendRedirect(JSPSite.CONFIRM_QUOTES_RESPONSE.url());
		resp.sendRedirect(JSPSite.CREATE_QUOTES.url());
	}
	
	// Small function to serialize objects
	private byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
  	    ObjectOutputStream os = new ObjectOutputStream(out);
  	    os.writeObject(obj);
  	    
  	    return out.toByteArray();
	}
}
