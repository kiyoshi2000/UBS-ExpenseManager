import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CreateUserDialog } from '../../src/components/Users/CreateUserDialog';

describe('CreateUserDialog', () => {
  const defaultProps = {
    open: true,
    onOpenChange: vi.fn(),
    onSubmit: vi.fn(),
  };

  it('renders dialog title and description', () => {
    render(<CreateUserDialog {...defaultProps} />);

    expect(screen.getByText('Create New User')).toBeInTheDocument();
    expect(screen.getByText('Fill in the details of the new user below')).toBeInTheDocument();
  });

  it('renders all form fields', () => {
    render(<CreateUserDialog {...defaultProps} />);

    expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^role/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/manager email/i)).toBeInTheDocument();
  });

  it('renders Cancel and Create User buttons', () => {
    render(<CreateUserDialog {...defaultProps} />);

    expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create user/i })).toBeInTheDocument();
  });

  it('Create User button starts disabled', () => {
    render(<CreateUserDialog {...defaultProps} />);

    expect(screen.getByRole('button', { name: /create user/i })).toBeDisabled();
  });

  it('enables Create User button when all required fields are valid', async () => {
    const user = userEvent.setup();
    render(<CreateUserDialog {...defaultProps} />);

    await user.type(screen.getByLabelText(/name/i), 'John Doe');
    await user.type(screen.getByLabelText(/^email/i), 'john@ubs.com');
    await user.selectOptions(screen.getByLabelText(/^role/i), 'manager');

    expect(screen.getByRole('button', { name: /create user/i })).toBeEnabled();
  });

  it('requires manager email for employee role', async () => {
    const user = userEvent.setup();
    render(<CreateUserDialog {...defaultProps} />);

    await user.type(screen.getByLabelText(/name/i), 'John Doe');
    await user.type(screen.getByLabelText(/^email/i), 'john@ubs.com');
    await user.selectOptions(screen.getByLabelText(/^role/i), 'employee');

    // Button should be disabled without manager email
    expect(screen.getByRole('button', { name: /create user/i })).toBeDisabled();

    await user.type(screen.getByLabelText(/manager email/i), 'manager@ubs.com');

    // Button should be enabled with manager email
    expect(screen.getByRole('button', { name: /create user/i })).toBeEnabled();
  });

  it('shows validation error for empty name', async () => {
    const user = userEvent.setup();
    render(<CreateUserDialog {...defaultProps} />);

    const nameInput = screen.getByLabelText(/name/i);
    await user.type(nameInput, 'test');
    await user.clear(nameInput);

    expect(screen.getByText(/name is required/i)).toBeInTheDocument();
  });

  it('calls onSubmit with correct data when form is submitted', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<CreateUserDialog {...defaultProps} onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/name/i), 'John Doe');
    await user.type(screen.getByLabelText(/^email/i), 'john@ubs.com');
    await user.selectOptions(screen.getByLabelText(/^role/i), 'finance');

    const submitButton = screen.getByRole('button', { name: /create user/i });
    await user.click(submitButton);

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'John Doe',
      email: 'john@ubs.com',
      role: 'finance',
      managerEmail: '',
    });
  });

  it('calls onOpenChange(false) when Cancel is clicked', async () => {
    const user = userEvent.setup();
    const onOpenChange = vi.fn();
    render(<CreateUserDialog {...defaultProps} onOpenChange={onOpenChange} />);

    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    await user.click(cancelButton);

    expect(onOpenChange).toHaveBeenCalledWith(false);
  });

  it('does not render dialog when open is false', () => {
    render(<CreateUserDialog {...defaultProps} open={false} />);

    expect(screen.queryByText('Create New User')).not.toBeInTheDocument();
  });

  it('shows asterisk for required fields', () => {
    render(<CreateUserDialog {...defaultProps} />);

    // Check that required field labels have asterisks
    const nameLabel = screen.getByText(/name/i).parentElement;
    const emailLabel = screen.getByText(/^email/i).parentElement;
    const roleLabel = screen.getByText(/^role/i).parentElement;

    expect(nameLabel).toHaveTextContent('*');
    expect(emailLabel).toHaveTextContent('*');
    expect(roleLabel).toHaveTextContent('*');
  });

  it('shows asterisk for manager email when employee role is selected', async () => {
    const user = userEvent.setup();
    render(<CreateUserDialog {...defaultProps} />);

    // Initially, manager email should not have asterisk
    const managerLabel = screen.getByText(/manager email/i).parentElement;
    expect(managerLabel?.textContent).not.toMatch(/\*/);

    // Select employee role
    await user.selectOptions(screen.getByLabelText(/^role/i), 'employee');

    // Now manager email should have asterisk
    expect(managerLabel).toHaveTextContent('*');
  });

  it('clears manager email error when role changes from employee to manager', async () => {
    const user = userEvent.setup();
    render(<CreateUserDialog {...defaultProps} />);

    await user.type(screen.getByLabelText(/name/i), 'John Doe');
    await user.type(screen.getByLabelText(/^email/i), 'john@ubs.com');
    await user.selectOptions(screen.getByLabelText(/^role/i), 'employee');

    // Should show error for missing manager email
    expect(screen.getByRole('button', { name: /create user/i })).toBeDisabled();

    // Change to manager role
    await user.selectOptions(screen.getByLabelText(/^role/i), 'manager');

    // Button should now be enabled
    expect(screen.getByRole('button', { name: /create user/i })).toBeEnabled();
  });

  it('includes manager email in submission when provided for non-employee roles', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<CreateUserDialog {...defaultProps} onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/name/i), 'John Doe');
    await user.type(screen.getByLabelText(/^email/i), 'john@ubs.com');
    await user.selectOptions(screen.getByLabelText(/^role/i), 'manager');
    await user.type(screen.getByLabelText(/manager email/i), 'director@ubs.com');

    const submitButton = screen.getByRole('button', { name: /create user/i });
    await user.click(submitButton);

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'John Doe',
      email: 'john@ubs.com',
      role: 'manager',
      managerEmail: 'director@ubs.com',
    });
  });

  it('does not close dialog when clicking outside', async () => {
    const user = userEvent.setup();
    const onOpenChange = vi.fn();
    const { container } = render(<CreateUserDialog {...defaultProps} onOpenChange={onOpenChange} />);

    // Try to click on the overlay/backdrop
    const overlay = container.querySelector('[data-radix-dialog-overlay]');
    if (overlay) {
      await user.click(overlay);
    }

    // onOpenChange should not be called with false
    expect(onOpenChange).not.toHaveBeenCalledWith(false);
  });

  it('does not close dialog when pressing Escape key', async () => {
    const user = userEvent.setup();
    const onOpenChange = vi.fn();
    render(<CreateUserDialog {...defaultProps} onOpenChange={onOpenChange} />);

    // Press Escape key
    await user.keyboard('{Escape}');

    // onOpenChange should not be called with false
    expect(onOpenChange).not.toHaveBeenCalledWith(false);
  });

  it('resets form and closes dialog after successful submission', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    const onOpenChange = vi.fn();
    render(<CreateUserDialog {...defaultProps} onSubmit={onSubmit} onOpenChange={onOpenChange} />);

    await user.type(screen.getByLabelText(/name/i), 'John Doe');
    await user.type(screen.getByLabelText(/^email/i), 'john@ubs.com');
    await user.selectOptions(screen.getByLabelText(/^role/i), 'finance');

    const submitButton = screen.getByRole('button', { name: /create user/i });
    await user.click(submitButton);

    expect(onSubmit).toHaveBeenCalled();
    expect(onOpenChange).toHaveBeenCalledWith(false);
  });
});
