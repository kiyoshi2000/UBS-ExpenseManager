import { LoginForm } from "./components/LoginForm";
import { Logo } from "@/components/Navigation";
import { useTheme } from "@/hooks/useTheme";

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
  useTheme();

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
        <div className="flex flex-col items-center text-center space-y-4 sm:space-y-5 mb-8 sm:mb-10">
          <Logo className='h-20 sm:h-30 w-auto' />
          <h1 className="text-3xl sm:text-4xl lg:text-5xl font-semibold text-foreground">
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
