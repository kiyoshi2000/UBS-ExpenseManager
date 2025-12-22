import { describe, it, expect } from 'vitest';
import { validateEmail, validatePassword } from './validation';

describe('validateEmail', () => {
  it('retorna erro quando email está vazio', () => {
    expect(validateEmail('')).toBe('Email é obrigatório');
  });

  it('retorna erro quando email contém apenas espaços', () => {
    expect(validateEmail('   ')).toBe('Email é obrigatório');
  });

  it('retorna erro para formato de email inválido', () => {
    expect(validateEmail('emailinvalido')).toBe('Formato de email inválido');
    expect(validateEmail('email@')).toBe('Formato de email inválido');
    expect(validateEmail('@dominio.com')).toBe('Formato de email inválido');
    expect(validateEmail('email@dominio')).toBe('Formato de email inválido');
  });

  it('retorna null para email válido', () => {
    expect(validateEmail('usuario@dominio.com')).toBeNull();
    expect(validateEmail('usuario@ubs.com')).toBeNull();
    expect(validateEmail('nome.sobrenome@empresa.com.br')).toBeNull();
  });
});

describe('validatePassword', () => {
  it('retorna erro quando senha está vazia', () => {
    expect(validatePassword('')).toBe('Senha é obrigatória');
  });

  it('retorna null para senha preenchida', () => {
    expect(validatePassword('123456')).toBeNull();
    expect(validatePassword('qualquersenha')).toBeNull();
  });
});
