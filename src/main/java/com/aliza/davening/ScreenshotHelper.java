package com.aliza.davening;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import java.io.File;
import java.nio.file.Files;

import javax.imageio.ImageIO;

public class ScreenshotHelper {
    public static void captureScreenshot(WebDriver driver, String filePath) throws Exception {
//        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
//        Files.copy(screenshot.toPath(), new File(filePath).toPath());
//        System.out.println("Screenshot saved: " + filePath);
        
        Screenshot screenshot = new AShot()
        	    .shootingStrategy(ShootingStrategies.viewportPasting(1000))
        	    .takeScreenshot(driver);
        	ImageIO.write(screenshot.getImage(), "PNG", new File(filePath));
        
        
    }
}
