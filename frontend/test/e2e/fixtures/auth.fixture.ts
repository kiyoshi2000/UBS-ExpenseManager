import { test as base, type Page } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { testUsers } from './test-data';

/**
 * Custom Test Fixtures
 *
 * Extends base Playwright test with custom fixtures
 * for login page and authenticated sessions
 */

type Fixtures = {
  loginPage: LoginPage;
  authenticatedPage: Page;
};

export const test = base.extend<Fixtures>({
  /**
   * LoginPage fixture
   * Automatically creates and navigates to LoginPage 
   */
  loginPage: async ({ page }, use) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await use(loginPage);
  },

  /**
   * Authenticated page fixture
   * Pre-authenticates user before test runs
   *
   * Note: This is a placeholder for when you have actual authentication
   * Currently just navigates to login page
   */
  authenticatedPage: async ({ page }, use) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login(testUsers.valid.email, testUsers.valid.password);

    // TODO: Wait for successful authentication and navigation
    // await page.waitForURL(/\/dashboard|\/home/);

    await use(page);
  },
});

export { expect } from '@playwright/test';
