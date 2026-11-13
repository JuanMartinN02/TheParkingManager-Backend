import { Box } from '@mui/material';
import logo from '../assets/esw-logo.png';

// The logo always sits on a white circle, so its blue and red never clash with the blue bar
export function LogoBadge({ size = 38 }: { size?: number }) {
  return (
    <Box sx={{ width: size, height: size, borderRadius: '50%', bgcolor: '#FFFFFF', display: 'grid', placeItems: 'center', flex: 'none' }}>
      <Box component="img" src={logo} alt="" sx={{ width: size * 0.84 }} />
    </Box>
  );
}