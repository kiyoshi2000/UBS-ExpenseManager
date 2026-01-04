import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CreateUserDialog } from '../../src/components/Users/CreateUserDialog';
import * as departmentService from '../../src/services/department.service';
import * as userApi from '../../src/api/user.api';
import type { InternalAxiosRequestConfig } from 'axios';

// Mock the API modules
vi.mock('../../src/services/department.service');
vi.mock('../../src/api/user.api');

describe('CreateUserDialog', () => {
  const mockDepartments = [
    { id: 1, name: 'Engineering', monthlyBudget: 10000, currency: 'USD' },
    { id: 2, name: 'Finance', monthlyBudget: 15000, currency: 'USD' },
  ];

  const mockManagers = [
    { id: 1, email: 'manager1@ubs.com', name: 'Manager One', role: 'MANAGER', active: true },
    { id: 2, email: 'manager2@ubs.com', name: 'Manager Two', role: 'MANAGER', active: true },
  ];

  beforeEach(() => {
    // Reset mocks before each test
    vi.clearAllMocks();

    // Mock department service
    vi.mocked(departmentService.listDepartments).mockResolvedValue({
      data: mockDepartments,
      status: 200,
      statusText: 'OK',
      headers: {},
      config: {} as InternalAxiosRequestConfig,
    });

    // Mock user API
    vi.mocked(userApi.getManagersByDepartment).mockResolvedValue(mockManagers);
  });
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

  it('renders all form fields', async () => {
    render(<CreateUserDialog {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/^email/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/^role/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/^department/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/^manager/i)).toBeInTheDocument();
    });
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

    await waitFor(() => expect(screen.getByLabelText(/^department/i)).toBeInTheDocument());

    await user.type(screen.getByLabelText(/name/i), 'John Doe');
    await user.type(screen.getByLabelText(/^email/i), 'john@ubs.com');
    await user.selectOptions(screen.getByLabelText(/^role/i), 'manager');
    await user.selectOptions(screen.getByLabelText(/^department/i), '1');

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create user/i })).toBeEnabled();
    });
  });

  it('requires manager for employee role', async () => {
    const user = userEvent.setup();
    render(<CreateUserDialog {...defaultProps} />);

    await waitFor(() => expect(screen.getByLabelText(/^department/i)).toBeInTheDocument());

    await user.type(screen.getByLabelText(/name/i), 'John Doe');
    await user.type(screen.getByLabelText(/^email/i), 'john@ubs.com');
    await user.selectOptions(screen.getByLabelText(/^role/i), 'employee');
    await user.selectOptions(screen.getByLabelText(/^department/i), '1');

    // Wait for managers to load
    await waitFor(() => expect(screen.getByText(/manager one/i)).toBeInTheDocument());

    // Button should be disabled without manager
    expect(screen.getByRole('button', { name: /create user/i })).toBeDisabled();

    await user.selectOptions(screen.getByLabelText(/^manager/i), 'manager1@ubs.com');

    // Button should be enabled with manager
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create user/i })).toBeEnabled();
    });
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

    await waitFor(() => expect(screen.getByLabelText(/^department/i)).toBeInTheDocument());

    await user.type(screen.getByLabelText(/name/i), 'John Doe');
    await user.type(screen.getByLabelText(/^email/i), 'john@ubs.com');
    await user.selectOptions(screen.getByLabelText(/^role/i), 'finance');
    await user.selectOptions(screen.getByLabelText(/^department/i), '1');

    const submitButton = screen.getByRole('button', { name: /create user/i });
    
    await waitFor(() => expect(submitButton).toBeEnabled());
    await user.click(submitButton);

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'John Doe',
      email: 'john@ubs.com',
      role: 'finance',
      departmentId: '1',
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

  it('shows asterisk for required fields', async () => {
    render(<CreateUserDialog {...defaultProps} />);

    await waitFor(() => expect(screen.getByLabelText(/^department/i)).toBeInTheDocument());

    // Check that required field labels have asterisks
    const nameLabel = screen.getByText(/^name/i).closest('label');
    const emailLabel = screen.getByText(/^email/i).closest('label');
    const roleLabel = screen.getByText(/^role/i).closest('label');
    const departmentLabel = screen.getByText(/^department/i).closest('label');

    expect(nameLabel).toHaveTextContent('*');
    expect(emailLabel).toHaveTextContent('*');
    expect(roleLabel).toHaveTextContent('*');
    expect(departmentLabel).toHaveTextContent('*');
  });

  it('shows asterisk for manager when employee role is selected', async () => {
    const user = userEvent.setup();
    render(<CreateUserDialog {...defaultProps} />);

    await waitFor(() => expect(screen.getByLabelText(/^department/i)).toBeInTheDocument());

    // Find the manager select field and its label
    const managerSelect = screen.getByLabelText(/^manager/i);
    const managerLabel = managerSelect.previousElementSibling as HTMLElement;
    
    // Initially, manager should not have asterisk
    expect(managerLabel?.textContent).not.toMatch(/\*/);

    // Select employee role
    await user.selectOptions(screen.getByLabelText(/^role/i), 'employee');

    // Now manager should have asterisk
    await waitFor(() => {
      expect(managerLabel).toHaveTextContent('*');
    });
  });

  it('clears manager error when role changes from employee to manager', async () => {
    const user = userEvent.setup();
    render(<CreateUserDialog {...defaultProps} />);

    await waitFor(() => expect(screen.getByLabelText(/^department/i)).toBeInTheDocument());

    await user.type(screen.getByLabelText(/name/i), 'John Doe');
    await user.type(screen.getByLabelText(/^email/i), 'john@ubs.com');
    await user.selectOptions(screen.getByLabelText(/^department/i), '1');
    await user.selectOptions(screen.getByLabelText(/^role/i), 'employee');

    // Should show error for missing manager
    expect(screen.getByRole('button', { name: /create user/i })).toBeDisabled();

    // Change to manager role
    await user.selectOptions(screen.getByLabelText(/^role/i), 'manager');

    // Button should now be enabled
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create user/i })).toBeEnabled();
    });
  });

  it('includes manager in submission when provided for non-employee roles', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<CreateUserDialog {...defaultProps} onSubmit={onSubmit} />);

    await waitFor(() => expect(screen.getByLabelText(/^department/i)).toBeInTheDocument());

    await user.type(screen.getByLabelText(/name/i), 'John Doe');
    await user.type(screen.getByLabelText(/^email/i), 'john@ubs.com');
    await user.selectOptions(screen.getByLabelText(/^role/i), 'manager');
    await user.selectOptions(screen.getByLabelText(/^department/i), '1');

    // Wait for managers to load
    await waitFor(() => expect(screen.getByText(/manager one/i)).toBeInTheDocument());

    await user.selectOptions(screen.getByLabelText(/^manager/i), 'manager1@ubs.com');

    const submitButton = screen.getByRole('button', { name: /create user/i });
    await waitFor(() => expect(submitButton).toBeEnabled());
    await user.click(submitButton);

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'John Doe',
      email: 'john@ubs.com',
      role: 'manager',
      departmentId: '1',
      managerEmail: 'manager1@ubs.com',
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

  it('calls onSubmit but does not close dialog after successful submission', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    const onOpenChange = vi.fn();
    render(<CreateUserDialog {...defaultProps} onSubmit={onSubmit} onOpenChange={onOpenChange} />);

    await waitFor(() => expect(screen.getByLabelText(/^department/i)).toBeInTheDocument());

    await user.type(screen.getByLabelText(/name/i), 'John Doe');
    await user.type(screen.getByLabelText(/^email/i), 'john@ubs.com');
    await user.selectOptions(screen.getByLabelText(/^role/i), 'finance');
    await user.selectOptions(screen.getByLabelText(/^department/i), '1');

    const submitButton = screen.getByRole('button', { name: /create user/i });
    await waitFor(() => expect(submitButton).toBeEnabled());
    await user.click(submitButton);

    expect(onSubmit).toHaveBeenCalled();
    // Dialog should NOT close automatically - parent handles closing on success
    expect(onOpenChange).not.toHaveBeenCalledWith(false);
  });
});
