import { MobileNav, Sidebar } from '@/components/Navigation/';

import { Outlet } from 'react-router-dom';
import { Header } from './Header';

export const AuthenticatedLayout = () => {
  return (
    <div className='min-h-screen bg-gray-50 dark:bg-gray-900'>
      <Sidebar className='hidden md:block' />

      <MobileNav />

      <Header />

      <main className='pt-16 md:ml-64 md:pt-16'>
        <div className='mx-auto max-w-7xl p-4 md:p-6 lg:p-8'>
          <Outlet />
        </div>
      </main>
    </div>
  );
};
