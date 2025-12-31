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
    <div className="flex min-h-screen lg:h-screen lg:overflow-hidden">
      {/* Painel Esquerdo - Imagem */}
      <div className="hidden lg:flex flex-[2] h-screen">
        <img src={ubsLogin} alt="UBS" className="w-full h-full object-cover" />
      </div>

      {/* Painel Direito - Formulário */}
      <div className="w-full lg:flex-[1.5] flex flex-col items-center justify-center lg:justify-start px-4 py-8 sm:p-6 lg:pt-12 lg:px-10 bg-background overflow-y-auto">
        {/* Logo e Título */}
        <div className="flex flex-col items-center text-center space-y-4 sm:space-y-5 mb-8 sm:mb-10">
          <Logo className='h-20 sm:h-30 w-auto' />
          <h1 className="text-3xl sm:text-4xl lg:text-5xl font-semibold text-foreground">
            Expense Manager
          </h1>
        </div>

        {/* Formulário */}
        <LoginForm />
      </div>
    </div>
  );
};

export default LoginPage;