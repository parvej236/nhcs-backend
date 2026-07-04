// app.jsx - Coordinator, Canvas Zoom & Pan layout, and Artboard controls
const { ThemeProvider, useTheme, Btn, Icon } = window;
const { DesktopShell, NavigationHeader, Footer, LoginPortal } = window;
const { WelcomeBanner, WebHero, StatRow } = window;
const { VitalsChecker, DoctorQueueTracker, HealthBlogHub, PatientPortal, DoctorPortal, HospitalPortal, EmergencyBloodRequest, LiveDonorAvailability, OurSolutions, SuccessStories } = window;

const { useState } = React;

function AppContent() {
  const { theme, themeMode } = useTheme();
  const [hasStarted, setHasStarted] = useState(false);
  const [currentTab, setCurrentTab] = useState('home');
  const [zoom, setZoom] = useState(0.85);
  const [showLogin, setShowLogin] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userRole, setUserRole] = useState('patient'); // patient | doctor | hospital

  const canvasStyle = {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    padding: '40px',
    background: themeMode === 'dark' ? '#0f172a' : '#f1f5f9',
    width: '100vw',
    height: '100vh',
    overflow: 'auto',
    position: 'relative'
  };

  const workspaceStyle = {
    transform: `scale(${zoom})`,
    transformOrigin: 'top center',
    transition: 'transform 0.15s cubic-bezier(0.4, 0, 0.2, 1)',
    display: 'flex',
    flexDirection: 'column',
    gap: '60px',
    paddingBottom: '200px'
  };

  const controlsStyle = {
    position: 'fixed',
    bottom: '30px',
    left: '50%',
    transform: 'translateX(-50%)',
    background: themeMode === 'dark' ? 'rgba(30, 41, 59, 0.85)' : 'rgba(255, 255, 255, 0.85)',
    backdropFilter: 'blur(12px)',
    border: `1px solid ${theme.border}`,
    borderRadius: '30px',
    padding: '10px 24px',
    display: 'flex',
    gap: '16px',
    alignItems: 'center',
    boxShadow: theme.shadow,
    zIndex: 1000
  };

  const renderActiveTab = () => {
    switch (currentTab) {
      case 'vitals':
        return <VitalsChecker />;
      case 'queue':
        return <DoctorQueueTracker />;
      case 'blood':
        return (
          <>
            <EmergencyBloodRequest />
            <LiveDonorAvailability />
          </>
        );
      case 'blog':
        return <HealthBlogHub />;
      case 'home':
      default:
        return (
          <>
            <WebHero 
              onScrollToVitals={() => document.getElementById('vitals-section')?.scrollIntoView({ behavior: 'smooth' })} 
              onScrollToDoctors={() => document.getElementById('doctors-section')?.scrollIntoView({ behavior: 'smooth' })} 
            />
            <StatRow />
            <VitalsChecker />
            <DoctorQueueTracker />
            <EmergencyBloodRequest />
            <LiveDonorAvailability />
            <OurSolutions />
            <SuccessStories />
            <HealthBlogHub />
          </>
        );
    }
  };

  if (!hasStarted) {
    return (
      <div style={{ ...canvasStyle, justifyContent: 'center' }}>
        <WelcomeBanner onStart={() => setHasStarted(true)} />
      </div>
    );
  }

  return (
    <div style={canvasStyle}>
      {/* Zoom / Navigation Controls */}
      <div style={controlsStyle}>
        <span style={{ fontSize: '13px', fontWeight: '700', color: theme.textSecondary }}>Zoom: {Math.round(zoom * 100)}%</span>
        <button 
          style={{ background: 'transparent', border: 'none', color: theme.textPrimary, cursor: 'pointer', display: 'flex', alignItems: 'center' }} 
          onClick={() => setZoom(prev => Math.max(0.5, prev - 0.05))}
        >
          ➖
        </button>
        <button 
          style={{ background: 'transparent', border: 'none', color: theme.textPrimary, cursor: 'pointer', display: 'flex', alignItems: 'center' }} 
          onClick={() => setZoom(prev => Math.min(1.5, prev + 0.05))}
        >
          ➕
        </button>
        <button 
          style={{ background: theme.brandPrimary, border: 'none', color: '#ffffff', padding: '6px 14px', borderRadius: '20px', fontSize: '12px', fontWeight: '700', cursor: 'pointer' }}
          onClick={() => setZoom(0.85)}
        >
          Reset View
        </button>
      </div>

      <div style={workspaceStyle}>
        <div style={{ textAlign: 'center', marginBottom: '-20px' }}>
          <h2 style={{ fontSize: '24px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>
            Live Interactive Desktop Prototype
          </h2>
          <p style={{ fontSize: '14px', color: theme.textSecondary }}>Toggle between tabs in the browser header to test specific features</p>
        </div>

        {/* Live Mock Desktop Browser frame */}
        <DesktopShell>
          {isLoggedIn ? (
            userRole === 'doctor' ? (
              <DoctorPortal onLogout={() => { setIsLoggedIn(false); setShowLogin(false); }} />
            ) : userRole === 'hospital' ? (
              <HospitalPortal onLogout={() => { setIsLoggedIn(false); setShowLogin(false); }} />
            ) : (
              <PatientPortal onLogout={() => { setIsLoggedIn(false); setShowLogin(false); }} />
            )
          ) : showLogin ? (
            <LoginPortal 
              onClose={() => setShowLogin(false)} 
              onLogin={(uname) => {
                const isDoc = uname && uname.toLowerCase().startsWith('doctor');
                const isHosp = uname && uname.toLowerCase().startsWith('hospital');
                setUserRole(isHosp ? 'hospital' : (isDoc ? 'doctor' : 'patient'));
                setIsLoggedIn(true);
              }}
            />
          ) : (
            <>
              <NavigationHeader 
                currentTab={currentTab} 
                onTabChange={setCurrentTab} 
                onOpenLogin={() => setShowLogin(true)} 
              />
              {renderActiveTab()}
              <Footer />
            </>
          )}
        </DesktopShell>
      </div>
    </div>
  );
}

// Root Entry Point
function App() {
  return (
    <ThemeProvider>
      <AppContent />
    </ThemeProvider>
  );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
