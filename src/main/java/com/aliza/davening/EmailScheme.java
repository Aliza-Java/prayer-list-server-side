//Variables needed for email-related actions
package com.aliza.davening;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;

@Component
public class EmailScheme {

	public final static String client = SchemeValues.client;

	@Value("${server.url}")
	public String server;

	@Value("${admin.email}")
	public String adminEmail;

	public String getServer() {
		return server;
	}

	public static final String h5Header = "<h5>%s</h5>";

	public static final String inMemoryEnglish = "L\'iluy Nishmat Esther Naomi Bat Yaakov";

	public static final String inMemoryHebrew = "לעילוי נשמת אסתר נעמי בת יעקב ע\"ה";

	public static final String hostagesAndSoldiersEnglish = "For the hostages' and soldiers' safe return";

	public static final String hostagesAndSoldiersHebrew = "לשובם לשלום של החטופים והחיילים";

	public static final String hafrashatChallahEnglish = "Hafrashat Challah";

	public static final String hafrashatChallahHebrew = "הפרשת חלה";;
	public static final String banimLineEnglish = "Yehi ratzon she_____ yipakdu b'zera chaya v'kayama";

	public static final String banimLineHebrew = "יהי רצון ש___ יפקדו בזרע חיא וקימא";

	public static final String confirmationEmailTextLocation = "src/main/resources/static/confirmationEmailText.html";

	public static final String pageDivider = "<script>document.addEventListener(\"DOMContentLoaded\", function () { const rows = Array.from(document.querySelectorAll(\"table tr\")); const pageHeight = 900; let currentPage = document.createElement(\"div\"); currentPage.classList.add(\"page\"); document.body.appendChild(currentPage); let currentHeight = 0; rows.forEach(row => { const rowHeight = row.offsetHeight; if (currentHeight + rowHeight > pageHeight) { currentPage = document.createElement(\"div\"); currentPage.classList.add(\"page\"); document.body.appendChild(currentPage); currentHeight = 0; } currentPage.appendChild(row); currentHeight += rowHeight; }); });</script>";

//TODO*: move to separate file, to make editing easier.  All long htmls should be read from files
	public static final String htmlHead = "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><title>Emek Hafrashat Challah Davening List</title><style>.container {height:100%%; width: 650px; margin: 0 auto; } html, body {overflow-x: hidden; overflow-y: %s ; height: 100%%; margin: 0 auto;} @media print { .page { page-break-after: always; }  tr { page-break-inside: avoid; } }   body{text-align:center} table, td {padding: 10px; border-collapse: collapse; border: 1px solid black} h2, h3, h4, h5{text-align:center} html, body {width: 650px; margin: 0; padding: 0;}  tr{height:auto} td { vertical-align: top; padding: 3px;  }  .page {width: 650px; height: 100%%; font-size: 13.5pt; border: 1px solid black;  box-sizing: border-box;}</style></head>";

	public static final String htmlBodyStart = "<body><div class='container'><div class='page'>";

	public static final String tableStart = "<table style='height:100%;width:100%;border-collapse:collapse;'>";

	public static final String htmlNameRowInList = "<tr style='height: 28px'><td style='text-align:left'>%s</td><td style='text-align:right'>%s</td></tr>";

	public static final String htmlBanimRowInList = "<tr><td style='text-align:left'>%s<br>%s</td><td style='text-align:right'>%s<br>%s</td></tr>";

	public static final String boldHtmlRow = "<tr style='font-weight:bold'><td style='text-align:left'>%s</td><td style='text-align:right'>%s</td></tr>";

	public static final String tableClose = "</table>";

	public static final String nextWeekEnglish = "Next week %s";

	public static final String nextWeekHebrew = "שבוע הבא %s";

	public static final String sendGoodNewsMessage = "<tr><td colspan='2' style='text-align:center; vertical-align:bottom; height:100%%;'>Please email %s with names and good news!</td></tr>";

	public static final String htmlBodyEnd = "</div></div></body></html>";

	public static final String emailBodyStyle = "<div style='font-family: Arial, sans-serif; font-size: 18px; color: #000000; line-height: 2;'>%s</div>";

	// Subject includes the category
	public static final String weeklyEmailSubject = "Emek Hafrashat Challah Davening list for week of: %s";

	// Default subject in emails from admin
	public static final String adminMessageSubject = "Message from Emek Hafrashat Challah Davening list admin";

	public static final String userdeactivated = "We are confirming that your participation on the Hafrashat Challah Davening list has been deactivated. <br><br> You will no longer receive emails regarding the Hafrashat Challah Davening list. <br><br>If you think you did not deactivate your participation on the list, please contact your list admin immediately. ";

	public static final String userdeactivatedSubject = "You have been unsubscribed";

	public static final String userActivatedSubject = "Welcome to the Emek Hafrashat Challah Davening List";

	// Text appearing in Admin's email requesting to daven urgently (can be Banim or
	// any category)
	public static final String urgentDavenforEmailBanim = "Please daven now for <b>%s - %s</b> and <b>%s - %s</b>, for: <b>%s</b>. <br>";

	public static final String urgentDavenforEmailText = "Please daven now for <b>%s - %s</b> for: <b>%s<b/>. <br>";

	// The text in email informing admin of an update made to a name.
	public static final String informAdminOfUpdate = "<b>%s</b> has just updated the name: <br><b>%s <br> %s </b><br>  in the category: <br> <b>%s. </b><br><br> You might want to check that it was properly updated. ";

