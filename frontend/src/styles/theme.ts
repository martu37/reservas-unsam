import { createTheme } from '@mui/material/styles'

export const theme = createTheme({
  cssVariables: true,
  palette: {
    primary: { main: '#0f766e' },
    background: { default: '#f4f6f8', paper: '#ffffff' },
    success: { main: '#16a34a' },
    error: { main: '#dc2626' },
  },
  shape: { borderRadius: 8 },
  components: {
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: { root: { textTransform: 'none', fontWeight: 600 } },
    },
    MuiTextField: {
      defaultProps: {
        size: 'small',
        slotProps: { inputLabel: { shrink: true } },
      },
    },
    MuiSelect: {
      defaultProps: { size: 'small' },
    },
    MuiInputBase: {
      styleOverrides: { root: { fontSize: '0.8125rem' } },
    },
    MuiTable: {
      defaultProps: { size: 'small' },
    },
    MuiTableCell: {
      styleOverrides: {
        root: { padding: '12px 16px' },
        head: ({ theme }) => ({
          fontSize: '0.6875rem',
          fontWeight: 600,
          letterSpacing: '0.04em',
          textTransform: 'uppercase',
          lineHeight: 1.5,
          whiteSpace: 'nowrap',
          color: theme.palette.text.secondary,
        }),
      },
    },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    fontSize: 13,
    h5: { fontWeight: 700 },
    h6: { fontWeight: 700 },
  },
})
