package tests;

import com.github.javafaker.Faker;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import org.uranus.configuration.LoadProperties;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


/// The TestBase class serves as the base class for all test classes in the application.
/// It contains common setup and teardown logic that is shared across multiple test classes.
/// Developers can inherit from this class to ensure consistent test execution.

public class TestBase {

    public static String token;
    SoftAssert softAssert = new SoftAssert();
    public static WebDriver webDriver;
    WebDriverWait webDriverWait;
    WebElement webElement;
    Faker faker = new Faker();



    // Sets up the necessary web driver and url product under test for the test to run.
    @BeforeClass
    public void startDriver() {
        String downloadDir = System.getProperty("user.dir") + "\\TestFiles";
        File dir = new File(downloadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safebrowsing.enabled", true);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);

        WebDriverManager.chromedriver().setup();
        webDriver = new ChromeDriver(options);
        webDriver.navigate().to(LoadProperties.env.getProperty("URL"));
        webDriver.manage().window().maximize();
    }


    @AfterClass
    public void endDriver() {
        webDriver.close();
    }

    public void assertIsEqual(By by, String expected) {
        if (expected != null) {
            webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(30));
            webDriverWait.until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOfElementLocated(by)));
            webElement = webDriver.findElement(by);
            webDriverWait.until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOf(webElement)));
            softAssert.assertEquals(webElement.getText(), expected);
        } else {
            return;
        }
    }

    public void assertFileIsEqual(String actualFilePath, String expectedFilePath) {
        if (actualFilePath.equals(expectedFilePath)) {
            System.out.println("FAILED DECRYPTION");
            System.out.println("[ASSERT PASS] The decrypted file is the same as the encrypted file. No new file was created.");
        } else {
            System.out.println("[ASSERT FAIL] Decryption created a different file:");
            System.out.println("Expected (Original): " + expectedFilePath);
            System.out.println("Actual (Decryption): " + actualFilePath);
        }
    }



}
