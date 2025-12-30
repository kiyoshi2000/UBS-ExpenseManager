import { getMenuItemsForRole } from '@/config/navigation';
import { useAuth } from '@/hooks/useAuth';
import { useTheme } from '@/hooks/useTheme';
import { cn } from '@/lib/utils';
import { ChevronDown, LogOut, Menu, Moon, Sun, User, X } from 'lucide-react';
import { useRef, useState } from 'react';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { MenuItem } from './MenuItem';
import { Logo } from './Logo';

export const MobileNav = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const { isDarkMode, toggleDarkMode } = useTheme();
  const { user, logout } = useAuth();
  const profileMenuRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

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

  const handleMenuItemClick = () => {
    setIsOpen(false);
  };

  const handleProfileClick = () => {
    setIsProfileMenuOpen(!isProfileMenuOpen);
  };

  const handleProfilePageClick = () => {
    setIsProfileMenuOpen(false);
    setIsOpen(false);
    navigate('/profile');
  };

  const handleLogoutClick = () => {
    setIsProfileMenuOpen(false);
    setIsOpen(false);
    logout();
  };

  return (
    <>
      <header className='fixed left-0 right-0 top-0 z-50 flex h-16 items-center justify-between border-b border-gray-200 bg-white px-4 dark:border-gray-800 dark:bg-gray-900 md:hidden'>
        <div className='flex items-center gap-3'>
          <Logo className='h-10 pb-1' />
        </div>

        <button
          onClick={() => setIsOpen(!isOpen)}
          className='rounded-lg p-2 text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800'
          aria-label='Toggle menu'
        >
          {isOpen ? <X className='h-6 w-6' /> : <Menu className='h-6 w-6' />}
        </button>
      </header>

      {isOpen && (
        <div
          className='fixed inset-0 z-40 bg-black/50 md:hidden'
          onClick={() => setIsOpen(false)}
        />
      )}

      <aside
        className={cn(
          'fixed right-0 top-0 z-50 h-screen w-72 transform border-l border-gray-200 bg-white transition-transform duration-300 ease-in-out dark:border-gray-800 dark:bg-gray-900 md:hidden',
          isOpen ? 'translate-x-0' : 'translate-x-full'
        )}
      >
        <div className='flex h-16 items-center justify-between border-b border-gray-200 px-4 dark:border-gray-800'>
          <div className='flex items-center gap-2'>
            <button
              onClick={toggleDarkMode}
              className='rounded-lg p-2 text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800'
              aria-label='Toggle dark mode'
            >
              {isDarkMode ? (
                <Sun className='h-5 w-5' />
              ) : (
                <Moon className='h-5 w-5' />
              )}
            </button>
          </div>

          <button
            onClick={() => setIsOpen(false)}
            className='rounded-lg p-2 text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800'
          >
            <X className='h-6 w-6' />
          </button>
        </div>

        <div
          className='relative border-b border-gray-200 px-4 py-4 dark:border-gray-800'
          ref={profileMenuRef}
        >
          <button
            onClick={handleProfileClick}
            className='flex w-full items-center gap-3 rounded-lg transition-colors hover:bg-gray-50 dark:hover:bg-gray-800 p-2 -ml-2'
          >
            <div className='flex h-10 w-10 items-center justify-center rounded-full bg-red-100 text-red-600 dark:bg-red-900 dark:text-red-300'>
              <span className='text-sm font-semibold'>
                {user.email.charAt(0).toUpperCase()}
              </span>
            </div>
            <div className='flex-1 min-w-0 text-left'>
              <p className='truncate text-sm font-medium text-gray-900 dark:text-white'>
                {user.name || user.email}
              </p>
              <p className='truncate text-xs text-gray-500 dark:text-gray-400'>
                {user.email}
              </p>
            </div>
            <ChevronDown
              className={cn(
                'h-4 w-4 text-gray-400 transition-transform',
                isProfileMenuOpen && 'rotate-180'
              )}
            />
          </button>

          {isProfileMenuOpen && (
            <div className='absolute left-4 right-4 top-full mt-2 z-50 rounded-lg border border-gray-200 bg-white shadow-lg dark:border-gray-700 dark:bg-gray-800'>
              <div className='py-1'>
                <button
                  onClick={handleProfilePageClick}
                  className='flex w-full items-center gap-3 px-4 py-2.5 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 dark:text-gray-200 dark:hover:bg-gray-700'
                >
                  <User className='h-4 w-4' />
                  <span>Profile Page</span>
                </button>
                <div className='border-t border-gray-100 dark:border-gray-700' />
                <button
                  onClick={handleLogoutClick}
                  className='flex w-full items-center gap-3 px-4 py-2.5 text-sm font-medium text-red-600 transition-colors hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-950'
                >
                  <LogOut className='h-4 w-4' />
                  <span>Logout</span>
                </button>
              </div>
            </div>
          )}
        </div>

        <nav className='flex-1 space-y-1 px-3 py-4'>
          {menuItems.map((item) => (
            <MenuItem
              key={item.id}
              label={item.label}
              path={item.path}
              icon={item.icon}
              onClick={handleMenuItemClick}
            />
          ))}
        </nav>
      </aside>
    </>
  );
};
