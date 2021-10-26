package com.aliza.davening;

public class SchemeValues {

	// a static field to stand for id's that have not been created.
	public static final long NON_EXIST = -1;

	// The default amount of days to wait after warning a submitter before deleting
	// a davenfor.
	public static int waitBeforeDeletion = 7;
	public static boolean adminNewNamePrompt = false;

	public static String banimName = "Zera Shel Kayama";

	public static int adminId = 10;

	// Links inserted to email allowing submitters to extend or delete names. URL
	// will change when uploaded to AWS
	private final static String linkToExtend = "http://localhost:8080/dlist/extend/%s?email=%s";
	private final static String linkToDelete = "http://localhost:8080/dlist/delete/%s?email=%s";

	private final static String linkToLogin = "http://localhost:4200/admin";
	private final static String linkToSendList = "http://localhost:8080/dlist/admin/weeklylist";
	private final static String linkToReviewWeekly = "http://localhost:4200/admin/weekly";

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