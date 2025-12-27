import { useAuth } from '@/hooks/useAuth';

export const ProfilePage = () => {
  const { user } = useAuth();

  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-3xl font-bold text-gray-900 dark:text-white'>
          Profile
        </h1>
        <p className='mt-2 text-sm text-gray-600 dark:text-gray-400'>
          Manage your account information
        </p>
      </div>

      <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
        <div className='space-y-4'>
          <div>
            <label className='text-sm font-medium text-gray-900 dark:text-white'>
              Email
            </label>
            <p className='mt-1 text-sm text-gray-600 dark:text-gray-400'>
              {user?.email}
            </p>
          </div>
          <div>
            <label className='text-sm font-medium text-gray-900 dark:text-white'>
              Role
            </label>
            <p className='mt-1 text-sm text-gray-600 dark:text-gray-400'>
              {user?.role}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
