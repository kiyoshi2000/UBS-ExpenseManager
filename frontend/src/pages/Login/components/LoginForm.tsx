// src/pages/Login/components/LoginForm.tsx

/**
 * LoginForm
 *
 * Handles the authentication workflow:
 * - Form state and validation (react-hook-form + zod)
 * - Submission to the backend authentication endpoint
 * - Error handling and user feedback
 *
 * On successful authentication:
 * - Stores JWT token in localStorage
 * - Stores user information (id, email, role)
 * - Redirects the user to a protected route
 */

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { AlertCircle, Eye, EyeOff } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from "@/services/auth.service";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';

const loginFormSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Invalid email format'),
  password: z
    .string()
    .min(1, 'Password is required')
    .min(6, 'Password must be at least 6 characters'),
});

type LoginFormData = z.infer<typeof loginFormSchema>;

export const LoginForm = () => {
  const [showPassword, setShowPassword] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);

  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors, touchedFields, isValid },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginFormSchema),
    mode: 'onTouched',
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = async (data: LoginFormData) => {
    setLoginError(null);
    try {
      const { data: result } = await login(data);

      localStorage.setItem('jwt_token', result.token);
      localStorage.setItem('user', JSON.stringify(result.user));
      localStorage.setItem('user_role', result.user.role);

      navigate('/dashboard');
    } catch (error) {
      let message = 'Unexpected error while logging in. Try again later!';

      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as {
          response?: { data?: { message?: string } };
        };
        message = axiosError.response?.data?.message ?? message;
      }

      setLoginError(message);
    }
  };

  return (
    <Card className='w-full max-w-sm mx-auto'>
      <CardHeader className='text-center'>
        <CardTitle className='text-xl text-left'>
          Login to your account
        </CardTitle>
        <CardDescription className='text-left'>
          Enter your email below to login to your account
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form
          onSubmit={handleSubmit(onSubmit)}
          noValidate
          className='space-y-6'
        >
          <div className='space-y-2'>
            <Label htmlFor='email'>Email</Label>
            <Input
              id='email'
              type='email'
              {...register('email')}
              placeholder='seu.email@ubs.com'
              aria-invalid={
                touchedFields.email && !!errors.email ? 'true' : 'false'
              }
            />
            {touchedFields.email && errors.email && (
              <p className='text-sm text-destructive'>{errors.email.message}</p>
            )}
          </div>

          <div className='space-y-2'>
            <Label htmlFor='password'>Password</Label>
            <div className='relative'>
              <Input
                id='password'
                type={showPassword ? 'text' : 'password'}
                {...register('password')}
                placeholder='••••••'
                aria-invalid={
                  touchedFields.password && !!errors.password ? 'true' : 'false'
                }
              />
              <button
                type='button'
                onClick={() => setShowPassword(!showPassword)}
                className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
            {touchedFields.password && errors.password && (
              <p className='text-sm text-destructive'>
                {errors.password.message}
              </p>
            )}
          </div>

          {loginError && (
            <Alert variant='destructive'>
              <AlertCircle className='h-4 w-4' />
              <AlertDescription>{loginError}</AlertDescription>
            </Alert>
          )}

          <Button
            variant='default'
            type='submit'
            className='w-full'
            disabled={!isValid}
          >
            Login
          </Button>
        </form>
      </CardContent>
    </Card>
  );
};
