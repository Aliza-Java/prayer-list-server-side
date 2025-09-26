package com.aliza.davening;

import java.io.File;

import javax.imageio.ImageIO;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class ScreenshotHelper {
	public static void captureScreenshot(WebDriver driver, String filePath) throws Exception {
		// Scroll to top
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");

		Screenshot screenshot = new AShot().shootingStrategy(ShootingStrategies.simple()) // No viewport pasting
				.takeScreenshot(driver);

		ImageIO.write(screenshot.getImage(), "PNG", new File(filePath));
	}
}