import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LoginForm } from '../../src/pages/Login/components/LoginForm';

describe('LoginForm', () => {
  it('renderiza os campos de email e senha', () => {
    render(<LoginForm />);

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  it('botão inicia desabilitado com campos vazios', () => {
    render(<LoginForm />);

    expect(screen.getByRole('button', { name: /login/i })).toBeDisabled();
  });

  it('botão continua desabilitado com email inválido', async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    await user.type(screen.getByLabelText(/email/i), 'emailinvalido');
    await user.type(screen.getByLabelText(/password/i), '123456');

    expect(screen.getByRole('button', { name: /login/i })).toBeDisabled();
  });

  it('botão fica habilitado com email e senha válidos', async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    await user.type(screen.getByLabelText(/email/i), 'usuario@ubs.com');
    await user.type(screen.getByLabelText(/password/i), '123456');

    expect(screen.getByRole('button', { name: /login/i })).toBeEnabled();
  });

  it('mostra erro de email ao perder foco com email inválido', async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    const emailInput = screen.getByLabelText(/email/i);
    await user.type(emailInput, 'emailinvalido');
    await user.tab();

    expect(screen.getByText(/invalid email format/i)).toBeInTheDocument();
  });

  it('mostra erro de senha ao perder foco com senha vazia', async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    const passwordInput = screen.getByLabelText(/password/i);
    await user.click(passwordInput);
    await user.tab();

    expect(screen.getByText(/password is required/i)).toBeInTheDocument();
  });

  it('mostra erro de senha com menos de 6 caracteres', async () => {
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
