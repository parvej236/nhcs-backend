// screens-common.jsx - Welcome page, web Hero section, stats, and login portal screen
const { useTheme, Card, Btn, Input, Select, Chip, Icon } = window;
const { useState } = React;

// 1. Artboard 0: Welcome panel / ReadMe Board
function WelcomeBanner({ onStart }) {
  const { theme } = useTheme();

  const containerStyle = {
    padding: '40px',
    maxWidth: '800px',
    margin: '40px auto',
    background: theme.bgCard,
    border: `1px solid ${theme.border}`,
    borderRadius: theme.radius,
    boxShadow: theme.shadow,
    color: theme.textPrimary
  };

  return (
    <div style={containerStyle}>
      <h1 style={{ fontSize: '32px', fontWeight: '800', marginBottom: '16px', color: theme.brandPrimary, fontFamily: 'Outfit' }}>
        NHCS Web Portal Prototype
      </h1>
      <p style={{ fontSize: '16px', color: theme.textSecondary, lineHeight: '1.6', marginBottom: '24px' }}>
        Welcome to the premium interactive UI/UX prototype for the <strong>National Healthcare System (NHCS)</strong>. 
        This dashboard focuses exclusively on the mandatory features you implemented in the backend, removing bloated pages for an elite, simplified user experience.
      </p>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '32px' }}>
        <div style={{ background: theme.bgInput, padding: '16px', borderRadius: theme.innerRadius, border: `1px solid ${theme.border}` }}>
          <h3 style={{ fontSize: '16px', fontWeight: '700', marginBottom: '8px', color: theme.brandPrimary }}>Real-time Vitals Analyzer</h3>
          <p style={{ fontSize: '13px', color: theme.textSecondary }}>Input symptoms & vitals to see automatic clinical risk ratings and warning categories.</p>
        </div>
        <div style={{ background: theme.bgInput, padding: '16px', borderRadius: theme.innerRadius, border: `1px solid ${theme.border}` }}>
          <h3 style={{ fontSize: '16px', fontWeight: '700', marginBottom: '8px', color: theme.brandPrimary }}>Live Doctor Queue Status</h3>
          <p style={{ fontSize: '13px', color: theme.textSecondary }}>Search and view active specialists with real-time patient queue trackers.</p>
        </div>
      </div>

      <div style={{ borderTop: `1px solid ${theme.border}`, paddingTop: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontSize: '13px', color: theme.textSecondary }}>Dual Theme Mode & Real-time Sync Support</span>
        <Btn onClick={onStart}>Launch Live Landing Page</Btn>
      </div>
    </div>
  );
}
window.WelcomeBanner = WelcomeBanner;

