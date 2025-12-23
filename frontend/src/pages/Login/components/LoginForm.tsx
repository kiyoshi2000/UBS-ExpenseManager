// src/pages/Login/components/LoginForm.tsx

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useNavigate } from 'react-router-dom';
import { api } from "@/services/api";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';

const loginFormSchema = z.object({
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email format'),
  password: z
    .string()
    .min(1, 'Password is required')
    .min(6, 'Password must be at least 6 characters'),
});

type LoginFormData = z.infer<typeof loginFormSchema>;

export const LoginForm = () => {
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
    try {
      const { data: result } = await api.post(
        "/api/auth/login",
        data,
        // { withCredentials: true }
      );
      navigate("/dashboard");
    } catch (error: any) {
      const message =
        error.response?.data?.message ??
          "Unexpected error while logging in. Try again later!";

      alert(message);
    }
  };

  return (
    <Card className='w-full max-w-sm'>
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
              aria-invalid={touchedFields.email && !!errors.email ? 'true' : 'false'}
            />
            {touchedFields.email && errors.email && (
              <p className='text-sm text-destructive'>{errors.email.message}</p>
            )}
          </div>

          <div className='space-y-2'>
            <Label htmlFor='password'>Password</Label>
            <Input
              id='password'
              type='password'
              {...register('password')}
              placeholder='••••••'
              aria-invalid={touchedFields.password && !!errors.password ? 'true' : 'false'}
            />
            {touchedFields.password && errors.password && (
              <p className='text-sm text-destructive'>
                {errors.password.message}
              </p>
            )}
          </div>

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
