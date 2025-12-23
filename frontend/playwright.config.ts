import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright Configuration for E2E Testing
 *
 * See https://playwright.dev/docs/test-configuration
 */

const isCI = !!process.env.CI;

export default defineConfig({
  testDir: './test/e2e',
  testMatch: /.*\.e2e-spec\.ts$/,

  // Maximum time one test can run for
  timeout: 30 * 1000,

  // Expect timeout for assertions
  expect: {
    timeout: 5000,
  },

  // Run tests in files in parallel
  fullyParallel: true,

  // Fail the build on CI if you accidentally left test.only
  forbidOnly: isCI,

  // Retry on CI only
  retries: isCI ? 2 : 0,

  // Opt out of parallel tests on CI to avoid resource issues
  workers: isCI ? 1 : undefined,

  // Reporter configuration
  reporter: isCI
    ? [
        ['html', { outputFolder: 'playwright-report' }],
        ['junit', { outputFile: 'test-results/junit.xml' }],
        ['github'], // GitHub Actions annotations
      ]
    : [['html'], ['list']],

  // Shared settings for all projects
  use: {
    // Base URL to use in actions like `await page.goto('/')`
    baseURL: 'http://localhost:5173',

    // Collect trace when retrying failed test
    trace: isCI ? 'on-first-retry' : 'retain-on-failure',

    // Screenshot on failure
    screenshot: 'only-on-failure',

    // Video on failure
    video: isCI ? 'retain-on-failure' : 'on-first-retry',

    // Maximum time for each action
    actionTimeout: 10 * 1000,

    // Navigation timeout
    navigationTimeout: 15 * 1000,
  },

  // Configure projects for major browsers
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },

    // Uncomment to test on Firefox and Safari
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },

    // Uncomment to test on mobile viewports
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] },
    // },
  ],

  // Run your local dev server before starting the tests
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !isCI,
    timeout: 120 * 1000,
    stdout: 'ignore',
    stderr: 'pipe',
  },

  // Output folder for test artifacts
  outputDir: 'test-results',
});
