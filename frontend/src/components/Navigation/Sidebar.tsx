import ubsLogo from '@/assets/ubs-logo.svg';
import { getMenuItemsForRole } from '@/config/navigation';
import { useAuth } from '@/hooks/useAuth';
import { cn } from '@/lib/utils';
import { useEffect, useRef, useState } from 'react';
import { MenuItem } from './MenuItem';

interface SidebarProps {
  className?: string;
}

export const Sidebar = ({ className }: SidebarProps) => {
  const { user } = useAuth();
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const profileMenuRef = useRef<HTMLDivElement>(null);

  if (!user) {
    return null;
  }

  const menuItems = getMenuItemsForRole(user.role);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        profileMenuRef.current &&
        !profileMenuRef.current.contains(event.target as Node)
      ) {
        setIsProfileMenuOpen(false);
      }
    };

    if (isProfileMenuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      return () =>
        document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [isProfileMenuOpen]);

  return (
    <aside
      className={cn(
        'fixed left-0 top-0 z-40 h-screen w-64 border-r border-gray-200 bg-white dark:border-gray-800 dark:bg-gray-900',
        className
      )}
    >
      <div className='flex h-16 items-center border-b border-gray-200 px-6 dark:border-gray-800'>
        <img src={ubsLogo} alt='UBS' className='h-10 mx-auto my-90 pb-1' />
      </div>

      <nav className='flex-1 space-y-1 px-3 py-4'>
        {menuItems.map((item) => (
          <MenuItem
            key={item.id}
            label={item.label}
            path={item.path}
            icon={item.icon}
          />
        ))}
      </nav>
    </aside>
  );
};
