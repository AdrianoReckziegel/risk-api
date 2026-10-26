import { test, expect, Page } from '@playwright/test';

const MOCK_CUSTOMERS = [
  { id: 101, externalId: 'CUST-8401', name: 'Sarah Jenkins', email: 's.jenkins@example.com', birthDate: '1988-04-12', creditScore: 785, annualIncome: 125000, createdAt: '2026-09-15T10:00:00Z' },
  { id: 102, externalId: 'CUST-8402', name: 'Marcus Vance', email: 'm.vance@example.com', birthDate: '1992-09-23', creditScore: 690, annualIncome: 74000, createdAt: '2026-09-18T11:30:00Z' },
  { id: 103, externalId: 'CUST-8403', name: 'Elena Rostova', email: 'e.rostova@example.com', birthDate: '1981-11-05', creditScore: 820, annualIncome: 160000, createdAt: '2026-09-20T14:15:00Z' },
];

const MOCK_ASSESSMENTS = [
  { id: 501, customerId: 101, assessmentDate: '2026-10-04', riskScore: 820, riskLevel: 'LOW', decision: 'APPROVED', createdAt: '2026-10-04T10:15:00Z' },
  { id: 502, customerId: 102, assessmentDate: '2026-10-04', riskScore: 675, riskLevel: 'MEDIUM', decision: 'REVIEW', createdAt: '2026-10-04T11:20:00Z' },
  { id: 503, customerId: 103, assessmentDate: '2026-10-03', riskScore: 480, riskLevel: 'HIGH', decision: 'REJECTED', createdAt: '2026-10-03T14:40:00Z' },
];

const MOCK_METRICS = {
  totalVisits: 148,
  uniqueVisitors: 64,
  viewOnlyVisits: 29,
  topReferrers: {
    LinkedIn: 58,
    'Direct Navigation': 41,
    GitHub: 26,
    'Portfolio (adreck.ca)': 15,
    'Google Search': 8,
  },
  topLocations: {
    'Toronto, ON (Canada)': 54,
    'Montreal, QC (Canada)': 26,
    'Ottawa, ON (Canada)': 21,
    'New York / Eastern (US)': 18,
    'San Francisco / LA (US)': 14,
  },
  topDevices: { Desktop: 102, Mobile: 40, Tablet: 6 },
  recentVisits: [],
};

async function setupApiMocks(page: Page) {
  await page.route('**/api/metrics/track', async (route) => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: '{}' });
  });

  await page.route('**/api/auth/login', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ token: 'mock-test-jwt-token' }),
    });
  });

  await page.route('**/api/customers', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(MOCK_CUSTOMERS),
    });
  });

  await page.route('**/api/risk-assessments', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(MOCK_ASSESSMENTS),
    });
  });

  await page.route('**/api/metrics/summary', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(MOCK_METRICS),
    });
  });
}

