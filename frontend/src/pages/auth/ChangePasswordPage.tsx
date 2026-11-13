import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Button, Stack, TextField, Typography } from '@mui/material';
import { changePassword } from '../../api/auth';
import { ApiError } from '../../api/client';
import { homeFor } from '../../auth/home';
import { useSession } from '../../auth/SessionContext';
import { FormError } from '../../components/FormError';

// Same rule as the backend's PasswordPolicy. The backend still checks it, this just saves a round trip
const MIN_LENGTH = 12;

export function ChangePasswordPage() {
  const { me, refresh } = useSession();
  const navigate = useNavigate();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [busy, setBusy] = useState(false);

  const forced = me?.mustChangePassword === true;
  const tooShort = newPassword.length > 0 && newPassword.length < MIN_LENGTH;
  const mismatch = confirm.length > 0 && confirm !== newPassword;

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setFieldErrors({});
    setBusy(true);
    try {
      await changePassword(currentPassword, newPassword);
      // Reload the user so mustChangePassword is false before leaving this screen
      const user = await refresh();
      navigate(user ? homeFor(user) : '/login', { replace: true });
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
        setFieldErrors(err.fieldErrors);
      } else {
        setError("Can't reach the server. Check your connection and try again");
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <Box sx={{ maxWidth: 420, mx: 'auto', px: 2.5, py: 4 }}>
      <Typography variant="h1">{forced ? 'Choose your password' : 'Change password'}</Typography>
      {forced && (
        <Typography color="text.secondary" sx={{ mt: 1 }}>
          You signed in with a temporary password. Pick your own to continue.
        </Typography>
      )}

      <Stack component="form" spacing={2} sx={{ mt: 3 }} onSubmit={handleSubmit} noValidate>
        <FormError message={error} />
        <TextField label={forced ? 'Temporary password' : 'Current password'} type="password" autoComplete="current-password"
                   value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)}
                   error={!!fieldErrors.currentPassword} helperText={fieldErrors.currentPassword} />
        <TextField label="New password" type="password" autoComplete="new-password"
                   value={newPassword} onChange={(e) => setNewPassword(e.target.value)}
                   error={tooShort || !!fieldErrors.newPassword}
                   helperText={fieldErrors.newPassword ?? `At least ${MIN_LENGTH} characters`} />
        <TextField label="Repeat new password" type="password" autoComplete="new-password"
                   value={confirm} onChange={(e) => setConfirm(e.target.value)}
                   error={mismatch} helperText={mismatch ? "Doesn't match the new password" : ' '} />
        <Button type="submit" variant="contained" size="large"
                disabled={busy || !currentPassword || newPassword.length < MIN_LENGTH || confirm !== newPassword}>
          {busy ? 'Saving' : 'Save password'}
        </Button>
      </Stack>
    </Box>
  );
}