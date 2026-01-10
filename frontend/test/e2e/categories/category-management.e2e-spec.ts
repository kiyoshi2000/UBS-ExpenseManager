import { test, expect, Page } from '@playwright/test';

/**
 * E2E Test: Category Management Full Workflow
 * 
 * This test demonstrates the complete category management lifecycle:
 * 1. Create a new category
 * 2. Edit the category (all fields)
 * 3. Verify changes are reflected
 */

interface Category {
  id: number;
  name: string;
  dailyBudget: number;
  monthlyBudget: number;
  currencyName: string;
}

// Generate unique test data for each test run
const timestamp = Date.now();
const testCategory = {
  name: `Test Category ${timestamp}`,
  dailyBudget: 50.00,
  monthlyBudget: 1500.00,
  currencyName: 'USD',
};

const updatedCategory = {
  name: `Updated Category ${timestamp}`,
  dailyBudget: 75.50,
  monthlyBudget: 2250.75,
  currencyName: 'BRL',
};

/**
 * Set up API mocks for category management operations
 */
async function setupCategoryManagementMocks(page: Page) {
  let categoryIdCounter = 100;
  const categories: Category[] = [];

  // Mock get categories list (pageable)
  await page.route('**/api/expense-categories?*', async (route) => {
    const url = new URL(route.request().url());
    const search = url.searchParams.get('search');

    let filteredCategories = categories;

    if (search) {
      filteredCategories = filteredCategories.filter(c => 
        c.name.toLowerCase().includes(search.toLowerCase())
      );
    }

    // Sort by name
    filteredCategories.sort((a, b) => a.name.localeCompare(b.name));

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        content: filteredCategories,
        totalPages: 1,
        totalElements: filteredCategories.length,
        number: 0,
        size: 10,
        first: true,
        last: true,
        numberOfElements: filteredCategories.length,
        empty: filteredCategories.length === 0,
      }),
    });
  });

  // Mock create category
  await page.route('**/api/expense-categories', async (route) => {
    if (route.request().method() === 'POST') {
      const requestData = route.request().postDataJSON() as Omit<Category, 'id'>;
      const newCategory: Category = {
        id: categoryIdCounter++,
        name: requestData.name,
        dailyBudget: requestData.dailyBudget,
        monthlyBudget: requestData.monthlyBudget,
        currencyName: requestData.currencyName,
      };
      categories.push(newCategory);

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(newCategory),
      });
    } else {
      await route.continue();
    }
  });

  // Mock update category
  await page.route('**/api/expense-categories/*', async (route) => {
    if (route.request().method() === 'PUT') {
      const categoryId = parseInt(route.request().url().split('/').pop() || '0');
      const requestData = route.request().postDataJSON() as Omit<Category, 'id'>;
      
      const categoryIndex = categories.findIndex(c => c.id === categoryId);
      if (categoryIndex >= 0) {
        categories[categoryIndex] = {
          ...categories[categoryIndex],
          name: requestData.name,
          dailyBudget: requestData.dailyBudget,
          monthlyBudget: requestData.monthlyBudget,
          currencyName: requestData.currencyName,
        };

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(categories[categoryIndex]),
        });
      } else {
        await route.fulfill({ status: 404 });
      }
    } else {
      await route.continue();
    }
  });

  // Mock login for Finance user (who can manage categories)
  await page.route('**/api/auth/login', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        message: 'Login successful',
        token: 'mock-jwt-token',
        user: {
          id: 1,
          email: 'finance@ubs.com',
          role: 'ROLE_FINANCE',
          name: 'Finance User',
        },
      }),
    });
  });
}

