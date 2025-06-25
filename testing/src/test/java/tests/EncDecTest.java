package tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.uranus.configuration.LoadProperties;
import org.uranus.pages.EncryptionPage;
import org.uranus.pages.DecryptionPage;
import org.uranus.pages.HomePage;

public class EncDecTest extends TestBase{
    HomePage homePage;
    EncryptionPage encryptionPage;
    DecryptionPage decryptionPage;

    public static List<String> encPlaintextOutput = new ArrayList<>();
    public static List<String> encFileOutput = new ArrayList<>();
    public static List<String> decPlaintextOutput = new ArrayList<>();
    public static List<String> decFileOutput = new ArrayList<>();
    public static List<String> decPlaintextWrongKeyOutput = new ArrayList<>();
    public static List<String> decFileWrongKeyOutput = new ArrayList<>();

    @Test(priority = 0)
    public void SuccessfulEncryption() throws IOException, InterruptedException {
        homePage = new HomePage(webDriver);
        encryptionPage = new EncryptionPage(webDriver);

        homePage.login(LoadProperties.env.getProperty("ADMIN_EMAIL"), LoadProperties.env.getProperty("ADMIN_PASSWORD"));
        homePage.openService("encryption");

        assertIsEqual(EncryptionPage.encryptionPageId, "File & Text Encryption");
        encPlaintextOutput = encryptionPage.encryptPlainText(LoadProperties.env.getProperty("plainText"), LoadProperties.env.getProperty("encryptType"));

        System.out.println("\n------------------------------------------------------");
        System.out.println("PLAINTEXT ENCRYPTION");
        System.out.println("Key Save Toast MSG: " + encPlaintextOutput.get(0));
        System.out.println("Encryption Key Text: " + encPlaintextOutput.get(1));
        System.out.println("Encryption Toast MSG: " + encPlaintextOutput.get(2));
        System.out.println("Encrypted Output Text: " + encPlaintextOutput.get(3));

        assertIsEqualByStringOnly(encPlaintextOutput.get(0), "Key is Saved Successfully");
        assertIsEqualByStringOnly(encPlaintextOutput.get(2), "data encrypted successfully");

        String originalContent = "This is a test. Hopefully it passes!";
        Path tempFile = createTempFile(originalContent);
        String tempFilePath = tempFile.toAbsolutePath().toString();

        encFileOutput = encryptionPage.encryptFile(tempFilePath, LoadProperties.env.getProperty("encryptType"));

        System.out.println("\nFILE ENCRYPTION");
        System.out.println("Original File Path: " + encFileOutput.get(0));
        System.out.println("Key Save Toast MSG: " + encFileOutput.get(1));
        System.out.println("Encryption Key Text: " + encFileOutput.get(2));
        if (encFileOutput.get(3) != null) {
            System.out.println("Encrypted File Path: " + encFileOutput.get(3));
        } else {
            System.out.println("No file found in the downloads directory.");
        }
        System.out.println("------------------------------------------------------\n");

        assertIsEqualByStringOnly(encFileOutput.get(1), "Key is Saved Successfully");

        softAssert.assertAll();
    }

