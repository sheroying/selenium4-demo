package com.selenium4.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.console.Console;
import org.openqa.selenium.devtools.network.Network;
import org.openqa.selenium.devtools.network.model.*;
import org.openqa.selenium.devtools.performance.Performance;
import org.openqa.selenium.devtools.performance.model.Metric;
import org.openqa.selenium.devtools.security.Security;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.openqa.selenium.devtools.console.Console.messageAdded;
import static org.openqa.selenium.devtools.network.Network.*;


public class DevToolsTest extends Selenium4Test {
    private DevTools devTools;
    private ChromiumDriver chromeDriver;

    @Before
    public void setupTest() {
        this.chromeDriver = initLocalChromeDriver();
        this.wait = new WebDriverWait(chromeDriver, ofSeconds(10));
        this.devTools = chromeDriver.getDevTools();
        this.devTools.createSession();
    }

    @After
    public void tearDown() {
        this.chromeDriver.quit();
    }

    @Test
    public void testUserAgent() {
        //WebToMobileView UserAgent
        String fakeAgent = "Mozilla/5.0 (Linux; Android 7.0; SM-G930V Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36";

        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        devTools.send(Network.setUserAgentOverride(fakeAgent, Optional.empty(), Optional.empty()));
        devTools.addListener(requestWillBeSent(),
                requestWillBeSent -> {
            System.out.println(String.format("Verify %s, actual User-Agent is %s", String.valueOf(fakeAgent.equals(requestWillBeSent.getRequest().getHeaders().get("User-Agent"))), requestWillBeSent.getRequest().getHeaders().get("User-Agent")));});
        chromeDriver.navigate().to("https://www.google.com");
    }

    @Test
    public void testAddingCustomHeaders() {

        //enable Network
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        //set custom header
        devTools.send(Network.setExtraHTTPHeaders(new Headers(ImmutableMap.of("customHeaderName", "123customHeaderValue"))));

        //add event listener to verify that requests are sending with the custom header
        devTools.addListener(requestWillBeSent(), requestWillBeSent ->
                System.out.println(String.format("request url is %s, request header value for customHeaderName is : %s", requestWillBeSent.getRequest().getUrl(), requestWillBeSent.getRequest().getHeaders().get("customHeaderName"))));

        chromeDriver.get("https://www.google.com");
    }

    @Test
    public void testUrlFilter() {
        //enable Network
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        //set blocked URL patterns
        devTools.send(Network.setBlockedURLs(ImmutableList.of("*.css", "*.png")));

        //add event listener to verify that css and png are blocked
        devTools.addListener(Network.loadingFailed(), loadingFailed -> {

            if (loadingFailed.getType().equals(ResourceType.STYLESHEET)) {
                Assert.assertEquals(loadingFailed.getBlockedReason(), BlockedReason.INSPECTOR);
            }

            else if (loadingFailed.getType().equals(ResourceType.IMAGE)) {
                Assert.assertEquals(loadingFailed.getBlockedReason(), BlockedReason.INSPECTOR);
            }

        });

        chromeDriver.get("https://apache.org");
    }

    @Test
    public void testBlockRequests() {
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        devTools.send(Network.setBlockedURLs(singletonList("*://*/*.css")));

        AtomicReference<BlockedReason> blockedReason = new AtomicReference<>();
        devTools.addListener(loadingFailed(), loadingFailed -> {
            if (loadingFailed.getType().equals(ResourceType.STYLESHEET)) {
                blockedReason.set(loadingFailed.getBlockedReason());
            }
        });
        chromeDriver.get("https://apache.org/");
        wait.until(d -> blockedReason.get() != null);
        assertEquals(blockedReason.get(), BlockedReason.INSPECTOR);

//        devTools.send(Network.enable(Optional.empty(),Optional.empty(),Optional.empty()));
//        devTools.addListener(
//                requestPaused(),
//                p -> {
//                    Assert.assertNotNull(p);
//                    devTools.send(failRequest(p.getRequestId(), ErrorReason.BLOCKEDBYCLIENT));
//                });
//        List<RequestPattern> patterns = new ArrayList<>();
//        patterns.add(new RequestPattern(
//                Optional.of("*://*.*"),
//                Optional.of(ResourceType.DOCUMENT),
//                Optional.of(RequestStage.REQUEST)));
//        devTools.send(enable(Optional.of(patterns), Optional.empty()));
//        chromeDriver.get("https://apache.org/");
    }

