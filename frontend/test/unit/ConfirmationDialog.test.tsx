import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ConfirmationDialog } from '../../src/components/ConfirmationDialog/ConfirmationDialog';

describe('ConfirmationDialog', () => {
  const defaultProps = {
    open: true,
    onOpenChange: vi.fn(),
    title: 'Confirm Action',
    description: 'Are you sure you want to proceed?',
    onConfirm: vi.fn(),
  };

  it('renders title and description', () => {
    render(<ConfirmationDialog {...defaultProps} />);

    expect(screen.getByText('Confirm Action')).toBeInTheDocument();
    expect(screen.getByText('Are you sure you want to proceed?')).toBeInTheDocument();
  });

  it('renders with default button texts', () => {
    render(<ConfirmationDialog {...defaultProps} />);

    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Confirm' })).toBeInTheDocument();
  });

  it('renders with custom button texts', () => {
    render(
      <ConfirmationDialog
        {...defaultProps}
        confirmText="Delete"
        cancelText="Go Back"
      />
    );

    expect(screen.getByRole('button', { name: 'Go Back' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Delete' })).toBeInTheDocument();
  });

  it('calls onConfirm and onOpenChange when confirm button is clicked', async () => {
    const user = userEvent.setup();
    const onConfirm = vi.fn();
    const onOpenChange = vi.fn();

    render(
      <ConfirmationDialog
        {...defaultProps}
        onConfirm={onConfirm}
        onOpenChange={onOpenChange}
      />
    );

    const confirmButton = screen.getByRole('button', { name: 'Confirm' });
    await user.click(confirmButton);

    expect(onConfirm).toHaveBeenCalledTimes(1);
    expect(onOpenChange).toHaveBeenCalledWith(false);
  });

  it('calls onCancel and onOpenChange when cancel button is clicked', async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();
    const onOpenChange = vi.fn();

    render(
      <ConfirmationDialog
        {...defaultProps}
        onCancel={onCancel}
        onOpenChange={onOpenChange}
      />
    );

    const cancelButton = screen.getByRole('button', { name: 'Cancel' });
    await user.click(cancelButton);

    expect(onCancel).toHaveBeenCalledTimes(1);
    expect(onOpenChange).toHaveBeenCalledWith(false);
  });

  it('only calls onOpenChange when cancel is clicked without onCancel prop', async () => {
    const user = userEvent.setup();
    const onOpenChange = vi.fn();

    render(
      <ConfirmationDialog
        {...defaultProps}
        onOpenChange={onOpenChange}
      />
    );

    const cancelButton = screen.getByRole('button', { name: 'Cancel' });
    await user.click(cancelButton);

    expect(onOpenChange).toHaveBeenCalledWith(false);
  });

  it('renders icon when provided', () => {
    const icon = <span data-testid="test-icon">⚠️</span>;

    render(<ConfirmationDialog {...defaultProps} icon={icon} />);

    expect(screen.getByTestId('test-icon')).toBeInTheDocument();
  });

  it('does not render icon when not provided', () => {
    render(<ConfirmationDialog {...defaultProps} />);

    expect(screen.queryByTestId('test-icon')).not.toBeInTheDocument();
  });

  it('applies default variant styles', () => {
    render(<ConfirmationDialog {...defaultProps} />);

    const confirmButton = screen.getByRole('button', { name: 'Confirm' });
    expect(confirmButton).toHaveClass('bg-blue-600');
  });

  it('applies danger variant styles', () => {
    render(
      <ConfirmationDialog {...defaultProps} variant="danger" />
    );

    const confirmButton = screen.getByRole('button', { name: 'Confirm' });
    expect(confirmButton).toHaveClass('bg-red-600');
  });

  it('applies success variant styles', () => {
    render(
      <ConfirmationDialog {...defaultProps} variant="success" />
    );

    const confirmButton = screen.getByRole('button', { name: 'Confirm' });
    expect(confirmButton).toHaveClass('bg-green-600');
  });

  it('does not render dialog when open is false', () => {
    render(<ConfirmationDialog {...defaultProps} open={false} />);

    expect(screen.queryByText('Confirm Action')).not.toBeInTheDocument();
  });

  it('calls onOpenChange when dialog state changes', async () => {
    const user = userEvent.setup();
    const onOpenChange = vi.fn();

    render(
      <ConfirmationDialog
        {...defaultProps}
        onOpenChange={onOpenChange}
      />
    );

    const confirmButton = screen.getByRole('button', { name: 'Confirm' });
    await user.click(confirmButton);

    expect(onOpenChange).toHaveBeenCalled();
  });

  it('handles multiple rapid clicks on confirm button', async () => {
    const user = userEvent.setup();
    const onConfirm = vi.fn();

    render(
      <ConfirmationDialog
        {...defaultProps}
        onConfirm={onConfirm}
      />
    );

    const confirmButton = screen.getByRole('button', { name: 'Confirm' });
    await user.click(confirmButton);
    await user.click(confirmButton);
    await user.click(confirmButton);

    // All clicks are processed before dialog closes
    expect(onConfirm).toHaveBeenCalledTimes(3);
  });

  it('renders with complex description text', () => {
    const longDescription = 'This is a very long description that explains in detail what will happen if you proceed with this action. Please read carefully before confirming.';

    render(
      <ConfirmationDialog
        {...defaultProps}
        description={longDescription}
      />
    );

    expect(screen.getByText(longDescription)).toBeInTheDocument();
  });

  it('renders with special characters in title', () => {
    render(
      <ConfirmationDialog
        {...defaultProps}
        title="Delete user: john@example.com?"
      />
    );

    expect(screen.getByText('Delete user: john@example.com?')).toBeInTheDocument();
  });

  it('applies text-white class to confirm button', () => {
    render(<ConfirmationDialog {...defaultProps} />);

    const confirmButton = screen.getByRole('button', { name: 'Confirm' });
    expect(confirmButton).toHaveClass('text-white');
  });

  it('cancel button has proper styling classes', () => {
    render(<ConfirmationDialog {...defaultProps} />);

    const cancelButton = screen.getByRole('button', { name: 'Cancel' });
    expect(cancelButton).toHaveClass('border-gray-300');
    expect(cancelButton).toHaveClass('bg-white');
  });
});
