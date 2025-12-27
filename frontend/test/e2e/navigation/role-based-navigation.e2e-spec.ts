import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

test.describe('Role-Based Navigation', () => {
  let loginPage: LoginPage;

  test.beforeEach(async ({ page }) => {
    // Mock the login API endpoint with dynamic role based on email
    await page.route('**/api/auth/login', async route => {
      const request = route.request();
      const postData = request.postDataJSON();
      const email = postData?.email || '';

      let role = 'ROLE_EMPLOYEE';
      let name = 'Employee';

      if (email.includes('finance')) {
        role = 'ROLE_FINANCE';
        name = 'Finance User';
      } else if (email.includes('manager')) {
        role = 'ROLE_MANAGER';
        name = 'Manager';
      }

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          message: 'Login successful',
          token: 'mock-jwt-token',
          user: {
            id: 1,
            email: email,
            role: role,
            name: name
          }
        })
      });
    });

    loginPage = new LoginPage(page);
    await loginPage.goto();
  });

  test('should display limited menu items for employee role', async ({ page }) => {
    await loginPage.login('employee@example.com', 'password123');

    await page.waitForURL('**/dashboard');

    // Check that employee menu items are visible
    await expect(page.getByRole('link', { name: /dashboard/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /my expenses/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /expenses report/i })).toBeVisible();

    // Check that finance/manager/finance-only items are NOT visible
    await expect(page.getByRole('link', { name: /approvals/i })).toBeHidden();
    await expect(page.getByRole('link', { name: /user management/i })).toBeHidden();
  });

  test('should display menu items for manager role', async ({ page }) => {
    await loginPage.login('manager@example.com', 'password123');

    await page.waitForURL('**/dashboard');

    // Check that employee menu items are visible
    await expect(page.getByRole('link', { name: /dashboard/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /my expenses/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /expenses report/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /approvals/i })).toBeVisible();

    // Check that finance-only items are NOT visible
    await expect(page.getByRole('link', { name: /user management/i })).toBeHidden();
  });

  test('should display menu items for finance role', async ({ page }) => {
    await loginPage.login('finance@example.com', 'password123');

    await page.waitForURL('**/dashboard');

    // Check that employee menu items are visible
    await expect(page.getByRole('link', { name: /dashboard/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /my expenses/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /expenses report/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /approvals/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /expenses report/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /user management/i })).toBeVisible();
  });

  test('should navigate between menu items correctly', async ({ page }) => {
    await loginPage.login('finance@example.com', 'password123');
    await page.waitForURL('**/dashboard');

    // Click on different menu items and verify navigation
    await page.getByRole('link', { name: /my expenses/i }).click();
    await expect(page).toHaveURL(/.*\/expenses/);

    await page.getByRole('link', { name: /dashboard/i }).click();
    await expect(page).toHaveURL(/.*\/dashboard/);
  });

  test('should highlight active menu item', async ({ page }) => {
    await loginPage.login('employee@example.com', 'password123');
    await page.waitForURL('**/dashboard');

    // Dashboard should be active
    const dashboardLink = page.getByRole('link', { name: /dashboard/i });
    await expect(dashboardLink).toHaveClass(/bg-gray-100/);

    // Navigate to expenses
    await page.getByRole('link', { name: /my expenses/i }).click();
    await page.waitForURL('**/expenses');

    // Expenses should now be active
    const expensesLink = page.getByRole('link', { name: /my expenses/i });
    await expect(expensesLink).toHaveClass(/bg-gray-100/);
  });
});
