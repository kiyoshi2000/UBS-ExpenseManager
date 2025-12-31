import ubsLogo from '@/assets/ubs-logo.svg';
import ubsLogoDark from '@/assets/ubs-logo-dark.svg';

interface LogoProps {
  className?: string;
}

export const Logo = ({ className }: LogoProps) => {
  return (
    <>
      <img
        src={ubsLogo}
        alt='UBS'
        className={`${className} dark:hidden`}
      />
      <img
        src={ubsLogoDark}
        alt='UBS'
        className={`${className} hidden dark:block`}
      />
    </>
  );
};
