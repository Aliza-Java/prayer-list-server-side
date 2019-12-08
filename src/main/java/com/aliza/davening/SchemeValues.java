package com.aliza.davening;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SchemeValues {
	public static int waitBeforeDeletion = 7;
	public static String inMemoryHebrew = "לעילוי נשמת אסתר נעמי בת יעקב ע\"ה";
	public static String inMemoryEnglish = "L\'iluy Nishmat Esther Naomi Bat Yaakov";
	public static String confirmationEmailTextLocation = "src/main/resources/static/confirmationEmailText.html";

	// TODO: This is temporary till system is generic allowing groups.
	public static long adminId = 5;

//	public static String createConfirmationEmailText(Davenfor davenfor) {
//		return String.format("Thank you for submitting %s to the Davening List, in the %s category.  <br>"
//				+ "In order to keep our lists relevant, you will receive emails like this once in a while"
//				+ " to confirm that the davening is still relevant.  <br>"
//				+ "When you get these emails, please click the big green button to confirm the name on the list.  "
//				+ "When the name is no longer relevant for this list, simply ignore the request to confirm "
//				+ "or go back to any of these emails and click the big red Remove button. ", davenfor.getNameEnglish(), davenfor.getCategory().getEnglish());
//	}

	public static String createConfirmationEmailText() throws IOException {

		return new String(Files.readAllBytes(Paths.get(confirmationEmailTextLocation)), StandardCharsets.UTF_8);

	}

}