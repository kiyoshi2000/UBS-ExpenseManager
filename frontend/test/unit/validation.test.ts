import { describe, it, expect } from 'vitest';
import { validateEmail, validatePassword } from '../../src/utils/validation';

describe('validateEmail', () => {
  it('returns error when email is empty', () => {
    expect(validateEmail('')).toBe('Email is required');
  });

  it('returns error when email contains only spaces', () => {
    expect(validateEmail('   ')).toBe('Email is required');
  });

  it('returns error for invalid email format', () => {
    expect(validateEmail('invalidemail')).toBe('Invalid email format');
    expect(validateEmail('email@')).toBe('Invalid email format');
    expect(validateEmail('@domain.com')).toBe('Invalid email format');
    expect(validateEmail('email@domain')).toBe('Invalid email format');
  });

  it('returns null for valid email', () => {
    expect(validateEmail('user@domain.com')).toBeNull();
    expect(validateEmail('user@ubs.com')).toBeNull();
    expect(validateEmail('first.last@company.com.br')).toBeNull();
  });
});

describe('validatePassword', () => {
  it('returns error when password is empty', () => {
    expect(validatePassword('')).toBe('Password is required');
  });

  it('returns null for filled password', () => {
    expect(validatePassword('123456')).toBeNull();
    expect(validatePassword('anypassword')).toBeNull();
  });
});
