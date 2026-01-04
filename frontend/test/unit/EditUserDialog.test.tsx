import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { EditUserDialog } from '../../src/components/Users/EditUserDialog';
import * as departmentService from '../../src/services/department.service';
import * as userApi from '../../src/api/user.api';
import { InternalAxiosRequestConfig } from 'axios';

// Mock the API modules
vi.mock('../../src/services/department.service');
vi.mock('../../src/api/user.api');

describe('EditUserDialog', () => {
  const mockDepartments = [
    { id: 1, name: 'Engineering', monthlyBudget: 10000, currency: 'USD' },
    { id: 2, name: 'Finance', monthlyBudget: 15000, currency: 'USD' },
  ];

  const mockManagers = [
    { id: 1, email: 'manager1@ubs.com', name: 'Manager One', role: 'MANAGER', active: true },
    { id: 2, email: 'manager2@ubs.com', name: 'Manager Two', role: 'MANAGER', active: true },
  ];

  const mockUser = {
    id: 10,
    name: 'John Doe',
    email: 'john@ubs.com',
    role: 'EMPLOYEE',
    department: 'Engineering',
    manager: {
      id: 1,
      name: 'Manager One',
      email: 'manager1@ubs.com',
    },
  };

  const defaultProps = {
    open: true,
    onOpenChange: vi.fn(),
    onSubmit: vi.fn(),
    user: mockUser,
  };

  beforeEach(() => {
    vi.clearAllMocks();

    vi.mocked(departmentService.listDepartments).mockResolvedValue({
      data: mockDepartments,
      status: 200,
      statusText: 'OK',
      headers: {},
      config: {} as InternalAxiosRequestConfig,
    });

    vi.mocked(userApi.getManagersByDepartment).mockResolvedValue(mockManagers);
  });

  it('renders dialog title and description', () => {
    render(<EditUserDialog {...defaultProps} />);

    expect(screen.getByText('Edit User')).toBeInTheDocument();
    expect(screen.getByText('Update the user information below')).toBeInTheDocument();
  });

  it('renders all form fields', async () => {
    render(<EditUserDialog {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByLabelText(/^email/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/^role/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/^department/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/^manager/i)).toBeInTheDocument();
    });
  });

  it('renders Cancel and Save Changes buttons', () => {
    render(<EditUserDialog {...defaultProps} />);

    expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /save changes/i })).toBeInTheDocument();
  });

  it('pre-fills form with user data', async () => {
    render(<EditUserDialog {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByLabelText(/^email/i)).toHaveValue('john@ubs.com');
      expect(screen.getByLabelText(/^role/i)).toHaveValue('EMPLOYEE');
      expect(screen.getByLabelText(/name/i)).toHaveValue('John Doe');
    });

    // Also verify department and manager are loaded
    await waitFor(() => {
      expect(screen.getByLabelText(/^department/i)).toHaveValue('1');
      expect(screen.getByLabelText(/^manager/i)).toHaveValue('manager1@ubs.com');
    });
  });

  it('displays email and role as read-only fields', async () => {
    render(<EditUserDialog {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByLabelText(/^email/i)).toBeDisabled();
      expect(screen.getByLabelText(/^role/i)).toBeDisabled();
    });

    const readOnlyTexts = screen.getAllByText(/this field cannot be edited/i);
    expect(readOnlyTexts).toHaveLength(2);
  });

  it('loads managers when dialog opens', async () => {
    render(<EditUserDialog {...defaultProps} />);

    await waitFor(() => {
      expect(userApi.getManagersByDepartment).toHaveBeenCalledWith(1);
      expect(screen.getByText(/manager one/i)).toBeInTheDocument();
      expect(screen.getByText(/manager two/i)).toBeInTheDocument();
    });
  });

  it('enables Save Changes button with valid data', async () => {
    render(<EditUserDialog {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /save changes/i })).toBeEnabled();
    });
  });

  it('disables Save Changes button when name is empty', async () => {
    const user = userEvent.setup();
    render(<EditUserDialog {...defaultProps} />);

    await waitFor(() => expect(screen.getByLabelText(/name/i)).toBeInTheDocument());

    const nameInput = screen.getByLabelText(/name/i);
    await user.clear(nameInput);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /save changes/i })).toBeDisabled();
    });
  });

  it('shows validation error for empty name', async () => {
    const user = userEvent.setup();
    render(<EditUserDialog {...defaultProps} />);

    await waitFor(() => expect(screen.getByLabelText(/name/i)).toBeInTheDocument());

    const nameInput = screen.getByLabelText(/name/i);
    await user.clear(nameInput);

    expect(screen.getByText(/name is required/i)).toBeInTheDocument();
  });

  it('requires manager for employee role', async () => {
    render(<EditUserDialog {...defaultProps} />);

    await waitFor(() => {
      const managerLabel = screen.getByLabelText(/^manager/i).previousElementSibling as HTMLElement;
      expect(managerLabel).toHaveTextContent('*');
    });
  });

  it('does not require manager for non-employee roles', async () => {
    const managerUser = {
      ...mockUser,
      role: 'MANAGER',
    };

    render(<EditUserDialog {...defaultProps} user={managerUser} />);

    await waitFor(() => {
      const managerLabel = screen.getByLabelText(/^manager/i).previousElementSibling as HTMLElement;
      expect(managerLabel?.textContent).not.toMatch(/Manager.*\*/);
    });
  });

  it('loads new managers when department changes', async () => {
    const user = userEvent.setup();
    render(<EditUserDialog {...defaultProps} />);

    await waitFor(() => expect(screen.getByLabelText(/^department/i)).toBeInTheDocument());

    const departmentSelect = screen.getByLabelText(/^department/i);
    await user.selectOptions(departmentSelect, '2'); // Select Finance

    await waitFor(() => {
      expect(userApi.getManagersByDepartment).toHaveBeenCalledWith(2);
    });
  });

  it('calls onSubmit with updated data when form is submitted', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<EditUserDialog {...defaultProps} onSubmit={onSubmit} />);

    await waitFor(() => expect(screen.getByLabelText(/name/i)).toBeInTheDocument());

    // Wait for department and manager fields to load
    await waitFor(() => {
      const departmentSelect = screen.getByLabelText(/^department/i);
      expect(departmentSelect).toHaveValue('1');
    });

    await waitFor(() => {
      const managerSelect = screen.getByLabelText(/^manager/i);
      expect(managerSelect).toHaveValue('manager1@ubs.com');
    });

    const nameInput = screen.getByLabelText(/name/i);
    await user.clear(nameInput);
    await user.type(nameInput, 'Jane Smith');

    const submitButton = screen.getByRole('button', { name: /save changes/i });
    await waitFor(() => expect(submitButton).toBeEnabled());
    await user.click(submitButton);

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'Jane Smith',
      departmentId: '1',
      managerEmail: 'manager1@ubs.com',
    });
  });

  it('calls onOpenChange(false) when Cancel is clicked', async () => {
    const user = userEvent.setup();
    const onOpenChange = vi.fn();
    render(<EditUserDialog {...defaultProps} onOpenChange={onOpenChange} />);

    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    await user.click(cancelButton);

    expect(onOpenChange).toHaveBeenCalledWith(false);
  });

  it('does not render dialog when open is false', () => {
    render(<EditUserDialog {...defaultProps} open={false} />);

    expect(screen.queryByText('Edit User')).not.toBeInTheDocument();
  });

  it('does not render dialog when user is null', () => {
    render(<EditUserDialog {...defaultProps} user={null} />);

    expect(screen.queryByText('Edit User')).toBeInTheDocument();
  });

  it('displays error message when error prop is provided', () => {
    const errorMessage = 'Failed to update user';
    render(<EditUserDialog {...defaultProps} error={errorMessage} />);

    expect(screen.getByText(/error updating user/i)).toBeInTheDocument();
    expect(screen.getByText(errorMessage)).toBeInTheDocument();
  });

  it('does not close dialog when clicking outside', async () => {
    const user = userEvent.setup();
    const onOpenChange = vi.fn();
    const { container } = render(<EditUserDialog {...defaultProps} onOpenChange={onOpenChange} />);

    const overlay = container.querySelector('[data-radix-dialog-overlay]');
    if (overlay) {
      await user.click(overlay);
    }

    expect(onOpenChange).not.toHaveBeenCalledWith(false);
  });

  it('does not close dialog when pressing Escape key', async () => {
    const user = userEvent.setup();
    const onOpenChange = vi.fn();
    render(<EditUserDialog {...defaultProps} onOpenChange={onOpenChange} />);

    await user.keyboard('{Escape}');

    expect(onOpenChange).not.toHaveBeenCalledWith(false);
  });

  it('allows updating manager to empty for non-employee roles', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    const managerUser = {
      ...mockUser,
      role: 'MANAGER',
    };

    render(<EditUserDialog {...defaultProps} user={managerUser} onSubmit={onSubmit} />);

    // Wait for form to be fully loaded including manager
    await waitFor(() => {
      expect(screen.getByLabelText(/^name/i)).toHaveValue('John Doe');
    });

    const managerSelect = screen.getByLabelText(/^manager/i);
    
    // Verify initial manager is set
    await waitFor(() => {
      expect(managerSelect).toHaveValue('manager1@ubs.com');
    });
    
    // Select empty option to clear manager
    await user.selectOptions(managerSelect, '');

    // Wait for submit button to be enabled
    const submitButton = screen.getByRole('button', { name: /save changes/i });
    await waitFor(() => expect(submitButton).toBeEnabled());
    
    // Submit the form
    await user.click(submitButton);

    // Verify the form was submitted with empty manager
    expect(onSubmit).toHaveBeenCalledTimes(1);
    expect(onSubmit).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'John Doe',
        departmentId: '1',
        managerEmail: '',
      })
    );
  });

  it('resets form data when dialog closes and reopens', async () => {
    const user = userEvent.setup();
    const { rerender } = render(<EditUserDialog {...defaultProps} />);

    await waitFor(() => expect(screen.getByLabelText(/name/i)).toHaveValue('John Doe'));

    // Wait for all fields to load initially
    await waitFor(() => {
      expect(screen.getByLabelText(/^manager/i)).toHaveValue('manager1@ubs.com');
    });

    // Modify the name
    const nameInput = screen.getByLabelText(/name/i);
    await user.clear(nameInput);
    await user.type(nameInput, 'Modified Name');

    // Close dialog
    rerender(<EditUserDialog {...defaultProps} open={false} />);

    // Reopen dialog
    rerender(<EditUserDialog {...defaultProps} open={true} />);

    // Wait for form to reset
    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toHaveValue('John Doe');
    });

    // Verify manager also reset
    await waitFor(() => {
      expect(screen.getByLabelText(/^manager/i)).toHaveValue('manager1@ubs.com');
    });
  });
});
