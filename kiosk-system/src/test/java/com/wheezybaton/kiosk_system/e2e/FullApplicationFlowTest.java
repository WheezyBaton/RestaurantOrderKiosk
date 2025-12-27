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
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    @Test
    void completeCustomerOrderFlowTest() throws InterruptedException {
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

        WebElement orderNumberSpan = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("h1 span, .display-1 span")));

        String orderNumber = orderNumberSpan.getText().replaceAll("[^0-9]", "");
        System.out.println("TEST: Proces klienta zakończony pomyślnie. Numer zamówienia: " + orderNumber);

        assertThat(orderNumber).isNotEmpty();
        assertThat(Integer.parseInt(orderNumber)).isGreaterThan(0);
    }
}