import js from '@eslint/js';
import tseslint from 'typescript-eslint';
import playwright from 'eslint-plugin-playwright';

export default tseslint.config(
  // Base JavaScript recommended rules
  js.configs.recommended,

  // TypeScript recommended rules
  ...tseslint.configs.recommendedTypeChecked,

  // Project-wide settings
  {
    languageOptions: {
      parserOptions: {
        project: './tsconfig.app.json',
        tsconfigRootDir: import.meta.dirname,
      },
    },
  },

  // Source files configuration
  {
    files: ['src/**/*.{ts,tsx}'],
    rules: {
      '@typescript-eslint/no-floating-promises': 'off',

      // Allow unused vars with underscore prefix
      '@typescript-eslint/no-unused-vars': [
        'error',
        {
          argsIgnorePattern: '^_',
          varsIgnorePattern: '^_',
        },
      ],

      // Disable rules that are too strict for React
      '@typescript-eslint/no-unsafe-assignment': 'off',
      '@typescript-eslint/no-unsafe-member-access': 'off',
      '@typescript-eslint/no-unsafe-call': 'off',
      '@typescript-eslint/no-unsafe-argument': 'off',
      '@typescript-eslint/no-misused-promises': 'off',
      '@typescript-eslint/no-explicit-any': 'warn', // Warn instead of error
    },
  },

  // Playwright test files configuration
  {
    files: ['test/e2e/**/*.ts'],
    ...playwright.configs['flat/recommended'],
    rules: {
      ...playwright.configs['flat/recommended'].rules,

      // CRITICAL: Enforce awaiting Playwright async calls
      '@typescript-eslint/no-floating-promises': 'error',

      // Playwright-specific rules
      'playwright/expect-expect': 'error',
      'playwright/no-conditional-in-test': 'warn',
      'playwright/no-element-handle': 'error',
      'playwright/no-eval': 'error',
      'playwright/no-focused-test': 'error',
      'playwright/no-force-option': 'warn',
      'playwright/no-networkidle': 'warn', // Warn about networkidle usage
      'playwright/no-page-pause': 'error',
      'playwright/no-skipped-test': 'warn',
      'playwright/no-useless-await': 'error',
      'playwright/no-useless-not': 'warn', // Warn about not.toBeVisible() vs toBeHidden()
      'playwright/no-wait-for-timeout': 'error', // Enforces what we just fixed!
      'playwright/prefer-web-first-assertions': 'error',
      'playwright/valid-expect': 'error',
    },
  },

  // Ignore patterns
  {
    ignores: [
      'dist/**',
      'build/**',
      'node_modules/**',
      'playwright-report/**',
      'test-results/**',
      '*.config.{js,ts}',
      'vite.config.ts',
      'playwright.config.ts',
    ],
  }
);
