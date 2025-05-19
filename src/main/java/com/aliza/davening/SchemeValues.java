//General variables throughout the project (email variables are stored in EmailScheme)
package com.aliza.davening;

public class SchemeValues {

	// a static field to stand for id's that have not been created.
	public static final long NON_EXIST = -1;
	
	public static final String client = "http://localhost:4200";

	// The default amount of days to wait after warning a submitter before deleting
	// a davenfor.
	public static int waitBeforeDeletion = 7;
	public static boolean adminNewNamePrompt = false;

	public final static String banimName = "Banim";

	public static int adminId = 10;

	public final static String notAdminsEmail = "The email you provided is not associated with an admin.";

	public static String getNotAdminsEmailMessage() {
		return notAdminsEmail;
	}

	public final static String unsubscribeText = "The email %s has been successfully unsubscribed";
}