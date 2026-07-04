// chrome.jsx - Mock Desktop Browser shell, Header Navigation, and Canvas controls
const { useTheme, Icon } = window;

// 1. Mock Desktop Browser Frame
function DesktopShell({ children, url = 'http://localhost:8080/nhcs-dashboard' }) {
  const { theme } = useTheme();

  const shellStyle = {
    width: '1280px',
    height: '800px',
    display: 'flex',
    flexDirection: 'column',
    background: theme.bgMain,
    border: `2px solid ${theme.border}`,
    borderRadius: '12px',
    boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
    overflow: 'hidden',
    position: 'relative'
  };

  const barStyle = {
    background: theme.mode === 'dark' ? '#111827' : '#e2e8f0',
    padding: '12px 18px',
    display: 'flex',
    alignItems: 'center',
    gap: '16px',
    borderBottom: `1px solid ${theme.border}`
  };

  const dotsStyle = {
    display: 'flex',
    gap: '8px'
  };

  const dot = (color) => ({
    width: '12px',
    height: '12px',
    borderRadius: '50%',
    background: color
  });

  const addressStyle = {
    flexGrow: 1,
    background: theme.mode === 'dark' ? '#1f2937' : '#ffffff',
    border: `1px solid ${theme.border}`,
    color: theme.textSecondary,
    fontSize: '13px',
    padding: '6px 16px',
    borderRadius: '8px',
    fontFamily: 'monospace',
    display: 'flex',
    alignItems: 'center',
    gap: '8px'
  };

  return (
    <div style={shellStyle}>
      {/* Browser Bar */}
      <div style={barStyle}>
        <div style={dotsStyle}>
          <div style={dot('#ef4444')}></div>
          <div style={dot('#f59e0b')}></div>
          <div style={dot('#10b981')}></div>
        </div>
        <div style={addressStyle}>
          <span style={{ color: theme.success }}>🔒</span> {url}
        </div>
      </div>
      {/* Web Content Area */}
      <div style={{ flexGrow: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column' }}>
        {children}
      </div>
    </div>
  );
}
window.DesktopShell = DesktopShell;

// 2. Navigation Header inside the web application
function NavigationHeader({ currentTab, onTabChange, onOpenLogin }) {
  const { theme, themeMode, toggleTheme } = useTheme();

  const headerStyle = {
    background: theme.bgCard,
    borderBottom: `1px solid ${theme.border}`,
    padding: '16px 40px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    position: 'sticky',
    top: 0,
    zIndex: 100
  };

  const logoStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    fontWeight: '800',
    fontSize: '22px',
    fontFamily: 'Outfit',
    color: theme.brandPrimary,
    cursor: 'pointer'
  };

  const navLinksStyle = {
    display: 'flex',
    gap: '28px',
    alignItems: 'center'
  };

  const linkStyle = (active) => ({
    color: active ? theme.brandPrimary : theme.textSecondary,
    fontWeight: active ? '700' : '500',
    fontSize: '15px',
    cursor: 'pointer',
    transition: 'color 0.2s ease',
    textDecoration: 'none',
    position: 'relative',
    paddingBottom: '4px'
  });

  const indicatorStyle = {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: '3px',
    background: theme.brandPrimary,
    borderRadius: '2px'
  };

  const navItems = [
    { id: 'home', label: 'Home' },
    { id: 'vitals', label: 'Risk Analyzer' },
    { id: 'queue', label: 'Specialists & Queue Status' },
    { id: 'blood', label: 'Emergency Blood' },
    { id: 'blog', label: 'Health Blog' }
  ];

  return (
    <header style={headerStyle}>
      <div style={logoStyle} onClick={() => onTabChange('home')}>
        <Icon.Heart size={26} color={theme.brandPrimary} fill={theme.brandPrimary} />
        <span>NHCS AI</span>
      </div>

      <nav style={navLinksStyle}>
        {navItems.map(item => (
          <a
            key={item.id}
            style={linkStyle(currentTab === item.id)}
            onClick={() => onTabChange(item.id)}
          >
            {item.label}
            {currentTab === item.id && <span style={indicatorStyle}></span>}
          </a>
        ))}
      </nav>

      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <button
          style={{
            background: theme.bgInput,
            border: `1px solid ${theme.border}`,
            padding: '10px',
            borderRadius: '50%',
            cursor: 'pointer',
            color: theme.brandPrimary,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}
          onClick={toggleTheme}
          title="Toggle Dark/Light Mode"
        >
          {themeMode === 'dark' ? <Icon.Sun size={18} /> : <Icon.Moon size={18} />}
        </button>
        <button
          style={{
            background: theme.brandPrimary,
            color: '#ffffff',
            border: 'none',
            padding: '10px 20px',
            borderRadius: '10px',
            fontWeight: '600',
            fontSize: '14px',
            cursor: 'pointer'
          }}
          onClick={onOpenLogin}
        >
          Login Portal
        </button>
      </div>
    </header>
  );
}
window.NavigationHeader = NavigationHeader;

// 3. Web Footer matching HemoGenesis design
function Footer() {
  const { theme } = useTheme();

  const footerStyle = {
    background: theme.mode === 'dark' ? '#090f1e' : '#e2e8f0',
    borderTop: `1px solid ${theme.border}`,
    padding: '40px 80px 20px 80px',
    color: theme.textSecondary,
    fontSize: '14px',
    marginTop: 'auto'
  };

  return (
    <footer style={footerStyle}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '40px', flexWrap: 'wrap', gap: '20px' }}>
        <div style={{ maxWidth: '300px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontWeight: '800', fontSize: '20px', color: theme.brandPrimary, marginBottom: '12px' }}>
            <Icon.Heart size={24} color={theme.brandPrimary} fill={theme.brandPrimary} />
            <span>NHCS AI</span>
          </div>
          <p style={{ lineHeight: '1.6' }}>Universal digital healthcare system bridging patients, doctors, and clinical facilities securely.</p>
        </div>
        <div>
          <h4 style={{ color: theme.textPrimary, marginBottom: '12px', fontWeight: '700' }}>Emergency Support</h4>
          <p style={{ marginBottom: '8px' }}>Medical Hotline: <strong>16263</strong></p>
          <p>Virtual Counseling: <strong>01833-311199</strong></p>
        </div>
        <div>
          <h4 style={{ color: theme.textPrimary, marginBottom: '12px', fontWeight: '700' }}>Affiliates</h4>
          <p style={{ marginBottom: '8px' }}>BSMMU Hospital</p>
          <p>Bangladesh National Health Registry</p>
        </div>
      </div>
      <div style={{ borderTop: `1px solid ${theme.border}`, paddingTop: '20px', display: 'flex', justifyContent: 'space-between', fontSize: '12px' }}>
        <span>© {new Date().getFullYear()} NHCS AI. All rights reserved.</span>
        <span>Secured using HIPAA Complaint Standards</span>
      </div>
    </footer>
  );
}
window.Footer = Footer;
