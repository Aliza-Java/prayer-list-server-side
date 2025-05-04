package com.aliza.davening;

import java.io.File;

import javax.imageio.ImageIO;

import org.openqa.selenium.WebDriver;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

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
