import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import type { Role } from '../api/auth';
import { homeFor } from './home';
import { useSession } from './SessionContext';

// These only decide which screen to show. The real security is the backend's AccessGuard

export function RequireSession({ children, allowPasswordChange = false }: { children: ReactNode; allowPasswordChange?: boolean }) {
  const { me, loading } = useSession();

  if (loading) {
    return (
      <Box sx={{ display: 'grid', placeItems: 'center', minHeight: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }
  if (!me) return <Navigate to="/login" replace />;
  // A temporary password has to be replaced before anything else
  if (me.mustChangePassword && !allowPasswordChange) return <Navigate to="/change-password" replace />;
  return <>{children}</>;
}

export function RequireRole({ roles, children }: { roles: Role[]; children: ReactNode }) {
  const { me } = useSession();
  if (!me?.role || !roles.includes(me.role)) return <Navigate to={me ? homeFor(me) : '/login'} replace />;
  return <>{children}</>;
}