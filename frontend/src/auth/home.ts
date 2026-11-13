import type { Me } from '../api/auth';

// Where each role lands after signing in
export function homeFor(me: Me): string {
  switch (me.role) {
    case 'TOWING_OPERATOR':
      return '/lookup';
    case 'SUPER_ADMIN':
    case 'PROPERTY_ADMIN':
      return '/admin';
    case 'RESIDENT':
      return '/me';
    default:
      return '/login';
  }
}