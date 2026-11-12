import { Alert } from '@mui/material';

// One place that shows a screen-level error, so every form looks the same
export function FormError({ message }: { message: string | null }) {
  if (!message) return null;
  return (
    <Alert severity="error" variant="filled" role="alert">
      {message}
    </Alert>
  );
}