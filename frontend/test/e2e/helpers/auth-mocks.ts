import { Page } from '@playwright/test';

export const mockUsers = {
  employee: {
    id: 1,
    email: 'johndoe@email.com',
    role: 'ROLE_EMPLOYEE',
    name: 'John Doe',
  },
  manager: {
    id: 3,
    email: 'manager@example.com',
    role: 'ROLE_MANAGER',
    name: 'Manager',
  },
  finance: {
    id: 4,
    email: 'finance@example.com',
    role: 'ROLE_FINANCE',
    name: 'Finance User',
  },
} as const;

/**
 * Mock the login API endpoint with a specific user
 */
export async function mockLoginApi(
  page: Page,
  user: typeof mockUsers[keyof typeof mockUsers]
) {
  await page.route('**/api/auth/login', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        message: 'Login successful',
        token: 'mock-jwt-token',
        user,
      }),
    });
  });
}

/**
 * Mock login API with dynamic role detection based on email
 * Useful for role-based navigation tests
 */
export async function mockLoginApiDynamic(page: Page) {
  await page.route('**/api/auth/login', async (route) => {
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
          name: name,
        },
      }),
    });
  });
}
