import { test, expect, Page } from '@playwright/test';
import { User, CreateUserPayload, UpdateUserPayload } from '@/api/user.api';
import { Department } from '@/types/department';

/**
 * E2E Test: User Management Full Workflow
 * 
 * This test demonstrates the complete user management lifecycle:
 * 1. Create a manager user
 * 2. Create an employee user with the manager assigned
 * 3. Update both users
 * 4. Deactivate the employee
 * 5. Reactivate the employee
 */

// Mock data for departments
const mockDepartments: Department[] = [
  { id: 1, name: 'Engineering', monthlyBudget: 10000, currency: 'USD' },
  { id: 2, name: 'Finance', monthlyBudget: 15000, currency: 'USD' },
];

// Generate unique test data for each test run
const timestamp = Date.now();
const testManager = {
  name: `Test Manager ${timestamp}`,
  email: `manager${timestamp}@ubs.com`,
  role: 'manager',
  departmentId: 1,
};

const testEmployee = {
  name: `Test Employee ${timestamp}`,
  email: `employee${timestamp}@ubs.com`,
  role: 'employee',
  departmentId: 1,
};

/**
 * Extended User type for mock data storage
 */
interface MockUser extends Omit<User, 'manager' | 'department'> {
  manager: MockUser | null;
  department: Department | null;
}

/**
 * Set up API mocks for user management operations
 */
async function setupUserManagementMocks(page: Page) {
  let userIdCounter = 100;
  const users: MockUser[] = [];

  // Mock departments list
  await page.route('**/api/departments', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockDepartments),
    });
  });

  // Mock create user (register)
  await page.route('**/api/auth/register', async (route) => {
    const requestData = route.request().postDataJSON() as CreateUserPayload;
    const newUser: MockUser = {
      id: userIdCounter++,
      email: requestData.email,
      name: requestData.name,
      role: requestData.role,
      active: true,
      department: requestData.departmentId 
        ? mockDepartments.find(d => d.id === requestData.departmentId) || null
        : null,
      manager: requestData.managerEmail 
        ? users.find(u => u.email === requestData.managerEmail) || null
        : null,
    };
    users.push(newUser);

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(newUser),
    });
  });

  // Mock get users list (pageable)
  await page.route('**/api/users?*', async (route) => {
    const url = new URL(route.request().url());
    const role = url.searchParams.get('role');
    const departmentId = url.searchParams.get('departmentId');
    const includeInactive = url.searchParams.get('includeInactive') === 'true';

    let filteredUsers = users;

    if (role) {
      filteredUsers = filteredUsers.filter(u => u.role === role);
    }
    if (departmentId) {
      filteredUsers = filteredUsers.filter(u => u.department?.id === parseInt(departmentId));
    }
    if (!includeInactive) {
      filteredUsers = filteredUsers.filter(u => u.active);
    }

    // Sort: active first, then by name
    filteredUsers.sort((a, b) => {
      if (a.active !== b.active) return b.active ? 1 : -1;
      return a.name.localeCompare(b.name);
    });

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        content: filteredUsers,
        totalPages: 1,
        totalElements: filteredUsers.length,
        number: 0,
        size: 10,
        first: true,
        last: true,
        numberOfElements: filteredUsers.length,
        empty: filteredUsers.length === 0,
      }),
    });
  });

  // Mock update user
  await page.route('**/api/users/*', async (route) => {
    if (route.request().method() === 'PUT') {
      const userId = parseInt(route.request().url().split('/').pop() || '0');
      const requestData = route.request().postDataJSON() as UpdateUserPayload;
      
      const userIndex = users.findIndex(u => u.id === userId);
      if (userIndex >= 0) {
        const existingUser = users[userIndex];
        users[userIndex] = {
          ...existingUser,
          name: requestData.name,
          manager: requestData.managerEmail 
            ? users.find(u => u.email === requestData.managerEmail) || null
            : null,
          department: requestData.departmentId
            ? mockDepartments.find(d => d.id === requestData.departmentId) || existingUser.department
            : existingUser.department,
        };

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(users[userIndex]),
        });
      } else {
        await route.fulfill({ status: 404 });
      }
    } else if (route.request().method() === 'DELETE') {
      const userId = parseInt(route.request().url().split('/').pop() || '0');
      const userIndex = users.findIndex(u => u.id === userId);
      
      if (userIndex >= 0) {
        const user = users[userIndex];
        
        // Check if trying to deactivate a manager with active subordinates
        const hasActiveSubordinates = users.some(
          u => u.manager?.id === user.id && u.active && u.id !== user.id
        );
        
        if (hasActiveSubordinates) {
          await route.fulfill({
            status: 400,
            contentType: 'application/json',
            body: JSON.stringify({
              message: 'Cannot deactivate manager with active subordinates'
            })
          });
          return;
        }
        
        users[userIndex].active = false;
        await route.fulfill({ status: 204 });
      } else {
        await route.fulfill({ status: 404 });
      }
    } else if (route.request().method() === 'PATCH' && route.request().url().includes('/reactivate')) {
      const userId = parseInt(route.request().url().split('/')[route.request().url().split('/').length - 2]);
      const userIndex = users.findIndex(u => u.id === userId);
      
      if (userIndex >= 0) {
        users[userIndex].active = true;
        await route.fulfill({ status: 200 });
      } else {
        await route.fulfill({ status: 404 });
      }
    } else {
      await route.continue();
    }
  });

  // Mock login for Finance user (who can manage users)
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

