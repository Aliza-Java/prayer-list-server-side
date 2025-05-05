//Helpful miscellaneous functions
package com.aliza.davening;

import java.io.File;
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

	public File buildListImage(Category category, String weekName, String fileName) throws EmptyInformationException {

		String weeklyHtml = createWeeklyHtml(category, weekName, false);

		String fileNameInFolder = "builtFiles/" + fileName;
		Path filePath = Paths.get(fileNameInFolder);

		// moving from manual chrome extraction to automatic
//		String driverPath = null;
//		try {
//			driverPath = ChromeDriverUtil.extractChromeDriver();
//		} catch (Exception e) {
//			System.out.println("There was a problem extracting ChromeDriver: " + e.getMessage());
//			throw new EmailException("There was a problem creating the list image");
//		}
//		System.setProperty("webdriver.chrome.driver", driverPath);

		// Set up headless Chrome
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless", "--disable-gpu", "--window-size=600,1080");

		//WebDriverManager.chromedriver().driverVersion("136.0.0").setup(); //Add this version specification in case it doesn't detect version automatically
		WebDriverManager.chromedriver().setup();
		System.out.println("Inferring driver version automatically, should be at least 136");
		WebDriver driver = new ChromeDriver(options);
		
		// Load HTML and take a screenshot (JPEG)
		driver.get("data:text/html;charset=utf-8," + weeklyHtml);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			System.out.println("There was an error with the thread sleeping: " + e.getMessage());
		}

		JavascriptExecutor js = (JavascriptExecutor) driver;
		long height = (long) js.executeScript("return document.body.scrollHeight;");
		driver.manage().window().setSize(new Dimension(width, (int) height)); // Adjust height dynamically

		System.out.println("Computed Page Height: " + height);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("There was an error with the thread sleeping: " + e.getMessage());
		}

		try {
			ScreenshotHelper.captureScreenshot(driver, fileNameInFolder);
		} catch (Exception e) {
			System.out.println("There was an error with capturing the screenshot: " + e.getMessage());
		}

		driver.quit();

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

		Date expiry = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000); // 24 hours ahead
		String token = jwtUtils.generateDirectAdminToken(adminEmail, expiry);

		// Prepare buttons
		String button1 = createButton(String.format(linkToReviewWeekly, token, adminEmail.trim()), "#ffa200",
				"Review the list first");

		// This button includes also a parasha id and needs to be built
		String button2 = createButton(String.format(linkToSendList, token, adminEmail.trim()), "#32a842",
				"Send out the list");

		String buttonArea = "<table cellspacing='2' cellpadding='2'>	<tbody>	<tr> " + button1 + "<tr>" + button2
				+ "</tr></tbody></table>";

		String message = "This is a reminder to send out the weekly davening list."
				+ "<br><br> If you would like to review the lists first, please log in to the system first by clicking the orange button.  "
				+ "<br><br> Or, click the green button to go ahead and send out the list. "
				// Inserting the button tds to an html table and connecting them to the bottom
				// of the message
				+ buttonArea + "<br> Please note: " + jwtUtils.getExpiryNotice(expiry);

		return message;
	}

	public String setExpiringNameMessage(Davenfor davenfor) {

		// Creating the button 'components' as html tds
		String button1 = createButton(emailSender.getLinkToExtend(davenfor), "#32a842", "Keep the name on the list");
		String button2 = createButton(emailSender.getLinkToDelete(davenfor), "#d10a3f", "Remove this name");

		// Inserting the button tds to an html table
		String buttonArea = "<table cellspacing='2' cellpadding='2'>	<tbody>	<tr> " + button1 + "<tr>" + button2
				+ "</tr></tbody></table>";

		// building the email message with the button area as a table at the bottom
		String message = String.format("We've been davening for %s for %s.", davenfor.getNameEnglish(),
				davenfor.getCategory())
				+ "  In order to keep our lists relevant, please confirm that the davening is still relevant. "
				+ "<br><br>  If the davening is no longer relevant for this list either simply ignore this email or click the big red remove button."
				+ buttonArea;

		return message;
	}

	public String createWeeklyHtml(Category category, String weekName, boolean preview)
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

		// building headlines and starting table
		stringBuilder.append(String.format(EmailScheme.h5Header, EmailScheme.inMemory));

		stringBuilder.append(String.format(EmailScheme.categoryAndParashaHeader, weekName, category.getCname(),
				category.getCname().getHebName()));

		stringBuilder.append(EmailScheme.tableStart);

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

		// Closing table
		stringBuilder.append(EmailScheme.tableClose);

		// Adding line about next week's category
		Category nextCategory = getNextCategory(category);
		stringBuilder.append(String.format(EmailScheme.nextWeekCategory, nextCategory.getCname(),
				nextCategory.getCname().getHebName()));

		// Adding line to email with name and good news.
		stringBuilder.append(String.format(EmailScheme.sendGoodNewsMessage, adminEmail));

		// Closing <body> tag
		stringBuilder.append(EmailScheme.htmlBodyEnd);
		String finalString = stringBuilder.toString();

		return finalString;

	}

	// Creates a button according to varying parameters sent in
	private String createButton(String link, String buttonColor, String buttonText) {

		return String.format(
				"<td style='-webkit-border-radius: 5px; -moz-border-radius: 5px; border-radius: 5px; color: #ffffff; display: block;' align='center' bgcolor=%s width='300' height='40'><a style='font-size: 16px; font-weight: bold; font-family: Helvetica, Arial, sans-serif; text-decoration: none; line-height: 40px;  display: inline-block;' href=%s><span style='color: #ffffff;'>%s</span></a></td>",
				buttonColor, link, buttonText);

	}

	public String formatFileName(String weekName) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
		String formattedNow = now.format(formatter);
		return weekName + "_" + formattedNow + ".png";
	}

	public String toTitlecase(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		return Arrays.stream(input.split("\\s+"))
				.map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
				.collect(Collectors.joining(" "));
	}
}
