import { useAuth } from '@/hooks/useAuth';
import { cn } from '@/lib/utils';
import { ChevronDown, LogOut, User, Moon, Sun } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export const Header = () => {
  const { user, logout } = useAuth();
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState(() => {
    if (typeof window !== 'undefined') {
      return document.documentElement.classList.contains('dark');
    }
    return false;
  });
  const profileMenuRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  if (!user) {
    return null;
  }

  useEffect(() => {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
      document.documentElement.classList.add('dark');
      setIsDarkMode(true);
    } else if (savedTheme === 'light') {
      document.documentElement.classList.remove('dark');
      setIsDarkMode(false);
    }
  }, []);

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

  const toggleDarkMode = () => {
    const newDarkMode = !isDarkMode;
    setIsDarkMode(newDarkMode);

    if (newDarkMode) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  };

  const handleProfileClick = () => {
    setIsProfileMenuOpen(!isProfileMenuOpen);
  };

  const handleProfilePageClick = () => {
    setIsProfileMenuOpen(false);
    navigate('/profile');
  };

  const handleLogoutClick = () => {
    setIsProfileMenuOpen(false);
    logout();
  };

  return (
    <header className='fixed right-0 top-0 z-30 flex h-16 items-center justify-end gap-4 border-b border-gray-200 bg-white px-6 dark:border-gray-800 dark:bg-gray-900 md:left-64'>
      <button
        onClick={toggleDarkMode}
        className='rounded-lg p-2 text-gray-600 transition-colors hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800'
        aria-label='Toggle dark mode'
      >
        {isDarkMode ? (
          <Sun className='h-5 w-5' />
        ) : (
          <Moon className='h-5 w-5' />
        )}
      </button>

      <div className='relative' ref={profileMenuRef}>
        <button
          onClick={handleProfileClick}
          className='flex items-center gap-3 rounded-lg p-2 transition-colors hover:bg-gray-50 dark:hover:bg-gray-800'
        >
          <div className='flex h-10 w-10 items-center justify-center rounded-full bg-red-100 text-red-600 dark:bg-red-900 dark:text-red-300'>
            <span className='text-sm font-semibold'>
              {user?.name
                ? user.name.charAt(0).toUpperCase()
                : user.email.charAt(0).toUpperCase()}
            </span>
          </div>
          <div className='hidden text-left sm:block'>
            <p className='text-sm font-medium text-gray-900 dark:text-white'>
              {user.name || user.email}
            </p>
            <p className='text-xs text-gray-500 dark:text-gray-400'>
              {user.role}
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
          <div className='absolute right-0 top-full mt-2 w-56 rounded-lg border border-gray-200 bg-white shadow-lg dark:border-gray-700 dark:bg-gray-800'>
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
    </header>
  );
};
