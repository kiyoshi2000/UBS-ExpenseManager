import { LoginForm } from './components/LoginForm';
import ubsLogo from '@/assets/ubs-logo.svg';

/**
 * LoginPage
 *
 * Responsible for rendering the login screen layout.
 *
 * This component:
 * - Defines the page structure and branding (UBS visual identity)
 * - Delegates all authentication logic to LoginForm
 *
 * Business logic (API calls, validation, authentication)
 * must NOT be implemented here.
 */

export const LoginPage = () => {
  return (
    <div className="flex min-h-screen">
      {/* Left panel - UBS branding */}
      <div
        className="hidden lg:block lg:w-2/3 bg-[#E60100]"
        aria-hidden="true"
      />

      {/* Right panel - Login form */}
      <div className="w-full lg:w-1/2 flex flex-col items-center p-6 pt-15 bg-background">
        {/* Logo e Título */}
        <div className="flex flex-col items-center space-y-5 mb-10">
          <img src={ubsLogo} alt="UBS" className="h-30 w-auto" />
          <h1 className="text-5xl font-semibold text-foreground">
            Expense Manager
          </h1>
        </div>

        {/* Authentication form */}
        <LoginForm />
      </div>
    </div>
  );
};

export default LoginPage;
