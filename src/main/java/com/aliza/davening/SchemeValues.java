//General variables throughout the project (email variables are stored in EmailScheme)
package com.aliza.davening;

import org.springframework.beans.factory.annotation.Value;

public class SchemeValues {

	// a static field to stand for id's that have not been created.
	public static final long NON_EXIST = -1;

	// The default amount of days to wait after warning a submitter before deleting
	// a davenfor.
	public static int waitBeforeDeletion = 7;
	public static boolean adminNewNamePrompt = false;

	public final static String banimName = "Zera Shel Kayama";

	public static int adminId = 10;
	
	public final static String notAdminsEmail = "The email you provided is not associated with an admin.";
	
	public static String getLinkToExtend() {
		return EmailScheme.linkToExtend;
	}

	public static String getLinkToDelete() {
		return EmailScheme.linkToDelete;
	}

	public static String getLinkToLogin() {
		return EmailScheme.linkToLogin;
	}

	public static String getLinkToSendList() {
		return EmailScheme.linkToSendList;
	}

	public static String getLinkToReviewWeekly() {
		return EmailScheme.linkToReviewWeekly;
	}

	public static String getNotAdminsEmailMessage() {
		return notAdminsEmail;
	}
}