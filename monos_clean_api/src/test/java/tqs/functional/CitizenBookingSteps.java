package tqs.functional;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.*;

public class CitizenBookingSteps {

    private final SharedContext sharedContext;

    private Browser browser;
    private BrowserContext context;
    private Page page;

    public CitizenBookingSteps(SharedContext sharedContext) {
        this.sharedContext = sharedContext;
    }

    @Before
    public void setUp() {
        Playwright playwright = Playwright.create();
        browser = playwright.chromium().launch();
        context = browser.newContext();
        page = context.newPage();
    }

    @After
    public void tearDown() {
        browser.close();
    }

    @Given("I am on the new booking page")
    public void goToBookingPage() {
        page.navigate("http://localhost:5173");
    }

    @When("I fill in my contact info as {string}")
    public void fillContactInfo(String email) {
        page.fill("input[name='contactInfo']", email);
    }

    @When("I select select address as {string}")
    public void fillAdress(String address) {
        page.fill("input[name='address']", address);
    }

    @When("I select municipality {string}")
    public void selectMunicipality(String municipality) {
        page.waitForSelector("select[name='municipality'] option");
        page.selectOption("select[name='municipality']", municipality);
    }

    @When("I choose date {string} and time slot {string}")
    public void selectDateAndTime(String date, String timeSlot) {
        page.fill("input[name='collectionDate']", date);
        page.selectOption("select[name='timeSlot']", timeSlot);
    }

    @When("I describe items as {string}")
    public void describeItems(String description) {
        page.fill("textarea[name='description']", description);
    }

    @When("I submit the booking form")
    public void submitForm() {
        page.click("button[type='submit']");
        page.waitForSelector(".result.success");
    }

    @Then("I should see a success message with a booking token")
    public void verifySuccess() {
        String resultText = page.textContent(".result");
        assertTrue(resultText.contains("Booking created successfully!"));
        String token = resultText.split("token: ")[1].split("Save")[0];
        sharedContext.setBookingToken(token);
        System.out.println("Booking token: " + sharedContext.getBookingToken());
    }

    @When("I navigate to the check booking page")
    public void goToCheckPage() {
        page.click("a[href='/check-booking']");
    }

    @When("I enter the token and submit")
    public void enterToken() {
        page.fill("input[name='token']", sharedContext.getBookingToken());
        page.click("button[type='submit']");
        page.waitForSelector(".booking-details");
    }

    @Then("I should see the booking status as {string}")
    public void verifyStatus(String status) {
        String statusText = page.textContent(".status");
        assertTrue(statusText.contains(status));
    }
}