     @Test(priority = 1) // Bug Fix Test Case
     public void FailedDecryption() throws InterruptedException {
        homePage = new HomePage(webDriver);
        decryptionPage = new DecryptionPage(webDriver);

//         homePage.login(LoadProperties.env.getProperty("ADMIN_EMAIL"), LoadProperties.env.getProperty("ADMIN_PASSWORD"));
        homePage.openService("decryption");

        assertIsEqual(DecryptionPage.decryptionPageId, "File & Text Decryption");

        decPlaintextOutput = decryptionPage.decryptPlainText(encPlaintextOutput.get(3), LoadProperties.env.getProperty("decryptType"), encPlaintextOutput.get(1));

        System.out.println("\n------------------------------------------------------");
        System.out.println("PLAINTEXT DECRYPTION");
        System.out.println("Decryption Key Text: " + decPlaintextOutput.get(0));
        System.out.println("Decryption Toast MSG: " + decPlaintextOutput.get(1));
        System.out.println("Decrypted Output Text: " + decPlaintextOutput.get(2));

        assertIsEqualByStringOnly(decPlaintextOutput.get(1), "data encrypted successfully");
        assertIsEqualByStringOnly(LoadProperties.env.getProperty("plainText"), decPlaintextOutput.get(2));
        String filePath = encFileOutput.get(3);

        decFileOutput = decryptionPage.decryptFile(filePath, LoadProperties.env.getProperty("decryptType"), encFileOutput.get(2));

        System.out.println("\nFILE DECRYPTION (BUG)");
        System.out.println("Encrypted File Path: " + decFileOutput.get(0));
        System.out.println("Encrypted Key Text: " + decFileOutput.get(1));
        System.out.println("Decryption Toast MSG: " + decFileOutput.get(2));

        if (decFileOutput.get(3) != null) {
            System.out.println("Decrypted File Path: " + decFileOutput.get(3));
        } else {
            System.out.println("No file found in the downloads directory.");
        }
        assertFileIsEqual(decFileOutput.get(3), encFileOutput.get(3));
        System.out.println("------------------------------------------------------\n");

        assertIsEqualByStringOnly(decFileOutput.get(2), "Encryption key for Algorithm AES is not a valid key!");

        softAssert.assertAll();
     }

    @Test(priority = 2)
    public void FailedDecryptionWithWrongKey() throws InterruptedException {
        homePage = new HomePage(webDriver);
        decryptionPage = new DecryptionPage(webDriver);

        homePage.openService("encryption");
        homePage.openService("decryption");
        assertIsEqual(DecryptionPage.decryptionPageId, "File & Text Decryption");

        int plaintextKeyIndex = 1;
        int fileKeyIndex = 2;
        decPlaintextWrongKeyOutput = decryptionPage.decryptPlainTextWithSavedKey(encPlaintextOutput.get(3), LoadProperties.env.getProperty("decryptType"), fileKeyIndex);

        System.out.println("\n------------------------------------------------------");
        System.out.println("PLAINTEXT DECRYPTION WITH WRONG KEY");
        System.out.println("Decryption Key Text: " + decPlaintextWrongKeyOutput.get(0));
        System.out.println("Decryption Toast MSG: " + decPlaintextWrongKeyOutput.get(1));
        System.out.println("Decrypted Output Text: " + decPlaintextWrongKeyOutput.get(2));

        assertIsEqualByStringOnly(decPlaintextWrongKeyOutput.get(1), "Error in Decrypting text for AES algorithm please verify your key is correct");

        String filePath = encFileOutput.get(3);
        System.out.println("Encrypted File Path: " + filePath);

        decFileWrongKeyOutput = decryptionPage.decryptFileWithSavedKey(filePath, LoadProperties.env.getProperty("decryptType"), plaintextKeyIndex);

        System.out.println("\nFILE DECRYPTION WITH WRONG KEY");
        System.out.println("Encrypted File Path: " + decFileWrongKeyOutput.get(0));
        System.out.println("Encrypted Key Text: " + decFileWrongKeyOutput.get(1));
        System.out.println("Decryption Toast MSG: " + decFileWrongKeyOutput.get(2));

        if (decFileWrongKeyOutput.get(3) != null) {
            System.out.println("Decrypted File Path: " + decFileWrongKeyOutput.get(3));
        } else {
            System.out.println("No file found in the downloads directory.");
        }
        assertFileIsEqual(decFileWrongKeyOutput.get(3), encFileOutput.get(3));
        System.out.println("------------------------------------------------------\n");

        assertIsEqualByStringOnly(decFileWrongKeyOutput.get(2), "Error in Decrypting file for AES algorithm please verify your key is correct");

        softAssert.assertAll();
    }

    private void assertIsEqualByStringOnly(String actual, String expected) {
        if (!actual.equals(expected)) {
            System.out.println("Expected: \"" + expected + "\" Actual: \"" + actual + "\"");
        }
    }

    private Path createTempFile(String content) throws IOException {
        Path tempFile = Files.createTempFile("encryptTest", ".txt");
        Files.writeString(tempFile, content);
        return tempFile;
    }

}

