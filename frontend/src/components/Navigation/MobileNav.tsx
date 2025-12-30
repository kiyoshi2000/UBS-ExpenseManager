import { getMenuItemsForRole } from '@/config/navigation';
import { useAuth } from '@/hooks/useAuth';
import { useTheme } from '@/hooks/useTheme';
import { cn } from '@/lib/utils';
import { Menu, Moon, Sun, X } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MenuItem } from './MenuItem';
import { Logo } from './Logo';
import { ProfileDropdown } from '../Layout/ProfileDropdown';

export const MobileNav = () => {
  const [isOpen, setIsOpen] = useState(false);
  const { isDarkMode, toggleDarkMode } = useTheme();
  const { user } = useAuth();
  const navigate = useNavigate();

  if (!user) {
    return null;
  }

  const menuItems = getMenuItemsForRole(user.role);

  const handleMenuItemClick = () => {
    setIsOpen(false);
  };

  const handleLogoClick = () => {
    navigate('/dashboard');
  };

  return (
    <>
      <header className='fixed left-0 right-0 top-0 z-50 flex h-16 items-center justify-between border-b border-gray-200 bg-white px-4 dark:border-gray-800 dark:bg-gray-900 md:hidden'>
        <div className='flex-1 flex justify-center'>
          <button
            onClick={handleLogoClick}
            className='bg-transparent border-none p-0 cursor-pointer hover:opacity-80 transition-opacity'
            aria-label='Go to dashboard'
          >
            <Logo className='h-10 pb-1' />
          </button>
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

        <ProfileDropdown />

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
