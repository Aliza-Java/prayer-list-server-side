//Helpful miscellaneous functions
package com.aliza.davening;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.ParashaRepository;
import com.aliza.davening.security.JwtUtils;
import com.aliza.davening.services.AdminService;
import com.aliza.davening.services.EmailSender;

import io.github.bonigarcia.wdm.WebDriverManager;

//A helper class for building long and winding messages and files

@Component
public class Utilities {

	public final String client = SchemeValues.client;

	@Value("${server.url}")
	public String server;

	@Value("${page.width}")
	public int width;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	DavenforRepository davenforRepository;

	@Autowired
	ParashaRepository parashaRepository;

	@Autowired
	AdminService adminService;

	@Autowired
	EmailSender emailSender;

	@Autowired
	JwtUtils jwtUtils;

	@Value("${admin.email}")
	String adminEmail;

	String linkToExtend = client + EmailScheme.linkToExtend;
	String linkToDelete = client + EmailScheme.linkToDelete;
	String linkToLogin = client + EmailScheme.linkToLogin;
	String linkToSendList = client + EmailScheme.linkToSendList;
	String linkToReviewWeekly = client + EmailScheme.linkToReviewWeekly;

	public File buildListImage(Category category, String pEnglish, String pHebrew, String fileName)
			throws EmptyInformationException {

		String weeklyHtml = createWeeklyHtml(category, pEnglish, pHebrew, false);

		String fileNameInFolder = "builtFiles/" + fileName;
		Path filePath = Paths.get(fileNameInFolder);

		// Ensure the directory exists
		try {
			Files.createDirectories(filePath.getParent());
		} catch (IOException e1) {
			System.out.println("Unable to create parent file 'builtFiles'.");
		}

		// Start with minimal window size
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--hide-scrollbars",
				"--window-size=670,1300");

		WebDriverManager.chromedriver().setup();
		WebDriver driver = new ChromeDriver(options);

