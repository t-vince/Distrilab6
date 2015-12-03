package ds.gae.task;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

class MailService {
	
	private String renter;
	private String mail;
	private String subject;
	private String content;

	protected MailService(String renter, String mail, String subject, String content) {
		this.renter = renter;
		this.mail = mail;
		this.subject = subject;
		this.content = content;
	}
	
	protected void sentMail() {
		// For testing purposes, the mail is printed to the console
		System.out.println("-- Mail --");
		
		try {
			System.out.println("Receiver: "+ mail);
			System.out.println("Subject: "+ subject);
			System.out.println("Content: \n"+ content);
			
			// Sending the message
			Properties properties = new Properties();
			Session session = Session.getInstance(properties, null);
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("noreply@rentalcompany.com", "Rental Company")); // Dummy mail
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(mail, renter));
			message.setSubject(subject);
			message.setContent(content, "text/html; charset=ISO-8859-1");
			
			// The mail will not actually be sent due to local setup, but this event will be logged
			Transport.send(message);
			
		} catch (UnsupportedEncodingException | MessagingException e) {
			System.out.println("Sending mail failed.");
			e.printStackTrace();
		}
	}

}
