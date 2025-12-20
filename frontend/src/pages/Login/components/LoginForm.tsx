// src/pages/Login/components/LoginForm.tsx

import { useState, FormEvent } from 'react';
import { validateEmail, validatePassword } from '@/utils/validation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';

export const LoginForm = () => {
  // Estados dos campos
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // Estados de erro
  const [errors, setErrors] = useState<{ email?: string; password?: string }>({});

  // Controla se o campo já foi tocado (para mostrar erro só após interação)
  const [touched, setTouched] = useState<{ email: boolean; password: boolean }>({
    email: false,
    password: false,
  });

  // Verifica se o formulário é válido para habilitar o botão
  const isFormValid = !validateEmail(email) && !validatePassword(password);

  // Valida campo ao perder foco
  const handleBlur = (field: 'email' | 'password') => {
    setTouched((prev) => ({ ...prev, [field]: true }));

    if (field === 'email') {
      const error = validateEmail(email);
      setErrors((prev) => ({ ...prev, email: error || undefined }));
    } else {
      const error = validatePassword(password);
      setErrors((prev) => ({ ...prev, password: error || undefined }));
    }
  };

  // Submit do formulário
  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();

    // Marca todos como tocados
    setTouched({ email: true, password: true });

    // Valida tudo
    const emailError = validateEmail(email);
    const passwordError = validatePassword(password);

    setErrors({
      email: emailError || undefined,
      password: passwordError || undefined,
    });
  };

  return (
    <Card className="w-full max-w-sm">
      <CardHeader className="text-center">
        <CardTitle className="text-xl text-left">Login to your account</CardTitle>
        <CardDescription className="text-left">
          Enter your email below to login to your account
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} noValidate className="space-y-6">
          {/* Campo Email */}
          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              onBlur={() => handleBlur('email')}
              placeholder="seu.email@ubs.com"
              aria-invalid={touched.email && !!errors.email}
            />
            {touched.email && errors.email && (
              <p className="text-sm text-destructive">{errors.email}</p>
            )}
          </div>

          {/* Campo Senha */}
          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onBlur={() => handleBlur('password')}
              placeholder="••••••"
              aria-invalid={touched.password && !!errors.password}
            />
            {touched.password && errors.password && (
              <p className="text-sm text-destructive">{errors.password}</p>
            )}
          </div>

          {/* Botão Submit */}
          <Button variant="default" type="submit" className="w-full" disabled={!isFormValid}>
            Login
          </Button>
        </form>
      </CardContent>
    </Card>
  );
};

export default LoginForm;
