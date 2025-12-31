import { useAuth } from '@/hooks/useAuth';
import { CheckCircle, Clock, Receipt, TrendingUp } from 'lucide-react';

export const DashboardPage = () => {
  const { user } = useAuth();

  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-3xl font-bold text-gray-900 dark:text-white'>
          Dashboard
        </h1>
        <p className='mt-2 text-sm text-gray-600 dark:text-gray-400'>
          Welcome back, {user?.email}!
        </p>
      </div>

      <div className='grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4'>
        <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm font-medium text-gray-600 dark:text-gray-400'>
                Total Expenses
              </p>
              <p className='mt-2 text-2xl font-bold text-gray-900 dark:text-white'>
                $12,430
              </p>
            </div>
            <div className='flex h-12 w-12 items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900'>
              <Receipt className='h-6 w-6 text-blue-600 dark:text-blue-400' />
            </div>
          </div>
        </div>

        <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm font-medium text-gray-600 dark:text-gray-400'>
                Approved
              </p>
              <p className='mt-2 text-2xl font-bold text-gray-900 dark:text-white'>
                18
              </p>
            </div>
            <div className='flex h-12 w-12 items-center justify-center rounded-full bg-green-100 dark:bg-green-900'>
              <CheckCircle className='h-6 w-6 text-green-600 dark:text-green-400' />
            </div>
          </div>
        </div>

        <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm font-medium text-gray-600 dark:text-gray-400'>
                Pending
              </p>
              <p className='mt-2 text-2xl font-bold text-gray-900 dark:text-white'>
                5
              </p>
            </div>
            <div className='flex h-12 w-12 items-center justify-center rounded-full bg-yellow-100 dark:bg-yellow-900'>
              <Clock className='h-6 w-6 text-yellow-600 dark:text-yellow-400' />
            </div>
          </div>
        </div>

        <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm font-medium text-gray-600 dark:text-gray-400'>
                This Month
              </p>
              <p className='mt-2 text-2xl font-bold text-gray-900 dark:text-white'>
                $3,240
              </p>
            </div>
            <div className='flex h-12 w-12 items-center justify-center rounded-full bg-purple-100 dark:bg-purple-900'>
              <TrendingUp className='h-6 w-6 text-purple-600 dark:text-purple-400' />
            </div>
          </div>
        </div>
      </div>

      <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
        <h2 className='text-lg font-semibold text-gray-900 dark:text-white'>
          Last Expenses
        </h2>
        <div className='mt-4 space-y-3'>
          <div className='flex items-center justify-between border-b border-gray-100 pb-3 dark:border-gray-800'>
            <div>
              <p className='font-medium text-gray-900 dark:text-white'>
                Office Supplies
              </p>
              <p className='text-sm text-gray-600 dark:text-gray-400'>
                Dec 24, 2025
              </p>
            </div>
            <span className='inline-flex items-center rounded-full bg-green-100 px-3 py-1 text-xs font-medium text-green-800 dark:bg-green-900 dark:text-green-200'>
              Approved
            </span>
          </div>
          <div className='flex items-center justify-between border-b border-gray-100 pb-3 dark:border-gray-800'>
            <div>
              <p className='font-medium text-gray-900 dark:text-white'>
                Client Lunch
              </p>
              <p className='text-sm text-gray-600 dark:text-gray-400'>
                Dec 23, 2025
              </p>
            </div>
            <span className='inline-flex items-center rounded-full bg-yellow-100 px-3 py-1 text-xs font-medium text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'>
              Pending
            </span>
          </div>
          <div className='flex items-center justify-between'>
            <div>
              <p className='font-medium text-gray-900 dark:text-white'>
                Transportation
              </p>
              <p className='text-sm text-gray-600 dark:text-gray-400'>
                Dec 22, 2025
              </p>
            </div>
            <span className='inline-flex items-center rounded-full bg-green-100 px-3 py-1 text-xs font-medium text-green-800 dark:bg-green-900 dark:text-green-200'>
              Approved
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