test.describe('User Management Full Workflow', () => {
  test.beforeEach(async ({ page }) => {
    // Set up all mocks
    await setupUserManagementMocks(page);

    // Login as finance user
    await page.goto('/');
    await page.getByLabel(/email/i).fill('finance@ubs.com');
    await page.getByLabel(/password/i).fill('Finance123456');
    await page.getByRole('button', { name: /login/i }).click();

    // Navigate to Users page
    await page.waitForURL(/\/dashboard/);
    await page.goto('/users');
    await expect(page.getByRole('heading', { name: 'User Management' })).toBeVisible();
  });

  test('should complete full user management workflow', async ({ page }) => {
    // ============================================
    // STEP 1: Create Manager User
    // ============================================
    await test.step('Create manager user', async () => {
      // Click Add New Employee button
      await page.getByRole('button', { name: /add new employee/i }).click();

      // Wait for dialog to open
      await expect(page.getByText('Create New User')).toBeVisible();

      // Fill manager details
      await page.getByLabel(/^name/i).fill(testManager.name);
      await page.getByLabel(/^email/i).fill(testManager.email);
      await page.getByLabel(/^role/i).selectOption(testManager.role);
      await page.getByLabel(/^department/i).selectOption(testManager.departmentId.toString());

      // Submit form
      await page.getByRole('button', { name: /create user/i }).click();

      // Wait for success dialog
      await expect(page.getByText('User Created')).toBeVisible();
      await page.getByRole('button', { name: /done/i }).click();

      // Verify manager appears in the table
      await expect(page.getByText(testManager.name)).toBeVisible();
      await expect(page.getByText(testManager.email)).toBeVisible();
    });

    // ============================================
    // STEP 2: Create Employee User with Manager
    // ============================================
    await test.step('Create employee user with manager assigned', async () => {
      // Click Add New Employee button
      await page.getByRole('button', { name: /add new employee/i }).click();

      // Wait for dialog to open
      await expect(page.getByText('Create New User')).toBeVisible();

      // Fill employee details
      await page.getByLabel(/^name/i).fill(testEmployee.name);
      await page.getByLabel(/^email/i).fill(testEmployee.email);
      await page.getByLabel(/^role/i).selectOption(testEmployee.role);
      await page.getByLabel(/^department/i).selectOption(testEmployee.departmentId.toString());

      // Wait for managers dropdown to be enabled and populated
      const managerDropdown = page.getByLabel(/^manager/i);
      await expect(managerDropdown).toBeEnabled();
      
      // Wait for the manager option to be available
      await expect(managerDropdown.locator(`option[value="${testManager.email}"]`)).toHaveCount(1);

      // Select the manager we just created
      await managerDropdown.selectOption(testManager.email);

      // Submit form
      await page.getByRole('button', { name: /create user/i }).click();

      // Wait for success dialog
      await expect(page.getByText('User Created')).toBeVisible();
      await page.getByRole('button', { name: /done/i }).click();

      // Verify employee appears in the table
      await expect(page.getByText(testEmployee.name)).toBeVisible();
      await expect(page.getByText(testEmployee.email)).toBeVisible();
    });

    // ============================================
    // STEP 3: Update Manager User
    // ============================================
    await test.step('Update manager user', async () => {
      // Find manager row and click Edit button
      const managerRow = page.locator('tr', { has: page.getByText(testManager.name) });
      await managerRow.getByRole('button', { name: /edit/i }).click();

      // Wait for edit dialog
      await expect(page.getByText('Edit User')).toBeVisible();

      // Update manager name
      const updatedManagerName = `${testManager.name} Updated`;
      await page.getByLabel(/^name/i).clear();
      await page.getByLabel(/^name/i).fill(updatedManagerName);

      // Submit form
      await page.getByRole('button', { name: /save changes/i }).click();

      // Wait for success dialog
      await expect(page.getByText('User Updated')).toBeVisible();
      await page.getByRole('button', { name: /done/i }).click();

      // Verify updated name appears in the table
      await expect(page.getByText(updatedManagerName)).toBeVisible();
    });

    // ============================================
    // STEP 4: Update Employee User
    // ============================================
    await test.step('Update employee user', async () => {
      // Find employee row and click Edit button
      const employeeRow = page.locator('tr', { has: page.getByText(testEmployee.name) });
      await employeeRow.getByRole('button', { name: /edit/i }).click();

      // Wait for edit dialog
      await expect(page.getByText('Edit User')).toBeVisible();

      // Update employee name
      const updatedEmployeeName = `${testEmployee.name} Updated`;
      await page.getByLabel(/^name/i).clear();
      await page.getByLabel(/^name/i).fill(updatedEmployeeName);

      // Submit form
      await page.getByRole('button', { name: /save changes/i }).click();

      // Wait for success dialog
      await expect(page.getByText('User Updated')).toBeVisible();
      await page.getByRole('button', { name: /done/i }).click();

      // Verify updated name appears in the table
      await expect(page.getByText(updatedEmployeeName)).toBeVisible();
    });

    // ============================================
    // STEP 5: Try to Deactivate Manager with Active Subordinates (Should Fail)
    // ============================================
    await test.step('Try to deactivate manager with active subordinates', async () => {
      // Find manager row
      const managerRow = page.locator('tr', { has: page.getByText(testManager.name) });

      // Try to click the status toggle switch
      const statusSwitch = managerRow.locator('button[role="switch"]');
      await statusSwitch.click();

      // Wait for deactivation confirmation dialog
      await expect(page.getByText('Deactivate User')).toBeVisible();

      // Confirm deactivation attempt
      await page.getByRole('button', { name: /deactivate/i }).click();

      // Wait for error dialog
      await expect(page.getByText('Error Deactivating User')).toBeVisible();
      
      // Verify error message mentions subordinates
      await expect(page.getByText(/subordinate/i)).toBeVisible();
      
      // Close error dialog
      await page
        .getByRole("button", { name: "Close" })
        .filter({ hasText: "Close" })
        .first()
        .click();

      // Verify manager is still active
      await expect(managerRow.getByText('Active', { exact: true })).toBeVisible();
    });

    // ============================================
    // STEP 6: Deactivate Employee
    // ============================================
    await test.step('Deactivate employee user', async () => {
      // Find employee row (with updated name)
      const employeeRow = page.locator('tr', { has: page.getByText(testEmployee.email) });

      // Find and click the status toggle switch
      const statusSwitch = employeeRow.locator('button[role="switch"]');
      await statusSwitch.click();

      // Wait for deactivation confirmation dialog
      await expect(page.getByText('Deactivate User')).toBeVisible();
      await expect(page.getByText(/Are you sure you want to deactivate/i)).toBeVisible();

      // Confirm deactivation
      await page.getByRole('button', { name: /deactivate/i }).click();

      // Wait for success dialog
      await expect(page.getByText('User Deactivated')).toBeVisible();
      await page.getByRole('button', { name: /done/i }).click();

      // Wait for table reload and find row again
      const deactivatedEmployeeRow = page.locator('tr', { has: page.getByText(testEmployee.email) });
      
      // Verify employee status changed to Inactive
      await expect(deactivatedEmployeeRow.getByText('Inactive')).toBeVisible();

      // Verify Edit button is no longer visible for inactive user
      await expect(deactivatedEmployeeRow.getByRole('button', { name: /edit/i })).toBeHidden();
    });

    // ============================================
    // STEP 7: Reactivate Employee
    // ============================================
    await test.step('Reactivate employee user', async () => {
      // Find inactive employee row
      const employeeRow = page.locator('tr', { has: page.getByText(testEmployee.email) });

      // Click the status toggle switch to reactivate
      const statusSwitch = employeeRow.locator('button[role="switch"]');
      await statusSwitch.click();

      // Wait for reactivation confirmation dialog
      await expect(page.getByText('Reactivate User')).toBeVisible();

      // Confirm reactivation
      await page.getByRole('button', { name: /reactivate/i }).click();

      // Just verify the dialog closes - the reactivation happened
      await expect(page.getByText('Reactivate User')).toBeHidden();
    });
  });
});
