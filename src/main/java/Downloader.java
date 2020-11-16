import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Downloader {
    private Downloader() {
    }

    public static void downloadFromTxt(String file) throws InterruptedException {
//        List<String> priceUrls = new ArrayList<>();
        List<String> priceUrls = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replaceAll("\\s+", "");
                String url = String.format("https://www.cophieu68.vn/export/metastock.php?id=%s&df=&dt=", line);
                priceUrls.add(url);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        System.setProperty("webdriver.chrome.driver", "chromedriver");
        ChromeOptions options = new ChromeOptions();

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.prompt_for_download", false);
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200", "--ignore-certificate-errors");
        options.setExperimentalOption("prefs", prefs);

        RemoteWebDriver driver = new ChromeDriver(options);

        driver.get("https://www.cophieu68.vn/account/login.php");
        Thread.sleep(1000);
        WebElement username = driver.findElement(new By.ByXPath("/html/body/div[6]/table/tbody/tr/td[5]/form/table/tbody/tr[4]/td[2]/input"));
        WebElement password = driver.findElement(new By.ByXPath("/html/body/div[6]/table/tbody/tr/td[5]/form/table/tbody/tr[5]/td[2]/input"));
        WebElement submitBtn = driver.findElement(new By.ByXPath("/html/body/div[6]/table/tbody/tr/td[5]/form/table/tbody/tr[7]/td[2]/input"));


        try (InputStream input = Downloader.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            username.sendKeys(prop.getProperty("username"));
            password.sendKeys(prop.getProperty("password"));
            submitBtn.click();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Thread.sleep(1000);

        priceUrls.forEach(driver::get);

        Thread.sleep(1000);
    }
}
