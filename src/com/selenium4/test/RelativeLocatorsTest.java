package com.selenium4.test;

import org.junit.Test;
import org.openqa.selenium.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.support.locators.RelativeLocator.withTagName;

public class RelativeLocatorsTest extends Selenium4Test {

    final private String URL = "https://4dvanceboy.github.io/lambdatest/lambdasampleapp.html";

    @Test
    public void testRelativeLocatorsAbove() {
        driver.get(URL);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        WebElement lowest = driver.findElement(By.name("li5"));
        ScreenshotUtils.saveScreenshot(lowest.getScreenshotAs(OutputType.FILE), "li5-1.png");
        List<WebElement> elements = driver.findElements(withTagName("input").above(lowest));
        List<String> names = elements.stream().map(e -> e.getAttribute("name")).collect(Collectors.toList());
        assertThat(names).isEqualTo(Arrays.asList("li1", "li2", "li3", "li4"));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @Test
    public void testRelativeLocatorsCombineFilters() {
        driver.get(URL);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        WebElement lowest = driver.findElement(By.name("li5"));
        ScreenshotUtils.saveScreenshot(lowest.getScreenshotAs(OutputType.FILE), "li5-2.png");
        String name = driver.findElement(withTagName("input")
                                            .above(lowest)
                                            .below(By.name("li3")))
                            .getAttribute("name");
        assertEquals(name, "li4");

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

}