// 2. Main Web Hero Banner
function WebHero({ onScrollToVitals, onScrollToDoctors }) {
  const { theme } = useTheme();

  const heroStyle = {
    background: theme.mode === 'dark' 
      ? 'radial-gradient(circle at top right, rgba(239, 68, 68, 0.15), transparent 60%), #131c2e'
      : 'radial-gradient(circle at top right, rgba(239, 68, 68, 0.08), transparent 60%), #ffffff',
    padding: '80px 60px',
    borderRadius: theme.radius,
    border: `1px solid ${theme.border}`,
    margin: '30px 40px',
    position: 'relative',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '40px',
    boxShadow: theme.shadow
  };

  const badgeStyle = {
    background: 'rgba(239, 68, 68, 0.12)',
    color: theme.brandPrimary,
    padding: '8px 16px',
    borderRadius: '30px',
    fontSize: '13px',
    fontWeight: '700',
    display: 'inline-flex',
    alignItems: 'center',
    gap: '6px',
    marginBottom: '20px'
  };

  return (
    <div style={heroStyle}>
      <div style={{ maxWidth: '650px' }}>
        <div style={badgeStyle}>
          Real-time Clinical Sync Engine Active
        </div>
        <h1 style={{ fontSize: '46px', fontWeight: '800', lineHeight: '1.2', marginBottom: '20px', color: theme.textPrimary, fontFamily: 'Outfit' }}>
          Smart Health Hub: <br />
          <span style={{ color: theme.brandPrimary }}>Universal Digital Health</span> for Everyone
        </h1>
        <p style={{ fontSize: '17px', color: theme.textSecondary, lineHeight: '1.6', marginBottom: '32px' }}>
          An AI-powered central medical registry providing real-time vital analysis, doctor queue tracking, and secure digital prescription access.
        </p>
        
        <div style={{ display: 'flex', gap: '16px' }}>
          <Btn onClick={onScrollToVitals}>Run Vitals Check</Btn>
          <Btn variant="secondary" onClick={onScrollToDoctors}>Search for Doctor</Btn>
        </div>
      </div>

      {/* Decorative Interactive Widget Preview */}
      <Card style={{ width: '420px', background: 'rgba(27, 38, 64, 0.4)', backdropFilter: 'blur(10px)', border: `1px solid rgba(239, 68, 68, 0.25)` }}>
        <h3 style={{ fontSize: '18px', fontWeight: '700', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px', color: theme.brandPrimary }}>
          <Icon.Activity color={theme.brandPrimary} /> NHCS Live Tracker
        </h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '14px', color: theme.textSecondary }}>Active Registries</span>
            <strong style={{ fontSize: '16px', color: theme.textPrimary }}>1,200 Patients</strong>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '14px', color: theme.textSecondary }}>Hospitals Affiliated</span>
            <strong style={{ fontSize: '16px', color: theme.textPrimary }}>30 Centres</strong>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '14px', color: theme.textSecondary }}>Specialists Online</span>
            <strong style={{ fontSize: '16px', color: theme.textPrimary }}>70 Doctors</strong>
          </div>
        </div>
      </Card>
    </div>
  );
}
window.WebHero = WebHero;

// 3. Stats Row
function StatRow() {
  const { theme } = useTheme();

  const rowStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(3, 1fr)',
    gap: '24px',
    margin: '0 40px 40px 40px'
  };

  const statCardStyle = {
    textAlign: 'center',
    padding: '24px',
    background: theme.bgCard,
    border: `1px solid ${theme.border}`,
    borderRadius: theme.radius,
    boxShadow: theme.shadow
  };

  return (
    <div style={rowStyle}>
      <div style={statCardStyle}>
        <h2 style={{ fontSize: '36px', fontWeight: '800', color: theme.brandPrimary, marginBottom: '4px' }}>1.2K+</h2>
        <p style={{ color: theme.textSecondary, fontSize: '14px', fontWeight: '600' }}>Patients Seeded</p>
      </div>
      <div style={statCardStyle}>
        <h2 style={{ fontSize: '36px', fontWeight: '800', color: theme.brandPrimary, marginBottom: '4px' }}>70+</h2>
        <p style={{ color: theme.textSecondary, fontSize: '14px', fontWeight: '600' }}>Verified Specialists</p>
      </div>
      <div style={statCardStyle}>
        <h2 style={{ fontSize: '36px', fontWeight: '800', color: theme.brandPrimary, marginBottom: '4px' }}>30+</h2>
        <p style={{ color: theme.textSecondary, fontSize: '14px', fontWeight: '600' }}>Active Medical Facilities</p>
      </div>
    </div>
  );
}
window.StatRow = StatRow;

