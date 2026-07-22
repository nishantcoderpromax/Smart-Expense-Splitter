import { createTheme } from '@mui/material/styles';

const palettes = {
  light: {
    mode: 'light',
    primary: { main: '#B8892B', dark: '#96700F', contrastText: '#fff' },
    success: { main: '#1F7A5C', light: '#E7F2EC' },
    error: { main: '#B23A2E', light: '#FBEBE9' },
    background: { default: '#F7F3EB', paper: '#FFFFFF' },
    text: { primary: '#1B2430', secondary: '#5B6472' },
    divider: '#DDD6C6',
  },
  dark: {
    mode: 'dark',
    primary: { main: '#E0B15C', dark: '#C99A3E', contrastText: '#1C1917' },
    success: { main: '#4CC79A', light: 'rgba(76,199,154,0.15)' },
    error: { main: '#E2756B', light: 'rgba(226,117,107,0.15)' },
    background: { default: '#1C1917', paper: '#262220' },
    text: { primary: '#EDEAE2', secondary: '#A9A79E' },
    divider: '#3A352C',
  },
};

export default function getTheme(mode = 'light') {
  const palette = palettes[mode];

  return createTheme({
    palette,
    shape: { borderRadius: 12 },
    typography: {
      fontFamily: "'Inter', system-ui, sans-serif",
      h1: { fontFamily: "'Fraunces', serif", fontWeight: 600, letterSpacing: '-0.01em' },
      h2: { fontFamily: "'Fraunces', serif", fontWeight: 600, letterSpacing: '-0.01em' },
      h3: { fontFamily: "'Fraunces', serif", fontWeight: 600 },
      h4: { fontFamily: "'Fraunces', serif", fontWeight: 600 },
      h5: { fontFamily: "'Fraunces', serif", fontWeight: 600 },
      h6: { fontFamily: "'Fraunces', serif", fontWeight: 600 },
      button: { fontWeight: 600, textTransform: 'none' },
    },
    components: {
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: 10,
            transition: 'transform 0.15s ease, box-shadow 0.15s ease, background-color 0.15s ease',
            '&:hover': { transform: 'translateY(-1px)' },
            '&:active': { transform: 'translateY(0)' },
          },
          containedPrimary: {
            boxShadow: mode === 'light' ? '0 2px 8px rgba(184,137,43,0.25)' : '0 2px 8px rgba(0,0,0,0.4)',
            '&:hover': { boxShadow: mode === 'light' ? '0 4px 14px rgba(184,137,43,0.35)' : '0 4px 14px rgba(0,0,0,0.5)' },
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            border: `1px solid ${palette.divider}`,
            boxShadow: mode === 'light'
              ? '0 1px 2px rgba(27,36,48,0.05), 0 6px 16px rgba(27,36,48,0.04)'
              : '0 1px 2px rgba(0,0,0,0.3), 0 6px 16px rgba(0,0,0,0.25)',
            transition: 'transform 0.18s ease, box-shadow 0.18s ease, background-color 0.25s ease',
          },
        },
      },
      MuiChip: {
        styleOverrides: {
          root: { fontWeight: 600, transition: 'transform 0.12s ease' },
        },
      },
      MuiTextField: {
        defaultProps: { size: 'small' },
      },
      MuiPaper: {
        styleOverrides: { root: { backgroundImage: 'none' } },
      },
      MuiIconButton: {
        styleOverrides: {
          root: { transition: 'background-color 0.15s ease, transform 0.15s ease' },
        },
      },
      MuiCssBaseline: {
        styleOverrides: {
          body: {
            backgroundColor: 'transparent', // theme.css owns the actual layered gradient background
          },
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundColor: mode === 'light' ? 'rgba(255,255,255,0.85)' : 'rgba(38,34,32,0.85)',
          },
        },
      },
    },
  });
}