test.describe('Category Management Full Workflow', () => {
  test.beforeEach(async ({ page }) => {
    // Set up all mocks
    await setupCategoryManagementMocks(page);

    // Login as finance user
    await page.goto('/');
    await page.getByLabel(/email/i).fill('finance@ubs.com');
    await page.getByLabel(/password/i).fill('Finance123456');
    await page.getByRole('button', { name: /login/i }).click();

    // Navigate to Categories page
    await page.waitForURL(/\/dashboard/);
    await page.goto('/category');
    await expect(page.getByRole('heading', { name: 'Category Management' })).toBeVisible();
  });

  test('should complete full category management workflow', async ({ page }) => {
    // ============================================
    // STEP 1: Create a new category
    // ============================================
    await test.step('Create new category', async () => {
      // Click Add Category button
      await page.getByRole('button', { name: /add category/i }).click();

      // Wait for dialog to open
      await expect(page.getByText('Create New Category')).toBeVisible();

      // Fill category name
      await page.getByLabel('Name').fill(testCategory.name);

      // Select currency
      await page.locator('#currency').selectOption(testCategory.currencyName);

      // Wait for currency selection to enable the money inputs
      const dailyBudgetInput = page.getByLabel('Daily Budget');
      await expect(dailyBudgetInput).toBeEnabled();
      await dailyBudgetInput.fill(testCategory.dailyBudget.toString());

      // Fill monthly budget
      const monthlyBudgetInput = page.getByLabel('Monthly Budget');
      await monthlyBudgetInput.fill(testCategory.monthlyBudget.toString());

      // Verify form values are set correctly before submission
      await expect(page.getByLabel('Name')).toHaveValue(testCategory.name);
      await expect(page.locator('#currency')).toHaveValue(testCategory.currencyName);

      // Submit form - ensure button is enabled before clicking
      const createButton = page.getByRole('button', { name: /create category/i });
      await expect(createButton).toBeEnabled();
      await createButton.click();

      // Wait for success dialog
      await expect(page.getByText('Category Created')).toBeVisible();
      await page.getByRole('button', { name: /done/i }).click();

      // Verify category appears in the table
      await expect(page.getByText(testCategory.name)).toBeVisible();
      
      // Verify budget values are displayed
      const categoryRow = page.locator('tr', { has: page.getByText(testCategory.name) });
      await expect(categoryRow).toBeVisible();
    });

    // ============================================
    // STEP 2: Edit the category (all fields)
    // ============================================
    await test.step('Edit category with all fields updated', async () => {
      // Find category row and click Edit button
      const categoryRow = page.locator('tr', { has: page.getByText(testCategory.name) });
      await categoryRow.getByRole('button', { name: /edit/i }).click();

      // Wait for edit dialog
      await expect(page.getByText('Edit Category')).toBeVisible();

      // Update category name
      const nameInput = page.getByLabel('Name');
      await nameInput.clear();
      await nameInput.fill(updatedCategory.name);

      // Change currency
      await page.locator('#currency').selectOption(updatedCategory.currencyName);

      // Wait for currency change to process
      const dailyBudgetInput = page.getByLabel('Daily Budget');
      await expect(dailyBudgetInput).toBeEnabled();
      await dailyBudgetInput.fill(updatedCategory.dailyBudget.toString());

      // Update monthly budget
      const monthlyBudgetInput = page.getByLabel('Monthly Budget');
      await monthlyBudgetInput.fill(updatedCategory.monthlyBudget.toString());

      // Submit form
      await page.getByRole('button', { name: /save changes/i }).click();

      // Wait for success dialog
      await expect(page.getByText('Category Updated')).toBeVisible();
      await page.getByRole('button', { name: /done/i }).click();

      // Verify updated name appears in the table
      await expect(page.getByText(updatedCategory.name)).toBeVisible();

      // Verify old name is no longer visible
      await expect(page.getByText(testCategory.name)).toBeHidden();
    });

    // ============================================
    // STEP 3: Verify all changes are reflected
    // ============================================
    await test.step('Verify category changes by editing again', async () => {
      // Find updated category row and click Edit button
      const categoryRow = page.locator('tr', { has: page.getByText(updatedCategory.name) });
      await categoryRow.getByRole('button', { name: /edit/i }).click();

      // Wait for edit dialog
      await expect(page.getByText('Edit Category')).toBeVisible();

      // Verify name is correct
      const nameInput = page.getByLabel('Name');
      await expect(nameInput).toHaveValue(updatedCategory.name);

      // Verify currency is correct
      const currencySelect = page.locator('#currency');
      await expect(currencySelect).toHaveValue(updatedCategory.currencyName);

      // Verify daily budget is correct (formatted)
      const dailyBudgetInput = page.getByLabel('Daily Budget');
      const dailyBudgetValue = await dailyBudgetInput.inputValue();
      expect(parseFloat(dailyBudgetValue)).toBe(updatedCategory.dailyBudget);

      // Verify monthly budget is correct (formatted)
      const monthlyBudgetInput = page.getByLabel('Monthly Budget');
      const monthlyBudgetValue = await monthlyBudgetInput.inputValue();
      expect(parseFloat(monthlyBudgetValue)).toBe(updatedCategory.monthlyBudget);

      // Close dialog without making changes
      await page.getByRole('button', { name: /cancel/i }).click();

      // Verify dialog is closed
      await expect(page.getByText('Edit Category')).toBeHidden();
    });
  });
});
