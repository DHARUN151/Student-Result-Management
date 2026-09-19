package service;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

    private static final String SMTP_HOST =
            "smtp.gmail.com";

    private static final String SMTP_PORT =
            "587";

    /*
     * IMPORTANT:
     * Replace these with your email configuration.
     * Do not commit the password to GitHub.
     */
    private static final String SMTP_USERNAME =
            "controllerofexamination975@gmail.com";

    private static final String SMTP_PASSWORD =
            "efdx jbug pugm rfoh";

    public boolean sendResultPublishedEmail(
            String recipientEmail,
            String studentName,
            int semester,
            String resultLink) {

        if (recipientEmail == null ||
                recipientEmail.trim().isEmpty()) {

            System.out.println(
                    "Student email is empty.");

            return false;
        }

        try {

            Properties properties =
                    new Properties();

            properties.put(
                    "mail.smtp.host",
                    SMTP_HOST);

            properties.put(
                    "mail.smtp.port",
                    SMTP_PORT);

            properties.put(
                    "mail.smtp.auth",
                    "true");

            properties.put(
                    "mail.smtp.starttls.enable",
                    "true");

            Session session =
                    Session.getInstance(
                            properties,
                            new Authenticator() {

                                @Override
                                protected PasswordAuthentication
                                getPasswordAuthentication() {

                                    return new PasswordAuthentication(
                                            SMTP_USERNAME,
                                            SMTP_PASSWORD);
                                }
                            });

            Message message =
                    new MimeMessage(session);

            message.setFrom(
                    new InternetAddress(
                            SMTP_USERNAME));

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(
                            recipientEmail));

            message.setSubject(
                    "Result Published - Semester "
                            + semester);

            String emailBody =
                    "Dear " + studentName + ",\n\n"
                    + "Your Semester "
                    + semester
                    + " result has been published.\n\n"
                    + "You can view your result by "
                    + "logging into the Student Result "
                    + "Management System.\n\n"
                    + "Result Link:\n"
                    + resultLink
                    + "\n\n"
                    + "You can also download your "
                    + "official Grade Sheet PDF "
                    + "from the student dashboard.\n\n"
                    + "Regards,\n"
                    + "Examination Cell\n"
                    + "M. Kumarasamy College of Engineering";

            message.setText(emailBody);

            Transport.send(message);

            System.out.println(
                    "Result email sent to: "
                            + recipientEmail);

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Unable to send result email.");

            e.printStackTrace();
        }

        return false;
    }
}