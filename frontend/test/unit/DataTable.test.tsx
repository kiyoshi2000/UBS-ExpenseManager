import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DataTable, ColumnDef, RowAction } from '../../src/components/DataTable/DataTable';

interface TestData {
  id: number;
  name: string;
  email: string;
  status: string;
}

const mockData: TestData[] = [
  { id: 1, name: 'John Doe', email: 'john@example.com', status: 'Active' },
  { id: 2, name: 'Jane Smith', email: 'jane@example.com', status: 'Inactive' },
  { id: 3, name: 'Bob Johnson', email: 'bob@example.com', status: 'Active' },
];

const mockColumns: ColumnDef<TestData>[] = [
  { key: 'name', label: 'Name' },
  { key: 'email', label: 'Email' },
  { key: 'status', label: 'Status' },
];

describe('DataTable', () => {
  it('renders columns correctly', () => {
    render(<DataTable columns={mockColumns} data={mockData} />);

    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Email')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();
  });

  it('renders data correctly', () => {
    render(<DataTable columns={mockColumns} data={mockData} />);

    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    expect(screen.getByText('jane@example.com')).toBeInTheDocument();
    expect(screen.getByText('Bob Johnson')).toBeInTheDocument();
    expect(screen.getByText('bob@example.com')).toBeInTheDocument();
  });

  it('displays message when there is no data', () => {
    render(<DataTable columns={mockColumns} data={[]} />);

    expect(screen.getByText('Nenhum dado disponível')).toBeInTheDocument();
  });

  it('displays custom message when there is no data', () => {
    render(
      <DataTable 
        columns={mockColumns} 
        data={[]} 
        emptyMessage="No users found" 
      />
    );

    expect(screen.getByText('No users found')).toBeInTheDocument();
  });

  it('renders actions column when actions are provided', () => {
    const mockActions: RowAction<TestData>[] = [
      {
        label: 'Edit',
        icon: <span>Edit Icon</span>,
        onClick: vi.fn(),
        color: 'blue',
      },
    ];

    render(<DataTable columns={mockColumns} data={mockData} actions={mockActions} />);

    expect(screen.getByText('Ações')).toBeInTheDocument();
  });

  it('does not render actions column when actions are not provided', () => {
    render(<DataTable columns={mockColumns} data={mockData} />);

    expect(screen.queryByText('Ações')).not.toBeInTheDocument();
  });

  it('calls onClick when action is clicked', async () => {
    const user = userEvent.setup();
    const handleClick = vi.fn();
    const mockActions: RowAction<TestData>[] = [
      {
        label: 'Edit',
        icon: <span>✏️</span>,
        onClick: handleClick,
        color: 'blue',
      },
    ];

    render(<DataTable columns={mockColumns} data={mockData} actions={mockActions} />);

    const buttons = screen.getAllByTitle('Edit');
    await user.click(buttons[0]);

    expect(handleClick).toHaveBeenCalledTimes(1);
    expect(handleClick).toHaveBeenCalledWith(mockData[0]);
  });

  it('renders multiple actions per row', () => {
    const mockActions: RowAction<TestData>[] = [
      {
        label: 'Edit',
        icon: <span>✏️</span>,
        onClick: vi.fn(),
        color: 'blue',
      },
      {
        label: 'Delete',
        icon: <span>🗑️</span>,
        onClick: vi.fn(),
        color: 'red',
      },
    ];

    render(<DataTable columns={mockColumns} data={mockData} actions={mockActions} />);

    expect(screen.getAllByTitle('Edit')).toHaveLength(3);
    expect(screen.getAllByTitle('Delete')).toHaveLength(3);
  });

  it('applies custom render when provided', () => {
    const columnsWithRender: ColumnDef<TestData>[] = [
      {
        key: 'name',
        label: 'Name',
        render: (value) => <strong>{String(value)}</strong>,
      },
      { key: 'email', label: 'Email' },
    ];

    render(<DataTable columns={columnsWithRender} data={mockData} />);

    const strongElements = screen.getAllByText('John Doe');
    expect(strongElements[0].tagName).toBe('STRONG');
  });

  it('filters actions based on shouldShow', () => {
    const mockActions: RowAction<TestData>[] = [
      {
        label: 'Edit',
        icon: <span>✏️</span>,
        onClick: vi.fn(),
        color: 'blue',
      },
      {
        label: 'Delete',
        icon: <span>🗑️</span>,
        onClick: vi.fn(),
        color: 'red',
        shouldShow: (row) => row.status === 'Active',
      },
    ];

    render(<DataTable columns={mockColumns} data={mockData} actions={mockActions} />);

    // Edit should appear in all 3 rows
    expect(screen.getAllByTitle('Edit')).toHaveLength(3);
    
    // Delete should appear only in rows with Active status (2 rows)
    expect(screen.getAllByTitle('Delete')).toHaveLength(2);
  });

  it('applies custom className', () => {
    const { container } = render(
      <DataTable 
        columns={mockColumns} 
        data={mockData} 
        className="custom-class" 
      />
    );

    const tableWrapper = container.querySelector('.custom-class');
    expect(tableWrapper).toBeInTheDocument();
  });

  it('applies custom rowClassName', () => {
    render(
      <DataTable 
        columns={mockColumns} 
        data={mockData} 
        rowClassName="custom-row-class" 
      />
    );

    const rows = screen.getAllByRole('row');
    // Skip the header row (index 0)
    expect(rows[1]).toHaveClass('custom-row-class');
  });

  it('renders multiple columns with different data types', () => {
    const mixedData = [
      { id: 1, name: 'Test', count: 42, active: true },
    ];

    const mixedColumns: ColumnDef<typeof mixedData[0]>[] = [
      { key: 'name', label: 'Name' },
      { key: 'count', label: 'Count' },
      { key: 'active', label: 'Active' },
    ];

    render(<DataTable columns={mixedColumns} data={mixedData} />);

    expect(screen.getByText('Test')).toBeInTheDocument();
    expect(screen.getByText('42')).toBeInTheDocument();
    expect(screen.getByText('true')).toBeInTheDocument();
  });

  it('applies custom width to columns', () => {
    const columnsWithWidth: ColumnDef<TestData>[] = [
      { key: 'name', label: 'Name', width: '200px' },
      { key: 'email', label: 'Email' },
    ];

    const { container } = render(
      <DataTable columns={columnsWithWidth} data={mockData} />
    );

    const nameHeader = container.querySelector('th');
    expect(nameHeader).toHaveStyle({ width: '200px' });
  });

  it('applies different colors to actions', () => {
    const mockActions: RowAction<TestData>[] = [
      {
        label: 'Edit',
        icon: <span>✏️</span>,
        onClick: vi.fn(),
        color: 'blue',
      },
      {
        label: 'Delete',
        icon: <span>🗑️</span>,
        onClick: vi.fn(),
        color: 'red',
      },
      {
        label: 'Approve',
        icon: <span>✓</span>,
        onClick: vi.fn(),
        color: 'green',
      },
    ];

    const { container } = render(
      <DataTable columns={mockColumns} data={mockData} actions={mockActions} />
    );

    const blueAction = container.querySelector('.text-blue-600');
    const redAction = container.querySelector('.text-red-600');
    const greenAction = container.querySelector('.text-green-600');

    expect(blueAction).toBeInTheDocument();
    expect(redAction).toBeInTheDocument();
    expect(greenAction).toBeInTheDocument();
  });
});
