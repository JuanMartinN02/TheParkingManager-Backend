import { Navigate, Route, Routes } from 'react-router-dom';
import { RequireRole, RequireSession } from './auth/guards';
import { homeFor } from './auth/home';
import { useSession } from './auth/SessionContext';
import { AppShell } from './components/AppShell';
import { AdminHomePage } from './pages/admin/AdminHomePage';
import { ChangePasswordPage } from './pages/auth/ChangePasswordPage';
import { LoginPage } from './pages/auth/LoginPage';
import { LookupPage } from './pages/operator/LookupPage';
import { ResidentHomePage } from './pages/resident/ResidentHomePage';

// Any unknown address sends the user to their own home screen
function HomeRedirect() {
  const { me, loading } = useSession();
  if (loading) return null;
  return <Navigate to={me ? homeFor(me) : '/login'} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/change-password" element={<RequireSession allowPasswordChange><ChangePasswordPage /></RequireSession>} />

      <Route element={<RequireSession><AppShell /></RequireSession>}>
        <Route path="/lookup" element={<RequireRole roles={['SUPER_ADMIN', 'PROPERTY_ADMIN', 'TOWING_OPERATOR']}><LookupPage /></RequireRole>} />
        <Route path="/admin" element={<RequireRole roles={['SUPER_ADMIN', 'PROPERTY_ADMIN']}><AdminHomePage /></RequireRole>} />
        <Route path="/me" element={<RequireRole roles={['RESIDENT']}><ResidentHomePage /></RequireRole>} />
      </Route>

      <Route path="*" element={<HomeRedirect />} />
    </Routes>
  );
}