		// Load HTML content
		driver.get("data:text/html;charset=utf-8," + weeklyHtml);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			System.out.println("There was an error with the thread sleeping: " + e.getMessage());
		}

		// Get [largest] EXACT content dimensions
		JavascriptExecutor js = (JavascriptExecutor) driver;
		long contentWidth = (long) js.executeScript("return Math.max(" + "document.body.scrollWidth, "
				+ "document.body.offsetWidth, " + "document.documentElement.clientWidth, "
				+ "document.documentElement.scrollWidth, " + "document.documentElement.offsetWidth" + ");");

		long contentHeight = (long) js.executeScript("return Math.max(" + "document.body.scrollHeight, "
				+ "document.body.offsetHeight, " + "document.documentElement.clientHeight, "
				+ "document.documentElement.scrollHeight, " + "document.documentElement.offsetHeight" + ");");

		// Set window to EXACT content size
		driver.manage().window().setSize(new Dimension((int) Math.max(contentWidth, 670),
				(int) Math.max(calculateDocHeight(category), (int) contentHeight)));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("There was an error with the thread sleeping: " + e.getMessage());
		}

		// Ensure we're at top-left
		js.executeScript("window.scrollTo(0, 0);");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("There was an error with the thread sleeping: " + e.getMessage());
		}

		System.out.println("Content Width: " + contentWidth);
		System.out.println("Content Height: " + contentHeight);

		try {
			ScreenshotHelper.captureScreenshot(driver, fileNameInFolder);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("There was an error with capturing the screenshot: " + e.getMessage());
		}

		driver.quit();
		return filePath.toFile();
	}

	public File buildListHtml(Category category, String pEnglish, String pHebrew, String fileName)
			throws EmptyInformationException {

		String weeklyHtml = createWeeklyHtml(category, pEnglish, pHebrew, false);

		String fileNameInFolder = "builtFiles/" + fileName;
		Path filePath = Paths.get(fileNameInFolder);

		try {
			Files.write(filePath, weeklyHtml.getBytes(StandardCharsets.UTF_8));
			System.out.println("File path: " + filePath.toAbsolutePath());
			System.out.println("Exists? " + Files.exists(filePath));
		} catch (Exception e) {
			System.out.println("There was an error creating the html file: " + e.getMessage());
		}

		return filePath.toFile();
	}

	public Category getNextCategory(Category current) {

		List<Category> allCategories = categoryRepository.findAll();

		int max = Integer.MAX_VALUE;
		int currentPosition = current.getCatOrder();
		Category nextCategory = null;

		/*
		 * Search for the category containing the very next cat_order - must be bigger
		 * than current position but lower than all other 'next' categories
		 */
		for (Category c : allCategories) {
			if (c.getCatOrder() > currentPosition && c.getCatOrder() < max) {
				nextCategory = c;
				max = c.getCatOrder();
			}
		}

		/*
		 * If didn't find the next in line, because current cat_order is the highest -
		 * restart rotation and search for lowest value.
		 */
		if (nextCategory == null) {
			int min = Integer.MAX_VALUE;
			for (Category c : allCategories) {
				if (c.getCatOrder() < min) {
					nextCategory = c;
					min = c.getCatOrder();
				}
			}
		}
		return nextCategory;
	}

	public Parasha getNextParasha(Parasha current) {
		List<Parasha> parashot = parashaRepository.findAll();
		Parasha nextParasha = parashot.stream().filter(p -> p.getId() == current.getId() + 1).findFirst().orElse(null);
		if (nextParasha == null)
			return parashot.get(0);
		else
			return nextParasha;
	}

	public Parasha findParasha(long parashaId) {
		return null;
	}

	// Builds the long and complex email message that gets sent every week to Admin
	// (to review and send list)
	// todo* in future: use StringBuilder (and in all text concats in this
	// program...)
	public String setWeeklyAdminReminderMessage() {

		Date expiry = new Date(System.currentTimeMillis() + (48 * 60 * 60 * 1000)); // 48 hours ahead
		String token = jwtUtils.generateDirectAdminToken(adminEmail, expiry);

		// Prepare buttons
		String button1 = createButton(String.format(linkToReviewWeekly, token, adminEmail.trim()), "#ffa200",
				"Review the list first");

		// This button includes also a parasha id and needs to be built
		String button2 = createButton(String.format(linkToSendList, token, adminEmail.trim()), "#32a842",
				"Send out the list");

		String buttonArea = "<table cellspacing='2' cellpadding='2'>	<tbody>	<tr> " + button1 + button2
				+ "</tr></tbody></table>";

		String message = "This is a reminder to send out the Hafrashat Challah Weekly Davening list."
				+ "<br><br> If you would like to review the lists first, please log in to the system first by clicking the orange button.  "
				+ "<br><br> Or, click the green button to go ahead and send out the list. "
				// Inserting the button tds to an html table and connecting them to the bottom
				// of the message
				+ buttonArea + "<br> Please note: " + jwtUtils.getExpiryNotice(expiry);

		return message;
	}

	public String setExpiringNameMessage(List<Davenfor> davenfors, String category) {
		String link = emailSender.getLinkCombinedConfirm(davenfors, category);

		StringBuilder sb = new StringBuilder(String.format(
				" <br>This week, the list being sent out will include names under the <b>%s</b> category.  <br>In order to keep our list relevant, please confirm the names you have submitted in this category.  <br> <br> You can do so by clicking the button below:",
				category));

		sb.append(createSingleButton(link, "#32a842", "Confirm my submitted names"));

		sb.append(
				"<br><b>Important: If we receive no response, unconfirmed names will automatically be removed from the list.</b>");

		sb.append("<br><br>Let us know if you need any help!");
		sb.append("<br>The Emek Hafrashat Challah Davening List team");

		return sb.toString();
	}

	public String setWasDeletedMessage(List<Davenfor> davenfors) {
		// TODO: make beginning and end like setExpiringNameMessage, then a repeating
		// question and button for each.
		// TODO: also would be nice to give feedback not in a new tab, rather an alert
		// or even better - a button that changes.

		String name;
		String buttonText;
		String categoryName = Category.getCategory(davenfors.get(0).getCategory()).getCname().getVisual();

		StringBuilder sb = new StringBuilder("Hi! <br>");
		sb.append(String.format(
				"We tried reaching out, but since we didn’t hear back, the following names in the <b>%s</b> category have been removed from our Davening list as part of our cleanup process. <br><br>",
				categoryName));
		sb.append(
				"No worries though — if you'd like to repost any of the following name(s) - although they might not be included in this week's list - you can do so easily by clicking the button with the name: <br>");
		sb.append("<table cellspacing='6' cellpadding='2'> <tbody> ");
		for (Davenfor d : davenfors) {
			name = d.getNameEnglish().trim().length() == 0 ? d.getNameHebrew() : d.getNameEnglish();
			buttonText = String.format("The name %s is still relevant", name);

			sb.append(
					"<tr><td style='padding-top: 20px; padding-bottom: 6px; font-size: 16px; font-family: Helvetica, Arial, sans-serif;'>");
			sb.append(createSingleButton(emailSender.getLinkToExtend(d), "#32a842", buttonText));
			sb.append("</td></tr>");
		}

		sb.append("Let us know if you need any help! <br>");
		sb.append("The Emek Hafrashat Challah Davening List team");

		return sb.toString();
	}

	public String createWeeklyHtml(Category category, String pEnglish, String pHebrew, boolean preview)
			throws EmptyInformationException {
		// todo*: make solution for too many names. Onto another page? two columns?
		StringBuilder stringBuilder = new StringBuilder();

		List<Davenfor> categoryDavenfors = davenforRepository.findAllDavenforByCategory(category.getCname().toString());

		if (categoryDavenfors.isEmpty()) {
			throw new EmptyInformationException("There are no names to daven for in this category. ");
		}

		// building standard html format: head and opening <body> tag
		String allowOverflow = preview ? "auto" : "hidden";
		stringBuilder.append(String.format(EmailScheme.htmlHead, allowOverflow));
		stringBuilder.append(EmailScheme.htmlBodyStart);

		stringBuilder.append(EmailScheme.tableStart);

		stringBuilder.append(
				String.format(EmailScheme.htmlNameRowInList, EmailScheme.inMemoryEnglish, EmailScheme.inMemoryHebrew));

		stringBuilder.append(String.format(EmailScheme.htmlNameRowInList, EmailScheme.hostagesAndSoldiersEnglish,
				EmailScheme.hostagesAndSoldiersHebrew));

		String hafrashatChallahEng = EmailScheme.hafrashatChallahEnglish;
		if (pEnglish != null && pEnglish.length() > 0)
			hafrashatChallahEng += " - ".concat(pEnglish);

		String hafrashatChallahHeb = EmailScheme.hafrashatChallahHebrew;
		if (pHebrew != null && pHebrew.length() > 0)
			hafrashatChallahHeb += " - ".concat(pHebrew);

		stringBuilder.append(String.format(EmailScheme.boldHtmlRow, hafrashatChallahEng, hafrashatChallahHeb));

		stringBuilder.append(String.format(EmailScheme.boldHtmlRow, category.getCname().getListName(),
				category.getCname().getHebName()));

		// Running through names, adding them in columns - English and Hebrew

		// banim category prints nusach first, and includes name and spouse name in one
		// box
		if (Category.isBanim(category.getCname().toString())) {
			stringBuilder.append(String.format(EmailScheme.htmlNameRowInList, EmailScheme.banimLineEnglish,
					EmailScheme.banimLineHebrew));

			// Inserting in one box both name and spouse name. If spouse name is null (it is
			// not mandatory), just put an empty string.
			for (Davenfor d : categoryDavenfors) {
				stringBuilder.append(String.format(EmailScheme.htmlBanimRowInList, d.getNameEnglish(),
						d.getNameEnglishSpouse() != null ? d.getNameEnglishSpouse() : "", d.getNameHebrew(),
						d.getNameHebrewSpouse() != null ? d.getNameHebrewSpouse() : ""));
			}
		}

		// All other categories print every name in a single row
		else {
			for (Davenfor d : categoryDavenfors) {
				stringBuilder
						.append(String.format(EmailScheme.htmlNameRowInList, d.getNameEnglish(), d.getNameHebrew()));
			}
		}

		// Adding line about next week's category
		Category nextCategory = getNextCategory(category);
		String nextWeekEng = String.format(EmailScheme.nextWeekEnglish, nextCategory.getCname().getListName());
		String nextWeekHeb = String.format(EmailScheme.nextWeekHebrew, nextCategory.getCname().getHebName());
		stringBuilder.append(String.format(EmailScheme.boldHtmlRow, nextWeekEng, nextWeekHeb));

		// Adding line to email with name and good news.
		stringBuilder.append(String.format(EmailScheme.sendGoodNewsMessage, adminEmail));

		// Closing table
		stringBuilder.append(EmailScheme.tableClose);

		// Closing <body> tag
		stringBuilder.append(EmailScheme.htmlBodyEnd);
		String finalString = stringBuilder.toString();

		return finalString;

	}

	// Creates a button in a table (like 2 side by side) according to varying
	// parameters sent in
	public String createButton(String link, String buttonColor, String buttonText) {
		return String.format(
				"<td style='-webkit-border-radius: 5px; -moz-border-radius: 5px; border-radius: 5px; color: #ffffff;' align='center' bgcolor=%s width='100px' height='40px'><a style='font-size: 16px; font-weight: bold; font-family: Helvetica, Arial, sans-serif; text-decoration: none; line-height: 40px;  display: inline-block;' href=%s target='_blank'><span style='color: #ffffff;'>%s</span></a></td>",
				buttonColor, link, buttonText);
	}

	// Creates a single button in a wider format according to varying parameters
	// sent in
	public String createSingleButton(String link, String buttonColor, String buttonText) {
		return String.format(
				"<div style='text-align: center; -webkit-border-radius: 5px; -moz-border-radius: 5px; border-radius: 5px; background-color:%s; padding: 5px 20px; width: fit-content;'><a style='color: white; font-size: 20px; font-weight: bold; font-family: Helvetica, Arial, sans-serif; text-decoration: none; display: inline-block;' href=%s target='_blank'>%s</a></div>",
				buttonColor, link, buttonText);
	}

	public String formatFileName(String weekName, String suffix) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
		String formattedNow = now.format(formatter);
		return weekName + "_" + formattedNow + "." + suffix;
	}

	public String toTitlecase(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		return Arrays.stream(input.split("\\s+"))
				.map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
				.collect(Collectors.joining(" "));
	}

	public long getDaysInMs(int daysNumber) {
		return daysNumber * 24 * 60 * 60 * 1000;
	}

	private int calculateDocHeight(Category category) {
		int davenforAmount = davenforRepository.findAllDavenforByCategory(category.getCname().toString()).size();
		int multiply = 1; // one line per davenfor.

		if (Category.isBanim(category.getCname().toString()))
			multiply = 2;

		// row height currently
		int rowHeight = 33;

		// base amount is 200, plus 10px buffer, plus more - testing the window height
		// buffer problem
		return 210 + (davenforAmount * rowHeight * multiply);
	}
}
