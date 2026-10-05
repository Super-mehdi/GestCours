package ma.emi.backend.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StudentPortalE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;


    @BeforeEach
    void setUp(){
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown(){
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testBrowserLaunchesSuccessfully() {
        driver.get("http://localhost:4200/students");

        WebElement title = this.wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h2[contains(text(), 'Students & Enrolled Courses')]")
                )
        );
        assertTrue(title.isDisplayed());

        WebElement seededStudent = this.wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//span[contains(text(), 'Youssef El Amrani')]")
                )
        );
        assertTrue(seededStudent.isDisplayed());

    }

    @Test
    void shouldCreateNewStudentViaModalDialog() {
        driver.get("http://localhost:4200/students");

        WebElement addStudentBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(.,'Add Student')]")
                )
        );
        addStudentBtn.click();

        WebElement firstNameInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("input[formControlName='firstName']")
                )
        );
        firstNameInput.sendKeys("El mehdi");

        WebElement lastNameInput = driver.findElement(
                By.cssSelector("input[formControlName='lastName']")
        );

        lastNameInput.sendKeys("Amghary");

        WebElement emailInput = driver.findElement(
                By.cssSelector("input[formControlName='email']")
        );
        emailInput.sendKeys("amgharyelmehdi@gmail.com");

        WebElement submitBtn = driver.findElement(
                By.xpath("//mat-dialog-actions//button[@type='submit']")
        );
        submitBtn.click();

        WebElement newStudentSpan = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//span[contains(text(), 'El mehdi Amghary')]")
                )
        );
        assertTrue(newStudentSpan.isDisplayed());
    }


}
