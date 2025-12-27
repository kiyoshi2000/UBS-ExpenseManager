import { expect, test } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { mockLoginApi, mockUsers } from '../helpers/auth-mocks';

test.describe('Profile Dropdown', () => {
  let loginPage: LoginPage;

  test.beforeEach(async ({ page }) => {
    // Mock the login API endpoint
    await mockLoginApi(page, mockUsers.employee);

    loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login('johndoe@email.com', 'password123');
    await page.waitForURL('**/dashboard');
  });

  test('should display user avatar in header', async ({ page }) => {
    const header = page.locator('header');
    const avatar = header.locator('.bg-red-100').first();
    await expect(avatar).toBeVisible();
    await expect(avatar).toContainText('J');
  });

  test('should open profile dropdown when clicked', async ({ page }) => {
    // Find and click the profile button
    // const profileButton = page.locator('header button').first();
    const profileButton = page.getByRole('button', { name: /John Doe/i });
    await profileButton.click();

    // Check that dropdown menu appears
    await expect(page.getByText('Profile Page')).toBeVisible();
    await expect(page.getByText('Logout')).toBeVisible();
  });

  test('should close dropdown when clicking outside', async ({ page }) => {
    // Open dropdown
    const profileButton = page.getByRole('button', { name: /John Doe/i });
    await profileButton.click();
    await expect(page.getByText('Profile Page')).toBeVisible();

    // Click outside (on main content)
    await page.locator('main').click();

    // Dropdown should be closed
    await expect(page.getByText('Profile Page')).toBeHidden();
  });

  test('should navigate to profile page when Profile Page is clicked', async ({ page }) => {
    // Open dropdown
    const profileButton = page.getByRole('button', { name: /John Doe/i });
    await profileButton.click();

    // Click Profile Page
    await page.getByText('Profile Page').click();

    // Should navigate to profile page
    await expect(page).toHaveURL(/.*\/profile/);
  });

  test('should logout when Logout is clicked', async ({ page }) => {
    // Open dropdown
    const profileButton = page.getByRole('button', { name: /John Doe/i });
    await profileButton.click();

    // Click Logout
    await page.getByText('Logout').click();

    // Should redirect to login page
    await expect(page).toHaveURL(/.*\//);
    await expect(page.getByPlaceholder(/email/i)).toBeVisible();
  });

  test('should rotate chevron icon when dropdown is opened', async ({ page }) => {
    const profileButton = page.getByRole('button', { name: /John Doe/i });
    const chevron = profileButton.locator('svg').last();

    // Click to open
    await profileButton.click();

    // Wait for dropdown to be visible (ensures animation completed)
    await expect(page.getByText('Profile Page')).toBeVisible();

    // Check that chevron has rotate-180 class
    await expect(chevron).toHaveClass(/rotate-180/);
  });

  test('should display user name and role in dropdown button', async ({ page }) => {
    // Check that user info is displayed (on desktop)
    const userInfo = page.locator('header').getByText('ROLE_EMPLOYEE');
    await expect(userInfo).toBeVisible();
  });
});
