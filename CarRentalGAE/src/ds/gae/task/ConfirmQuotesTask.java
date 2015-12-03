package ds.gae.task;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.taskqueue.DeferredTask;

import ds.gae.CarRentalModel;
import ds.gae.ReservationException;
import ds.gae.entities.Quote;

public class ConfirmQuotesTask implements DeferredTask {

	private String renter;
	private String mail;
	private List<Quote> quotes = new ArrayList<>();
	
	public ConfirmQuotesTask(String renter, String mail, List<Quote> quotes) {
		this.renter = renter;
		this.mail = mail;
		this.quotes.addAll(quotes);
	}

	@Override
	public void run() {
		StringBuilder content = new StringBuilder();
		String subject = "";
		
		try {
			// Confirm quotes
			CarRentalModel.get().confirmQuotes(quotes);
			
			content.append("Following quotes successfully confirmed:\n");
			subject = "Successful reservations";

		} catch (ReservationException e) {
			subject = "Reservations failed.";

			content.append("Server responded with following error:\n");
			content.append(e.getMessage());
			content.append("\n");
		}
		
		// Sent feedback to user
		for (Quote q: quotes) { 
			content.append(" - ");
			content.append(q.getCarType());
			content.append("(from ");
			content.append(q.getStartDate());
			content.append(" until ");
			content.append(q.getEndDate());
			content.append(")\n");
		}
		
		MailService mailService = new MailService(renter, mail, subject, content.toString());
		mailService.sentMail();
		
	}

}
