package org.uranus.pages;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EncryptionPage extends PageBase{
    public EncryptionPage (WebDriver webDriver){
        super(webDriver);
    }

    public static By encryptionPageId = By.cssSelector("h2.secondary-txt.text-center.mb-0");
    By textToEncrypt = By.cssSelector("[formcontrolname='textToEncrypt']");
    By fileToEncrypt = By.cssSelector("input[type='file']");
    By encryptionTechnique = By.cssSelector("[formcontrolname='encryptionTechnique']");
    By autoGenKey = By.cssSelector("button.btn.btn-secondary.fs-10");
    By keyTextArea = By.cssSelector("[formcontrolname='encryptionKey']");
    By toast = By.cssSelector("div.p-toast-detail");
    By enKeySave = By.cssSelector(".p-element:nth-child(2) > svg");
    By encryptBTn = By.cssSelector("button.btn.btn-primary.submit[type='submit']");

    List<String> plainTextOutputs = new ArrayList<>();
    List<String> fileOutputs = new ArrayList<>();

    public List<String> encryptPlainText(String plainText, String encryptionType){
        type(textToEncrypt, plainText);

        switch (encryptionType) {
            case "aes" -> select(encryptionTechnique, "aes");
            case "tripledes" -> select(encryptionTechnique, "triple_des");
            default -> select(encryptionTechnique, "blowfish");
        }

        click(autoGenKey);
        click(enKeySave);

        String keySavedToast = waitForToast("Key is Saved Successfully");
        plainTextOutputs.add(keySavedToast);

        String key = getKey();
        plainTextOutputs.add(key);

        click(encryptBTn);
        String encryptionToast = waitForToast("data encrypted successfully");
        plainTextOutputs.add(encryptionToast);

        String encryptedOutput = getOutput();
        plainTextOutputs.add(encryptedOutput);

        return plainTextOutputs;
    }

    public List<String> encryptFile(String filePath, String encryptionType) throws InterruptedException {
        webDriver.findElement(fileToEncrypt).sendKeys(filePath);

        switch (encryptionType) {
            case "aes" -> select(encryptionTechnique, "aes");
            case "tripledes" -> select(encryptionTechnique, "triple_des");
            default -> select(encryptionTechnique, "blowfish");
        }

        fileOutputs.add(filePath);
        click(autoGenKey);
        click(enKeySave);
        String keySavedToast = waitForToast("Key is Saved Successfully");
        fileOutputs.add(keySavedToast);
        String key = getKey();
        fileOutputs.add(key);
        click(encryptBTn);

        Thread.sleep(2000);

        File encryptedFile = getLatestFile(System.getProperty("user.dir") + "\\TestFiles");
        assert encryptedFile != null;
        fileOutputs.add(encryptedFile.getAbsolutePath());

        return fileOutputs;
    }

    private String waitForToast(String expectedToast) {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        Boolean toastAppeared = wait.until(driver -> {
            try {
                String toastText = driver.findElement(toast).getText().trim();
                return toastText.equals(expectedToast);
            } catch (Exception e) {
                return false;
            }
        });

        if (toastAppeared) {
            return webDriver.findElement(toast).getText().trim();
        } else {
            throw new RuntimeException("Toast with text '" + expectedToast + "' not found within 10 seconds");
        }
    }

    private String getKey() {
        return webDriver.findElement(keyTextArea).getAttribute("value").trim();
    }

    private String getOutput() {
        return webDriver.findElement(By.cssSelector("textarea[formcontrolname='encryptedText']"))
                .getAttribute("value").trim();
    }

    private File getLatestFile(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File latestFile = files[0];
        for (File file : files) {
            if (file.lastModified() > latestFile.lastModified()) {
                latestFile = file;
            }
        }
        return latestFile;
    }
}
