import { useAuth } from '@/hooks/useAuth';
import { cn } from '@/lib/utils';
import { ChevronDown, LogOut, User } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export const ProfileDropdown = () => {
  const { user, logout } = useAuth();
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const profileMenuRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  if (!user) {
    return null;
  }

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
    <div className='relative' ref={profileMenuRef}>
      <button
        onClick={handleProfileClick}
        className='cursor-pointer flex items-center gap-3 rounded-lg p-2 transition-colors hover:bg-gray-50 dark:hover:bg-gray-800'
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
              className='cursor-pointer flex w-full items-center gap-3 px-4 py-2.5 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 dark:text-gray-200 dark:hover:bg-gray-700'
            >
              <User className='h-4 w-4' />
              <span>Profile Page</span>
            </button>
            <div className='border-t border-gray-100 dark:border-gray-700' />
            <button
              onClick={handleLogoutClick}
              className='cursor-pointer flex w-full items-center gap-3 px-4 py-2.5 text-sm font-medium text-red-600 transition-colors hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-950'
            >
              <LogOut className='h-4 w-4' />
              <span>Logout</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
