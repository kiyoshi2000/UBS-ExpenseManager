export const AnalyticsPage = () => {
  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-3xl font-bold text-gray-900 dark:text-white'>
          Analytics Dashboard
        </h1>
        <p className='mt-2 text-sm text-gray-600 dark:text-gray-400'>
          Advanced expense analytics and insights
        </p>
      </div>

      <div className='grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3'>
        <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
          <h3 className='font-semibold text-gray-900 dark:text-white'>
            Total Expenses
          </h3>
          <p className='mt-2 text-2xl font-bold text-gray-900 dark:text-white'>
            $125,430
          </p>
        </div>
        <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
          <h3 className='font-semibold text-gray-900 dark:text-white'>
            Pending Approvals
          </h3>
          <p className='mt-2 text-2xl font-bold text-gray-900 dark:text-white'>
            24
          </p>
        </div>
        <div className='rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900'>
          <h3 className='font-semibold text-gray-900 dark:text-white'>
            This Month
          </h3>
          <p className='mt-2 text-2xl font-bold text-gray-900 dark:text-white'>
            $8,542
          </p>
        </div>
      </div>
    </div>
  );
};
