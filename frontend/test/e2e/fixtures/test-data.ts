/**
 * Test Data for E2E Tests
 *
 * Centralized test data to be used across all E2E tests
 */

export const testUsers = {
  valid: {
    email: 'user@ubs.com',
    password: 'Test123456',
  },
  finance: {
    email: 'finance@ubs.com',
    password: 'finance123456',
  },
  invalidEmail: {
    email: 'invalid-email',
    password: 'Test123456',
  },
  shortPassword: {
    email: 'user@ubs.com',
    password: '12345', // Less than 6 characters
  },
  wrongCredentials: {
    email: 'wrong@ubs.com',
    password: 'WrongPassword123',
  },
};

export const validEmails = [
  'user@ubs.com',
  'test.user@ubs.com',
  'user+tag@ubs.com',
  'user123@ubs.co.uk',
];

export const invalidEmails = [
  'invalid',
  'invalid@',
  '@ubs.com',
  'user@',
  'user @ubs.com',
];

export const errorMessages = {
  emailRequired: /email is required/i,
  emailInvalid: /invalid email format/i,
  passwordRequired: /password is required/i,
  passwordTooShort: /password must be at least 6 characters/i,
};
