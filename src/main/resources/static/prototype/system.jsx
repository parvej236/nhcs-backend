// system.jsx - Design tokens, Icons, Theme Context & Design Primitives
const { createContext, useContext, useState, useEffect } = React;

// 1. Theme Design System Tokens
const TD = {
  mode: 'dark',
  bgMain: '#0b0f19',
  bgCard: '#131c2e',
  bgInput: '#1b2640',
  border: '#2a3a5a',
  textPrimary: '#f8fafc',
  textSecondary: '#94a3b8',
  brandPrimary: '#ef4444', // Red / Coral
  brandSecondary: '#f87171',
  success: '#10b981',
  warning: '#f59e0b',
  danger: '#ef4444',
  shadow: '0 10px 30px rgba(0,0,0,0.5)',
  radius: '16px',
  innerRadius: '12px'
};

const TL = {
  mode: 'light',
  bgMain: '#f8fafc',
  bgCard: '#ffffff',
  bgInput: '#f1f5f9',
  border: '#e2e8f0',
  textPrimary: '#0f172a',
  textSecondary: '#64748b',
  brandPrimary: '#de3b3b', // Coral Red
  brandSecondary: '#ef4444',
  success: '#059669',
  warning: '#d97706',
  danger: '#dc2626',
  shadow: '0 8px 24px rgba(220,38,38,0.06)',
  radius: '16px',
  innerRadius: '12px'
};

// Expose tokens globally
window.TD = TD;
window.TL = TL;

// 2. Theme Context Provider
const ThemeContext = createContext();
window.ThemeContext = ThemeContext;

function ThemeProvider({ children }) {
  const [themeMode, setThemeMode] = useState('dark');
  const theme = themeMode === 'dark' ? TD : TL;

  const toggleTheme = () => {
    setThemeMode(prev => prev === 'dark' ? 'light' : 'dark');
  };

  return (
    <ThemeContext.Provider value={{ theme, themeMode, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}
window.ThemeProvider = ThemeProvider;

function useTheme() {
  return useContext(ThemeContext);
}
window.useTheme = useTheme;

// 3. SVG Icon Primitives
const Icon = {
  Activity: ({ size = 20, color }) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color || "currentColor"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
    </svg>
  ),
  Heart: ({ size = 20, color, fill }) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill={fill || "none"} stroke={color || "currentColor"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
    </svg>
  ),
  Calendar: ({ size = 20, color }) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color || "currentColor"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
      <line x1="16" y1="2" x2="16" y2="6"></line>
      <line x1="8" y1="2" x2="8" y2="6"></line>
      <line x1="3" y1="10" x2="21" y2="10"></line>
    </svg>
  ),
  FileText: ({ size = 20, color }) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color || "currentColor"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
      <polyline points="14 2 14 8 20 8"></polyline>
      <line x1="16" y1="13" x2="8" y2="13"></line>
      <line x1="16" y1="17" x2="8" y2="17"></line>
      <polyline points="10 9 9 9 8 9"></polyline>
    </svg>
  ),
  User: ({ size = 20, color }) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color || "currentColor"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
      <circle cx="12" cy="7" r="4"></circle>
    </svg>
  ),
  Stethoscope: ({ size = 20, color }) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color || "currentColor"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M4.5 16.5c-1.5 1.26-2.5 3.19-2.5 5.5h20c0-2.31-1-4.24-2.5-5.5"></path>
      <path d="M12 2v10a4 4 0 0 0 8 0V2"></path>
      <path d="M12 6h8"></path>
      <circle cx="6" cy="12" r="4"></circle>
    </svg>
  ),
  Sun: ({ size = 20, color }) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color || "currentColor"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="5"></circle>
      <line x1="12" y1="1" x2="12" y2="3"></line>
      <line x1="12" y1="21" x2="12" y2="23"></line>
      <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
      <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
      <line x1="1" y1="12" x2="3" y2="12"></line>
      <line x1="21" y1="12" x2="23" y2="12"></line>
      <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
      <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
    </svg>
  ),
  Moon: ({ size = 20, color }) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color || "currentColor"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
    </svg>
  ),
  Plus: ({ size = 20, color }) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color || "currentColor"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <line x1="12" y1="5" x2="12" y2="19"></line>
      <line x1="5" y1="12" x2="19" y2="12"></line>
    </svg>
  ),
  Search: ({ size = 20, color }) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color || "currentColor"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="11" cy="11" r="8"></circle>
      <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
    </svg>
  )
};
window.Icon = Icon;

// 4. Reusable Primitives
function Card({ children, style, className }) {
  const { theme } = useTheme();
  const cardStyle = {
    background: theme.bgCard,
    border: `1px solid ${theme.border}`,
    borderRadius: theme.radius,
    padding: '24px',
    boxShadow: theme.shadow,
    transition: 'all 0.3s ease',
    color: theme.textPrimary,
    ...style
  };
  return <div style={cardStyle} className={className}>{children}</div>;
}
window.Card = Card;

