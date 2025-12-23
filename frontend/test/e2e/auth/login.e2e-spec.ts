import { expect, test } from '@playwright/test';

test.describe('Login Page', () => {
  test('should render login page correctly', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' });

    await expect(page.getByText('Login to your account', { exact: true })).toBeVisible();
    await expect(page.getByText(/enter your email below to login/i)).toBeVisible();
    await expect(page.getByLabel(/email/i)).toBeVisible();
    await expect(page.getByLabel(/password/i)).toBeVisible();
    await expect(page.getByRole('button', { name: /login/i })).toBeVisible();
  });

  test('should have login button disabled initially', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' });

    const loginButton = page.getByRole('button', { name: /login/i });

    await expect(loginButton).toBeDisabled();
  });

  test('should show error when email is invalid', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' });

    const emailInput = page.getByLabel(/email/i);

    await emailInput.fill('invalid-email');
    await emailInput.blur();

    await page.waitForTimeout(500);

    const errorMessage = page.getByText(/invalid email format/i);
    await expect(errorMessage).toBeVisible();
  });

  test('should show error when password is too short', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' });

    const passwordInput = page.getByLabel(/password/i);

    await passwordInput.fill('12345');
    await passwordInput.blur();

    // Wait a bit for validation
    await page.waitForTimeout(500);

    const errorMessage = page.getByText(/password must be at least 6 characters/i);
    await expect(errorMessage).toBeVisible();
  });

  test('should enable login button with valid credentials', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' });

    const emailInput = page.getByLabel(/email/i);
    const passwordInput = page.getByLabel(/password/i);
    const loginButton = page.getByRole('button', { name: /login/i });

    // Initially disabled
    await expect(loginButton).toBeDisabled();

    // Fill valid data
    await emailInput.fill('user@ubs.com');
    await passwordInput.fill('Test123456');

    // Wait for validation
    await page.waitForTimeout(500);

    // Should be enabled now
    await expect(loginButton).toBeEnabled();
  });

  test('should submit form with valid credentials', async ({ page }) => {
    // Listen for console.log to verify form submission
    const consoleLogs: string[] = [];
    page.on('console', (msg) => {
      consoleLogs.push(msg.text());
    });

    await page.goto('/', { waitUntil: 'networkidle' });

    const emailInput = page.getByLabel(/email/i);
    const passwordInput = page.getByLabel(/password/i);
    const loginButton = page.getByRole('button', { name: /login/i });

    await emailInput.fill('user@ubs.com');
    await passwordInput.fill('Test123456');

    // Wait for validation
    await page.waitForTimeout(500);

    await loginButton.click();

    // Wait a bit for form submission
    await page.waitForTimeout(500);

    // Verify console.log was called (form submits and logs data)
    const hasFormData = consoleLogs.some((log) => log.includes('Form data'));
    expect(hasFormData).toBe(true);
  });

  test('should be keyboard navigable', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' });

    // Tab to email field
    await page.keyboard.press('Tab');
    await expect(page.getByLabel(/email/i)).toBeFocused();

    // Type email
    await page.keyboard.type('user@ubs.com');

    // Tab to password field
    await page.keyboard.press('Tab');
    await expect(page.getByLabel(/password/i)).toBeFocused();

    // Type password
    await page.keyboard.type('Test123456');

    // Wait for validation
    await page.waitForTimeout(500);

    // Tab to login button
    await page.keyboard.press('Tab');
    await expect(page.getByRole('button', { name: /login/i })).toBeFocused();

    // Button should be enabled
    await expect(page.getByRole('button', { name: /login/i })).toBeEnabled();
  });

  test('should have proper ARIA attributes', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' });

    const emailInput = page.getByLabel(/email/i);

    // Initially not invalid
    await expect(emailInput).toHaveAttribute('aria-invalid', 'false');

    // Trigger validation error
    await emailInput.fill('invalid');
    await emailInput.blur();

    // Wait for validation
    await page.waitForTimeout(500);

    // Should be marked as invalid
    await expect(emailInput).toHaveAttribute('aria-invalid', 'true');
  });

  test('should clear error when input becomes valid', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' });

    const emailInput = page.getByLabel(/email/i);

    // Create error
    await emailInput.fill('invalid');
    await emailInput.blur();
    await page.waitForTimeout(500);

    // Error should be visible
    await expect(page.getByText(/invalid email format/i)).toBeVisible();

    // Fix the error
    await emailInput.clear();
    await emailInput.fill('user@ubs.com');
    await page.waitForTimeout(500);

    // Error should be gone
    await expect(page.getByText(/invalid email format/i)).not.toBeVisible();
  });
});

test.describe('Login API Integration', () => {
  test('should redirect to dashboard on successful login (200)', async ({ page }) => {
    // Mock successful API response
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          message: 'Login successful',
          token: 'fake-jwt-token',
        }),
      });
    });

    await page.goto('/', { waitUntil: 'networkidle' });

    const emailInput = page.getByLabel(/email/i);
    const passwordInput = page.getByLabel(/password/i);
    const loginButton = page.getByRole('button', { name: /login/i });

    await emailInput.fill('user@ubs.com');
    await passwordInput.fill('password123');

    // Wait for button to be enabled
    await page.waitForTimeout(500);

    await loginButton.click();

    // Should redirect to dashboard
    await expect(page).toHaveURL(/\/dashboard/);
  });

  test('should show alert on login error', async ({ page }) => {
    // Mock error API response
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({
          message: 'Invalid credentials',
        }),
      });
    });

    // Listen for dialog (alert)
    page.on('dialog', async (dialog) => {
      expect(dialog.type()).toBe('alert');
      expect(dialog.message()).toBe('Invalid credentials');
      await dialog.accept();
    });

    await page.goto('/', { waitUntil: 'networkidle' });

    const emailInput = page.getByLabel(/email/i);
    const passwordInput = page.getByLabel(/password/i);
    const loginButton = page.getByRole('button', { name: /login/i });

    await emailInput.fill('user@ubs.com');
    await passwordInput.fill('wrongpassword');

    // Wait for button to be enabled
    await page.waitForTimeout(500);

    await loginButton.click();

    // Wait for alert to appear
    await page.waitForTimeout(500);

    // Should stay on login page
    await expect(page).toHaveURL('/');
  });
});
