//Variables needed for email-related actions
package com.aliza.davening;

import org.springframework.beans.factory.annotation.Value;

public class EmailScheme {
	private static int imageWidth = 500;
	private static int imageHeight = 800;

	//Values in application properties
	@Value("${mailgun.api.username}")
	public static String mailGunAPIUsername;
	@Value("${mailgun.api.password}")
	public static String mailGunAPIPassword;
	@Value("${mailgun.api.base.url}")
	public static String mailGunAPIBaseUrl;
	@Value("${mailgun.api.messages.url}")
	public static String mailGunAPIMessagesUrl;


	private static String simpleHeader = "<h2>%s</h2>";

	private static String bilingualHeader = "<h2>%s     %s</h2>";

	private static String inMemoryHebrew = "לעילוי נשמת אסתר נעמי בת יעקב ע\"ה";

	private static String inMemoryEnglish = "L\'iluy Nishmat Esther Naomi Bat Yaakov";

	private static String banimLineEnglish = "Yehi ratzon she_____ yipakdu b'zera chaya v'kayama";

	private static String banimLineHebrew = "יהי רצון ש___ יפקדו בזרע חיא וקימא";

	private static String confirmationEmailTextLocation = "src/main/resources/static/confirmationEmailText.html";

	private static String htmlHead = "<!DOCTYPE html><html><head><title>Weekly Davening List</title><style>body{text-align:center} table, td {padding: 10px; border-collapse: collapse; border: 1px solid black} h2, h3, h4{text-align:center}</style></head>";

	private static String htmlBodyStart = "<body>";

	private static String tableStart = "<table style='width:100%'>";

	private static String htmlNameRowInList = "<tr><td style='text-align:left'>%s</td><td style='text-align:right'>%s</td></tr>";

	private static String htmlBanimRowInList = "<tr><td style='text-align:left'>%s<br>%s</td><td style='text-align:right'>%s<br>%s</td></tr>";

	private static String tableClose = "</table>";

	private static String nextWeekCategory = "<h3>Next week:  %s                שבוע הבא: %s</h3>";

	private static String sendGoodNewsMessage = "Please email %s with name and good news!";

	private static String htmlBodyEnd = "</body></html>";

	//TODONOW - make a real unsubscribe link
	private static String weeklyEmailText = "To unsubscribe from the weekly davening list, click <a href='http://www.google.com'>HERE</a>";

	// Subject includes the category
	private static String weeklyEmailSubject = "Weekly davening list for %s";

	// Default subject in emails from admin
	private static String adminMessageSubject = "Message from davening list admin";

	private static String userDisactivated = "We are confirming that your participation on the davening list has been disactivated. <br><br> You will no longer receive emails regarding the davening list.  You may resubscribe at any time.  <br><br>If you think you did not disactivate your participation on the list, please contact your list admin immediately. ";

	private static String userActivated = "We are confirming that your participation on the davening list has been activated. <br><br> You will now be receiving emails regarding the davening list.  You may unsubscribe at any time.  <br><br>If you did not request to join the list, please contact your list admin immediately. ";

	// Text appearing in Admin's email requesting to daven urgently (can be Banim or
	// any category)
	private static String urgentDavenforEmailBanim = "Please daven now for <b>%s - %s</b> and <b>%s - %s</b>, for: <b>%s</b>. <br>";

	private static String urgentDavenforEmailText = "Please daven now for <b>%s - %s</b>, for: <b>%s<b/>. <br>";

	// The text in email informing admin of an update made to a name.
	private static String informAdminOfUpdate = "<b>%s</b> has just updated the name: <br><b>%s <br> %s </b><br>  in the category: <br> <b>%s. </b><br><br> You might want to check that it was properly updated. ";

	private static String informAdminOfUpdateSubject = "A name has been updated on your davening list. ";

	private static String weeklyFileName = "Davening List %s";

	// putting one message first, in bold, with new line before other text.
	private static String boldFirstMessage = "<h4>%s</h4>%s";

	// putting second message, in bold, on new line after other text.
	private static String boldSecondMessage = "%s<h4>%s</h4>";

	private static String confirmationEmailSubject = "Davening list submission";

	private static String informAdminOfNewName = "The name: <br><b>%s <br> %s </b><br> has been added to the category: <br> <b>%s. </b><br> by <b>%s</b>"
			+ "<br><br> You might want to check that it was properly entered. ";

	private static String informAdminOfNewNameSubject = "A new name has been added to your davening list. ";

	private static String weeklyAdminReminderSubject = "Davening list reminder: Send out the weekly list!";

	private static String expiringNameSubject = "Davening List Confirmation";

	public static int getImageWidth() {
		return imageWidth;
	}

	public static int getImageHeight() {
		return imageHeight;
	}

	public static String getSimpleHeader() {
		return simpleHeader;
	}

	public static String getBilingualHeader() {
		return bilingualHeader;
	}

	public static String getInMemoryHebrew() {
		return inMemoryHebrew;
	}

	public static String getInMemoryEnglish() {
		return inMemoryEnglish;
	}

	public static String getBanimLineEnglish() {
		return banimLineEnglish;
	}

	public static String getBanimLineHebrew() {
		return banimLineHebrew;
	}

	public static String getConfirmationEmailTextLocation() {
		return confirmationEmailTextLocation;
	}

	public static String getHtmlHead() {
		return htmlHead;
	}

	public static String getHtmlBodyStart() {
		return htmlBodyStart;
	}

	public static String getTableStart() {
		return tableStart;
	}

	public static String getHtmlNameRowInList() {
		return htmlNameRowInList;
	}

	public static String getHtmlBanimRowInList() {
		return htmlBanimRowInList;
	}

	public static String getTableClose() {
		return tableClose;
	}

	public static String getNextWeekCategory() {
		return nextWeekCategory;
	}

	public static String getSendGoodNewsMessage() {
		return sendGoodNewsMessage;
	}

	public static String getHtmlBodyEnd() {
		return htmlBodyEnd;
	}

	public static String getWeeklyEmailText() {
		return weeklyEmailText;
	}

	public static String getWeeklyEmailSubject() {
		return weeklyEmailSubject;
	}

	public static String getAdminMessageSubject() {
		return adminMessageSubject;
	}

	public static String getUserDisactivated() {
		return userDisactivated;
	}

	public static String getUserActivated() {
		return userActivated;
	}

	public static String getUrgentDavenforEmailBanim() {
		return urgentDavenforEmailBanim;
	}

	public static String getUrgentDavenforEmailText() {
		return urgentDavenforEmailText;
	}

	public static String getInformAdminOfUpdate() {
		return informAdminOfUpdate;
	}

	public static String getInformAdminOfUpdateSubject() {
		return informAdminOfUpdateSubject;
	}

	public static String getWeeklyFileName() {
		return weeklyFileName;
	}

	public static String getBoldFirstMessage() {
		return boldFirstMessage;
	}

	public static String getBoldSecondMessage() {
		return boldSecondMessage;
	}

	public static String getConfirmationEmailSubject() {
		return confirmationEmailSubject;
	}

	public static String getInformAdminOfNewName() {
		return informAdminOfNewName;
	}

	public static String getInformAdminOfNewNameSubject() {
		return informAdminOfNewNameSubject;
	}

	public static String getExpiringNameSubject() {
		return expiringNameSubject;
	}

	public static String getWeeklyAdminReminderSubject() {
		return weeklyAdminReminderSubject;
	}

}
