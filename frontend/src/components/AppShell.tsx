import { AppBar, Box, Button, Toolbar, Typography } from '@mui/material';
import { Outlet } from 'react-router-dom';
import { useSession } from '../auth/SessionContext';
import { fonts } from '../theme/theme';
import { LogoBadge } from './LogoBadge';

// The blue bar on every signed-in screen. Pages render where <Outlet /> is
export function AppShell() {
  const { me, signOut } = useSession();

  return (
    <>
      <AppBar position="sticky">
        <Toolbar sx={{ gap: 1.5 }}>
          <LogoBadge />
          <Box sx={{ minWidth: 0 }}>
            <Typography sx={{ fontSize: 12, opacity: 0.8, lineHeight: 1.2 }}>ESW Parking Management</Typography>
            <Typography noWrap sx={{ fontFamily: fonts.display, fontWeight: 600, fontSize: 19, lineHeight: 1.1 }}>
              {me?.email}
            </Typography>
          </Box>
          <Button color="inherit" onClick={() => void signOut()} sx={{ ml: 'auto', flex: 'none' }}>
            Sign out
          </Button>
        </Toolbar>
      </AppBar>
      <Box component="main" sx={{ p: 2 }}>
        <Outlet />
      </Box>
    </>
  );
}