package org.uranus.pages;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DecryptionPage extends PageBase{
    public DecryptionPage (WebDriver webDriver){
        super(webDriver);
    }

    public static By decryptionPageId = By.cssSelector("h2.secondary-txt.text-center.mb-0");
    By textToDecrypt = By.cssSelector("[formcontrolname='textToDecrypt']");
    By fileToDecrypt = By.cssSelector("input[type='file']");
    By decryptionTechnique = By.cssSelector("[formcontrolname='encryptionTechnique']");
    By dropDownKey = By.cssSelector("[formcontrolname='encryptionKey']");
    By keyTextArea = By.cssSelector("[formcontrolname='encryptionKeySelected']");
    By toast = By.cssSelector("div.p-toast-detail");
    By decryptBTn = By.cssSelector("button.btn.btn-primary.submit[type='submit']");

    List<String> plainTextOutputs = new ArrayList<>();
    List<String> fileOutputs = new ArrayList<>();

    public List<String> decryptPlainText(String plainText, String decryptionType, String encryptKey){
        type(textToDecrypt, plainText);
        type(keyTextArea, encryptKey);

        switch (decryptionType) {
            case "aes" -> select(decryptionTechnique, "aes");
            case "tripledes" -> select(decryptionTechnique, "triple_des");
            default -> select(decryptionTechnique, "blowfish");
        }

        plainTextOutputs.add(encryptKey); //[0]
        click(decryptBTn);
        String decryptionToast = waitForToast("data encrypted successfully");
        plainTextOutputs.add(decryptionToast); //[1]

        String decryptedOutput = getOutput();
        plainTextOutputs.add(decryptedOutput); //[2]

        return plainTextOutputs;
    }

    public List<String> decryptFile(String filePath, String decryptionType, String encryptKey) throws InterruptedException {
        webDriver.findElement(fileToDecrypt).sendKeys(filePath);
        type(keyTextArea, encryptKey);

        switch (decryptionType) {
            case "aes" -> select(decryptionTechnique, "aes");
            case "tripledes" -> select(decryptionTechnique, "triple_des");
            default -> select(decryptionTechnique, "blowfish");
        }

        fileOutputs.add(filePath); //[0]

        fileOutputs.add(encryptKey); //[1]
        click(decryptBTn);
        String failedDecryptionToast = waitForToast("Encryption key for Algorithm AES is not a valid key!");
        fileOutputs.add(failedDecryptionToast); //[2]

        Thread.sleep(2000);

        File decryptedFile = getLatestFile(System.getProperty("user.dir") + "\\TestFiles");
        assert decryptedFile != null;
        fileOutputs.add(decryptedFile.getAbsolutePath()); //[3]

        return fileOutputs;
    }


    public List<String> decryptPlainTextWithSavedKey(String plainText, String decryptionType, int savedKeyIndex) throws InterruptedException {
        type(textToDecrypt, plainText);

        Select keyDropdown = new Select(webDriver.findElement(dropDownKey));
//        System.out.println("Number of saved keys: " + keyDropdown.getOptions().size());
        keyDropdown.selectByIndex(savedKeyIndex);

        switch (decryptionType) {
            case "aes" -> select(decryptionTechnique, "aes");
            case "tripledes" -> select(decryptionTechnique, "triple_des");
            default -> select(decryptionTechnique, "blowfish");
        }

        String encryptKey = keyDropdown.getFirstSelectedOption().getText();
        plainTextOutputs.add(encryptKey); //[0]
        click(decryptBTn);

        String decryptionToast = waitForToast("Error in Decrypting text for AES algorithm please verify your key is correct");
        plainTextOutputs.add(decryptionToast); //[1]

        String decryptedOutput = getOutput();
        plainTextOutputs.add(decryptedOutput); //[2]

        return plainTextOutputs;
    }

    public List<String> decryptFileWithSavedKey(String filePath, String decryptionType, int savedKeyIndex) throws InterruptedException {
        webDriver.findElement(fileToDecrypt).sendKeys(filePath);

        Select keyDropdown = new Select(webDriver.findElement(dropDownKey));
        keyDropdown.selectByIndex(savedKeyIndex);

        switch (decryptionType) {
            case "aes" -> select(decryptionTechnique, "aes");
            case "tripledes" -> select(decryptionTechnique, "triple_des");
            default -> select(decryptionTechnique, "blowfish");
        }

        fileOutputs.add(filePath); //[0]

        String selectedKey = keyDropdown.getFirstSelectedOption().getText();
        fileOutputs.add(selectedKey); //[1]

        click(decryptBTn);
        String failedDecryptionToast = waitForToast("Error in Decrypting file for AES algorithm please verify your key is correct");
        fileOutputs.add(failedDecryptionToast); //[2]

        Thread.sleep(2000);

        File decryptedFile = getLatestFile(System.getProperty("user.dir") + "\\TestFiles");
        assert decryptedFile != null;
        fileOutputs.add(decryptedFile.getAbsolutePath()); //[3]

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

    private String getOutput() {
        return webDriver.findElement(By.cssSelector("textarea[formcontrolname='decryptedText']"))
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
