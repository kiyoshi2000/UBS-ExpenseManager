import { LoginForm } from './components/LoginForm';
import ubsLogo from '@/assets/ubs-logo.svg';
import ubsLoginImage from '@/assets/ubs-image.avif';

export const LoginPage = () => {
  return (
    <div className="flex h-screen overflow-hidden">
      {/* Painel Esquerdo - Vermelho UBS */}
      {/*className="hidden lg:block lg:w-2/3 bg-[#E60100]" aria-hidden="true"*/}
      <div className='hidden lg:flex flex-[2]'>
        <img src={ubsLoginImage} alt="UBS" className="w-full h-full object-cover"/>
      </div>


      {/* Painel Direito - Formulário */}
      <div className="w-full lg:w-1/2 flex-[1] flex flex-col items-center p-6 pt-10 bg-background">
        {/* Logo e Título */}
        <div className="flex flex-col items-center space-y-5 mb-10">
          <img src={ubsLogo} alt="UBS" className="h-30 w-auto" />
          <h1 className="text-5xl font-semibold text-foreground">
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
