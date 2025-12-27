import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { Sidebar } from '../Sidebar';
import type { UserRole } from '@/config/navigation';

vi.mock('@/hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('@/config/navigation', () => ({
  getMenuItemsForRole: vi.fn(),
}));

import { useAuth } from '@/hooks/useAuth';
import { getMenuItemsForRole } from '@/config/navigation';

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;
const mockGetMenuItemsForRole = getMenuItemsForRole as ReturnType<typeof vi.fn>;

describe('Sidebar', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should not render when user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      isAuthenticated: false,
      logout: vi.fn(),
      hasRole: vi.fn(),
      hasAnyRole: vi.fn(),
    });

    const { container } = render(
      <BrowserRouter>
        <Sidebar />
      </BrowserRouter>
    );

    expect(container.firstChild).toBeNull();
  });

  it('should render sidebar with menu items for authenticated user', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        email: 'admin@ubs.com',
        role: 'ROLE_ADMIN' as UserRole,
        name: 'Admin User',
      },
      isAuthenticated: true,
      logout: vi.fn(),
      hasRole: vi.fn(),
      hasAnyRole: vi.fn(),
    });

    mockGetMenuItemsForRole.mockReturnValue([
      {
        id: 'dashboard',
        label: 'Dashboard',
        path: '/dashboard',
        icon: 'LayoutDashboard',
      },
      { id: 'users', label: 'User Management', path: '/users', icon: 'Users' },
    ]);

    render(
      <BrowserRouter>
        <Sidebar />
      </BrowserRouter>
    );

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('User Management')).toBeInTheDocument();
  });

  it('should render UBS logo', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        email: 'user@ubs.com',
        role: 'ROLE_EMPLOYEE' as UserRole,
        name: 'Employee',
      },
      isAuthenticated: true,
      logout: vi.fn(),
      hasRole: vi.fn(),
      hasAnyRole: vi.fn(),
    });

    mockGetMenuItemsForRole.mockReturnValue([]);

    render(
      <BrowserRouter>
        <Sidebar />
      </BrowserRouter>
    );

    const logos = screen.getAllByAltText('UBS');
    expect(logos).toHaveLength(2); // Light and dark mode logos
    expect(logos[0]).toBeInTheDocument();
  });

  it('should render different menu items for different roles', () => {
    const employeeMenuItems = [
      {
        id: 'dashboard',
        label: 'Dashboard',
        path: '/dashboard',
        icon: 'LayoutDashboard',
      },
      {
        id: 'expenses',
        label: 'My Expenses',
        path: '/expenses',
        icon: 'Receipt',
      },
    ];

    mockGetMenuItemsForRole.mockReturnValue(employeeMenuItems);

    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        email: 'employee@ubs.com',
        role: 'ROLE_EMPLOYEE' as UserRole,
        name: 'Employee',
      },
      isAuthenticated: true,
      logout: vi.fn(),
      hasRole: vi.fn(),
      hasAnyRole: vi.fn(),
    });

    render(
      <BrowserRouter>
        <Sidebar />
      </BrowserRouter>
    );

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('My Expenses')).toBeInTheDocument();
    expect(screen.queryByText('User Management')).not.toBeInTheDocument();
  });
});