// 4. New Login & Sign Up Portal Screen (Full Page Mockup)
function LoginPortal({ onClose, onLogin }) {
  const { theme } = useTheme();
  const [activeForm, setActiveForm] = useState('login'); // login | register
  const [userRole, setUserRole] = useState('PATIENT');
  
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [username, setUsername] = useState('');

  const containerStyle = {
    padding: '60px 80px',
    minHeight: '650px',
    display: 'flex',
    flexDirection: 'column',
    position: 'relative',
    background: theme.bgMain,
    width: '100%',
    height: '100%'
  };

  const wrapperStyle = {
    display: 'grid',
    gridTemplateColumns: '1.2fr 1fr',
    gap: '60px',
    alignItems: 'center',
    margin: 'auto 0'
  };

  const roleOptions = [
    { value: 'PATIENT', label: 'Patient Portal' },
    { value: 'DOCTOR', label: 'Doctor Portal' },
    { value: 'HOSPITAL', label: 'Hospital Portal' }
  ];

  return (
    <div style={containerStyle}>
      {/* Back Button */}
      <div style={{ display: 'flex', justifyContent: 'flex-start', marginBottom: '30px' }}>
        <button
          style={{
            background: theme.bgCard,
            border: `1px solid ${theme.border}`,
            padding: '10px 18px',
            borderRadius: '20px',
            color: theme.textPrimary,
            fontWeight: '700',
            fontSize: '13px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}
          onClick={onClose}
        >
          🠔 Back to Home
        </button>
      </div>

      <div style={wrapperStyle}>
        {/* Left Side: Brand presentation */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '26px', fontWeight: '800', color: theme.brandPrimary }}>
            <Icon.Heart size={32} color={theme.brandPrimary} fill={theme.brandPrimary} />
            <span>NHCS Portal</span>
          </div>
          <h2 style={{ fontSize: '38px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit', lineHeight: '1.2' }}>
            Access Your Centralized Health Registries
          </h2>
          <p style={{ fontSize: '16px', color: theme.textSecondary, lineHeight: '1.6' }}>
            Log in to manage appointments, issue clinical prescriptions, provide laboratory evaluations, and view electronic health vault registries.
          </p>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', marginTop: '20px' }}>
            <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
              <Icon.Heart size={18} color={theme.brandPrimary} />
              <span style={{ fontSize: '14px', color: theme.textSecondary }}>Secure HIPAA compliant health vault records</span>
            </div>
            <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
              <Icon.Activity size={18} color={theme.brandPrimary} />
              <span style={{ fontSize: '14px', color: theme.textSecondary }}>Live clinical analytics sync for patients and doctors</span>
            </div>
          </div>
        </div>

        {/* Right Side: Tabbed Login / Sign Up card */}
        <Card style={{ padding: '30px' }}>
          {/* Tab Switcher */}
          <div style={{ display: 'flex', gap: '12px', borderBottom: `2px solid ${theme.border}`, paddingBottom: '16px', marginBottom: '24px' }}>
            <button
              style={{
                background: 'transparent',
                border: 'none',
                fontSize: '16px',
                fontWeight: '700',
                color: activeForm === 'login' ? theme.brandPrimary : theme.textSecondary,
                cursor: 'pointer',
                paddingBottom: '4px',
                borderBottom: activeForm === 'login' ? `3px solid ${theme.brandPrimary}` : 'none'
              }}
              onClick={() => setActiveForm('login')}
            >
              Sign In
            </button>
            <button
              style={{
                background: 'transparent',
                border: 'none',
                fontSize: '16px',
                fontWeight: '700',
                color: activeForm === 'register' ? theme.brandPrimary : theme.textSecondary,
                cursor: 'pointer',
                paddingBottom: '4px',
                borderBottom: activeForm === 'register' ? `3px solid ${theme.brandPrimary}` : 'none'
              }}
              onClick={() => setActiveForm('register')}
            >
              Register
            </button>
          </div>

          {activeForm === 'login' ? (
            /* LOGIN FORM */
            <div>
              <Input
                label="Username"
                placeholder="e.g. patient_100"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
              <Input
                label="Password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              
              <Btn style={{ width: '100%', marginTop: '10px' }} onClick={() => onLogin(username)}>
                Sign In to Portal
              </Btn>

              <div style={{ borderTop: `1px solid ${theme.border}`, marginTop: '20px', paddingTop: '15px' }}>
                <span style={{ fontSize: '11px', color: theme.textSecondary, display: 'block', marginBottom: '8px' }}>QUICK SIMULATOR LOGIN:</span>
                <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                  <Btn variant="secondary" style={{ padding: '6px 10px', fontSize: '11px', flexGrow: 1 }} onClick={() => { setUsername('patient_100'); onLogin('patient_100'); }}>Patient Demo</Btn>
                  <Btn variant="secondary" style={{ padding: '6px 10px', fontSize: '11px', flexGrow: 1 }} onClick={() => { setUsername('doctor_100'); onLogin('doctor_100'); }}>Doctor Demo</Btn>
                  <Btn variant="secondary" style={{ padding: '6px 10px', fontSize: '11px', flexGrow: 1 }} onClick={() => { setUsername('hospital_100'); onLogin('hospital_100'); }}>Hospital Demo</Btn>
                </div>
              </div>
            </div>
          ) : (
            /* REGISTER FORM */
            <div>
              <Input
                label="Full Name"
                placeholder="Nehal Ahmmed"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
              />
              <Input
                label="Email Address"
                placeholder="nehal@gmail.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
              <Input
                label="Username"
                placeholder="Choose username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
              <Input
                label="Password"
                type="password"
                placeholder="Create Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              
              <Btn style={{ width: '100%', marginTop: '10px' }} onClick={() => onLogin(username)}>
                Register Health Profile
              </Btn>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
window.LoginPortal = LoginPortal;

// 5. Emergency Blood Request Banner
function EmergencyBloodRequest() {
  const { theme } = useTheme();
  return (
    <div style={{ padding: '0 40px 40px 40px' }} id="blood-section">
      <div style={{
        background: 'linear-gradient(135deg, #e11d48, #9f1239)',
        borderRadius: theme.radius,
        padding: '40px 60px',
        color: '#ffffff',
        textAlign: 'center',
        boxShadow: theme.shadow,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '16px'
      }}>
        <h2 style={{ fontSize: '28px', fontWeight: '800', fontFamily: 'Outfit', margin: 0 }}>Emergency Blood Request</h2>
        <p style={{ fontSize: '15px', opacity: 0.9, maxWidth: '600px', margin: 0, lineHeight: '1.6' }}>
          Need blood urgently? Our AI-powered system instantly connects you with compatible donors in nearby hospital zones.
        </p>
        <div style={{ display: 'flex', gap: '16px', marginTop: '10px' }}>
          <button style={{ background: '#ffffff', color: '#dc2626', border: 'none', padding: '12px 24px', borderRadius: '10px', fontWeight: '700', fontSize: '14px', cursor: 'pointer', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }} onClick={() => alert('Emergency Blood Request Dispatched to nearby Donor network!')}>
            Request Blood Now
          </button>
          <button style={{ background: 'rgba(255,255,255,0.2)', color: '#ffffff', border: '1px solid rgba(255,255,255,0.4)', padding: '12px 24px', borderRadius: '10px', fontWeight: '700', fontSize: '14px', cursor: 'pointer' }} onClick={() => alert('Thank you for registering as an NHCS Donor!')}>
            Register as Donor
          </button>
        </div>
      </div>
    </div>
  );
}
window.EmergencyBloodRequest = EmergencyBloodRequest;

// 6. Live Donor Availability Widget
function LiveDonorAvailability() {
  const { theme } = useTheme();
  const hubs = [
    { name: 'Dhaka Central Hub', status: '92%', desc: 'Awaiting product configuration & matching', color: '#10b981' },
    { name: 'Chittagong Zone', status: '68%', desc: 'Collection status & verification active', color: '#f59e0b' },
    { name: 'Sylhet Division', status: '85%', desc: 'Efficiency optimization active', color: '#10b981' }
  ];

  return (
    <div style={{ padding: '0 40px 40px 40px' }}>
      <div style={{ textAlign: 'center', marginBottom: '24px' }}>
        <h2 style={{ fontSize: '24px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit', margin: '0 0 6px 0' }}>Live Donor Availability</h2>
        <span style={{ fontSize: '14px', color: theme.textSecondary }}>Real-time predictions for hospital hubs</span>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '24px' }}>
        {hubs.map((hub, idx) => (
          <Card key={idx} style={{ textAlign: 'center', padding: '24px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <h3 style={{ fontSize: '18px', fontWeight: '800', color: theme.textPrimary, margin: 0 }}>{hub.name}</h3>
            <span style={{ fontSize: '28px', fontWeight: '800', color: hub.color }}>{hub.status}</span>
            <p style={{ fontSize: '12px', color: theme.textSecondary, margin: '4px 0 12px 0' }}>{hub.desc}</p>
            <Btn variant="outline" style={{ width: '100%' }} onClick={() => alert(`Details for ${hub.name}: Active donors verified.`)}>View Details</Btn>
          </Card>
        ))}
      </div>
    </div>
  );
}
window.LiveDonorAvailability = LiveDonorAvailability;

// 7. Our Solutions
function OurSolutions() {
  const { theme } = useTheme();
  const solutions = [
    { title: 'Digital Vitals Risk Analyzer', desc: 'AI-powered immediate cardiac & diabetic symptom analysis and severity warnings.', icon: <Icon.Activity size={26} color={theme.brandPrimary} /> },
    { title: 'Universal Health Card ID', desc: 'One-click patient health profile & emergency ICE contact QR code registry.', icon: <Icon.Heart size={26} color={theme.brandPrimary} /> },
    { title: 'Active Doctor Queue Sync', desc: 'Links hospital arrivals check-in directly to specialist clinical workspaces.', icon: <Icon.User size={26} color={theme.brandPrimary} /> }
  ];

  return (
    <div style={{ padding: '0 40px 40px 40px' }}>
      <h2 style={{ fontSize: '26px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit', textAlign: 'center', marginBottom: '32px' }}>Our Solutions</h2>
      
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '24px' }}>
        {solutions.map((sol, idx) => (
          <Card key={idx} style={{ padding: '30px', display: 'flex', flexDirection: 'column', gap: '14px', height: '100%' }}>
            <div style={{ background: 'rgba(239, 68, 68, 0.08)', width: '56px', height: '56px', borderRadius: '14px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              {sol.icon}
            </div>
            <h3 style={{ fontSize: '17px', fontWeight: '800', color: theme.textPrimary, margin: 0 }}>{sol.title}</h3>
            <p style={{ fontSize: '13px', color: theme.textSecondary, lineHeight: '1.6', margin: 0 }}>{sol.desc}</p>
            <a style={{ fontSize: '13px', fontWeight: '700', color: theme.brandPrimary, textDecoration: 'none', cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: '4px', marginTop: 'auto' }} onClick={() => alert(`Details about ${sol.title} solution.`)}>
              Learn More ➔
            </a>
          </Card>
        ))}
      </div>
    </div>
  );
}
window.OurSolutions = OurSolutions;

// 8. Success Stories
function SuccessStories() {
  const { theme } = useTheme();
  return (
    <div style={{ padding: '0 40px 40px 40px' }}>
      <h2 style={{ fontSize: '26px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit', textAlign: 'center', marginBottom: '32px' }}>Success Stories</h2>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        <Card style={{ display: 'flex', gap: '16px', padding: '24px' }}>
          <div style={{ fontSize: '24px', color: theme.brandPrimary, display: 'flex', alignItems: 'center' }}><Icon.Heart size={24} /></div>
          <div>
            <h4 style={{ fontSize: '15px', fontWeight: '700', margin: '0 0 6px 0' }}>Dhaka Maternal Center</h4>
            <p style={{ fontSize: '13px', color: theme.textSecondary, fontStyle: 'italic', lineHeight: '1.6', margin: 0 }}>
              "NHCS helped us record and monitor maternal blood pressures in real time. The automatic Stage 1 Hypertension alert saved critical time during emergency deliveries."
            </p>
          </div>
        </Card>
        <Card style={{ display: 'flex', gap: '16px', padding: '24px' }}>
          <div style={{ fontSize: '24px', color: theme.brandPrimary, display: 'flex', alignItems: 'center' }}><Icon.Activity size={24} /></div>
          <div>
            <h4 style={{ fontSize: '15px', fontWeight: '700', margin: '0 0 6px 0' }}>BSMMU Queue Trial</h4>
            <p style={{ fontSize: '13px', color: theme.textSecondary, fontStyle: 'italic', lineHeight: '1.6', margin: 0 }}>
              "The reception check-in arrival sync reduces patient queue bottlenecks by 52%. Specialists can open active patient history in one click."
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
}
window.SuccessStories = SuccessStories;
