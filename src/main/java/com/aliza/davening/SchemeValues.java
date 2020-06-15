package com.aliza.davening;

public class SchemeValues {
	
	//a static field to stand for id's that have not been created.
	public static final long NON_EXIST = -1;
	
	//The default amount of days to wait after warning a submitter before deleting a davenfor.
	public static int waitBeforeDeletion = 7;
	
	// TODO: This is temporary till system is generic allowing groups.
	public static long adminId = 10;
	
	public static String banimName="Zera Shel Kayama";
	
	//Links inserted to email allowing submitters to extend or delete names.  URL will change when uploaded to AWS
	private final static String linkToExtend = "http://localhost:8080/dlist/extend/%s?email=%s";
	private final static String linkToDelete = "http://localhost:8080/dlist/delete/%s?email=%s";
	
	//TODO: here insert the login page for client side
	private final static String linkToLogin = "http://google.com";
	private final static String linkToSendList = "http://localhost:8080/dlist/admin/weeklylist/";
	
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
	

}