    @Test
    public void testInterceptingRequests() {
        //enable Network
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        //add listener to intercept request
        devTools.addListener(Network.requestIntercepted(),
                requestIntercepted ->
                {Assert.assertTrue(requestIntercepted.getRequest().getUrl().endsWith(".css"));
                    //Continue the intercept request
                    devTools.send(
                            Network.continueInterceptedRequest(requestIntercepted.getInterceptionId(),
                                    Optional.empty(),
                                    Optional.empty(),
                                    Optional.empty(), Optional.empty(),
                                    Optional.empty(),
                                    Optional.empty(), Optional.empty()));}
        );

        //set request interception only for css requests
//        RequestPattern requestPattern = new RequestPattern(Optional.of("*.css"), Optional.of(ResourceType.STYLESHEET), Optional.of(InterceptionStage.HEADERSRECEIVED)); //selenium alpha 4.0.6
        RequestPattern requestPattern = new RequestPattern("*.css", ResourceType.STYLESHEET, InterceptionStage.HEADERSRECEIVED);
        devTools.send(Network.setRequestInterception(ImmutableList.of(requestPattern)));

        chromeDriver.get("https://apache.org/");
    }

    @Test
    public void testConsoleLog() {
        String consoleMessage = "Hello Selenium 4";

        //enable Console
        chromeDriver.getDevTools().send(Console.enable());

        //add listener to verify the console message
        chromeDriver.getDevTools().addListener(messageAdded(), consoleMessageFromDevTools ->
                {
                System.out.println("TEST passed");
                Assert.assertEquals(true, consoleMessageFromDevTools.getText().equals(consoleMessage));}
                );
        chromeDriver.get("https://www.google.com");

        //execute JS - write console message
        chromeDriver.executeScript("console.log('" + consoleMessage + "');");
    }

    @Test
    public void testIgnoreCertificateErrors() throws InterruptedException {
        //Not ignore Certificate Errors
        chromeDriver.getDevTools().send(Security.enable());
        chromeDriver.getDevTools().send(Security.setIgnoreCertificateErrors(false));
        chromeDriver.get("https://expired.badssl.com/");
        ScreenshotUtils.saveScreenshot(chromeDriver.findElementByTagName("body").getScreenshotAs(OutputType.FILE), "IgnoreCertificateErrors-False.png");
        Thread.sleep(3L);

        //Ignore Certificate Errors
        chromeDriver.getDevTools().send(Security.setIgnoreCertificateErrors(true));
        chromeDriver.get("https://expired.badssl.com/");
        assertTrue(chromeDriver.getPageSource().contains("expired.<br>badssl.com"));
        ScreenshotUtils.saveScreenshot(chromeDriver.findElementByTagName("body").getScreenshotAs(OutputType.FILE), "IgnoreCertificateErrors-True.png");
        Thread.sleep(3L);
    }

    @Test
    public void verifyCacheDisabledAndClearCache() {
        devTools.send(enable(Optional.empty(), Optional.empty(), Optional.of(100000000)));
        chromeDriver.get("https://apache.org");

        devTools.send(setCacheDisabled(true));

        devTools.addListener(responseReceived(), responseReceived ->
                assertEquals(false, responseReceived.getResponse().getFromDiskCache().booleanValue()));

        chromeDriver.get("https://apache.org");
        devTools.send(clearBrowserCache());
    }

    @Test
    public void testEmulateNetworkConditions() {
        devTools.send(Network.enable(Optional.of(1000000), Optional.empty(), Optional.empty()));
        //Set 3G Emulate Network Conditions
        devTools.send(emulateNetworkConditions(false,100,200000,100000, Optional.of(ConnectionType.CELLULAR3G)));
        long startTime = System.currentTimeMillis();
        chromeDriver.navigate().to("https://www.swtestacademy.com");
        long endTime = System.currentTimeMillis();
        System.out.println("CELLULAR3G  -  Load time is "+ (endTime - startTime));

        //Set 4G Emulate Network Conditions
        devTools.send(emulateNetworkConditions(false,100,200000,100000, Optional.of(ConnectionType.CELLULAR4G)));
        long startTime2 = System.currentTimeMillis();
        chromeDriver.navigate().to("https://www.swtestacademy.com");
        long endTime2 = System.currentTimeMillis();
        System.out.println("CELLULAR4G  -  Load time is "+ (endTime2 - startTime2));
    }

    @Test(expected = WebDriverException.class)
    public void enableNetworkOffline() {
        devTools.send(Network.enable(Optional.of(100000000), Optional.empty(), Optional.empty()));
        devTools.send(emulateNetworkConditions(true, 100, 1000, 2000,
                Optional.of(ConnectionType.CELLULAR3G)));
        devTools.addListener(loadingFailed(), loadingFailed -> assertEquals(loadingFailed.getErrorText(), "net::ERR_INTERNET_DISCONNECTED"));
        chromeDriver.navigate().to("https://www.swtestacademy.com");
    }

    @Test
    public void testGetPerformanceMetrics() {
        devTools.send(Performance.enable());
        chromeDriver.get("https://www.gumtree.com.au");
        List<Metric> metrics = devTools.send(Performance.getMetrics());
        Objects.requireNonNull(metrics);
        Assert.assertFalse(metrics.isEmpty());
        metrics.forEach( metric -> {System.out.println(String.format("metric name: %s, metric value: %s \n", metric.getName(), metric.getValue()));});
    }

    @Test
    public void genericCrashBrowser() {
        devTools.send(new Command<>("Browser.crash", ImmutableMap.of()));
    }
}
