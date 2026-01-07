import { AuthenticatedLayout } from '@/components/Layout';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { PublicRoute } from '@/components/PublicRoute';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AnalyticsPage } from './pages/Analytics/AnalyticsPage';
import { ApprovalsPage } from './pages/Approvals/ApprovalsPage';
import { DashboardPage } from './pages/Dashboard/DashboardPage';
import { ExpensesPage } from './pages/Expenses/ExpensesPage';
import { ExpensesReport } from './pages/ExpensesReport/ExpensesReport';
import { LoginPage } from './pages/Login';
import { ProfilePage } from './pages/Profile/ProfilePage';
import { UsersPage } from './pages/Users/UsersPage';
import { DepartmentPage } from './pages/Department';
import { CategoriesPage } from './pages/Categories/CategoryPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path='/'
          element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          }
        />
        <Route
          element={
            <ProtectedRoute>
              <AuthenticatedLayout />
            </ProtectedRoute>
          }
        >
          <Route path='/approvals' element={<ApprovalsPage />} />
          <Route path='/dashboard' element={<DashboardPage />} />
          <Route path='/department' element={<DepartmentPage />} />
          <Route path='/expenses' element={<ExpensesPage />} />
          <Route path='/expenses-report' element={<ExpensesReport />} />
          <Route path='/analytics' element={<AnalyticsPage />} />
          <Route path='/users' element={<UsersPage />} />
          <Route path='/profile' element={<ProfilePage />} />
          <Route path='/category' element={<CategoriesPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;