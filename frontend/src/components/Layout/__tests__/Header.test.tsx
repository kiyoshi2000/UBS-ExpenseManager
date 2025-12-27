import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { Header } from '../Header';
import type { UserRole } from '@/config/navigation';

vi.mock('@/hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

import { useAuth } from '@/hooks/useAuth';

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;

describe('Header', () => {
  const mockLogout = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should not render when user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      isAuthenticated: false,
      logout: mockLogout,
      hasRole: vi.fn(),
      hasAnyRole: vi.fn(),
    });

    const { container } = render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );

    expect(container.firstChild).toBeNull();
  });

  it('should render user avatar with initial of email', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        email: 'john.doe@ubs.com',
        role: 'ROLE_EMPLOYEE' as UserRole,
      },
      isAuthenticated: true,
      logout: mockLogout,
      hasRole: vi.fn(),
      hasAnyRole: vi.fn(),
    });

    render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );

    expect(screen.getByText('J')).toBeInTheDocument();
    expect(screen.getByText('john.doe@ubs.com')).toBeInTheDocument();
    expect(screen.getByText('ROLE_EMPLOYEE')).toBeInTheDocument();
  });

  it('should render user avatar with initial of name if name is available', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        email: 'john.doe@ubs.com',
        role: 'ROLE_EMPLOYEE' as UserRole,
        name: 'Mock User',
      },
      isAuthenticated: true,
      logout: mockLogout,
      hasRole: vi.fn(),
      hasAnyRole: vi.fn(),
    });

    render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );

    expect(screen.getByText('M')).toBeInTheDocument();
    expect(screen.getByText('Mock User')).toBeInTheDocument();
    expect(screen.getByText('ROLE_EMPLOYEE')).toBeInTheDocument();
  });

  it('should open dropdown menu when profile is clicked', async () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        email: 'admin@ubs.com',
        role: 'ROLE_ADMIN' as UserRole,
        name: 'Admin',
      },
      isAuthenticated: true,
      logout: mockLogout,
      hasRole: vi.fn(),
      hasAnyRole: vi.fn(),
    });

    render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );

    // Get the profile button (not the dark mode toggle)
    const profileButton = screen.getByText('Admin').closest('button');
    fireEvent.click(profileButton!);

    await waitFor(() => {
      expect(screen.getByText('Profile Page')).toBeInTheDocument();
      expect(screen.getByText('Logout')).toBeInTheDocument();
    });
  });

  it('should navigate to profile page when Profile Page is clicked', async () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        email: 'user@ubs.com',
        role: 'ROLE_EMPLOYEE' as UserRole,
        name: 'User',
      },
      isAuthenticated: true,
      logout: mockLogout,
      hasRole: vi.fn(),
      hasAnyRole: vi.fn(),
    });

    render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );

    // Open dropdown
    const profileButton = screen.getByText('User').closest('button');
    fireEvent.click(profileButton!);

    // Click Profile Page
    await waitFor(() => {
      const profilePageButton = screen.getByText('Profile Page');
      fireEvent.click(profilePageButton);
    });

    expect(mockNavigate).toHaveBeenCalledWith('/profile');
  });

  it('should call logout when Logout is clicked', async () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        email: 'user@ubs.com',
        role: 'ROLE_EMPLOYEE' as UserRole,
        name: 'User',
      },
      isAuthenticated: true,
      logout: mockLogout,
      hasRole: vi.fn(),
      hasAnyRole: vi.fn(),
    });

    render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );

    // Open dropdown
    const profileButton = screen.getByText('User').closest('button');
    fireEvent.click(profileButton!);

    // Click Logout
    await waitFor(() => {
      const logoutButton = screen.getByText('Logout');
      fireEvent.click(logoutButton);
    });

    expect(mockLogout).toHaveBeenCalled();
  });

  it('should close dropdown when clicking outside', async () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        email: 'user@ubs.com',
        role: 'ROLE_EMPLOYEE' as UserRole,
        name: 'User',
      },
      isAuthenticated: true,
      logout: mockLogout,
      hasRole: vi.fn(),
      hasAnyRole: vi.fn(),
    });

    render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );

    // Open dropdown
    const profileButton = screen.getByText('User').closest('button');
    fireEvent.click(profileButton!);

    await waitFor(() => {
      expect(screen.getByText('Profile Page')).toBeInTheDocument();
    });

    // Click outside
    fireEvent.mouseDown(document.body);

    await waitFor(() => {
      expect(screen.queryByText('Profile Page')).not.toBeInTheDocument();
    });
  });
});
