import { test, expect } from '@playwright/test';

test.describe('Portal Access Request Flow', () => {
  test('should display login screen with clearance request link', async ({ page }) => {
    await page.goto('/');

    // Check header and primary login elements
    await expect(page.locator('h1')).toContainText('Welcome back');
    await expect(page.locator('#view-only-access-btn')).toBeVisible();
    await expect(page.locator('#open-request-access-btn')).toBeVisible();
  });

  test('should open clearance request form and submit directly without mailto', async ({ page }) => {
    await page.goto('/');

    // Navigate to request access form
    await page.locator('#open-request-access-btn').click();

    // Verify form subtitle and fields
    await expect(page.locator('h1')).toContainText('Request Access');
    await expect(page.locator('#request-subtitle')).toContainText('sent directly to the administrator');

    const nameInput = page.locator('input[name="name"]');
    const emailInput = page.locator('input[name="email"]');
    const reasonInput = page.locator('textarea[name="reason"]');
    const submitBtn = page.locator('#submit-access-btn');

    await expect(emailInput).toBeVisible();
    await expect(reasonInput).toBeVisible();
    await expect(submitBtn).toBeVisible();

    // Intercept Formsubmit and backend audit requests
    let formsubmitCalled = false;
    let formPayload: Record<string, unknown> = {};

    await page.route('https://formsubmit.co/ajax/**', async (route) => {
      formsubmitCalled = true;
      const postData = route.request().postDataJSON();
      formPayload = postData;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: 'true', message: 'Form submitted successfully.' }),
      });
    });

    await page.route('**/api/auth/request-access', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Request recorded.' }),
      });
    });

    // Fill the access form
    await nameInput.fill('Alex Mercer');
    await emailInput.fill('alex.mercer@fintech-demo.com');
    await reasonInput.fill('Conducting compliance audit on credit risk decision stream.');

    // Submit request
    await submitBtn.click();

    // Verify confirmation view rendered directly
    await expect(page.locator('h1')).toContainText('Access Requested');
    await expect(page.locator('.auth-card.success-state')).toBeVisible();
    await expect(page.locator('.subtitle')).toContainText('alex.mercer@fintech-demo.com');

    // Confirm mailto link container is NOT present
    await expect(page.locator('.mailto-link')).toHaveCount(0);
    await expect(page.locator('.direct-email-box')).toHaveCount(0);

    // Verify payload dispatched to Formsubmit
    expect(formsubmitCalled).toBe(true);
    expect(formPayload.email).toBe('alex.mercer@fintech-demo.com');
    expect(formPayload.name).toBe('Alex Mercer');
    expect(formPayload._replyto).toBe('alex.mercer@fintech-demo.com');

    // Test return to login button
    const returnBtn = page.locator('#return-to-login-btn');
    await expect(returnBtn).toBeVisible();
    await returnBtn.click();
    await expect(page.locator('h1')).toContainText('Welcome back');
  });
});
