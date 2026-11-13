import { useState, type FormEvent } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { AppBar, Box, Button, Stack, TextField, Toolbar, Typography } from '@mui/material';
import { ApiError } from '../../api/client';
import { homeFor } from '../../auth/home';
import { useSession } from '../../auth/SessionContext';
import { FormError } from '../../components/FormError';
import logo from '../../assets/esw-logo.png';
import { fonts } from '../../theme/theme';

export function LoginPage() {
  const { me, signIn } = useSession();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  // Already signed in (e.g. opened /login by hand): go home instead
  if (me) return <Navigate to={homeFor(me)} replace />;

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setBusy(true);
    try {
      const user = await signIn(email, password);
      navigate(user.mustChangePassword ? '/change-password' : homeFor(user), { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Can't reach the server. Check your connection and try again");
    } finally {
      setBusy(false);
    }
  }

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="static">
        <Toolbar sx={{ justifyContent: 'center' }}>
          <Typography sx={{ fontFamily: fonts.display, fontWeight: 600, fontSize: 18 }}>ESW Parking Management</Typography>
        </Toolbar>
      </AppBar>

      <Box sx={{ flex: 1, width: '100%', maxWidth: 400, mx: 'auto', px: 2.5, pb: 3, display: 'flex', flexDirection: 'column' }}>
        <Box sx={{ textAlign: 'center', pt: 6, pb: 4 }}>
          <Box component="img" src={logo} alt="ESW Towing Parking Management" sx={{ width: 150 }} />
          <Typography variant="h1" sx={{ mt: 2 }}>Sign in</Typography>
        </Box>

        <Stack component="form" spacing={2} onSubmit={handleSubmit} noValidate>
          <FormError message={error} />
          <TextField label="Email" type="email" autoComplete="username" required value={email}
                     onChange={(e) => setEmail(e.target.value)} />
          <TextField label="Password" type="password" autoComplete="current-password" required value={password}
                     onChange={(e) => setPassword(e.target.value)} />
          <Button type="submit" variant="contained" size="large" disabled={busy || !email || !password}>
            {busy ? 'Signing in' : 'Sign in'}
          </Button>
        </Stack>

        <Typography variant="body2" color="text.secondary" sx={{ mt: 'auto', pt: 4, textAlign: 'center' }}>
          Forgot your password? Ask your property office or ESW to reset it.
        </Typography>
      </Box>
    </Box>
  );
}