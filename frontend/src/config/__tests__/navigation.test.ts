import { describe, it, expect } from 'vitest';
import { getMenuItemsForRole, hasAccessToMenuItem } from '../navigation';
import type { UserRole } from '../navigation';

describe('Navigation Config', () => {
  describe('getMenuItemsForRole', () => {
    it('should return all menu items available to all roles', () => {
      const items = getMenuItemsForRole('ROLE_EMPLOYEE');

      expect(items).toEqual(
        expect.arrayContaining([
          expect.objectContaining({ id: 'dashboard' }),
          expect.objectContaining({ id: 'expenses' }),
          expect.objectContaining({ id: 'expenses-report' }),
        ])
      );
    });

    it('should return limited menu items for ROLE_EMPLOYEE', () => {
      const items = getMenuItemsForRole('ROLE_EMPLOYEE');

      // Employee has access to these
      expect(items.find(item => item.id === 'dashboard')).toBeDefined();
      expect(items.find(item => item.id === 'expenses')).toBeDefined();
      expect(items.find(item => item.id === 'expenses-report')).toBeDefined();

      // Should NOT include manager/finance items
      expect(items.find(item => item.id === 'approvals')).toBeUndefined();
      expect(items.find(item => item.id === 'analytics')).toBeUndefined();
    });

    it('should include approvals for ROLE_MANAGER', () => {
      const items = getMenuItemsForRole('ROLE_MANAGER');

      expect(items).toEqual(
        expect.arrayContaining([
          expect.objectContaining({ id: 'dashboard' }),
          expect.objectContaining({ id: 'expenses' }),
          expect.objectContaining({ id: 'expenses-report' }),
          expect.objectContaining({ id: 'approvals' }),
        ])
      );

      // Should NOT include finance-only analytics
      expect(items.find(item => item.id === 'analytics')).toBeUndefined();
    });

    it('should include analytics and users for ROLE_FINANCE', () => {
      const items = getMenuItemsForRole('ROLE_FINANCE');

      expect(items).toEqual(
        expect.arrayContaining([
          expect.objectContaining({ id: 'dashboard' }),
          expect.objectContaining({ id: 'expenses' }),
          expect.objectContaining({ id: 'expenses-report' }),
          expect.objectContaining({ id: 'approvals' }),
          expect.objectContaining({ id: 'analytics' }),
          expect.objectContaining({ id: 'users' }),
        ])
      );
    });
  });

  describe('hasAccessToMenuItem', () => {
    it('should return true when user has required role', () => {
      const result = hasAccessToMenuItem('users', 'ROLE_FINANCE');

      expect(result).toBe(true);
    });

    it('should return false when user does not have required role', () => {
      const result = hasAccessToMenuItem('analytics', 'ROLE_EMPLOYEE');

      expect(result).toBe(false);
    });

    it('should return true when user has one of multiple allowed roles', () => {
      const result = hasAccessToMenuItem('approvals', 'ROLE_MANAGER');

      expect(result).toBe(true);
    });

    it('should return false for non-existent menu item', () => {
      const result = hasAccessToMenuItem('non-existent', 'ROLE_EMPLOYEE');

      expect(result).toBe(false);
    });
  });

  describe('Menu item structure', () => {
    it('should have all required fields for each menu item', () => {
      const roles: UserRole[] = ['ROLE_MANAGER', 'ROLE_EMPLOYEE', 'ROLE_FINANCE'];

      roles.forEach(role => {
        const items = getMenuItemsForRole(role);

        items.forEach(item => {
          expect(item).toHaveProperty('id');
          expect(item).toHaveProperty('label');
          expect(item).toHaveProperty('path');
          expect(item).toHaveProperty('icon');
          expect(typeof item.id).toBe('string');
          expect(typeof item.label).toBe('string');
          expect(typeof item.path).toBe('string');
          expect(typeof item.icon).toBe('string');
        });
      });
    });
  });
});
