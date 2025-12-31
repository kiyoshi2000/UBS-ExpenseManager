import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { DashboardPage } from '../DashboardPage';
import type { UserRole } from '@/config/navigation';

vi.mock('@/hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

import { useAuth } from '@/hooks/useAuth';

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render welcome message with user email', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: 1,
        email: 'john.doe@ubs.com',
        role: 'ROLE_EMPLOYEE' as UserRole,
        name: 'John Doe',
      },
      isAuthenticated: true,
      logout: vi.fn(),
      hasRole: vi.fn(),
      hasAnyRole: vi.fn(),
    });

    render(
      <BrowserRouter>
        <DashboardPage />
      </BrowserRouter>
    );

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(
      screen.getByText(/Welcome back, john\.doe@ubs\.com!/i)
    ).toBeInTheDocument();
  });
});
