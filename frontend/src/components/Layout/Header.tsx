import { useTheme } from '@/hooks/useTheme';
import { Moon, Sun } from 'lucide-react';
import { ProfileDropdown } from './ProfileDropdown';

export const Header = () => {
  const { isDarkMode, toggleDarkMode } = useTheme();

  return (
    <header className='fixed right-0 top-0 z-30 flex h-16 items-center justify-end gap-4 border-b border-gray-200 bg-white px-6 dark:border-gray-800 dark:bg-gray-900 md:left-64'>
      <button
        onClick={toggleDarkMode}
        className='cursor-pointer rounded-lg p-2 text-gray-600 transition-colors hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800'
        aria-label='Toggle dark mode'
      >
        {isDarkMode ? (
          <Sun className='h-5 w-5' />
        ) : (
          <Moon className='h-5 w-5' />
        )}
      </button>

      <ProfileDropdown />
    </header>
  );
};
