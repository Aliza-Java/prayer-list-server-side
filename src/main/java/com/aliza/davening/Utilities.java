//Helpful miscellaneous functions
package com.aliza.davening;

import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JEditorPane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.services.AdminService;

//A helper class for building long and winding messages and files

@Component
public class Utilities {

	@Value("${client.origin}")
	public String client;

	@Value("${server.url}")
	public String server;	

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	DavenforRepository davenforRepository;

	@Autowired
	AdminService adminService;

	// @Value("${admin.email}")
	private String adminEmail = "davening.list@gmail.com";

	String linkToExtendFromServer = server + EmailScheme.linkToExtendS;
	String linkToDeleteFromServer = server + EmailScheme.linkToDeleteS;
	String linkToLoginFromClient = client + EmailScheme.linkToLoginC;
	String linkToSendListFromServer = server + EmailScheme.linkToSendListS;
	String linkToReviewWeeklyFromClient = client + EmailScheme.linkToReviewWeeklyC;

	public File buildListImage(Category category, String weekName) throws IOException, EmptyInformationException {

		int imageWidth = EmailScheme.imageWidth;
		int imageHeight = EmailScheme.imageHeight;

		String weeklyHtml = createWeeklyHtml(category, weekName);

		String fileName = "builtFiles/" + weekName + "_" + LocalDate.now().toString() + ".png";
		Path filePath = Paths.get("builtFiles/" + weekName + "_" + LocalDate.now().toString() + ".png");

		if (!GraphicsEnvironment.isHeadless()) {
			BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDefaultConfiguration().createCompatibleImage(imageWidth, imageHeight);

			Graphics graphics = image.createGraphics();

			JEditorPane jep = new JEditorPane("text/html", weeklyHtml);
			jep.setSize(imageWidth, imageHeight);
			jep.print(graphics);

			// png seems to be better than jpeg, writes without a blotch behind the text
			ImageIO.write(image, "png", new File(fileName));
		} else {
			System.out.println("File name: " + fileName);
			System.out.println("File path: " + filePath);

			try {
				Files.write(filePath, weeklyHtml.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}

			return filePath.toFile();

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

	public Parasha findParasha(long parashaId) {
		return null;
	}

	// Builds the long and complex email message that gets sent every week to Admin
	// (to review and send list)
	public String setWeeklyAdminReminderMessage() {

		// Prepare buttons
		String button1 = createButton(linkToReviewWeeklyFromClient, "#ffa200", "Review the list first");

		// This button includes also a parasha id and needs to be built
		String linkWithParasha = linkToSendListFromServer;
		String button2 = createButton(linkWithParasha, "#32a842", "Send out the list");

		String buttonArea = "<table cellspacing='0' cellpadding='0'>	<tbody>	<tr> " + button1 + "<tr>" + button2
				+ "</tr></tbody></table>";

		String message = "This is a reminder to send out the weekly davening list."
				+ "<br><br> If you would like to review the lists first, please log in to the system first by clicking the orange button.  "
				+ "<br><br> Or, click the green button to go ahead and send out the list. "
				// Inserting the button tds to an html table and connecting them to the bottom
				// of the message
				+ buttonArea;
		return message;
	}

	public String setExpiringNameMessage(Davenfor davenfor) {

		// Building links that the buttons will refer to
		String extendLink = linkToExtendFromServer;
		String personalizedExtendLink = String.format(extendLink, davenfor.getId(), davenfor.getUserEmail());
		String personalizedDeleteLink = String.format(linkToDeleteFromServer, davenfor.getId(),
				davenfor.getUserEmail());

		// Creating the button 'components' as html tds
		String button1 = createButton(personalizedExtendLink, "#32a842", "Keep the name on the list");
		String button2 = createButton(personalizedDeleteLink, "#d10a3f", "Remove this name");

		// Inserting the button tds to an html table
		String buttonArea = "<table cellspacing='0' cellpadding='0'>	<tbody>	<tr> " + button1 + "<tr>" + button2
				+ "</tr></tbody></table>";

		// building the email message with the button area as a table at the bottom
		String message = String.format("We've been davening for %s for %s.", davenfor.getNameEnglish(),
				davenfor.getCategory())
				+ "  In order to keep our lists relevant, please confirm that the davening is still relevant. "
				+ "<br><br>  If the davening is no longer relevant for this list either simply ignore this email or click the big red remove button."
				+ buttonArea;

		return message;
	}

	public String createWeeklyHtml(Category category, String weekName) throws EmptyInformationException {
		StringBuilder stringBuilder = new StringBuilder();

		List<Davenfor> categoryDavenfors = davenforRepository.findAllDavenforByCategory(category.getCname().toString());

		if (categoryDavenfors.isEmpty()) {
			throw new EmptyInformationException("There are no names to daven for in this category. ");
		}

		// building standard html format: head and opening <body> tag
		stringBuilder.append(EmailScheme.htmlHead);
		stringBuilder.append(EmailScheme.htmlBodyStart);

		// building headlines and starting table
		stringBuilder.append(String.format(EmailScheme.simpleHeader, EmailScheme.inMemoryHebrew));
		stringBuilder.append(String.format(EmailScheme.simpleHeader, EmailScheme.inMemoryEnglish));
		stringBuilder.append(weekName);

		stringBuilder.append(
				String.format(EmailScheme.bilingualHeader, category.getCname(), category.getCname().getHebName()));
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
}
