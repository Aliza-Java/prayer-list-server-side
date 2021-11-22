package com.aliza.davening;

import org.springframework.beans.factory.annotation.Value;

public class SchemeValues {

	// a static field to stand for id's that have not been created.
	public static final long NON_EXIST = -1;

	// The default amount of days to wait after warning a submitter before deleting
	// a davenfor.
	public static int waitBeforeDeletion = 7;
	public static boolean adminNewNamePrompt = false;

	public static String banimName = "Zera Shel Kayama";

	public static int adminId = 10;
	
	@Value("${client.origin}")
	public static String client;

	// Links inserted to email allowing submitters to extend or delete names. URL
	// will change when uploaded to cloud
	private final static String linkToExtend = "http://localhost:5000/dlist/extend/%s?email=%s";
	private final static String linkToDelete = "http://localhost:5000/dlist/delete/%s?email=%s";

	private final static String linkToLogin = client + "/admin";
	private final static String linkToSendList = "http://localhost:5000/dlist/admin/weeklylist";
	private final static String linkToReviewWeekly = client + "/admin/weekly";

	private final static String notAdminsEmail = "The email you provided is not associated with an admin.";
	
	public static String getLinkToExtend() {
		return linkToExtend;
	}

	public static String getLinkToDelete() {
		return linkToDelete;
	}

	public static String getLinkToLogin() {
		return linkToLogin;
	}

	public static String getLinkToSendList() {
		return linkToSendList;
	}

	public static String getLinkToReviewWeekly() {
		return linkToReviewWeekly;
	}

	public static String getNotAdminsEmailMessage() {
		return notAdminsEmail;
	}
}