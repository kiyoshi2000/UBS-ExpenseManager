import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { PublicRoute } from '@/components/PublicRoute';
import { LoginPage } from './pages/Login';
import { DashboardPage } from './pages/Dashboard';
import { DepartmentPage } from './pages/Department';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute /*allowedRoles={['ROLE_FINANCE']*/>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/departments"
          element={
            <ProtectedRoute /*allowedRoles={['ROLE_FINANCE']*/>
              <DepartmentPage />
            </ProtectedRoute>
          }
        />

        

      </Routes>
    </BrowserRouter>
  );
}

export default App;
