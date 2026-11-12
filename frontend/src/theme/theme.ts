import { createTheme } from '@mui/material/styles';

// The only colors the app may use. Components read them from here
export const tokens = {
  night: '#2B2A2C',
  panel: '#383639',
  panel2: '#434044',
  line: '#4E4B4F',
  text: '#F2F1EF',
  muted: '#B8B4B6',
  blue: '#0B2BD9',
  blueHover: '#2A47F0',
  focus: '#9DB0FF',
  ok: '#1E8A4A',
  hazard: '#F2A900',
  onHazard: '#1B1A1B',
  stop: '#E0000A',
} as const;

export const fonts = {
  display: '"Barlow Condensed", "Arial Narrow", sans-serif',
  body: '"Barlow", system-ui, sans-serif',
} as const;

export const theme = createTheme({
  palette: {
    mode: 'dark',
    background: { default: tokens.night, paper: tokens.panel },
    text: { primary: tokens.text, secondary: tokens.muted },
    primary: { main: tokens.blue, dark: tokens.blueHover, contrastText: '#FFFFFF' },
    success: { main: tokens.ok, contrastText: '#FFFFFF' },
    warning: { main: tokens.hazard, contrastText: tokens.onHazard },
    error: { main: tokens.stop, contrastText: '#FFFFFF' },
    divider: tokens.line,
  },
  shape: { borderRadius: 4 },
  typography: {
    fontFamily: fonts.body,
    // Screen titles like "Apartments"
    h1: { fontFamily: fonts.display, fontWeight: 700, fontSize: '2.125rem', lineHeight: 1 },
    // Section titles like "Parking"
    h2: { fontFamily: fonts.display, fontWeight: 600, fontSize: '1.25rem', lineHeight: 1.1 },
    body1: { fontSize: '1rem' },
    body2: { fontSize: '0.875rem' },
    // MUI uppercases buttons by default, the design doesn't
    button: { fontFamily: fonts.display, fontWeight: 600, fontSize: '1.125rem', textTransform: 'none' },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        '*:focus-visible': { outline: `3px solid ${tokens.focus}`, outlineOffset: 2 },
      },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: { root: { padding: '10px 18px' } },
    },
    MuiPaper: {
      defaultProps: { elevation: 0 },
      // MUI lightens dark surfaces with a white overlay, the design wants the flat panel color
      styleOverrides: { root: { backgroundImage: 'none' } },
    },
    MuiAppBar: {
      // In dark mode MUI paints app bars gray unless told to keep the color: ESW blue stays blue
      defaultProps: { elevation: 0, color: 'primary', enableColorOnDark: true },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: { backgroundColor: tokens.panel },
        notchedOutline: { borderColor: tokens.line, borderWidth: 2 },
      },
    },
  },
});