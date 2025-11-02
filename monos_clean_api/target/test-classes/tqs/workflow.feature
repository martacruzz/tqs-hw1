Feature: Full E2E workflow

  Scenario: Successful booking creation and status check
    # citizen
    Given I am on the new booking page
    When I fill in my contact info as "user@example.com"
    And I select select address as "Main Str."
    And I select municipality "Lisboa"
    And I choose date "2025-11-10" and time slot "Morning (8:00-12:00)"
    And I describe items as "Old sofa"
    And I submit the booking form
    Then I should see a success message with a booking token
    When I navigate to the check booking page
    And I enter the token and submit
    Then I should see the booking status as "RECEIVED"

    # staff
    Given I am on the staff dashboard
    When I update the status to "ASSIGNED"
    Then the booking status should be "ASSIGNED"