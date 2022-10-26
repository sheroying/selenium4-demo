package com.selenium4.test;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.time.Duration.ofSeconds;

public class Selenium4Test {
    public WebDriver driver;
    public WebDriverWait wait;
    public JavascriptExecutor javascriptExecutor;


    @Before
    public void setupTest() {
        this.driver = initLocalChromeDriver();
        this.wait = new WebDriverWait(driver, ofSeconds(10));
        this.javascriptExecutor = (JavascriptExecutor) driver;
        driver.manage().window().maximize();
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    public ChromeDriver initLocalChromeDriver() {

        System.setProperty("webdriver.chrome.driver", "/Users/yoyyu/Downloads/chromedriver");
        ChromeDriver driver = new ChromeDriver();
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setBrowserName("chrome");
        driver.manage().window().maximize();
        return driver;
    }

    public RemoteWebDriver initGridChromeDriver() {
        RemoteWebDriver driver = null;
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setBrowserName("chrome");
        try {
            driver = new RemoteWebDriver(new URL("https://selenium-hub.qa.au.ecg.so/wd/hub"), caps);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            // au selenium grid: https://selenium-hub.qa.au.ecg.so/wd/hub    http://10.224.70.138:4444/wd/hub
        } catch (MalformedURLException e) {
            Logger.getLogger("create RemoteWebDriver failed!");
            e.printStackTrace();
        }
        driver.manage().window().maximize();
        return driver;
    }
}
