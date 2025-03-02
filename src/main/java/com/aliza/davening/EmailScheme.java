//Variables needed for email-related actions
package com.aliza.davening;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailScheme {

	@Value("${client.origin}")
	public String client;

	@Value("${server.url}")
	public String server;

	public String getServer() {
		return server;
	}

	public static final String h5Header = "<h5>%s</h5>";

	public static final String categoryAndParashaHeader = "<h4>%s <br>%s   %s</h4>";

	public static final String inMemory = "לעילוי נשמת אסתר נעמי בת יעקב ע\"ה <br> L\'iluy Nishmat Esther Naomi Bat Yaakov";

	public static final String banimLineEnglish = "Yehi ratzon she_____ yipakdu b'zera chaya v'kayama";

	public static final String banimLineHebrew = "יהי רצון ש___ יפקדו בזרע חיא וקימא";

	public static final String confirmationEmailTextLocation = "src/main/resources/static/confirmationEmailText.html";
	
	public static final String pageDivider = "<script>document.addEventListener(\"DOMContentLoaded\", function () { const rows = Array.from(document.querySelectorAll(\"table tr\")); const pageHeight = 900; let currentPage = document.createElement(\"div\"); currentPage.classList.add(\"page\"); document.body.appendChild(currentPage); let currentHeight = 0; rows.forEach(row => { const rowHeight = row.offsetHeight; if (currentHeight + rowHeight > pageHeight) { currentPage = document.createElement(\"div\"); currentPage.classList.add(\"page\"); document.body.appendChild(currentPage); currentHeight = 0; } currentPage.appendChild(row); currentHeight += rowHeight; }); });</script>";
	
//TODO*: move to separate file, to make editing easier.  All long htmls should be read from files
	public static final String htmlHead = "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=15cm, initial-scale=1.0\"><title>Weekly Davening List</title><style>  @media print { .page { page-break-after: always; }  tr { page-break-inside: avoid; } }   body{text-align:center} table, td {padding: 10px; border-collapse: collapse; border: 1px solid black} h2, h3, h4, h5{text-align:center} html, body {width: 100%; margin: 0; padding: 0; overflow: hidden;}  tr{height:auto} td { vertical-align: top; padding: 5px;  }  .page {width: 100%; font-size: 12pt; border: 1px solid black;  box-sizing: border-box;}</style></head>";

	public static final String htmlBodyStart = "<body><div class='page'>";

	public static final String tableStart = "<table style='width:100%'>";

	public static final String htmlNameRowInList = "<tr><td style='text-align:left'>%s</td><td style='text-align:right'>%s</td></tr>";

	public static final String htmlBanimRowInList = "<tr><td style='text-align:left'>%s<br>%s</td><td style='text-align:right'>%s<br>%s</td></tr>";

	public static final String tableClose = "</table>";

	public static final String nextWeekCategory = "<h3>Next week:  %s                שבוע הבא: %s</h3>";

	public static final String sendGoodNewsMessage = "Please email %s with name and good news!";

	public static final String htmlBodyEnd = "</div></body></html>";

	public static final String weeklyEmailText = "To unsubscribe from the weekly davening list, click <a href='%s'>HERE</a>";

	// Subject includes the category
	public static final String weeklyEmailSubject = "Weekly davening list for %s";

	// Default subject in emails from admin
	public static final String adminMessageSubject = "Message from davening list admin";

	public static final String userDisactivated = "We are confirming that your participation on the davening list has been disactivated. <br><br> You will no longer receive emails regarding the davening list. <br><br>If you think you did not disactivate your participation on the list, please contact thes list admin immediately. ";

	public static final String userActivated = "We are confirming that your participation on the davening list has been activated. <br><br> You will now be receiving emails regarding the davening list.  You may unsubscribe at any time.  <br><br>If you did not request to join the list, please contact your list admin immediately. ";

	// Text appearing in Admin's email requesting to daven urgently (can be Banim or
	// any category)
	public static final String urgentDavenforEmailBanim = "Please daven now for <b>%s - %s</b> and <b>%s - %s</b>, for: <b>%s</b>. <br>";

	public static final String urgentDavenforEmailText = "Please daven now for <b>%s - %s</b>, for: <b>%s<b/>. <br>";

	// The text in email informing admin of an update made to a name.
	public static final String informAdminOfUpdate = "<b>%s</b> has just updated the name: <br><b>%s <br> %s </b><br>  in the category: <br> <b>%s. </b><br><br> You might want to check that it was properly updated. ";

	public static final String informAdminOfUpdateSubject = "A name has been updated on your davening list. ";

	public static final String weeklyFileName = "Davening List %s";

	// putting one message first, in bold, with new line before other text.
	public static final String boldFirstMessage = "<h5>%s</h5>%s";

	// putting second message, in bold, on new line after other text.
	public static final String boldSecondMessage = "%s<h5>%s</h5>";

	public static final String confirmationEmailSubject = "Davening list submission";


	public static final String informAdminOfNewName = "The name: <br><b>%s <br> %s </b><br> has been added to the category: <br> <b>%s. </b><br> by <b>%s</b>"
			+ "<br><br> You might want to check that it was properly entered.  <br> <a href=\"%s\">Take me to the website</a> ";

	public static final String informAdminOfNewNameSubject = "A new name has been added to your davening list. ";

	public static final String weeklyAdminReminderSubject = "Davening list reminder: Send out the weekly list!";

	public static final String expiringNameSubject = "Davening List Confirmation";

	public static final String unsubscribeSubject = "Unsubscribe - action required";
	
	public static final String deleteNameSubject = "Name deleted: %s";
	
	public static final String deleteNameMessage = "We want to let you know that the name <b>%s</b> from category <b>%s</b> has been deleted from the davening list by <b>%s</b>.";

	// Links inserted to email allowing submitters to extend or delete names. URL
	// will change when uploaded to cloud
	public static String linkToExtendS = "extend/%s?email=%s";
	public static String linkToDeleteS = "delete/%s?email=%s";
	public static String linkToLoginC = "admin";
	public static String linkToSendListS = "admin/weeklylist";
	public static String linkToReviewWeeklyC = "admin/weekly";

	public static final String unsubscribeMessage = "We are sorry to see you go. <br> Click <a href=%s>HERE</a> to complete the process. <br><br> If you ever wish to join again, email the list admin at %s.";
}
