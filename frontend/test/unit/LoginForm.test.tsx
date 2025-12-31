import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LoginForm } from '../../src/pages/Login/components/LoginForm';

describe('LoginForm', () => {
  it('renders email and password fields', () => {
    render(<LoginForm />);

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  it('button starts disabled with empty fields', () => {
    render(<LoginForm />);

    expect(screen.getByRole('button', { name: /login/i })).toBeDisabled();
  });

  it('button remains disabled with invalid email', async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    await user.type(screen.getByLabelText(/email/i), 'invalidemail');
    await user.type(screen.getByLabelText(/password/i), '123456');

    expect(screen.getByRole('button', { name: /login/i })).toBeDisabled();
  });

  it('button becomes enabled with valid email and password', async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    await user.type(screen.getByLabelText(/email/i), 'user@ubs.com');
    await user.type(screen.getByLabelText(/password/i), '123456');

    expect(screen.getByRole('button', { name: /login/i })).toBeEnabled();
  });

  it('shows email error on blur with invalid email', async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    const emailInput = screen.getByLabelText(/email/i);
    await user.type(emailInput, 'invalidemail');
    await user.tab();

    expect(screen.getByText(/invalid email format/i)).toBeInTheDocument();
  });

  it('shows password error on blur with empty password', async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    const passwordInput = screen.getByLabelText(/password/i);
    await user.click(passwordInput);
    await user.tab();

    expect(screen.getByText(/password is required/i)).toBeInTheDocument();
  });

  it('shows error for password with less than 6 characters', async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    const passwordInput = screen.getByLabelText(/password/i);
    await user.type(passwordInput, '12345'); // Only 5 characters
    await user.tab();

    expect(
      screen.getByText(/password must be at least 6 characters/i)
    ).toBeInTheDocument();
  });
});

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});
