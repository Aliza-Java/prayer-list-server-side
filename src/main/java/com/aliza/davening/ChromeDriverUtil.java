package com.aliza.davening;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ChromeDriverUtil {
    public static String extractChromeDriver() throws Exception {
        // Get ChromeDriver from resources
        InputStream inputStream = ChromeDriverUtil.class.getClassLoader().getResourceAsStream("chromedriver.exe");
        if (inputStream == null) {
            throw new RuntimeException("ChromeDriver not found in resources!");
        }

        // Create a temporary file to store the extracted ChromeDriver
        File tempFile = File.createTempFile("chromedriver", ".exe");
        tempFile.deleteOnExit(); // Ensure it's deleted when the app exits

        // Copy from resources to temporary file
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return tempFile.getAbsolutePath();
    }
}
