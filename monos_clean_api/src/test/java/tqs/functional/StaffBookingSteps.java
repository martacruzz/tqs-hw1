package tqs.functional;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.*;

public class StaffBookingSteps {

    private final SharedContext sharedContext;

    private Browser browser;
    private BrowserContext context;
    private Page page;

    public StaffBookingSteps(SharedContext sharedContext) {
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

    @Given("I am on the staff dashboard")
    public void goToStaffDashboard() {
        page.navigate("http://localhost:5173/staff");
    }

    @When("I update the status to {string}")
    public void updateStatus(String status) {
        String token = sharedContext.getBookingToken();
        if (token == null) {
            throw new IllegalStateException("No booking token found! Was the booking created?");
        }

        String selectSelector = "[data-testid='status-select-" + token + "']";
        page.selectOption(selectSelector, status);
    }

    @Then("the booking status should be {string}")
    public void verifyStatus(String expectedStatus) {
        String token = sharedContext.getBookingToken();
        String statusSelector = "[data-testid='current-status-" + token + "']";
        page.waitForSelector(statusSelector + ":has-text('" + expectedStatus + "')");
        String actual = page.textContent(statusSelector);
        assertTrue(actual.equals(expectedStatus));
    }
}