	public static final String informAdminOfUpdateSubject = "A name has been updated on your Davening list. ";

	public static final String weeklyFileName = "Hafrashat Challah Davening List %s";

	// putting one message first, in bold, with new line before other text.
	public static final String boldFirstMessage = "<h4>%s</h4>%s"; //todo* in future - make not bold

	// putting second message, in bold, on new line after other text.
	public static final String boldSecondMessage = "%s<h4>%s</h4>";

	public static final String confirmationEmailSubject = "Hafrashat Challah Davening list submission";

	public static final String informAdminOfPartialNewNameSubject = "Action Required: A partial new name has been added: '%s'";

	public static final String informAdminOfPartialEditNameSubject = "Action Required: The name '%s' has been edited, some info was removed";

	public static final String informAdminOfNewName = "The name: <br><b>%s <br> %s </b><br> has been added to the category: <br> <b>%s. </b><br> by <b>%s</b>"
			+ "<br><br> You might want to check that it was properly entered.  <br> <a href=\"%s\">Take me to the website</a> ";

	public static final String informAdminOfNewNameSubject = "A new name: %s has been added to your Davening list. ";

	public static final String weeklyAdminReminderSubject = "Hafrashat Challah Davening list reminder: Send out the weekly list!";

	public static final String expiringNameSubjectOne = "Action required - Is this name still relevant?";
	public static final String expiringNameSubjectMultiple = "Action required - Are these names still relevant?";

	public static final String unsubscribeSubject = "Unsubscribe - action required";

	public static final String deleteNameAdminSubject = "Name deleted: %s";

	public static final String deleteNameAdminMessage = "We want to let you know that the name <b>%s</b> from the <b>%s</b> category has been removed from the Hafrashat Challah Davening list by <b>%s</b>.";

	public static final String nameAutoDeletedUserSubject = "Missed Our Alerts? You Can Still Repost Your Davening Name (Internal code: %s)";

	public static final String nameAutoDeletedUserMessage = "Hi! <br> We tried reaching out, but since we didn’t hear back, the name for Davening <b>%s</b> has been removed from our <b>%s</b> category as part of our cleanup process. <br> "
			+ "No worries though — if you'd like to repost this name (although it might not be included in this week's list), you can do so easily by clicking the button below: <br>"
			+ "<div>%s </div>" + "Let us know if you need any help! <br>"
			+ "The Emek Hafrashat Challah Davening List team";

	public static final String unconfirmedSubject = "Names automatically deleted this week";

	public static final String submitEmailText = "Thank you for submitting <b>%s</b> to the Hafrashat Challah Davening list, in the <b>%s</b> category. <br> <br> In order to keep our lists relevant, you will receive reminder emails periodically. You can then choose to keep this name on the list or not.";
	// will change when uploaded to cloud
	public static String linkToExtend = "extend/%s?email=%s";
	public static String linkToDelete = "/guest/delete?id=%d&token=%s";
	public static String linkToLogin = "admin";
	public static String linkToSendList = "/direct-send?t=%s&email=%s";
	public static String linkToReviewWeekly = "/direct-preview?t=%s&email=%s";

	public static final String unsubscribeMessage = "We are sorry to see you go. <br> Click <a href=%s>HERE</a> to complete the process. <br><br> If you ever wish to join again, email the list admin at %s.";

	public static String createUnconfirmedMessage(String category, List<Davenfor> names) {
		StringBuilder sb = new StringBuilder();
		sb.append("The following names in the ");
		sb.append(category);
		sb.append(" have been automatically removed this week, as they were not confirmed by users: <br>");
		names.forEach(n -> sb
				.append((n.getNameEnglish().trim().length() == 0 ? n.getNameHebrew() : n.getNameEnglish()) + "<br>"));

		return sb.toString();
	}

	public static String setAdminAlertMessage(boolean isAdd, Davenfor df, String url) {

		StringBuffer sb = new StringBuffer("");
		sb.append("A name was ");
		sb.append(isAdd ? "added to " : "edited on ");
		sb.append("your Hafrashat Challah Davening list in the ");
		sb.append("<b>").append(Category.getCategory(df.getCategory()).getCname().getVisual()).append("</b> category.")
				.append("<br>");

		sb.append("However, ");
		sb.append(isAdd ? "it is partial. " : "the edits removed some important information.").append("<br>");
		sb.append("Currently, it has: ").append("<br>");
		sb.append("English name: <b>").append(getNameValue(df.getNameEnglish())).append("</b>").append("<br>");
		sb.append("Hebrew name: <b>").append(getNameValue(df.getNameHebrew())).append("</b>").append("<br>");
		if (Category.isBanim(df.getCategory())) {
			sb.append("English spouse name: <b>").append(getNameValue(df.getNameEnglishSpouse())).append("</b>")
					.append("<br>");
			sb.append("Hebrew spouse name: <b>").append(getNameValue(df.getNameHebrewSpouse())).append("</b>")
					.append("<br>");
		}

		sb.append("<br>");
		sb.append("You might want to fix it on the website.").append("<br>");
		sb.append("<a href=\"").append(url).append("\">Take me to the website</a>");

		return sb.toString();
	}

	private static String getNameValue(String name) {
		if (name.trim() == "")
			return "(blank)";
		else
			return name;
	}

}
