import { expect, test } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

test.describe('Dashboard Page', () => {
  let loginPage: LoginPage;

  test.beforeEach(async ({ page }) => {
    // Mock the login API endpoint
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          message: 'Login successful',
          token: 'fake-jwt-token',
          user: {
            id: 1,
            email: 'johndoe@email.com',
            role: 'ROLE_EMPLOYEE',
          }
        }),
      });
    });

    loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login('johndoe@email.com', 'password123');
    await page.waitForURL('**/dashboard');
  });

  test('should display dashboard title', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /dashboard/i })).toBeVisible();
  });

  test('should display welcome message with user name', async ({ page }) => {
    await expect(page.getByText(/welcome back/i)).toBeVisible();
  });

  test('should display all stat cards', async ({ page }) => {
    const statsGrid = page.locator('.grid').first();

    await expect(statsGrid.getByText('Total Expenses')).toBeVisible();
    await expect(statsGrid.getByText('Approved')).toBeVisible();
    await expect(statsGrid.getByText('Pending')).toBeVisible();
    await expect(statsGrid.getByText('This Month')).toBeVisible();
  });

  test('should display stat values', async ({ page }) => {
    const statsGrid = page.locator('.grid').first();

    await expect(statsGrid.getByText('$12,430')).toBeVisible();
    await expect(statsGrid.getByText('18')).toBeVisible();
    await expect(statsGrid.getByText('5')).toBeVisible();
    await expect(statsGrid.getByText('$3,240')).toBeVisible();
  });

  test('should display stat icons', async ({ page }) => {
    const statsGrid = page.locator('.grid').first();

    const iconContainers = statsGrid.locator('.bg-blue-100, .bg-green-100, .bg-yellow-100, .bg-purple-100');
    await expect(iconContainers).toHaveCount(4);
  });

  test('should display recent activity section', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /last expenses/i })).toBeVisible();
  });

  test('should display recent activity items', async ({ page }) => {
    // Check for activity items
    await expect(page.getByText('Office Supplies')).toBeVisible();
    await expect(page.getByText('Client Lunch')).toBeVisible();
    await expect(page.getByText('Transportation')).toBeVisible();
  });

  test('should display activity dates', async ({ page }) => {
    // Check for dates
    await expect(page.getByText('Dec 24, 2025')).toBeVisible();
    await expect(page.getByText('Dec 23, 2025')).toBeVisible();
    await expect(page.getByText('Dec 22, 2025')).toBeVisible();
  });

  test('should display activity status badges', async ({ page }) => {
    // Check for status badges
    const approvedBadges = page.getByText('Approved');
    const pendingBadges = page.getByText('Pending');

    await expect(approvedBadges.first()).toBeVisible();
    await expect(pendingBadges.first()).toBeVisible();
  });

  test('should have correct layout structure', async ({ page }) => {
    // Check for grid layout
    const statsGrid = page.locator('.grid').first();
    await expect(statsGrid).toBeVisible();

    // Check for recent activity card
    const activityCard = page.locator('.rounded-lg').filter({ hasText: 'Last Expenses' });
    await expect(activityCard).toBeVisible();
  });

  test('should be responsive on mobile', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });

    // Dashboard should still be visible
    await expect(page.getByRole('heading', { name: /dashboard/i })).toBeVisible();

    // Stats should stack vertically (check grid cols)
    const statsGrid = page.locator('.grid').first();
    await expect(statsGrid).toHaveClass(/grid-cols-1/);
  });

  test('should display dark mode correctly', async ({ page }) => {
    // Toggle dark mode via the dark mode button (user action)
    await page.getByRole('button', { name: /toggle dark mode/i }).click();

    // Verify dark class was added
    const htmlElement = page.locator('html');
    await expect(htmlElement).toHaveClass(/dark/);

    // Verify localStorage was updated
    const theme = await page.evaluate(() => localStorage.getItem('theme'));
    expect(theme).toBe('dark');
  });
});
