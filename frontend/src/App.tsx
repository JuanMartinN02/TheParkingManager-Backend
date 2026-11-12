import { AppBar, Box, Button, Stack, Toolbar, Typography } from '@mui/material';
import { fonts, tokens } from './theme/theme';

// Temporary page to check the theme. Replaced by the real routes next
export default function App() {
  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <Typography sx={{ fontFamily: fonts.display, fontWeight: 600, fontSize: 19 }}>
            Westpark Apartments
          </Typography>
        </Toolbar>
      </AppBar>
      <Box sx={{ p: 2 }}>
        <Typography variant="h1">Apartments</Typography>
        <Typography variant="h2" sx={{ mt: 3 }}>Parking</Typography>
        <Typography sx={{ mt: 1 }}>Unit 302 owns this car but has no active permit.</Typography>
        <Stack direction="row" spacing={1} sx={{ mt: 2 }}>
          <Button variant="contained">Primary action</Button>
          <Button variant="outlined" color="inherit">Secondary</Button>
        </Stack>
        <Box sx={{ mt: 3, p: 2, bgcolor: tokens.hazard, color: tokens.onHazard }}>
          <Typography sx={{ fontFamily: fonts.display, fontWeight: 700, fontSize: 38, lineHeight: 1 }}>
            No active permit
          </Typography>
        </Box>
      </Box>
    </>
  );
}