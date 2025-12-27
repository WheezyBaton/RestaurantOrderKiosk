package com.wheezybaton.kiosk_system.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/data-h2.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class FullApplicationFlowTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void safeClick(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        safeClick(element);
    }

    private void safeClick(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    @Test
    void completeOrderLifecycleTest() throws InterruptedException {
        driver.get(baseUrl + "/");

        safeClick(By.xpath("//form[.//input[@value='EAT_IN']]//button[@type='submit']"));
        wait.until(ExpectedConditions.urlContains("/menu"));
        safeClick(By.cssSelector("a.btn-warning[href*='/configure']"));

        wait.until(ExpectedConditions.urlContains("/configure"));
        safeClick(By.cssSelector("button.btn-success[type='submit']"));

        wait.until(ExpectedConditions.urlContains("/menu"));
        Thread.sleep(500);
        safeClick(By.cssSelector("a.btn-success[href*='/checkout']"));

        wait.until(ExpectedConditions.urlContains("/checkout"));
        safeClick(By.xpath("//form[contains(@action, '/order/pay')]//button[@type='submit']"));

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/success"),
                ExpectedConditions.urlContains("/order-success")
        ));
        WebElement orderNumberSpan = driver.findElement(By.cssSelector("h1 span, .display-1 span"));
        String orderNumberRaw = orderNumberSpan.getText().replaceAll("[^0-9]", "");

        System.out.println("Złożono zamówienie numer: " + orderNumberRaw);
        assertThat(orderNumberRaw).isNotEmpty();

        driver.manage().deleteAllCookies();
        driver.get(baseUrl + "/kitchen");
        if (driver.getCurrentUrl().contains("login")) {
            System.out.println("Logowanie do KUCHNI (user: kitchen)...");
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            usernameField.sendKeys("kitchen");
            driver.findElement(By.name("password")).sendKeys("kitchen");
            safeClick(By.cssSelector("button[type='submit']"));
        }
        wait.until(ExpectedConditions.urlContains("/kitchen"));
        WebElement orderHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h4[contains(text(), '#" + orderNumberRaw + "') or contains(text(), '" + orderNumberRaw + "')]")));
        assertThat(orderHeader.isDisplayed()).isTrue();
        WebElement readyButton = driver.findElement(By.xpath(
                "//h4[contains(text(), '" + orderNumberRaw + "')]/ancestor::div[contains(@class, 'card')]//form[contains(@action, 'promote')]//button"));
        safeClick(readyButton);

        driver.get(baseUrl + "/board");
        boolean foundOnBoard = false;
        for (int i = 0; i < 5; i++) {
            try {
                WebElement readyNumber = driver.findElement(
                        By.xpath("//span[contains(@class, 'ready-text') and contains(text(), '" + orderNumberRaw + "')]"));

                if (readyNumber.isDisplayed()) {
                    foundOnBoard = true;
                    break;
                }
            } catch (Exception e) {
                System.out.println("Tablica: Nie widzę jeszcze numeru " + orderNumberRaw + ". Odświeżam (" + (i + 1) + "/5)...");
                Thread.sleep(1000);
                driver.navigate().refresh();
            }
        }
        assertThat(foundOnBoard)
                .as("Numer zamówienia " + orderNumberRaw + " powinien pojawić się na tablicy w sekcji gotowych")
                .isTrue();

        driver.manage().deleteAllCookies();
        driver.get(baseUrl + "/admin");
        if (driver.getCurrentUrl().contains("login")) {
            System.out.println("Logowanie do ADMINA (user: admin)...");
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            usernameField.sendKeys("admin");
            driver.findElement(By.name("password")).sendKeys("admin");
            safeClick(By.cssSelector("button[type='submit']"));
        }
        wait.until(ExpectedConditions.urlContains("/admin"));

        WebElement dashboard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("container")));
        assertThat(dashboard.isDisplayed()).isTrue();
    }
}