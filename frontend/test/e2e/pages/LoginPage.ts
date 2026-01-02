import { type Page, type Locator, expect } from '@playwright/test';

/**
 * Page Object Model for Login Page
 *
 * Encapsulates all interactions with the login page
 * to make tests more maintainable and readable.
 */
export class LoginPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly loginButton: Locator;
  readonly pageTitle: Locator;
  readonly pageDescription: Locator;

  constructor(page: Page) {
    this.page = page;

    // Form elements
    this.emailInput = page.getByLabel(/email/i);
    this.passwordInput = page.getByLabel(/password/i);
    this.loginButton = page.getByRole('button', { name: /login/i });

    // Page content
    this.pageTitle = page.getByRole('heading', { name: /login to your account/i });
    this.pageDescription = page.getByText(/enter your email below to login/i);
  }

  /**
   * Navigate to the login page
   */
  async goto() {
    await this.page.goto('/');
    await this.expectPageLoaded();
  }

  /**
   * Fill in login form and submit
   */
  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
  }

  /**
   * Fill email field
   */
  async fillEmail(email: string) {
    await this.emailInput.fill(email);
  }

  /**
   * Fill password field
   */
  async fillPassword(password: string) {
    await this.passwordInput.fill(password);
  }

  /**
   * Click login button
   */
  async clickLogin() {
    await this.loginButton.click();
  }

  /**
   * Clear email field
   */
  async clearEmail() {
    await this.emailInput.clear();
  }

  /**
   * Clear password field
   */
  async clearPassword() {
    await this.passwordInput.clear();
  }

  /**
   * Get error message element by text
   * Uses user-facing text content instead of CSS selectors
   */
  getErrorByText(text: string | RegExp) {
    return this.page.getByText(text);
  }

  /**
   * Verify page is loaded correctly
   */
  async expectPageLoaded() {
    await expect(this.pageTitle).toBeVisible();
    await expect(this.emailInput).toBeVisible();
    await expect(this.passwordInput).toBeVisible();
    await expect(this.loginButton).toBeVisible();
  }

  /**
   * Verify email error is shown
   */
  async expectEmailError(message: string | RegExp) {
    const error = this.getErrorByText(message);
    await expect(error).toBeVisible();
  }

  /**
   * Verify password error is shown
   */
  async expectPasswordError(message: string | RegExp) {
    const error = this.getErrorByText(message);
    await expect(error).toBeVisible();
  }

  /**
   * Verify no errors are shown
   * Checks that form inputs don't have aria-invalid="true"
   */
  async expectNoErrors() {
    await expect(this.emailInput).toHaveAttribute('aria-invalid', 'false');
    await expect(this.passwordInput).toHaveAttribute('aria-invalid', 'false');
  }

  /**
   * Verify login button is disabled
   */
  async expectLoginButtonDisabled() {
    await expect(this.loginButton).toBeDisabled();
  }

  /**
   * Verify login button is enabled
   */
  async expectLoginButtonEnabled() {
    await expect(this.loginButton).toBeEnabled();
  }

  /**
   * Verify email field has aria-invalid attribute
   */
  async expectEmailInvalid(invalid: boolean = true) {
    await expect(this.emailInput).toHaveAttribute(
      'aria-invalid',
      invalid ? 'true' : 'false'
    );
  }

  /**
   * Verify password field has aria-invalid attribute
   */
  async expectPasswordInvalid(invalid: boolean = true) {
    await expect(this.passwordInput).toHaveAttribute(
      'aria-invalid',
      invalid ? 'true' : 'false'
    );
  }
}