test.describe('Dashboard & Charts Across Viewports', () => {
  test.beforeEach(async ({ page }) => {
    await setupApiMocks(page);
  });

  test('should display View-Only Mode with preview banner and sample charts', async ({ page }) => {
    await page.goto('/');
    await page.locator('#view-only-access-btn').click();

    // Confirm view-only mode header banner
    await expect(page.locator('.view-only-banner')).toBeVisible();

    // Verify stats grid has all 4 primary metric cards
    const statCards = page.locator('.stats-grid .stat-card');
    await expect(statCards).toHaveCount(4);
    await expect(statCards.nth(0)).toContainText('Total customers');
    await expect(statCards.nth(1)).toContainText('Assessments');
    await expect(statCards.nth(2)).toContainText('High risk');
    await expect(statCards.nth(3)).toContainText('Low risk');

    // Confirm that action buttons and non-overview tabs are disabled in View-Only mode
    await expect(page.locator('.nav-item.disabled-nav')).toHaveCount(3);
  });

  test('should review Risk Reports chart on dashboard', async ({ page }) => {
    await page.goto('/');
    await page.locator('#view-only-access-btn').click();

    const riskReportsHeader = page.locator('.panel-header h3', { hasText: 'Risk reports' });
    await expect(riskReportsHeader).toBeVisible();

    // Check report stack rows: Low, Medium, High
    const rows = page.locator('.panel:has-text("Risk reports") .report-row');
    await expect(rows).toHaveCount(3);

    const labels = ['Low', 'Medium', 'High'];
    for (let i = 0; i < labels.length; i++) {
      const row = rows.nth(i);
      await expect(row.locator('span')).toContainText(labels[i]);

      // Check the bar and filled indicator
      const bar = row.locator('.bar');
      await expect(bar).toBeVisible();

      const fill = row.locator('.bar i');
      await expect(fill).toBeVisible();

      // Check bar width is responsive and rendered
      const barBox = await bar.boundingBox();
      expect(barBox).not.toBeNull();
      expect(barBox!.width).toBeGreaterThanOrEqual(40);

      // Verify the value indicator
      const val = row.locator('strong');
      await expect(val).toBeVisible();
      const valText = await val.textContent();
      expect(Number(valText)).toBeGreaterThanOrEqual(0);
    }
  });

  test('should review Risk Level & Decision Distribution charts in Reports view', async ({ page }) => {
    await page.goto('/');

    // Log in to access full navigation tabs
    await page.locator('input[name="username"]').fill('admin');
    await page.locator('input[name="password"]').fill('admin123');
    await page.locator('#login-form button[type="submit"]').click();

    await expect(page.locator('.topbar h1')).toBeVisible();

    // Switch to Reports tab
    await page.locator('.nav-item[data-view="reports"]').click();
    await expect(page.locator('.topbar h1')).toContainText('Risk & Decision Reports');

    // Verify top distribution summary cards
    const rateCards = page.locator('.stats-grid .stat-card');
    await expect(rateCards).toHaveCount(4);
    await expect(rateCards.nth(1)).toContainText('Approval Rate');
    await expect(rateCards.nth(2)).toContainText('In Review');
    await expect(rateCards.nth(3)).toContainText('Rejection Rate');

    // 1. Review Risk Level Distribution chart
    const riskLevelPanel = page.locator('.panel:has-text("Risk Level Distribution")');
    await expect(riskLevelPanel).toBeVisible();

    const riskRows = riskLevelPanel.locator('.report-row');
    await expect(riskRows).toHaveCount(3);

    for (let i = 0; i < 3; i++) {
      const row = riskRows.nth(i);
      await expect(row.locator('.bar i')).toBeVisible();

      // Verify label and percentage format
      const val = await row.locator('strong').textContent();
      expect(val).toMatch(/\(\d+%\)/);
    }

    // 2. Review Decision Distribution chart
    const decisionPanel = page.locator('.panel:has-text("Decision Distribution")');
    await expect(decisionPanel).toBeVisible();

    const decisionRows = decisionPanel.locator('.report-row');
    await expect(decisionRows).toHaveCount(3);

    const decisionTypes = ['Approved', 'Review', 'Rejected'];
    for (let i = 0; i < 3; i++) {
      const row = decisionRows.nth(i);
      await expect(row.locator('span')).toContainText(decisionTypes[i]);

      const fill = row.locator('.bar i');
      await expect(fill).toBeVisible();

      const val = await row.locator('strong').textContent();
      expect(val).toMatch(/\(\d+%\)/);
    }
  });

  test('should review Inbound Channels & Visitor Geography charts in Analytics view', async ({ page }) => {
    await page.goto('/');

    // Log in to access analytics
    await page.locator('input[name="username"]').fill('admin');
    await page.locator('input[name="password"]').fill('admin123');
    await page.locator('#login-form button[type="submit"]').click();

    // Switch to Analytics tab
    await page.locator('.nav-item[data-view="analytics"]').click();
    await expect(page.locator('.topbar h1')).toContainText('Visitor Analytics');

    // 1. Review Inbound Channels chart
    const channelPanel = page.locator('.panel:has-text("Inbound Channels & Referrers")');
    await expect(channelPanel).toBeVisible();

    const channelRows = channelPanel.locator('.report-row');
    const channelCount = await channelRows.count();
    expect(channelCount).toBeGreaterThanOrEqual(3);

    for (let i = 0; i < channelCount; i++) {
      const row = channelRows.nth(i);
      await expect(row.locator('.source-pill')).toBeVisible();
      await expect(row.locator('.bar i')).toBeVisible();

      const barBox = await row.locator('.bar').boundingBox();
      expect(barBox!.width).toBeGreaterThanOrEqual(40);
    }

    // 2. Review Visitor Geography chart
    const geoPanel = page.locator('.panel:has-text("Visitor Geography")');
    await expect(geoPanel).toBeVisible();

    const geoRows = geoPanel.locator('.report-row');
    const geoCount = await geoRows.count();
    expect(geoCount).toBeGreaterThanOrEqual(3);

    for (let i = 0; i < geoCount; i++) {
      const row = geoRows.nth(i);
      await expect(row.locator('.bar i')).toBeVisible();
      await expect(row.locator('strong')).toBeVisible();
    }
  });

  test('should ensure zero horizontal layout overflow on the current resolution', async ({ page }) => {
    await page.goto('/');
    await page.locator('#view-only-access-btn').click();

    // Scroll width should never exceed client width (no broken overflowing horizontal scroll)
    const hasHorizontalOverflow = await page.evaluate(() => {
      const doc = document.documentElement;
      return doc.scrollWidth > window.innerWidth;
    });

    expect(hasHorizontalOverflow).toBe(false);
  });
});
