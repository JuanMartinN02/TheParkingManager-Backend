import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMe, login, logout, type Me } from '../api/auth';
import { setAuthHandlers } from '../api/client';

type Session = {
  me: Me | null;
  loading: boolean;
  signIn: (email: string, password: string) => Promise<Me>;
  signOut: () => Promise<void>;
  refresh: () => Promise<Me | null>;
};

const SessionContext = createContext<Session | null>(null);

export function SessionProvider({ children }: { children: ReactNode }) {
  const [me, setMe] = useState<Me | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const refresh = useCallback(async () => {
    try {
      const user = await getMe();
      setMe(user);
      return user;
    } catch {
      setMe(null);
      return null;
    }
  }, []);

  // Tells the API client what to do when the backend says "not signed in" or "change your password first"
  useEffect(() => {
    setAuthHandlers({
      onUnauthorized: () => {
        setMe(null);
        navigate('/login', { replace: true });
      },
      onPasswordChangeRequired: () => navigate('/change-password', { replace: true }),
    });
  }, [navigate]);

  // On page load (or reload): is there still a session from before?
  useEffect(() => {
    refresh().finally(() => setLoading(false));
  }, [refresh]);

  const signIn = useCallback(async (email: string, password: string) => {
    await login(email, password);
    const user = await getMe();
    setMe(user);
    return user;
  }, []);

  const signOut = useCallback(async () => {
    try {
      await logout();
    } finally {
      setMe(null);
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  return (
    <SessionContext.Provider value={{ me, loading, signIn, signOut, refresh }}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession(): Session {
  const session = useContext(SessionContext);
  if (!session) throw new Error('useSession must be used inside SessionProvider');
  return session;
}