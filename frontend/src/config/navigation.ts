export type UserRole = 'ROLE_MANAGER' | 'ROLE_EMPLOYEE' | 'ROLE_FINANCE';

export interface MenuItem {
  id: string;
  label: string;
  path: string;
  icon: string;
  allowedRoles: UserRole[];
  children?: MenuItem[];
}

export const navigationConfig: MenuItem[] = [
  {
    id: 'dashboard',
    label: 'Dashboard',
    path: '/dashboard',
    icon: 'LayoutDashboard',
    allowedRoles: ['ROLE_MANAGER', 'ROLE_EMPLOYEE', 'ROLE_FINANCE'],
  },
  {
    id: 'expenses',
    label: 'My Expenses',
    path: '/expenses',
    icon: 'Receipt',
    allowedRoles: ['ROLE_MANAGER', 'ROLE_EMPLOYEE', 'ROLE_FINANCE'],
  },
  {
    id: 'expenses-report',
    label: 'Expenses Report',
    path: '/expenses-report',
    icon: 'ChartBar',
    allowedRoles: ['ROLE_MANAGER', 'ROLE_EMPLOYEE', 'ROLE_FINANCE'],
  },
  {
    id: 'approvals',
    label: 'Approvals',
    path: '/approvals',
    icon: 'CheckSquare',
    allowedRoles: ['ROLE_MANAGER', 'ROLE_FINANCE'],
  },
  {
    id: 'analytics',
    label: 'Analytics',
    path: '/analytics',
    icon: 'TrendingUp',
    allowedRoles: ['ROLE_FINANCE'],
  },
  {
    id: 'department',
    label: 'Department',
    path: '/department',
    icon: 'Building2',
    allowedRoles: ['ROLE_FINANCE'],
  },
  {
    id: 'users',
    label: 'User Management',
    path: '/users',
    icon: 'Users',
   allowedRoles: ['ROLE_FINANCE'],
  },
   {
    id: 'category',
    label: 'Category Management',
    path: '/category',
    icon: 'ClipboardPenLine',
   allowedRoles: ['ROLE_FINANCE'],
  }
];

export const getMenuItemsForRole = (userRole: UserRole): MenuItem[] => {
  return navigationConfig.filter((item) =>
    item.allowedRoles.includes(userRole)
  );
};

export const hasAccessToMenuItem = (
  menuItemId: string,
  userRole: UserRole
): boolean => {
  const menuItem = navigationConfig.find((item) => item.id === menuItemId);
  return menuItem ? menuItem.allowedRoles.includes(userRole) : false;
};
