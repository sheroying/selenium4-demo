package com.selenium4.test;

import org.junit.Test;
import org.openqa.selenium.*;

import java.io.File;


public class BrowserWindowAndTabTest extends Selenium4Test {

    @Test
    public void testWindowAndTab() {
        driver.get("https://www.google.com");
        String oldWindow = driver.getWindowHandle();

        System.out.println("1. Go to new window");
        driver.switchTo().newWindow(WindowType.WINDOW);
        driver.navigate().to("https://www.google.co.in/maps?hl=en&tab=il");

        driver.switchTo().window(oldWindow);

        System.out.println("2. Go to new tab");
        driver.switchTo().newWindow(WindowType.TAB);
        driver.navigate().to("https://www.gumtree.com.au/t-login-form.html");
        WebElement submit = driver.findElement(By.id("btn-submit-login"));

//        String name = driver.switchTo().frame("sendFacebookEvent").findElements(By.name("title")).toString();
//        System.out.println("sendFacebookEvent iframe title" + name);

        System.out.println("3. Save element screenshot");
        File file = submit.getScreenshotAs(OutputType.FILE);
        ScreenshotUtils.saveScreenshot(file, "selenium ide logo.png");
    }
}