function Btn({ children, variant = 'primary', onClick, style, disabled }) {
  const { theme } = useTheme();
  
  let bg = theme.brandPrimary;
  let color = '#ffffff';
  let border = 'none';

  if (variant === 'secondary') {
    bg = theme.bgInput;
    color = theme.textPrimary;
    border = `1px solid ${theme.border}`;
  } else if (variant === 'outline') {
    bg = 'transparent';
    color = theme.brandPrimary;
    border = `1px solid ${theme.brandPrimary}`;
  } else if (variant === 'danger') {
    bg = theme.danger;
    color = '#ffffff';
  }

  const btnStyle = {
    background: bg,
    color: color,
    border: border,
    padding: '12px 24px',
    borderRadius: theme.innerRadius,
    fontWeight: '600',
    cursor: disabled ? 'not-allowed' : 'pointer',
    opacity: disabled ? 0.6 : 1,
    transition: 'all 0.2s ease',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '8px',
    fontSize: '14px',
    ...style
  };

  return (
    <button 
      style={btnStyle} 
      onClick={disabled ? undefined : onClick}
      disabled={disabled}
    >
      {children}
    </button>
  );
}
window.Btn = Btn;

function Input({ label, value, onChange, placeholder, type = 'text', style }) {
  const { theme } = useTheme();
  
  const labelStyle = {
    fontSize: '12px',
    fontWeight: '600',
    color: theme.textSecondary,
    marginBottom: '6px',
    display: 'block',
    textTransform: 'uppercase',
    letterSpacing: '0.05em'
  };

  const inputStyle = {
    width: '100%',
    background: theme.bgInput,
    border: `1px solid ${theme.border}`,
    color: theme.textPrimary,
    padding: '12px 16px',
    borderRadius: theme.innerRadius,
    fontSize: '14px',
    outline: 'none',
    transition: 'border-color 0.2s ease',
    ...style
  };

  return (
    <div style={{ marginBottom: '16px' }}>
      {label && <label style={labelStyle}>{label}</label>}
      <input 
        type={type} 
        value={value} 
        onChange={onChange} 
        placeholder={placeholder} 
        style={inputStyle}
      />
    </div>
  );
}
window.Input = Input;

function Select({ label, value, onChange, options = [], style }) {
  const { theme } = useTheme();

  const labelStyle = {
    fontSize: '12px',
    fontWeight: '600',
    color: theme.textSecondary,
    marginBottom: '6px',
    display: 'block',
    textTransform: 'uppercase',
    letterSpacing: '0.05em'
  };

  const selectStyle = {
    width: '100%',
    background: theme.bgInput,
    border: `1px solid ${theme.border}`,
    color: theme.textPrimary,
    padding: '12px 16px',
    borderRadius: theme.innerRadius,
    fontSize: '14px',
    outline: 'none',
    cursor: 'pointer',
    ...style
  };

  return (
    <div style={{ marginBottom: '16px' }}>
      {label && <label style={labelStyle}>{label}</label>}
      <select value={value} onChange={onChange} style={selectStyle}>
        {options.map((opt, idx) => (
          <option key={idx} value={opt.value} style={{ background: theme.bgCard, color: theme.textPrimary }}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
}
window.Select = Select;

function Chip({ label, status = 'normal' }) {
  const { theme } = useTheme();

  let bg = 'rgba(16, 185, 129, 0.15)';
  let color = theme.success;

  if (status === 'warning') {
    bg = 'rgba(245, 158, 11, 0.15)';
    color = theme.warning;
  } else if (status === 'danger') {
    bg = 'rgba(239, 68, 68, 0.15)';
    color = theme.danger;
  }

  const chipStyle = {
    background: bg,
    color: color,
    padding: '6px 12px',
    borderRadius: '20px',
    fontSize: '12px',
    fontWeight: '600',
    display: 'inline-flex',
    alignItems: 'center',
    width: 'fit-content'
  };

  return <span style={chipStyle}>{label}</span>;
}
window.Chip = Chip;

function Progress({ value, max = 100, color }) {
  const { theme } = useTheme();
  
  const containerStyle = {
    width: '100%',
    height: '8px',
    background: theme.bgInput,
    borderRadius: '4px',
    overflow: 'hidden'
  };

  const fillStyle = {
    width: `${(value / max) * 100}%`,
    height: '100%',
    background: color || theme.brandPrimary,
    borderRadius: '4px',
    transition: 'width 0.4s cubic-bezier(0.4, 0, 0.2, 1)'
  };

  return (
    <div style={containerStyle}>
      <div style={fillStyle}></div>
    </div>
  );
}
window.Progress = Progress;
