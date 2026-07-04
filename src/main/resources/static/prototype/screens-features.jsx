// screens-features.jsx - Vitals Risk Analyzer, Doctor Directory, Blog Hub, and Full Patient Portal
const { useTheme, Card, Btn, Input, Select, Chip, Progress, Icon } = window;
const { useState } = React;

// 1. Clinical Vitals Risk Analyzer Component (Also used as Healthcare AI)
function VitalsChecker({ embedded }) {
  const { theme } = useTheme();
  const [symptoms, setSymptoms] = useState('');
  const [analysisResult, setAnalysisResult] = useState(null);

  const runSimulation = (type) => {
    if (type === 'bp') {
      setSymptoms('I have a severe headache, occasionally feel dizzy when standing up, and my home BP machine showed a reading of 145/95 mmHg.');
      setAnalysisResult({
        category: 'Stage 2 Hypertension (High Risk)',
        severity: 'danger',
        bpVal: '145/95 mmHg',
        glucoseVal: '95 mg/dL (Normal)',
        summary: 'Your blood pressure is significantly elevated. Stage 2 hypertension requires professional clinical diagnosis and potential drug therapy.',
        recommendations: [
          'Seek medical evaluation immediately to avoid hypertensive crisis.',
          'Restrict dietary sodium and avoid strenuous physical efforts until checked.',
          'Log your blood pressure readings twice daily.'
        ]
      });
    } else if (type === 'diabetes') {
      setSymptoms('I feel constantly thirsty, urinate frequently during the night, and my fasting blood sugar index shows 152 mg/dL.');
      setAnalysisResult({
        category: 'Type-2 Diabetic Range (High Risk)',
        severity: 'danger',
        bpVal: '120/80 mmHg (Normal)',
        glucoseVal: '152 mg/dL (High)',
        summary: 'Your fasting blood glucose exceeds 126 mg/dL. Combined with frequent urination and excessive thirst, this strongly suggests diabetes.',
        recommendations: [
          'Schedule an HbA1c blood test to verify 3-month average glucose levels.',
          'Consult a specialist for personalized insulin or Metformin therapy.',
          'Eliminate refined sugars and simple carbs from your current diet.'
        ]
      });
    } else if (type === 'normal') {
      setSymptoms('Feeling healthy. Did a regular checkup: BP is 118/75 mmHg and glucose is 85 mg/dL.');
      setAnalysisResult({
        category: 'Normal & Healthy Baseline',
        severity: 'success',
        bpVal: '118/75 mmHg',
        glucoseVal: '85 mg/dL',
        summary: 'All vital indices are within safe clinical ranges. Excellent cardiovascular and metabolic baseline.',
        recommendations: [
          'Continue regular balanced diet and hydration.',
          'Engage in 150 minutes of moderate aerobic exercise weekly.',
          'Schedule routine checkup in 6 months.'
        ]
      });
    }
  };

  return (
    <div id="vitals-section" style={{ padding: embedded ? '0' : '0 40px 40px 40px' }}>
      {!embedded && (
        <h2 style={{ fontSize: '26px', fontWeight: '800', marginBottom: '24px', color: theme.textPrimary, fontFamily: 'Outfit' }}>
          🔬 Clinical Vitals Risk Analyzer
        </h2>
      )}
      
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px' }}>
        {/* Left Side: Vitals Input Form */}
        <Card style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <h3 style={{ fontSize: '18px', fontWeight: '700', color: theme.textPrimary }}>Symptom & Vitals Input</h3>
          
          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
            <Btn variant="outline" style={{ padding: '8px 14px', fontSize: '12px' }} onClick={() => runSimulation('bp')}>
              Simulate High BP
            </Btn>
            <Btn variant="outline" style={{ padding: '8px 14px', fontSize: '12px' }} onClick={() => runSimulation('diabetes')}>
              Simulate Diabetes
            </Btn>
            <Btn variant="outline" style={{ padding: '8px 14px', fontSize: '12px' }} onClick={() => runSimulation('normal')}>
              Simulate Healthy
            </Btn>
          </div>

          <textarea
            style={{
              width: '100%',
              height: '140px',
              background: theme.bgInput,
              border: `1px solid ${theme.border}`,
              color: theme.textPrimary,
              padding: '16px',
              borderRadius: theme.innerRadius,
              fontSize: '14px',
              outline: 'none',
              resize: 'none'
            }}
            value={symptoms}
            onChange={(e) => setSymptoms(e.target.value)}
            placeholder="Type symptoms or clinical records here (e.g. Feeling dizzy, BP 140/90, Glucose 110)..."
          />

          <div style={{ background: 'rgba(239, 68, 68, 0.08)', padding: '12px', borderRadius: theme.innerRadius, border: `1px dashed ${theme.border}`, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <span style={{ fontSize: '13px', color: theme.textSecondary }}>📎 Upload Medical Reports (PDF or TXT only)</span>
          </div>
        </Card>

        {/* Right Side: Risk Assessment Result */}
        <Card style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          {analysisResult ? (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <h3 style={{ fontSize: '18px', fontWeight: '700', color: theme.textPrimary }}>AI Health Assessment</h3>
                <Chip label={analysisResult.category} status={analysisResult.severity} />
              </div>
              
              <div style={{ display: 'flex', gap: '20px', marginBottom: '16px', fontSize: '13px', color: theme.textSecondary }}>
                <div>BP: <strong style={{ color: theme.textPrimary }}>{analysisResult.bpVal}</strong></div>
                <div>Glucose: <strong style={{ color: theme.textPrimary }}>{analysisResult.glucoseVal}</strong></div>
              </div>

              <p style={{ fontSize: '14px', color: theme.textPrimary, lineHeight: '1.6', marginBottom: '16px', background: theme.bgInput, padding: '14px', borderRadius: theme.innerRadius }}>
                {analysisResult.summary}
              </p>

              <h4 style={{ fontSize: '14px', fontWeight: '700', marginBottom: '8px', color: theme.textPrimary }}>Clinical Recommendations:</h4>
              <ul style={{ paddingLeft: '20px', fontSize: '13px', color: theme.textSecondary, lineHeight: '1.8' }}>
                {analysisResult.recommendations.map((rec, i) => <li key={i}>{rec}</li>)}
              </ul>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', color: theme.textSecondary, textAlign: 'center', padding: '40px 0' }}>
              <div style={{ color: theme.brandPrimary, marginBottom: '16px' }}><Icon.Activity size={48} /></div>
              <h3 style={{ fontSize: '18px', fontWeight: '700', color: theme.textPrimary, marginBottom: '8px' }}>Awaiting Input Parameters</h3>
              <p style={{ fontSize: '14px', maxWidth: '300px' }}>Select one of the simulation triggers on the left to see dynamic AI health screening in action.</p>
            </div>
          )}

          <div style={{ marginTop: '20px', borderTop: `1px solid ${theme.border}`, paddingTop: '15px', fontSize: '11px', color: theme.textSecondary }}>
            Disclaimers: AI predictions are for screening only. Do not replace clinical doctor diagnoses.
          </div>
        </Card>
      </div>
    </div>
  );
}
window.VitalsChecker = VitalsChecker;

// 2. Doctor Directory & Queue Tracker
function DoctorQueueTracker() {
  const { theme } = useTheme();
  const [searchTerm, setSearchTerm] = useState('');
  const [doctors, setDoctors] = useState([
    { id: 1001, name: 'Dr. Fatema Ahmed', specialty: 'Cardiology', hospital: 'Evercare Hospital', fee: '1000 BDT', experience: '12 Years', queue: 3, rating: 4.8 },
    { id: 1002, name: 'Dr. Khadija Begum', specialty: 'Psychiatry', hospital: 'Popular Diagnostic & Hospital', fee: '1200 BDT', experience: '15 Years', queue: 5, rating: 4.9 },
    { id: 1003, name: 'Dr. Mushfiqur Rahman', specialty: 'Neurology', hospital: 'United Hospital', fee: '1500 BDT', experience: '10 Years', queue: 2, rating: 4.7 }
  ]);

  const joinQueue = (id) => {
    setDoctors(prev => prev.map(doc => {
      if (doc.id === id) {
        return { ...doc, queue: doc.queue + 1 };
      }
      return doc;
    }));
  };

  const filteredDoctors = doctors.filter(doc => 
    doc.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    doc.specialty.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div id="doctors-section" style={{ padding: '0 40px 40px 40px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <h2 style={{ fontSize: '26px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>
          Search Specialists & Queue Status
        </h2>
        
        {/* Search Bar in Doctor Section */}
        <div style={{ position: 'relative', width: '320px' }}>
          <input
            type="text"
            style={{
              width: '100%',
              background: theme.bgCard,
              border: `1px solid ${theme.border}`,
              color: theme.textPrimary,
              padding: '10px 16px 10px 40px',
              borderRadius: '20px',
              fontSize: '14px',
              outline: 'none'
            }}
            placeholder="Search doctors or specialties..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <span style={{ position: 'absolute', left: '14px', top: '10px', color: theme.textSecondary }}>🔍</span>
        </div>
      </div>
      
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '24px' }}>
        {filteredDoctors.length > 0 ? (
          filteredDoctors.map(doc => (
            <Card key={doc.id} style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '14px' }}>
                  <div>
                    <h3 style={{ fontSize: '18px', fontWeight: '800', color: theme.textPrimary }}>{doc.name}</h3>
                    <span style={{ fontSize: '13px', color: theme.brandPrimary, fontWeight: '700' }}>{doc.specialty}</span>
                  </div>
                  <span style={{ background: 'rgba(245, 158, 11, 0.15)', color: '#f59e0b', padding: '4px 8px', borderRadius: '8px', fontSize: '12px', fontWeight: '700' }}>
                    ⭐ {doc.rating}
                  </span>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '20px', fontSize: '14px', color: theme.textSecondary }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>Affiliation:</span>
                    <strong style={{ color: theme.textPrimary }}>{doc.hospital}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>Consultation Fee:</span>
                    <strong style={{ color: theme.textPrimary }}>{doc.fee}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>Experience:</span>
                    <strong style={{ color: theme.textPrimary }}>{doc.experience}</strong>
                  </div>
                </div>
              </div>

              <div>
                <div style={{ marginBottom: '16px', background: theme.bgInput, padding: '12px', borderRadius: theme.innerRadius, border: `1px solid ${theme.border}` }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                    <span style={{ fontSize: '13px', color: theme.textSecondary }}>Queue Traffic:</span>
                    <strong style={{ fontSize: '14px', color: theme.textPrimary }}>{doc.queue} Patients Waiting</strong>
                  </div>
                  <Progress value={doc.queue} max={10} color={doc.queue > 4 ? theme.warning : theme.success} />
                </div>
                
                <Btn style={{ width: '100%' }} onClick={() => joinQueue(doc.id)}>Join Queue Slot</Btn>
              </div>
            </Card>
          ))
        ) : (
          <div style={{ gridColumn: 'span 3', textAlign: 'center', padding: '40px', color: theme.textSecondary }}>
            No specialists found matching your search term.
          </div>
        )}
      </div>
    </div>
  );
}
window.DoctorQueueTracker = DoctorQueueTracker;

// 3. Health Blog Hub
function HealthBlogHub() {
  const { theme } = useTheme();

  return (
    <div style={{ padding: '0 40px 40px 40px' }}>
      <h2 style={{ fontSize: '26px', fontWeight: '800', marginBottom: '24px', color: theme.textPrimary, fontFamily: 'Outfit' }}>
        📰 NHCS AI Health Insights (Blog Hub)
      </h2>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px' }}>
        <Card style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', width: 'fit-content', padding: '6px 12px', borderRadius: '8px', color: theme.brandPrimary, fontWeight: '700', fontSize: '12px' }}>
            GENETIC AWARENESS
          </div>
          <h3 style={{ fontSize: '20px', fontWeight: '800', color: theme.textPrimary }}>Understanding Thalassemia Carriers</h3>
          <p style={{ fontSize: '14px', color: theme.textSecondary, lineHeight: '1.6' }}>
            Being a carrier (Thalassemia trait) means you carry one mutated gene, but usually show no symptoms. Screening before marriage is vital. If both parents carry the trait, there is a 25% chance their child will have Thalassemia Major.
          </p>
          <span style={{ color: theme.brandPrimary, fontWeight: '700', fontSize: '13px', cursor: 'pointer' }}>Read Article ➔</span>
        </Card>

        <Card style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{ background: 'rgba(16, 185, 129, 0.1)', width: 'fit-content', padding: '6px 12px', borderRadius: '8px', color: theme.success, fontWeight: '700', fontSize: '12px' }}>
            CLINICAL CARDIO
          </div>
          <h3 style={{ fontSize: '20px', fontWeight: '800', color: theme.textPrimary }}>Cardiovascular Health & BP Guidelines</h3>
          <p style={{ fontSize: '14px', color: theme.textSecondary, lineHeight: '1.6' }}>
            Maintaining target blood pressure (below 120/80 mmHg) is critical for preventing renal stress and stroke risks. Monitor daily, reduce sodium intake, and understand your systolic ranges.
          </p>
          <span style={{ color: theme.brandPrimary, fontWeight: '700', fontSize: '13px', cursor: 'pointer' }}>Read Article ➔</span>
        </Card>
      </div>
    </div>
  );
}
window.HealthBlogHub = HealthBlogHub;

// 4. Redesigned Complete Patient Portal Workspace Component
function PatientPortal({ onLogout }) {
  const { theme } = useTheme();
  
  // Navigation State
  const [activeSubTab, setActiveSubTab] = useState('dashboard');
  
  // Wizard Modal States
  const [showWizard, setShowWizard] = useState(false);
  const [wizardStep, setWizardStep] = useState(0);

  // Blood Donation Simulator States
  const [isDonorActive, setIsDonorActive] = useState(false);
  const [bloodEligibilitySim, setBloodEligibilitySim] = useState('safe'); // safe | asthma | recent
  const [bloodRequests, setBloodRequests] = useState([
    { id: 'BR-1', hospital: 'Dhaka Central Hospital', location: 'Dhanmondi, Dhaka', patient: 'Rafiq Ahmmed', group: 'O+', urgency: 'High', time: 'Within 2 Hours', status: 'Pending' },
    { id: 'BR-2', hospital: 'Sylhet General Hospital', location: 'Amberkhana, Sylhet', patient: 'Salma Begum', group: 'O+', urgency: 'Moderate', time: 'Tomorrow Morning', status: 'Pending' }
  ]);
  const [donationHistory, setDonationHistory] = useState([
    { id: 'BH-1', hospital: 'Dhaka Central Hospital', recipient: 'Rafiqul Islam', group: 'O+', date: 'June 10, 2026', units: '1 Unit', status: 'Verified' }
  ]);

  const handleAcceptRequest = (id) => {
    setBloodRequests(prev => prev.map(req => req.id === id ? { ...req, status: 'Accepted' } : req));
    alert('Thank you! You have accepted the request. Syncing with the reception queue.');
  };

  const handleDeclineRequest = (id) => {
    setBloodRequests(prev => prev.map(req => req.id === id ? { ...req, status: 'Declined' } : req));
  };

  // Appointments Tab State
  const [appointmentTab, setAppointmentTab] = useState('Upcoming');
  const [showBookingDialog, setShowBookingDialog] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState('1002');
  const [appointmentDate, setAppointmentDate] = useState('2026-07-15');
  const [appointmentTime, setAppointmentTime] = useState('10:00 AM');
  const [symptomText, setSymptomText] = useState('');

  // Medical Vault Tab State
  const [vaultTab, setVaultTab] = useState('prescriptions');

  // AI Assistant chat states
  const [chatMessages, setChatMessages] = useState([
    { sender: 'ai', text: 'Hello Nehal! I am your AI Medical Booking Assistant. Describe your symptoms and I will suggest the appropriate specialist.' }
  ]);
  const [chatInput, setChatInput] = useState('');

  const sendChatMessage = (presetText) => {
    const textToSend = presetText || chatInput;
    if (!textToSend.trim()) return;

    const newMsgs = [...chatMessages, { sender: 'user', text: textToSend }];
    setChatMessages(newMsgs);
    setChatInput('');

    // AI suggestion response
    setTimeout(() => {
      let aiText = "Based on your symptoms, we suggest booking a Consultation with our Psychiatry department (Dr. Khadija Begum) or Cardiology department.";
      if (textToSend.toLowerCase().includes('chest') || textToSend.toLowerCase().includes('heart') || textToSend.toLowerCase().includes('bp')) {
        aiText = "Heart rate or BP symptoms detected. We highly recommend consulting Dr. Fatema Ahmed in Cardiology immediately.";
      } else if (textToSend.toLowerCase().includes('headache') || textToSend.toLowerCase().includes('stress')) {
        aiText = "Stress or anxiety signs detected. We suggest scheduling a counseling session with Dr. Khadija Begum in Psychiatry.";
      }
      setChatMessages(prev => [...prev, { sender: 'ai', text: aiText }]);
    }, 1000);
  };

  // Nav Items configuration
  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: <Icon.Activity size={18} /> },
    { id: 'profile', label: 'My Health Profile', icon: <Icon.User size={18} /> },
    { id: 'appointments', label: 'Doctors Appointment', icon: <Icon.Calendar size={18} /> },
    { id: 'timeline', label: 'Health Timeline', icon: <Icon.FileText size={18} /> },
    { id: 'vault', label: 'Medical Vault', icon: <Icon.FileText size={18} /> },
    { id: 'bloodDonation', label: 'Blood Donation', icon: <Icon.Heart size={18} /> },
    { id: 'ai', label: 'Healthcare AI', icon: <Icon.Activity size={18} /> }
  ];

  return (
    <div style={{ display: 'flex', minHeight: '680px', width: '100%', background: theme.bgMain }}>
      {/* Side Nav Bar */}
      <div style={{ width: '260px', borderRight: `1px solid ${theme.border}`, background: theme.bgCard, padding: '24px 16px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
        <div>
          {/* Logo */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontWeight: '800', fontSize: '20px', color: theme.brandPrimary, marginBottom: '32px', paddingLeft: '8px' }}>
            <Icon.Heart size={24} color={theme.brandPrimary} fill={theme.brandPrimary} />
            <span>NHCS Portal</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {navItems.map(item => {
              const active = activeSubTab === item.id;
              return (
                <button
                  key={item.id}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                    width: '100%',
                    padding: '12px 16px',
                    borderRadius: theme.innerRadius,
                    border: 'none',
                    background: active ? 'rgba(239, 68, 68, 0.12)' : 'transparent',
                    color: active ? theme.brandPrimary : theme.textSecondary,
                    fontWeight: active ? '700' : '600',
                    fontSize: '14px',
                    textAlign: 'left',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease'
                  }}
                  onClick={() => setActiveSubTab(item.id)}
                >
                  <span style={{ display: 'flex', alignItems: 'center', color: active ? theme.brandPrimary : theme.textSecondary }}>{item.icon}</span> {item.label}
                </button>
              );
            })}
          </div>
        </div>

        <button
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            padding: '12px 16px',
            borderRadius: theme.innerRadius,
            border: `1px solid ${theme.border}`,
            background: 'transparent',
            color: theme.brandPrimary,
            fontWeight: '700',
            fontSize: '14px',
            cursor: 'pointer',
            textAlign: 'left'
          }}
          onClick={onLogout}
        >
          Sign Out
        </button>
      </div>

      {/* Main Workspace Content Area */}
      <div style={{ flexGrow: 1, padding: '40px', overflowY: 'auto' }}>
        
        {/* DASHBOARD SCREEN */}
        {activeSubTab === 'dashboard' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            {/* Hero Health Card */}
            <div style={{
              background: `linear-gradient(135deg, ${theme.brandPrimary}, #de3b3b, #c92a2a)`,
              borderRadius: theme.radius,
              padding: '30px',
              color: '#ffffff',
              boxShadow: theme.shadow,
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}>
              <div>
                <span style={{ fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.05em', opacity: 0.8 }}>NUDHEB Digital Health Card</span>
                <h2 style={{ fontSize: '28px', fontWeight: '800', margin: '10px 0 6px 0', fontFamily: 'Outfit' }}>Nehal Ahmmed</h2>
                <div style={{ fontSize: '14px', opacity: 0.9, fontFamily: 'monospace' }}>Health ID: NHCS-800452</div>
                
                <div style={{ display: 'flex', gap: '20px', marginTop: '20px', fontSize: '13px' }}>
                  <div>Blood Group: <strong>O Positive (O+)</strong></div>
                  <div>Age: <strong>24 Years</strong></div>
                  <div>Chronic Conditions: <strong>Asthma</strong></div>
                </div>
              </div>
              <div style={{ background: 'rgba(255,255,255,0.15)', padding: '16px', borderRadius: '12px', fontSize: '32px' }}>
                QR
              </div>
            </div>

            {/* Quick Acti             <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px' }}>
              <Card style={{ display: 'flex', alignItems: 'center', gap: '14px', padding: '16px 20px', cursor: 'pointer' }} onClick={() => setActiveSubTab('appointments')}>
                <span style={{ display: 'flex', alignItems: 'center' }}><Icon.Calendar size={24} color={theme.brandPrimary} /></span>
                <div>
                  <h4 style={{ fontWeight: '700', fontSize: '15px' }}>Book Appointment</h4>
                  <span style={{ fontSize: '12px', color: theme.textSecondary }}>Find doctor slot</span>
                </div>
              </Card>
              <Card style={{ display: 'flex', alignItems: 'center', gap: '14px', padding: '16px 20px', cursor: 'pointer' }} onClick={() => setActiveSubTab('timeline')}>
                <span style={{ display: 'flex', alignItems: 'center' }}><Icon.FileText size={24} color={theme.brandPrimary} /></span>
                <div>
                  <h4 style={{ fontWeight: '700', fontSize: '15px' }}>Health Timeline</h4>
                  <span style={{ fontSize: '12px', color: theme.textSecondary }}>Check history log</span>
                </div>
              </Card>
              <Card style={{ display: 'flex', alignItems: 'center', gap: '14px', padding: '16px 20px', cursor: 'pointer' }} onClick={() => setActiveSubTab('vault')}>
                <span style={{ display: 'flex', alignItems: 'center' }}><Icon.Activity size={24} color={theme.brandPrimary} /></span>
                <div>
                  <h4 style={{ fontWeight: '700', fontSize: '15px' }}>Medical Vault</h4>
                  <span style={{ fontSize: '12px', color: theme.textSecondary }}>Clinical documents</span>
                </div>
              </Card>
            </div>

            {/* Vitals Summary & Briefing */}
            <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '30px' }}>
              <Card>
                <h3 style={{ fontSize: '18px', fontWeight: '800', color: theme.textPrimary, marginBottom: '16px' }}>AI Vitals & Health Briefing</h3>
                <div style={{ background: theme.bgInput, padding: '16px', borderRadius: theme.innerRadius, border: `1px solid ${theme.border}`, marginBottom: '16px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <strong>Blood Pressure Status:</strong>
                    <Chip label="Stage 1 Hypertension" status="warning" />
                  </div>
                  <p style={{ fontSize: '13px', color: theme.textSecondary }}>BP logged at 132/85 mmHg. Daily recommendations: restrict high sodium intake and log vitals.</p>
                </div>
                <div style={{ background: theme.bgInput, padding: '16px', borderRadius: theme.innerRadius, border: `1px solid ${theme.border}` }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <strong>Blood Glucose Status:</strong>
                    <Chip label="Normal Range" status="success" />
                  </div>
                  <p style={{ fontSize: '13px', color: theme.textSecondary }}>Glucose level logged at 90 mg/dL. Excellent metabolic baseline.</p>
                </div>
              </Card>

              <Card style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <h3 style={{ fontSize: '18px', fontWeight: '800', color: theme.textPrimary }}>Recent Actions Summary</h3>
                
                <div style={{ borderBottom: `1px solid ${theme.border}`, paddingBottom: '12px' }}>
                  <div style={{ fontSize: '12px', color: theme.textSecondary }}>Active Prescription</div>
                  <strong style={{ fontSize: '14px', color: theme.textPrimary }}>Metformin HCl 500mg</strong>
                </div>
                <div>
                  <div style={{ fontSize: '12px', color: theme.textSecondary }}>Upcoming Consultation</div>
                  <strong style={{ fontSize: '14px', color: theme.textPrimary }}>Dr. Khadija Begum (July 10)</strong>
                </div>
              </Card>
            </div>
          </div>
        )}

        {/* PROFILE SCREEN */}
        {activeSubTab === 'profile' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2 style={{ fontSize: '24px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>My Health Profile</h2>
              <Btn onClick={() => { setWizardStep(0); setShowWizard(true); }}>Update Profile Wizard</Btn>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '30px' }}>
              <Card style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                <h3 style={{ fontSize: '18px', fontWeight: '700', borderBottom: `1px solid ${theme.border}`, paddingBottom: '10px' }}>Personal Information</h3>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', fontSize: '14px' }}>
                  <div>Name: <strong style={{ color: theme.textPrimary }}>Nehal Ahmmed</strong></div>
                  <div>DOB: <strong style={{ color: theme.textPrimary }}>12 October, 2002</strong></div>
                  <div>Gender: <strong style={{ color: theme.textPrimary }}>Male</strong></div>
                  <div>NID: <strong style={{ color: theme.textPrimary }}>820492819</strong></div>
                  <div>Phone: <strong style={{ color: theme.textPrimary }}>01712-345678</strong></div>
                  <div>Occupation: <strong style={{ color: theme.textPrimary }}>Software Engineer</strong></div>
                </div>

                <h3 style={{ fontSize: '18px', fontWeight: '700', borderBottom: `1px solid ${theme.border}`, paddingBottom: '10px', marginTop: '10px' }}>Addresses</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '14px' }}>
                  <div>Present Address: <strong style={{ color: theme.textPrimary }}>House 42, Road 11, Dhanmondi, Dhaka</strong></div>
                  <div>Permanent Address: <strong style={{ color: theme.textPrimary }}>Village Green, P.O. Box 102, Sylhet</strong></div>
                </div>

                <h3 style={{ fontSize: '18px', fontWeight: '700', borderBottom: `1px solid ${theme.border}`, paddingBottom: '10px', marginTop: '10px' }}>Emergency Contacts</h3>
                <div style={{ fontSize: '14px' }}>
                  <div>ICE Name: <strong style={{ color: theme.textPrimary }}>Rafiq Ahmmed</strong> (Father) | Phone: <strong style={{ color: theme.textPrimary }}>01911-987654</strong></div>
                </div>
              </Card>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
                <Card>
                  <h3 style={{ fontSize: '18px', fontWeight: '700', marginBottom: '16px' }}>Allergies & Chronic Diseases</h3>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span>Pollen Allergy</span>
                      <Chip label="Low Severity" status="success" />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span>Dust Mites</span>
                      <Chip label="Medium Severity" status="warning" />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span>Asthma</span>
                      <Chip label="Active Case" status="danger" />
                    </div>
                  </div>
                </Card>

                <Card>
                  <h3 style={{ fontSize: '18px', fontWeight: '700', marginBottom: '16px' }}>Current Vitals Summary</h3>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', fontSize: '13px' }}>
                    <div style={{ background: theme.bgInput, padding: '10px', borderRadius: theme.innerRadius }}>
                      <span>Systolic BP</span>
                      <div style={{ fontSize: '18px', fontWeight: '800', color: theme.brandPrimary }}>132 mmHg</div>
                    </div>
                    <div style={{ background: theme.bgInput, padding: '10px', borderRadius: theme.innerRadius }}>
                      <span>Diastolic BP</span>
                      <div style={{ fontSize: '18px', fontWeight: '800', color: theme.brandPrimary }}>85 mmHg</div>
                    </div>
                    <div style={{ background: theme.bgInput, padding: '10px', borderRadius: theme.innerRadius }}>
                      <span>Blood Glucose</span>
                      <div style={{ fontSize: '18px', fontWeight: '800', color: theme.brandPrimary }}>90 mg/dL</div>
                    </div>
                    <div style={{ background: theme.bgInput, padding: '10px', borderRadius: theme.innerRadius }}>
                      <span>Weight</span>
                      <div style={{ fontSize: '18px', fontWeight: '800', color: theme.textPrimary }}>72 kg</div>
                    </div>
                  </div>
                </Card>
              </div>
            </div>
          </div>
        )}

        {/* APPOINTMENTS SCREEN */}
        {activeSubTab === 'appointments' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2 style={{ fontSize: '24px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>Doctors Appointment Portal</h2>
              <Btn onClick={() => setShowBookingDialog(true)}>Book New Appointment</Btn>
            </div>

            {/* AI Booking Assistant Panel */}
            <Card style={{ background: theme.mode === 'dark' ? 'rgba(239, 68, 68, 0.08)' : 'rgba(239, 68, 68, 0.03)', border: `1px solid rgba(239, 68, 68, 0.25)` }}>
              <h3 style={{ fontSize: '16px', fontWeight: '700', marginBottom: '14px', color: theme.brandPrimary }}>AI Smart Booking Assistant</h3>
              
              <div style={{ background: theme.bgCard, border: `1px solid ${theme.border}`, borderRadius: theme.innerRadius, padding: '14px', minHeight: '120px', maxHeight: '160px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '14px' }}>
                {chatMessages.map((msg, i) => (
                  <div key={i} style={{ alignSelf: msg.sender === 'ai' ? 'flex-start' : 'flex-end', background: msg.sender === 'ai' ? theme.bgInput : theme.brandPrimary, color: msg.sender === 'ai' ? theme.textPrimary : '#ffffff', padding: '8px 12px', borderRadius: '12px', fontSize: '13px', maxWidth: '80%' }}>
                    {msg.text}
                  </div>
                ))}
              </div>

              <div style={{ display: 'flex', gap: '10px' }}>
                <input
                  type="text"
                  style={{ flexGrow: 1, background: theme.bgInput, border: `1px solid ${theme.border}`, color: theme.textPrimary, padding: '10px 14px', borderRadius: theme.innerRadius, fontSize: '13px', outline: 'none' }}
                  placeholder="Type symptoms (e.g. severe chest pressure, constant headaches)..."
                  value={chatInput}
                  onChange={(e) => setChatInput(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && sendChatMessage()}
                />
                <Btn style={{ padding: '10px 18px' }} onClick={() => sendChatMessage()}>Send</Btn>
              </div>
            </Card>

            {/* Selector tabs & list */}
            <div style={{ display: 'flex', gap: '10px' }}>
              {['Upcoming', 'Past', 'Cancelled'].map(t => (
                <button
                  key={t}
                  style={{
                    background: appointmentTab === t ? theme.brandPrimary : theme.bgCard,
                    color: appointmentTab === t ? '#ffffff' : theme.textSecondary,
                    border: `1px solid ${theme.border}`,
                    padding: '8px 16px',
                    borderRadius: '20px',
                    cursor: 'pointer',
                    fontWeight: '700',
                    fontSize: '13px'
                  }}
                  onClick={() => setAppointmentTab(t)}
                >
                  {t} Appointments
                </button>
              ))}
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {appointmentTab === 'Upcoming' ? (
                <Card style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <h4 style={{ fontSize: '16px', fontWeight: '700' }}>Dr. Khadija Begum</h4>
                    <span style={{ fontSize: '13px', color: theme.brandPrimary, fontWeight: '700' }}>Psychiatry Consultation</span>
                    <div style={{ fontSize: '12px', color: theme.textSecondary, marginTop: '6px' }}>July 10, 2026 at 10:00 AM | Queue #04</div>
                  </div>
                  <Chip label="Upcoming" status="warning" />
                </Card>
              ) : (
                <div style={{ textAlign: 'center', padding: '40px', color: theme.textSecondary }}>No {appointmentTab.toLowerCase()} appointments registered.</div>
              )}
            </div>

            {/* Find a Medical Specialist (Manual List matching Flutter App) */}
            <h3 style={{ fontSize: '20px', fontWeight: '800', color: theme.textPrimary, marginTop: '20px', borderTop: `1px solid ${theme.border}`, paddingTop: '20px' }}>Find a Medical Specialist</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
              {[
                { id: '1001', name: 'Dr. Fatema Ahmed', specialty: 'Cardiology', hospital: 'Evercare Hospital', fee: '৳1000', experience: '12 Years' },
                { id: '1002', name: 'Dr. Khadija Begum', specialty: 'Psychiatry', hospital: 'Popular Diagnostic & Hospital', fee: '৳1200', experience: '15 Years' },
                { id: '1003', name: 'Dr. Mushfiqur Rahman', specialty: 'Neurology', hospital: 'United Hospital', fee: '৳1500', experience: '10 Years' }
              ].map(doc => (
                <Card key={doc.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <h4 style={{ fontWeight: '700', fontSize: '16px' }}>{doc.name}</h4>
                    <span style={{ fontSize: '13px', color: theme.brandPrimary, fontWeight: '700' }}>{doc.specialty} • {doc.experience} Exp</span>
                    <div style={{ fontSize: '12px', color: theme.textSecondary, marginTop: '4px' }}>Hospital: {doc.hospital}</div>
                    <div style={{ fontSize: '12px', color: theme.textSecondary }}>Fee: <strong>{doc.fee}</strong></div>
                  </div>
                  <Btn style={{ padding: '8px 14px', fontSize: '12px' }} onClick={() => { setSelectedDoctor(doc.id); setShowBookingDialog(true); }}>Book Slot</Btn>
                </Card>
              ))}
            </div>
          </div>
        )}

        {/* HEALTH TIMELINE SCREEN */}
        {activeSubTab === 'timeline' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            <h2 style={{ fontSize: '24px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>Clinical Health Timeline</h2>

            <div style={{ borderLeft: `2px solid ${theme.border}`, paddingLeft: '30px', marginLeft: '10px', display: 'flex', flexDirection: 'column', gap: '30px', position: 'relative' }}>
              {/* Event 1 */}
              <div style={{ position: 'relative' }}>
                <div style={{ position: 'absolute', left: '-40px', top: '4px', width: '18px', height: '18px', background: theme.brandPrimary, borderRadius: '50%', border: `3px solid ${theme.bgMain}` }}></div>
                <div style={{ fontSize: '12px', color: theme.brandPrimary, fontWeight: '800', marginBottom: '4px' }}>JULY 2026</div>
                <Card style={{ padding: '16px' }}>
                  <h4 style={{ fontWeight: '700' }}>Consultation Booking with Dr. Khadija Begum</h4>
                  <p style={{ fontSize: '13px', color: theme.textSecondary }}>Scheduled for Psychiatry counseling and therapy review.</p>
                </Card>
              </div>

              {/* Event 2 */}
              <div style={{ position: 'relative' }}>
                <div style={{ position: 'absolute', left: '-40px', top: '4px', width: '18px', height: '18px', background: theme.success, borderRadius: '50%', border: `3px solid ${theme.bgMain}` }}></div>
                <div style={{ fontSize: '12px', color: theme.success, fontWeight: '800', marginBottom: '4px' }}>JUNE 2026</div>
                <Card style={{ padding: '16px' }}>
                  <h4 style={{ fontWeight: '700' }}>Lab Report Registered: Fasting Blood Glucose (FBS)</h4>
                  <p style={{ fontSize: '13px', color: theme.textSecondary }}>Facility: Popular Diagnostic & Hospital. Result logged: 142 mg/dL.</p>
                </Card>
              </div>

              {/* Event 3 */}
              <div style={{ position: 'relative' }}>
                <div style={{ position: 'absolute', left: '-40px', top: '4px', width: '18px', height: '18px', background: theme.warning, borderRadius: '50%', border: `3px solid ${theme.bgMain}` }}></div>
                <div style={{ fontSize: '12px', color: theme.warning, fontWeight: '800', marginBottom: '4px' }}>MAY 2026</div>
                <Card style={{ padding: '16px' }}>
                  <h4 style={{ fontWeight: '700' }}>Whole Abdomen Ultrasonography Imaging</h4>
                  <p style={{ fontSize: '13px', color: theme.textSecondary }}>Findings of Grade-1 Fatty Liver and mild Hepatomegaly recorded in imaging reports.</p>
                </Card>
              </div>
            </div>
          </div>
        )}

        {/* MEDICAL VAULT SCREEN */}
        {activeSubTab === 'vault' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            <h2 style={{ fontSize: '24px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>Medical Vault Clinical Records</h2>

            <div style={{ display: 'flex', gap: '10px' }}>
              <button style={{ background: vaultTab === 'prescriptions' ? theme.brandPrimary : theme.bgCard, color: vaultTab === 'prescriptions' ? '#ffffff' : theme.textSecondary, border: `1px solid ${theme.border}`, padding: '8px 16px', borderRadius: '20px', cursor: 'pointer', fontWeight: '700', fontSize: '13px' }} onClick={() => setVaultTab('prescriptions')}>Prescriptions</button>
              <button style={{ background: vaultTab === 'lab' ? theme.brandPrimary : theme.bgCard, color: vaultTab === 'lab' ? '#ffffff' : theme.textSecondary, border: `1px solid ${theme.border}`, padding: '8px 16px', borderRadius: '20px', cursor: 'pointer', fontWeight: '700', fontSize: '13px' }} onClick={() => setVaultTab('lab')}>Lab Reports</button>
              <button style={{ background: vaultTab === 'imaging' ? theme.brandPrimary : theme.bgCard, color: vaultTab === 'imaging' ? '#ffffff' : theme.textSecondary, border: `1px solid ${theme.border}`, padding: '8px 16px', borderRadius: '20px', cursor: 'pointer', fontWeight: '700', fontSize: '13px' }} onClick={() => setVaultTab('imaging')}>Imaging Reports</button>
            </div>

            <Card>
              {vaultTab === 'prescriptions' && (
                <div>
                  <h3 style={{ fontSize: '18px', fontWeight: '700', marginBottom: '16px', color: theme.brandPrimary }}>Active Prescription Summary</h3>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    <div style={{ background: theme.bgInput, padding: '14px', borderRadius: theme.innerRadius }}>
                      <strong style={{ color: theme.textPrimary }}>Tab. Metformin HCl 500mg</strong>
                      <p style={{ fontSize: '12px', color: theme.textSecondary, marginTop: '4px' }}>Dosage: 1 + 0 + 1 | Timing: After meals | Duration: 3 Months</p>
                    </div>
                    <div style={{ background: theme.bgInput, padding: '14px', borderRadius: theme.innerRadius }}>
                      <strong style={{ color: theme.textPrimary }}>Tab. Amlodipine 5mg</strong>
                      <p style={{ fontSize: '12px', color: theme.textSecondary, marginTop: '4px' }}>Dosage: 0 + 0 + 1 | Timing: At night before sleep | Duration: 1 Month</p>
                    </div>
                  </div>
                </div>
              )}

              {vaultTab === 'lab' && (
                <div>
                  <h3 style={{ fontSize: '18px', fontWeight: '700', marginBottom: '16px', color: theme.brandPrimary }}>Seeded Laboratory Panel</h3>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                    <thead>
                      <tr style={{ borderBottom: `2px solid ${theme.border}`, textAlign: 'left', color: theme.textSecondary }}>
                        <th style={{ padding: '10px 8px' }}>Test Parameter</th>
                        <th style={{ padding: '10px 8px' }}>Measured Value</th>
                        <th style={{ padding: '10px 8px' }}>Ref Range</th>
                        <th style={{ padding: '10px 8px' }}>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr style={{ borderBottom: `1px solid ${theme.border}` }}>
                        <td style={{ padding: '10px 8px', fontWeight: '600' }}>Fasting Blood Sugar (FBS)</td>
                        <td style={{ padding: '10px 8px', color: theme.danger, fontWeight: '700' }}>142 mg/dL</td>
                        <td style={{ padding: '10px 8px' }}>70 - 100 mg/dL</td>
                        <td style={{ padding: '10px 8px' }}><Chip label="High Risk" status="danger" /></td>
                      </tr>
                      <tr style={{ borderBottom: `1px solid ${theme.border}` }}>
                        <td style={{ padding: '10px 8px', fontWeight: '600' }}>Serum Creatinine</td>
                        <td style={{ padding: '10px 8px' }}>0.9 mg/dL</td>
                        <td style={{ padding: '10px 8px' }}>0.6 - 1.2 mg/dL</td>
                        <td style={{ padding: '10px 8px' }}><Chip label="Normal" status="success" /></td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              )}

              {vaultTab === 'imaging' && (
                <div>
                  <h3 style={{ fontSize: '18px', fontWeight: '700', marginBottom: '16px', color: theme.brandPrimary }}>Ultrasound & Imaging impressions</h3>
                  <p style={{ fontSize: '14px', lineHeight: '1.6', color: theme.textSecondary }}>
                    Grade-1 Fatty Liver diagnosed with mild Hepatomegaly. Gallbladder wall thickness is normal. Spleen is healthy.
                  </p>
                </div>
              )}
            </Card>
          </div>
        )}

        {/* HEALTHCARE AI SCREEN */}
        {activeSubTab === 'ai' && (
          <VitalsChecker embedded={true} />
        )}

        {/* BLOOD DONATION SIMULATOR SCREEN */}
        {activeSubTab === 'bloodDonation' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h2 style={{ fontSize: '24px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>Blood Donor Registration & Status</h2>
                <span style={{ fontSize: '13px', color: theme.textSecondary }}>Register as an active blood donor and track match requests</span>
              </div>

              {/* Toggle Menu */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <span style={{ fontSize: '14px', fontWeight: '700', color: isDonorActive ? theme.success : theme.danger }}>
                  Donor Status: {isDonorActive ? 'ACTIVE' : 'INACTIVE'}
                </span>
                <button
                  style={{
                    width: '56px',
                    height: '28px',
                    borderRadius: '14px',
                    background: isDonorActive ? theme.success : theme.border,
                    border: 'none',
                    cursor: 'pointer',
                    position: 'relative',
                    transition: 'background 0.2s ease',
                    outline: 'none'
                  }}
                  onClick={() => setIsDonorActive(prev => !prev)}
                >
                  <div style={{
                    width: '22px',
                    height: '22px',
                    borderRadius: '50%',
                    background: '#ffffff',
                    position: 'absolute',
                    top: '3px',
                    left: isDonorActive ? '31px' : '3px',
                    transition: 'left 0.2s ease',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
                  }} />
                </button>
              </div>
            </div>

            {/* AI Eligibility Alert & Simulation Panel */}
            <Card>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <h3 style={{ fontSize: '16px', fontWeight: '700', color: theme.textPrimary }}>AI Donation Eligibility Analyzer</h3>
                
                {/* Simulator drop-down */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span style={{ fontSize: '11px', color: theme.textSecondary }}>Simulate Vitals State:</span>
                  <select
                    style={{ background: theme.bgInput, color: theme.textPrimary, border: `1px solid ${theme.border}`, padding: '4px 8px', borderRadius: theme.innerRadius, fontSize: '12px', outline: 'none' }}
                    value={bloodEligibilitySim}
                    onChange={(e) => setBloodEligibilitySim(e.target.value)}
                  >
                    <option value="safe">Healthy Baseline (Safe)</option>
                    <option value="asthma">Active Asthma Flare-up (Unsafe)</option>
                    <option value="recent">Donated 3 Weeks Ago (Unsafe)</option>
                  </select>
                </div>
              </div>

              {bloodEligibilitySim === 'safe' && (
                <div style={{ background: theme.mode === 'dark' ? 'rgba(16, 185, 129, 0.08)' : 'rgba(16, 185, 129, 0.03)', border: '1px solid rgba(16, 185, 129, 0.25)', borderRadius: theme.innerRadius, padding: '16px' }}>
                  <div style={{ fontWeight: '700', color: theme.success, display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                    AI Insights: Blood Donation is Safe
                  </div>
                  <p style={{ fontSize: '13px', color: theme.textSecondary, margin: 0, lineHeight: '1.6' }}>
                    Your blood group is <strong>O Positive (O+)</strong>. As a universal red blood donor, you can safely donate to: 
                    <strong style={{ color: theme.textPrimary }}> O+, A+, B+, AB+</strong> recipients. Your logged blood pressure (132/85 mmHg) and pulse parameters are stable.
                  </p>
                </div>
              )}

              {bloodEligibilitySim === 'asthma' && (
                <div style={{ background: theme.mode === 'dark' ? 'rgba(239, 68, 68, 0.08)' : 'rgba(239, 68, 68, 0.03)', border: '1px solid rgba(239, 68, 68, 0.25)', borderRadius: theme.innerRadius, padding: '16px' }}>
                  <div style={{ fontWeight: '700', color: theme.danger, display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                    AI Warning: Blood Donation Deferred
                  </div>
                  <p style={{ fontSize: '13px', color: theme.textSecondary, margin: 0, lineHeight: '1.6' }}>
                    You have active chronic <strong>Bronchial Asthma</strong> and were recently prescribed Albuterol bronchodilators in your workspace profile. For donor safety, donation is deferred.
                  </p>
                </div>
              )}

              {bloodEligibilitySim === 'recent' && (
                <div style={{ background: theme.mode === 'dark' ? 'rgba(239, 68, 68, 0.08)' : 'rgba(239, 68, 68, 0.03)', border: '1px solid rgba(239, 68, 68, 0.25)', borderRadius: theme.innerRadius, padding: '16px' }}>
                  <div style={{ fontWeight: '700', color: theme.danger, display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                    AI Warning: Donation Frequency Deferral
                  </div>
                  <p style={{ fontSize: '13px', color: theme.textSecondary, margin: 0, lineHeight: '1.6' }}>
                    You last donated blood on <strong>June 10, 2026</strong> (3 weeks ago). The minimum required safety interval between full blood donations is 3 months. Eligible again on: <strong style={{ color: theme.textPrimary }}>September 10, 2026</strong>.
                  </p>
                </div>
              )}
            </Card>

            {/* Blood Requests Panel */}
            <div>
              <h3 style={{ fontSize: '18px', fontWeight: '800', color: theme.textPrimary, marginBottom: '16px' }}>Active Match Blood Requests</h3>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {bloodRequests.map(req => (
                  <Card key={req.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                        <h4 style={{ fontWeight: '700', fontSize: '16px' }}>{req.patient} (Required: {req.group})</h4>
                        <Chip label={req.urgency + ' Urgency'} status={req.urgency === 'High' ? 'danger' : 'warning'} />
                      </div>
                      <div style={{ fontSize: '13px', color: theme.brandPrimary, fontWeight: '700', marginTop: '4px' }}>Hospital: {req.hospital}</div>
                      <div style={{ fontSize: '12px', color: theme.textSecondary }}>Location: {req.location} | Timeline: <strong>{req.time}</strong></div>
                    </div>

                    <div style={{ display: 'flex', gap: '10px' }}>
                      {req.status === 'Pending' ? (
                        <>
                          <Btn style={{ padding: '8px 16px', fontSize: '12px' }} onClick={() => handleAcceptRequest(req.id)}>Accept</Btn>
                          <Btn variant="outline" style={{ padding: '8px 16px', fontSize: '12px' }} onClick={() => handleDeclineRequest(req.id)}>Decline</Btn>
                        </>
                      ) : (
                        <Chip label={req.status} status={req.status === 'Accepted' ? 'success' : 'danger'} />
                      )}
                    </div>
                  </Card>
                ))}
              </div>
            </div>

            {/* Donation History */}
            <div>
              <h3 style={{ fontSize: '18px', fontWeight: '800', color: theme.textPrimary, marginBottom: '16px' }}>Donation History Logs</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {donationHistory.map(hist => (
                  <Card key={hist.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: theme.bgInput }}>
                    <div>
                      <h4 style={{ fontWeight: '700', fontSize: '15px' }}>Recipient: {hist.recipient} ({hist.group})</h4>
                      <span style={{ fontSize: '12px', color: theme.textSecondary }}>Hospital: {hist.hospital} | Date: {hist.date}</span>
                    </div>
                    <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                      <span style={{ fontSize: '12px', fontWeight: '700' }}>Units: {hist.units}</span>
                      <Chip label={hist.status} status="success" />
                    </div>
                  </Card>
                ))}
              </div>
            </div>

          </div>
        )}
      </div>

      {/* WIZARD MODAL DIALOG */}
      {showWizard && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 2000 }}>
          <Card style={{ width: '500px', padding: '30px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ fontSize: '18px', fontWeight: '800' }}>Update Profile Wizard</h3>
              <button style={{ background: 'transparent', border: 'none', color: theme.textPrimary, cursor: 'pointer', fontSize: '18px' }} onClick={() => setShowWizard(false)}>✕</button>
            </div>

            {/* Steps Indicator */}
            <div style={{ display: 'flex', gap: '8px', marginBottom: '20px' }}>
              {['Personal', 'Addresses', 'Emergency', 'Vitals'].map((step, i) => (
                <div key={i} style={{ flexGrow: 1, height: '4px', background: wizardStep >= i ? theme.brandPrimary : theme.border, borderRadius: '2px' }}></div>
              ))}
            </div>

            {/* Steps Content */}
            <div style={{ minHeight: '220px' }}>
              {wizardStep === 0 && (
                <div>
                  <Input label="Full Name" placeholder="Nehal Ahmmed" />
                  <Input label="Occupation" placeholder="Software Engineer" />
                </div>
              )}
              {wizardStep === 1 && (
                <div>
                  <Input label="Present Address" placeholder="House 42, Dhanmondi, Dhaka" />
                  <Input label="Permanent Address" placeholder="Green Valley, Sylhet" />
                </div>
              )}
              {wizardStep === 2 && (
                <div>
                  <Input label="Emergency ICE Name" placeholder="Rafiq Ahmmed" />
                  <Input label="ICE Phone" placeholder="01911-987654" />
                </div>
              )}
              {wizardStep === 3 && (
                <div>
                  <Input label="Weight (kg)" placeholder="72" />
                  <Input label="Height (cm)" placeholder="175" />
                </div>
              )}
            </div>

            {/* Dialog Footer Actions */}
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '20px' }}>
              <Btn variant="secondary" disabled={wizardStep === 0} onClick={() => setWizardStep(prev => prev - 1)}>Previous</Btn>
              {wizardStep < 3 ? (
                <Btn onClick={() => setWizardStep(prev => prev + 1)}>Next</Btn>
              ) : (
                <Btn onClick={() => setShowWizard(false)}>Save & Finish</Btn>
              )}
            </div>
          </Card>
        </div>
      )}

      {/* BOOK APPOINTMENT DIALOG */}
      {showBookingDialog && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 2000 }}>
          <Card style={{ width: '500px', padding: '30px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ fontSize: '18px', fontWeight: '800' }}>Book Specialist Appointment</h3>
              <button style={{ background: 'transparent', border: 'none', color: theme.textPrimary, cursor: 'pointer', fontSize: '18px' }} onClick={() => setShowBookingDialog(false)}>✕</button>
            </div>

            <Select
              label="Select Specialist"
              value={selectedDoctor}
              onChange={(e) => setSelectedDoctor(e.target.value)}
              options={[
                { value: '1001', label: 'Dr. Fatema Ahmed (Cardiology)' },
                { value: '1002', label: 'Dr. Khadija Begum (Psychiatry)' },
                { value: '1003', label: 'Dr. Mushfiqur Rahman (Neurology)' }
              ]}
            />

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <Input label="Appointment Date" type="date" value={appointmentDate} onChange={(e) => setAppointmentDate(e.target.value)} />
              <Input label="Preferred Slot" placeholder="e.g. 10:00 AM" value={appointmentTime} onChange={(e) => setAppointmentTime(e.target.value)} />
            </div>

            <textarea
              style={{ width: '100%', height: '80px', background: theme.bgInput, border: `1px solid ${theme.border}`, color: theme.textPrimary, padding: '12px', borderRadius: theme.innerRadius, fontSize: '13px', outline: 'none', resize: 'none', marginTop: '10px' }}
              placeholder="Describe symptoms or reasons for visit..."
              value={symptomText}
              onChange={(e) => setSymptomText(e.target.value)}
            />

            <Btn style={{ width: '100%', marginTop: '20px' }} onClick={() => setShowBookingDialog(false)}>Confirm Booking</Btn>
          </Card>
        </div>
      )}
    </div>
  );
}
window.PatientPortal = PatientPortal;

// 5. Redesigned Complete Doctor Portal Workspace Component
function DoctorPortal({ onLogout }) {
  const { theme } = useTheme();
  
  // Navigation State
  const [activeSubTab, setActiveSubTab] = useState('dashboard');
  
  // Active Workspace Patient
  const [selectedPatientId, setSelectedPatientId] = useState('1'); // 1 = Nehal Ahmmed, 2 = Rahim Islam, 3 = Fatema Zohra
  const [aiSummaryGenerated, setAiSummaryGenerated] = useState(false);

  // Workspace form states
  const [diagnosisName, setDiagnosisName] = useState('');
  const [diagnosisStatus, setDiagnosisStatus] = useState('Provisional');
  const [medicationName, setMedicationName] = useState('');
  const [medicationDosage, setMedicationDosage] = useState('1+0+1');
  const [medicationDuration, setMedicationDuration] = useState('2 Weeks');
  const [addedMedicines, setAddedMedicines] = useState([]);
  const [followUpDate, setFollowUpDate] = useState('In 2 weeks');

  // Report Review State
  const [activeReportTab, setActiveReportTab] = useState('Pending');
  const [reports, setReports] = useState([
    { id: '101', patientName: 'Rahim Islam', healthId: 'NHCS-900142', testName: 'Lipid Profile Panel', orderedDate: '01/07/2026', status: 'Pending', trendStatus: 'Worsening', trendSummary: 'Elevated low-density lipoprotein (LDL) suggests hypercholesterolemia. Elevated cardiovascular risk.', values: [{ name: 'Total Cholesterol', val: '240', ref: '< 200' }, { name: 'LDL Cholesterol', val: '160', ref: '< 100' }] },
    { id: '102', patientName: 'Nehal Ahmmed', healthId: 'NHCS-800452', testName: 'Fasting Blood Glucose', orderedDate: '30/06/2026', status: 'Pending', trendStatus: 'Stable', trendSummary: 'Glucose parameters are stable but pre-diabetic baseline remains active.', values: [{ name: 'FBS Glucose', val: '110', ref: '70 - 100' }] }
  ]);

  const markReportReviewed = (id) => {
    setReports(prev => prev.map(rep => {
      if (rep.id === id) {
        return { ...rep, status: 'Reviewed' };
      }
      return rep;
    }));
  };

  const handleAddMedicine = () => {
    if (!medicationName.trim()) return;
    setAddedMedicines(prev => [...prev, { name: medicationName, dosage: medicationDosage, duration: medicationDuration }]);
    setMedicationName('');
  };

  // Mock patient queue
  const queuePatients = [
    { id: '1', name: 'Nehal Ahmmed', healthId: 'NHCS-800452', age: '24', bloodGroup: 'O+', allergies: 'Pollen, Dust Mites', chronic: 'Bronchial Asthma', risk: 'Moderate', time: '10:00 AM', status: 'WAITING', vitals: { bp: '132/85 mmHg', glucose: '90 mg/dL', heartRate: '78 bpm', weight: '72 kg' } },
    { id: '2', name: 'Rahim Islam', healthId: 'NHCS-900142', age: '45', bloodGroup: 'A-', allergies: 'Penicillin', chronic: 'Hypertension', risk: 'Emergency', time: '10:15 AM', status: 'IN CONSULTATION', vitals: { bp: '155/98 mmHg', glucose: '120 mg/dL', heartRate: '92 bpm', weight: '80 kg' } },
    { id: '3', name: 'Fatema Zohra', healthId: 'NHCS-950482', age: '32', bloodGroup: 'B+', allergies: 'None', chronic: 'None', risk: 'Normal', time: '10:30 AM', status: 'WAITING', vitals: { bp: '118/75 mmHg', glucose: '85 mg/dL', heartRate: '72 bpm', weight: '60 kg' } }
  ];

  const activePatient = queuePatients.find(p => p.id === selectedPatientId) || queuePatients[0];

  const getRiskColor = (risk) => {
    if (risk === 'Emergency') return 'danger';
    if (risk === 'Moderate') return 'warning';
    return 'success';
  };

  const handleOpenWorkspace = (patientId) => {
    setSelectedPatientId(patientId);
    setAiSummaryGenerated(false);
    setAddedMedicines([]);
    setDiagnosisName('');
    setActiveSubTab('workspace');
  };

  const handleFormSubmit = () => {
    alert(`Success: Clinical treatment plan submitted successfully for patient ${activePatient.name}!`);
    // Reset
    setAddedMedicines([]);
    setDiagnosisName('');
    setAiSummaryGenerated(false);
    setActiveSubTab('dashboard');
  };

  // Side navigation options
  const doctorNavItems = [
    { id: 'dashboard', label: 'Doctor Dashboard', icon: <Icon.Activity size={18} /> },
    { id: 'workspace', label: 'Clinical Workspace', icon: <Icon.Stethoscope size={18} /> },
    { id: 'reports', label: 'Report Review', icon: <Icon.FileText size={18} /> },
    { id: 'profile', label: 'Professional Profile', icon: <Icon.User size={18} /> }
  ];

  return (
    <div style={{ display: 'flex', minHeight: '680px', width: '100%', background: theme.bgMain }}>
      {/* Sidebar Nav */}
      <div style={{ width: '260px', borderRight: `1px solid ${theme.border}`, background: theme.bgCard, padding: '24px 16px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
        <div>
          {/* Brand Logo */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontWeight: '800', fontSize: '20px', color: theme.brandPrimary, marginBottom: '32px', paddingLeft: '8px' }}>
            <Icon.Heart size={24} color={theme.brandPrimary} fill={theme.brandPrimary} />
            <span>NHCS Clinical</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {doctorNavItems.map(item => {
              const active = activeSubTab === item.id;
              return (
                <button
                  key={item.id}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                    width: '100%',
                    padding: '12px 16px',
                    borderRadius: theme.innerRadius,
                    border: 'none',
                    background: active ? 'rgba(239, 68, 68, 0.12)' : 'transparent',
                    color: active ? theme.brandPrimary : theme.textSecondary,
                    fontWeight: active ? '700' : '600',
                    fontSize: '14px',
                    textAlign: 'left',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease'
                  }}
                  onClick={() => {
                    setActiveSubTab(item.id);
                    if (item.id === 'workspace') setAiSummaryGenerated(false);
                  }}
                >
                  <span style={{ display: 'flex', alignItems: 'center', color: active ? theme.brandPrimary : theme.textSecondary }}>{item.icon}</span> {item.label}
                </button>
              );
            })}
          </div>
        </div>

        <button
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            padding: '12px 16px',
            borderRadius: theme.innerRadius,
            border: `1px solid ${theme.border}`,
            background: 'transparent',
            color: theme.brandPrimary,
            fontWeight: '700',
            fontSize: '14px',
            cursor: 'pointer',
            textAlign: 'left'
          }}
          onClick={onLogout}
        >
          Sign Out
        </button>
      </div>

      {/* Main workspace area */}
      <div style={{ flexGrow: 1, padding: '40px', overflowY: 'auto' }}>
        
        {/* DOCTOR DASHBOARD VIEW */}
        {activeSubTab === 'dashboard' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            {/* Header info */}
            <div>
              <h2 style={{ fontSize: '24px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>Good Morning, Dr. Ahmed Rahman</h2>
              <span style={{ fontSize: '13px', color: theme.textSecondary }}>Cardiology Department • Dhaka Central Hospital</span>
            </div>

            {/* Statistics row */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px' }}>
              <Card style={{ padding: '20px', position: 'relative' }}>
                <span style={{ fontSize: '20px', position: 'absolute', top: '16px', right: '16px' }}>👥</span>
                <span style={{ fontSize: '12px', color: theme.textSecondary }}>Today's Patients</span>
                <div style={{ fontSize: '28px', fontWeight: '800', color: theme.brandPrimary, margin: '8px 0 4px 0' }}>12</div>
                <span style={{ fontSize: '11px', color: theme.success }}>+2 vs yesterday</span>
              </Card>
              <Card style={{ padding: '20px', position: 'relative' }}>
                <span style={{ fontSize: '20px', position: 'absolute', top: '16px', right: '16px' }}>🔄</span>
                <span style={{ fontSize: '12px', color: theme.textSecondary }}>Follow-ups</span>
                <div style={{ fontSize: '28px', fontWeight: '800', color: theme.brandPrimary, margin: '8px 0 4px 0' }}>4</div>
                <span style={{ fontSize: '11px', color: theme.textSecondary }}>2 scheduled today</span>
              </Card>
              <Card style={{ padding: '20px', position: 'relative' }}>
                <span style={{ fontSize: '12px', color: theme.textSecondary }}>Emergency Cases</span>
                <div style={{ fontSize: '28px', fontWeight: '800', color: theme.danger, margin: '8px 0 4px 0' }}>1</div>
                <span style={{ fontSize: '11px', color: theme.danger }}>Action required</span>
              </Card>
              <Card style={{ padding: '20px', position: 'relative' }}>
                <span style={{ fontSize: '12px', color: theme.textSecondary }}>Pending Reports</span>
                <div style={{ fontSize: '28px', fontWeight: '800', color: theme.warning, margin: '8px 0 4px 0' }}>3</div>
                <span style={{ fontSize: '11px', color: theme.warning }}>3 unread in inbox</span>
              </Card>
            </div>

            {/* AI Briefing Panel */}
            <Card style={{ background: theme.mode === 'dark' ? 'rgba(16, 185, 129, 0.08)' : 'rgba(16, 185, 129, 0.03)', border: '1px solid rgba(16, 185, 129, 0.2)' }}>
              <h3 style={{ fontSize: '16px', fontWeight: '700', color: theme.success, marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                AI Daily Briefing & Patient Summary
              </h3>
              <p style={{ fontSize: '14px', color: theme.textPrimary, lineHeight: '1.6', marginBottom: '12px' }}>
                You have one high-risk cardiovascular alert for Rahim Islam (A-). Key diagnostic evaluations indicate elevated LDL and high BP (155/98 mmHg). Ensure follow-up guidelines are logged.
              </p>
              <ul style={{ paddingLeft: '20px', fontSize: '13px', color: theme.textSecondary, lineHeight: '1.8' }}>
                <li>Check-in on Emergency consult Patient: Rahim Islam immediately.</li>
                <li>Verify pending Lipid Profile reports for Rahim Islam.</li>
                <li>Review Fasting Blood Glucose measurements for Nehal Ahmmed.</li>
              </ul>
            </Card>

            {/* Patient Queue */}
            <div>
              <h3 style={{ fontSize: '18px', fontWeight: '800', color: theme.textPrimary, marginBottom: '16px' }}>Patient Queue — Today</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {queuePatients.map(pat => (
                  <Card key={pat.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
                      <div style={{ width: '44px', height: '44px', background: 'rgba(239, 68, 68, 0.1)', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '800', color: theme.brandPrimary }}>
                        {pat.name[0]}
                      </div>
                      <div>
                        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                          <h4 style={{ fontWeight: '700', fontSize: '15px' }}>{pat.name}</h4>
                          <Chip label={pat.risk + ' Risk'} status={getRiskColor(pat.risk)} />
                        </div>
                        <span style={{ fontSize: '12px', color: theme.textSecondary }}>Health ID: {pat.healthId} • Slot: <strong>{pat.time}</strong></span>
                      </div>
                    </div>

                    <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
                      <span style={{ fontSize: '12px', color: theme.textSecondary, background: theme.bgInput, padding: '4px 10px', borderRadius: '20px', fontWeight: '700' }}>
                        {pat.status}
                      </span>
                      <Btn style={{ padding: '8px 16px', fontSize: '12px' }} onClick={() => handleOpenWorkspace(pat.id)}>Open Workspace</Btn>
                    </div>
                  </Card>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* CLINICAL WORKSPACE VIEW */}
        {activeSubTab === 'workspace' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {/* Header / Active Patient context bar */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: `1px solid ${theme.border}`, paddingBottom: '16px' }}>
              <div>
                <h2 style={{ fontSize: '22px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>Patient Clinical Workspace</h2>
                <span style={{ fontSize: '13px', color: theme.textSecondary }}>Select a patient from the queue to start diagnostics</span>
              </div>

              <div style={{ width: '220px' }}>
                <select
                  style={{ width: '100%', background: theme.bgCard, color: theme.textPrimary, border: `1px solid ${theme.border}`, padding: '10px', borderRadius: theme.innerRadius, outline: 'none' }}
                  value={selectedPatientId}
                  onChange={(e) => { setSelectedPatientId(e.target.value); setAiSummaryGenerated(false); }}
                >
                  {queuePatients.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
              </div>
            </div>

            {/* Split layout */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px' }}>
              
              {/* Left Panel: Patient Medical History */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                <Card>
                  <h3 style={{ fontSize: '16px', fontWeight: '700', marginBottom: '14px', borderBottom: `1px solid ${theme.border}`, paddingBottom: '8px' }}>Patient Card & Vitals</h3>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', fontSize: '13px', marginBottom: '14px' }}>
                    <div>Name: <strong>{activePatient.name}</strong></div>
                    <div>Age / Group: <strong>{activePatient.age} Yrs / {activePatient.bloodGroup}</strong></div>
                    <div>Allergies: <strong style={{ color: theme.danger }}>{activePatient.allergies}</strong></div>
                    <div>Chronic: <strong style={{ color: theme.brandPrimary }}>{activePatient.chronic}</strong></div>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '10px', fontSize: '12px' }}>
                    <div style={{ background: theme.bgInput, padding: '8px', borderRadius: theme.innerRadius, textAlign: 'center' }}>
                      <span>BP</span>
                      <div style={{ fontWeight: '700', color: theme.brandPrimary }}>{activePatient.vitals.bp}</div>
                    </div>
                    <div style={{ background: theme.bgInput, padding: '8px', borderRadius: theme.innerRadius, textAlign: 'center' }}>
                      <span>Glucose</span>
                      <div style={{ fontWeight: '700', color: theme.brandPrimary }}>{activePatient.vitals.glucose}</div>
                    </div>
                    <div style={{ background: theme.bgInput, padding: '8px', borderRadius: theme.innerRadius, textAlign: 'center' }}>
                      <span>Pulse</span>
                      <div style={{ fontWeight: '700', color: theme.brandPrimary }}>{activePatient.vitals.heartRate}</div>
                    </div>
                    <div style={{ background: theme.bgInput, padding: '8px', borderRadius: theme.innerRadius, textAlign: 'center' }}>
                      <span>Weight</span>
                      <div style={{ fontWeight: '700' }}>{activePatient.vitals.weight}</div>
                    </div>
                  </div>
                </Card>

                {/* AI Patient Summary Generator (Requested Feature!) */}
                <Card style={{ border: `1px dashed ${theme.brandPrimary}`, background: theme.mode === 'dark' ? 'rgba(239, 68, 68, 0.05)' : 'rgba(239, 68, 68, 0.02)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <h4 style={{ fontWeight: '800', color: theme.brandPrimary, fontSize: '14px' }}>AI Medical History Briefing</h4>
                    <Btn style={{ padding: '6px 12px', fontSize: '11px' }} onClick={() => setAiSummaryGenerated(true)}>
                      Generate Briefing
                    </Btn>
                  </div>

                  {aiSummaryGenerated ? (
                    <p style={{ fontSize: '13px', color: theme.textPrimary, lineHeight: '1.6', background: theme.bgInput, padding: '12px', borderRadius: theme.innerRadius, borderLeft: `4px solid ${theme.brandPrimary}` }}>
                      <strong>Clinical Briefing:</strong> Patient <strong>{activePatient.name} ({activePatient.age}M)</strong> has documented <strong>{activePatient.chronic}</strong> with history of {activePatient.allergies !== 'None' ? `allergy to ${activePatient.allergies}` : 'no acute allergies'}. 
                      The BP baseline is currently <strong>{activePatient.vitals.bp}</strong>. 
                      {activePatient.chronic.includes('Asthma') && ' WARNING: Avoid Beta-Blocker class drugs for hypertension management due to hyper-reactive bronchial airways.'}
                      {activePatient.chronic.includes('Hypertension') && ' WARNING: Patient has clinical chronic hypertension. Monitor diastolic ranges.'}
                    </p>
                  ) : (
                    <div style={{ textAlign: 'center', padding: '20px 0', fontSize: '13px', color: theme.textSecondary }}>
                      Click "Generate Briefing" to parse and summarize patient medical history instantly.
                    </div>
                  )}
                </Card>

                <Card>
                  <h4 style={{ fontWeight: '700', fontSize: '14px', marginBottom: '10px' }}>Historical Diagnoses logs</h4>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '12px', color: theme.textSecondary }}>
                    <div style={{ background: theme.bgInput, padding: '10px', borderRadius: theme.innerRadius }}>
                      <strong>May 2026</strong>: provisional assessment of bronchial congestion.
                    </div>
                  </div>
                </Card>
              </div>

              {/* Right Panel: Treatment Creator Form */}
              <Card style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <h3 style={{ fontSize: '16px', fontWeight: '700', borderBottom: `1px solid ${theme.border}`, paddingBottom: '8px' }}>Register Treatment Plan</h3>
                
                <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '12px' }}>
                  <Input label="Diagnosis Disease" placeholder="e.g. Bronchial Asthma Exacerbation" value={diagnosisName} onChange={(e) => setDiagnosisName(e.target.value)} />
                  <Select
                    label="Status"
                    value={diagnosisStatus}
                    onChange={(e) => setDiagnosisStatus(e.target.value)}
                    options={[{ value: 'Provisional', label: 'Provisional' }, { value: 'Confirmed', label: 'Confirmed' }]}
                  />
                </div>

                <div style={{ background: theme.bgInput, padding: '14px', borderRadius: theme.innerRadius, border: `1px solid ${theme.border}` }}>
                  <h4 style={{ fontWeight: '700', fontSize: '13px', marginBottom: '10px', color: theme.brandPrimary }}>Add Medications</h4>
                  <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr 1fr', gap: '10px', marginBottom: '10px' }}>
                    <input type="text" placeholder="Drug (e.g. Albuterol)" style={{ background: theme.bgCard, color: theme.textPrimary, border: `1px solid ${theme.border}`, padding: '8px', borderRadius: '8px', fontSize: '12px', outline: 'none' }} value={medicationName} onChange={(e) => setMedicationName(e.target.value)} />
                    <input type="text" placeholder="Dosage (1+0+1)" style={{ background: theme.bgCard, color: theme.textPrimary, border: `1px solid ${theme.border}`, padding: '8px', borderRadius: '8px', fontSize: '12px', outline: 'none' }} value={medicationDosage} onChange={(e) => setMedicationDosage(e.target.value)} />
                    <input type="text" placeholder="Duration" style={{ background: theme.bgCard, color: theme.textPrimary, border: `1px solid ${theme.border}`, padding: '8px', borderRadius: '8px', fontSize: '12px', outline: 'none' }} value={medicationDuration} onChange={(e) => setMedicationDuration(e.target.value)} />
                  </div>
                  <Btn variant="outline" style={{ width: '100%', padding: '6px' }} onClick={handleAddMedicine}>+ Add Drug</Btn>

                  {addedMedicines.length > 0 && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', marginTop: '12px' }}>
                      {addedMedicines.map((med, idx) => (
                        <div key={idx} style={{ fontSize: '12px', color: theme.textPrimary, background: theme.bgCard, padding: '6px 10px', borderRadius: '6px', display: 'flex', justifyContent: 'space-between' }}>
                          <span>💊 {med.name} ({med.dosage})</span>
                          <strong>{med.duration}</strong>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                <Input label="Next Follow-Up" value={followUpDate} onChange={(e) => setFollowUpDate(e.target.value)} />

                <Btn style={{ width: '100%', marginTop: '10px' }} onClick={handleFormSubmit}>
                  Submit Treatment Plan
                </Btn>
              </Card>

            </div>
          </div>
        )}

        {/* REPORT REVIEW PORTAL VIEW */}
        {activeSubTab === 'reports' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2 style={{ fontSize: '24px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>Pending Reports Review</h2>
              
              <div style={{ display: 'flex', gap: '10px' }}>
                {['Pending', 'Reviewed'].map(tab => (
                  <button
                    key={tab}
                    style={{
                      background: activeReportTab === tab ? theme.brandPrimary : theme.bgCard,
                      color: activeReportTab === tab ? '#ffffff' : theme.textSecondary,
                      border: `1px solid ${theme.border}`,
                      padding: '8px 16px',
                      borderRadius: '20px',
                      cursor: 'pointer',
                      fontWeight: '700',
                      fontSize: '13px'
                    }}
                    onClick={() => setActiveReportTab(tab)}
                  >
                    {tab} Reports
                  </button>
                ))}
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              {reports.filter(r => r.status === activeReportTab).length > 0 ? (
                reports.filter(r => r.status === activeReportTab).map(rep => (
                  <Card key={rep.id} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div>
                        <h4 style={{ fontSize: '16px', fontWeight: '700' }}>{rep.patientName} (ID: {rep.healthId})</h4>
                        <span style={{ fontSize: '13px', color: theme.brandPrimary, fontWeight: '700' }}>{rep.testName} • Ordered: {rep.orderedDate}</span>
                      </div>
                      
                      <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                        <Chip label={'Trend: ' + rep.trendStatus} status={rep.trendStatus === 'Worsening' ? 'danger' : 'success'} />
                        {activeReportTab === 'Pending' && (
                          <Btn style={{ padding: '6px 12px', fontSize: '12px' }} onClick={() => markReportReviewed(rep.id)}>Mark Reviewed</Btn>
                        )}
                      </div>
                    </div>

                    <div style={{ background: theme.bgInput, padding: '14px', borderRadius: theme.innerRadius, fontSize: '13px' }}>
                      <strong>AI Trend Assessment:</strong> {rep.trendSummary}
                    </div>

                    <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                      <thead>
                        <tr style={{ borderBottom: `1px solid ${theme.border}`, textAlign: 'left', color: theme.textSecondary }}>
                          <th style={{ padding: '8px 4px' }}>Parameter</th>
                          <th style={{ padding: '8px 4px' }}>Measured</th>
                          <th style={{ padding: '8px 4px' }}>Ref Range</th>
                        </tr>
                      </thead>
                      <tbody>
                        {rep.values.map((val, idx) => (
                          <tr key={idx} style={{ borderBottom: `1px solid ${theme.border}` }}>
                            <td style={{ padding: '8px 4px', fontWeight: '600' }}>{val.name}</td>
                            <td style={{ padding: '8px 4px', color: theme.brandPrimary, fontWeight: '700' }}>{val.val}</td>
                            <td style={{ padding: '8px 4px' }}>{val.ref}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </Card>
                ))
              ) : (
                <div style={{ textAlign: 'center', padding: '60px', color: theme.textSecondary }}>
                  No reports in this category. All reports caught up!
                </div>
              )}
            </div>
          </div>
        )}

        {/* PROFESSIONAL PROFILE VIEW */}
        {activeSubTab === 'profile' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            <h2 style={{ fontSize: '24px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>Professional Specialist Profile</h2>

            <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '30px' }}>
              <Card style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <h3 style={{ fontSize: '18px', fontWeight: '700', borderBottom: `1px solid ${theme.border}`, paddingBottom: '8px' }}>Dr. Ahmed Rahman</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '14px' }}>
                  <div>Degrees: <strong>MBBS, MD (Cardiology), FCPS</strong></div>
                  <div>BMDC Registration: <strong>A-12345 (Verified)</strong></div>
                  <div>Department: <strong>Cardiology Dept</strong></div>
                  <div>Hospital Affiliation: <strong>Dhaka Central Hospital</strong></div>
                  <div>Languages Spoken: <strong>Bengali, English</strong></div>
                  <div>Consultation Fee: <strong>৳1000 BDT</strong></div>
                </div>
              </Card>

              <Card style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <h3 style={{ fontSize: '18px', fontWeight: '700' }}>Performance Stats</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', fontSize: '13px' }}>
                  <div style={{ display: 'flex', justifyContext: 'space-between' }}>
                    <span>Average Review Score:</span>
                    <strong>⭐ 4.8 / 5.0</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContext: 'space-between' }}>
                    <span>Total Patients Treated:</span>
                    <strong>12,450</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContext: 'space-between' }}>
                    <span>Follow-Up Compliance:</span>
                    <strong>87%</strong>
                  </div>
                </div>
              </Card>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}
window.DoctorPortal = DoctorPortal;

// 6. Complete Hospital Portal Component
function HospitalPortal({ onLogout }) {
  const { theme } = useTheme();
  
  // Navigation State
  const [activeSubTab, setActiveSubTab] = useState('dashboard');
  const [attendanceDept, setAttendanceDept] = useState('Cardiology');
  const [deptTab, setDeptTab] = useState('Cardiology');

  // Appointment requests list
  const [requests, setRequests] = useState([
    { id: 'R1', patient: 'Nehal Ahmmed', doctor: 'Dr. Khadija Begum', dept: 'Psychiatry', time: '10:00 AM', date: 'July 10, 2026', status: 'Pending' },
    { id: 'R2', patient: 'Rahim Islam', doctor: 'Dr. Fatema Ahmed', dept: 'Cardiology', time: '11:30 AM', date: 'July 10, 2026', status: 'Pending' }
  ]);

  // Attendance & Check-in active patient queues
  const [patientAttendance, setPatientAttendance] = useState([
    { id: 'P1', name: 'Nehal Ahmmed', dept: 'Psychiatry', doctor: 'Dr. Khadija Begum', status: 'Awaiting Arrival' },
    { id: 'P2', name: 'Rahim Islam', dept: 'Cardiology', doctor: 'Dr. Fatema Ahmed', status: 'Awaiting Arrival' },
    { id: 'P3', name: 'Fatema Zohra', dept: 'Cardiology', doctor: 'Dr. Fatema Ahmed', status: 'Checked In (Active Queue)' }
  ]);

  // Department timing shifts
  const departments = {
    Cardiology: [
      { doc: 'Dr. Fatema Ahmed', shift: '09:00 AM - 01:00 PM', status: 'Active In Chamber' }
    ],
    Psychiatry: [
      { doc: 'Dr. Khadija Begum', shift: '10:00 AM - 02:00 PM', status: 'Active In Chamber' }
    ],
    Neurology: [
      { doc: 'Dr. Mushfiqur Rahman', shift: '02:00 PM - 06:00 PM', status: 'Shift Starts at 2 PM' }
    ]
  };

  // Lab operation orders
  const [labOrders, setLabOrders] = useState([
    { id: 'L101', patient: 'Nehal Ahmmed', test: 'Fasting Blood Glucose', doctor: 'Dr. Khadija Begum', status: 'Awaiting Upload' },
    { id: 'L102', patient: 'Rahim Islam', test: 'Lipid Profile Panel', doctor: 'Dr. Fatema Ahmed', status: 'Awaiting Upload' }
  ]);

  const [activeUploadOrder, setActiveUploadOrder] = useState(null);
  const [measuredValue, setMeasuredValue] = useState('');
  const [refRange, setRefRange] = useState('70 - 100 mg/dL');

  const approveAppointment = (id) => {
    setRequests(prev => prev.map(req => req.id === id ? { ...req, status: 'Approved' } : req));
    alert('Success: Appointment approved! Syncing with attendance check-in counter.');
  };

  const markPatientPresent = (id) => {
    setPatientAttendance(prev => prev.map(pat => {
      if (pat.id === id) {
        return { ...pat, status: 'Checked In (Active Queue)' };
      }
      return pat;
    }));
    alert('Arrival Checked In: Patient added to doctor queue!');
  };

  const handleUploadReport = (order) => {
    setActiveUploadOrder(order);
    setMeasuredValue('');
    if (order.test.includes('Glucose')) {
      setRefRange('70 - 100 mg/dL');
    } else {
      setRefRange('< 200 mg/dL');
    }
  };

  const submitReportResults = () => {
    if (!measuredValue.trim()) return;
    setLabOrders(prev => prev.map(ord => ord.id === activeUploadOrder.id ? { ...ord, status: 'Uploaded & Dispatched' } : ord));
    alert(`Success: ${activeUploadOrder.test} report successfully sent to ${activeUploadOrder.doctor} for patient ${activeUploadOrder.patient}!`);
    setActiveUploadOrder(null);
  };

  // Stats computation
  const pendingRequestsCount = requests.filter(r => r.status === 'Pending').length;
  const activeCheckIns = patientAttendance.filter(p => p.status.includes('Active')).length;
  const pendingLabCounts = labOrders.filter(l => l.status === 'Awaiting Upload').length;

  return (
    <div style={{ display: 'flex', minHeight: '680px', width: '100%', background: theme.bgMain }}>
      {/* Sidebar Nav */}
      <div style={{ width: '260px', borderRight: `1px solid ${theme.border}`, background: theme.bgCard, padding: '24px 16px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontWeight: '800', fontSize: '20px', color: theme.brandPrimary, marginBottom: '32px', paddingLeft: '8px' }}>
            <Icon.Heart size={24} color={theme.brandPrimary} fill={theme.brandPrimary} />
            <span>NHCS Hospital</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {[
              { id: 'dashboard', label: 'Command Dashboard', icon: <Icon.Activity size={18} /> },
              { id: 'appointments', label: 'Appointment Requests', icon: <Icon.FileText size={18} /> },
              { id: 'attendance', label: 'Arrivals Check-In', icon: <Icon.User size={18} /> },
              { id: 'departments', label: 'Department Timings', icon: <Icon.Stethoscope size={18} /> },
              { id: 'lab', label: 'Laboratory Hub', icon: <Icon.Activity size={18} /> }
            ].map(item => {
              const active = activeSubTab === item.id;
              return (
                <button
                  key={item.id}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                    width: '100%',
                    padding: '12px 16px',
                    borderRadius: theme.innerRadius,
                    border: 'none',
                    background: active ? 'rgba(239, 68, 68, 0.12)' : 'transparent',
                    color: active ? theme.brandPrimary : theme.textSecondary,
                    fontWeight: active ? '700' : '600',
                    fontSize: '14px',
                    textAlign: 'left',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease'
                  }}
                  onClick={() => setActiveSubTab(item.id)}
                >
                  <span style={{ display: 'flex', alignItems: 'center', color: active ? theme.brandPrimary : theme.textSecondary }}>{item.icon}</span> {item.label}
                </button>
              );
            })}
          </div>
        </div>

        <button
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            padding: '12px 16px',
            borderRadius: theme.innerRadius,
            border: `1px solid ${theme.border}`,
            background: 'transparent',
            color: theme.brandPrimary,
            fontWeight: '700',
            fontSize: '14px',
            cursor: 'pointer',
            textAlign: 'left'
          }}
          onClick={onLogout}
        >
          Sign Out
        </button>
      </div>

      {/* Main content pane */}
      <div style={{ flexGrow: 1, padding: '40px', overflowY: 'auto' }}>
        
        {/* HOSPITAL DASHBOARD */}
        {activeSubTab === 'dashboard' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            <div>
              <h2 style={{ fontSize: '24px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>Dhaka Central Medical CommandCenter</h2>
              <span style={{ fontSize: '13px', color: theme.textSecondary }}>Universal Hospital Operations Overview</span>
            </div>

            {/* Quick Metrics */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px' }}>
              <Card>
                <span style={{ fontSize: '12px', color: theme.textSecondary }}>Beds Status</span>
                <div style={{ fontSize: '28px', fontWeight: '800', color: theme.brandPrimary, margin: '8px 0 4px 0' }}>290 / 350</div>
                <span style={{ fontSize: '11px', color: theme.textSecondary }}>Occupancy: 82%</span>
              </Card>
              <Card>
                <span style={{ fontSize: '12px', color: theme.textSecondary }}>Pending Approvals</span>
                <div style={{ fontSize: '28px', fontWeight: '800', color: theme.warning, margin: '8px 0 4px 0' }}>{pendingRequestsCount}</div>
                <span style={{ fontSize: '11px', color: theme.warning }}>Needs review</span>
              </Card>
              <Card>
                <span style={{ fontSize: '12px', color: theme.textSecondary }}>Active Arrivals</span>
                <div style={{ fontSize: '28px', fontWeight: '800', color: theme.success, margin: '8px 0 4px 0' }}>{activeCheckIns}</div>
                <span style={{ fontSize: '11px', color: theme.success }}>Checked-in queues</span>
              </Card>
              <Card>
                <span style={{ fontSize: '12px', color: theme.textSecondary }}>Lab Investigation requests</span>
                <div style={{ fontSize: '28px', fontWeight: '800', color: theme.brandPrimary, margin: '8px 0 4px 0' }}>{pendingLabCounts}</div>
                <span style={{ fontSize: '11px', color: theme.brandPrimary }}>Tests pending result</span>
              </Card>
            </div>

            {/* Hospital details banner */}
            <Card style={{ background: theme.mode === 'dark' ? 'rgba(239, 68, 68, 0.08)' : 'rgba(239, 68, 68, 0.02)', border: `1px solid ${theme.border}` }}>
              <h3 style={{ fontSize: '16px', fontWeight: '700', color: theme.brandPrimary, marginBottom: '8px' }}>Hospital Command Center</h3>
              <p style={{ fontSize: '13px', color: theme.textSecondary, lineHeight: '1.6' }}>
                Universal Health Card System integration is active. All receptionist arrivals automatically sync with specialist queues in real time. Lab orders dispatched here are published instantly to the clinical workspace.
              </p>
            </Card>
          </div>
        )}

        {/* APPOINTMENT MANAGEMENT */}
        {activeSubTab === 'appointments' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h2 style={{ fontSize: '22px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>📝 Patient Appointment Request Manager</h2>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {requests.map(req => (
                <Card key={req.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <h4 style={{ fontWeight: '700', fontSize: '16px' }}>{req.patient}</h4>
                    <span style={{ fontSize: '13px', color: theme.brandPrimary, fontWeight: '700' }}>Requesting: {req.doctor} ({req.dept})</span>
                    <div style={{ fontSize: '12px', color: theme.textSecondary, marginTop: '4px' }}>Date: {req.date} | Time: <strong>{req.time}</strong></div>
                  </div>

                  {req.status === 'Pending' ? (
                    <Btn style={{ padding: '8px 16px', fontSize: '12px' }} onClick={() => approveAppointment(req.id)}>Approve Request</Btn>
                  ) : (
                    <Chip label="Approved & Confirmed" status="success" />
                  )}
                </Card>
              ))}
            </div>
          </div>
        )}

        {/* ARRIVALS & ATTENDANCE CHECK-IN */}
        {activeSubTab === 'attendance' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h2 style={{ fontSize: '22px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>Patient Arrivals Check-In Console</h2>

            {/* Department tabs */}
            <div style={{ display: 'flex', gap: '10px' }}>
              {['Cardiology', 'Psychiatry', 'Neurology'].map(dept => (
                <button
                  key={dept}
                  style={{
                    background: attendanceDept === dept ? theme.brandPrimary : theme.bgCard,
                    color: attendanceDept === dept ? '#ffffff' : theme.textSecondary,
                    border: `1px solid ${theme.border}`,
                    padding: '8px 16px',
                    borderRadius: '20px',
                    cursor: 'pointer',
                    fontWeight: '700',
                    fontSize: '13px'
                  }}
                  onClick={() => setAttendanceDept(dept)}
                >
                  {dept} Department
                </button>
              ))}
            </div>

            <Card>
              <h3 style={{ fontSize: '16px', fontWeight: '700', marginBottom: '14px', borderBottom: `1px solid ${theme.border}`, paddingBottom: '8px' }}>
                Active Doctors in {attendanceDept}
              </h3>
              
              {departments[attendanceDept]?.map((doctor, idx) => (
                <div key={idx} style={{ marginBottom: '24px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
                    <strong>{doctor.doc}</strong>
                    <span style={{ fontSize: '12px', color: theme.textSecondary }}>Shift: {doctor.shift}</span>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', paddingLeft: '16px' }}>
                    {patientAttendance.filter(p => p.dept === attendanceDept && p.doctor === doctor.doc).length > 0 ? (
                      patientAttendance.filter(p => p.dept === attendanceDept && p.doctor === doctor.doc).map(pat => (
                        <div key={pat.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: theme.bgInput, padding: '10px', borderRadius: theme.innerRadius }}>
                          <span style={{ fontSize: '13px', color: theme.textPrimary }}>{pat.name}</span>
                          
                          {pat.status === 'Awaiting Arrival' ? (
                            <Btn style={{ padding: '6px 12px', fontSize: '11px' }} onClick={() => markPatientPresent(pat.id)}>Mark Present (Add to Queue)</Btn>
                          ) : (
                            <Chip label="Present in active queue" status="success" />
                          )}
                        </div>
                      ))
                    ) : (
                      <span style={{ fontSize: '12px', color: theme.textSecondary }}>No approved patient bookings registered.</span>
                    )}
                  </div>
                </div>
              ))}
            </Card>
          </div>
        )}

        {/* DEPARTMENT TIMINGS */}
        {activeSubTab === 'departments' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h2 style={{ fontSize: '22px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>Department Shift Schedules</h2>
            
            <div style={{ display: 'flex', gap: '10px' }}>
              {['Cardiology', 'Psychiatry', 'Neurology'].map(dept => (
                <button
                  key={dept}
                  style={{
                    background: deptTab === dept ? theme.brandPrimary : theme.bgCard,
                    color: deptTab === dept ? '#ffffff' : theme.textSecondary,
                    border: `1px solid ${theme.border}`,
                    padding: '8px 16px',
                    borderRadius: '20px',
                    cursor: 'pointer',
                    fontWeight: '700',
                    fontSize: '13px'
                  }}
                  onClick={() => setDeptTab(dept)}
                >
                  {dept}
                </button>
              ))}
            </div>

            <Card>
              <h3 style={{ fontSize: '16px', fontWeight: '700', marginBottom: '16px' }}>Shift Details for {deptTab}</h3>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                <thead>
                  <tr style={{ borderBottom: `1px solid ${theme.border}`, textAlign: 'left', color: theme.textSecondary }}>
                    <th style={{ padding: '10px 4px' }}>Doctor Name</th>
                    <th style={{ padding: '10px 4px' }}>Shift Timings</th>
                    <th style={{ padding: '10px 4px' }}>Room / Chamber</th>
                    <th style={{ padding: '10px 4px' }}>Chamber Status</th>
                  </tr>
                </thead>
                <tbody>
                  {departments[deptTab]?.map((doc, idx) => (
                    <tr key={idx} style={{ borderBottom: `1px solid ${theme.border}` }}>
                      <td style={{ padding: '12px 4px', fontWeight: '700' }}>{doc.doc}</td>
                      <td style={{ padding: '12px 4px' }}>{doc.shift}</td>
                      <td style={{ padding: '12px 4px' }}>Room #{300 + idx}</td>
                      <td style={{ padding: '12px 4px', color: theme.brandPrimary, fontWeight: '700' }}>{doc.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </Card>
          </div>
        )}

        {/* LABORATORY OPERATIONS HUB */}
        {activeSubTab === 'lab' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h2 style={{ fontSize: '22px', fontWeight: '800', color: theme.textPrimary, fontFamily: 'Outfit' }}>Laboratory Investigations & Upload</h2>

            <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '30px' }}>
              
              {/* Left Panel: Lab Order List */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                <h3 style={{ fontSize: '16px', fontWeight: '700' }}>Pending Diagnostics List</h3>
                {labOrders.map(ord => (
                  <Card key={ord.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <h4 style={{ fontWeight: '700', fontSize: '15px' }}>{ord.patient}</h4>
                      <span style={{ fontSize: '13px', color: theme.brandPrimary, fontWeight: '700' }}>Test: {ord.test}</span>
                      <div style={{ fontSize: '12px', color: theme.textSecondary, marginTop: '4px' }}>Ordered by: {ord.doctor}</div>
                    </div>

                    {ord.status === 'Awaiting Upload' ? (
                      <Btn style={{ padding: '8px 14px', fontSize: '12px' }} onClick={() => handleUploadReport(ord)}>Upload Report</Btn>
                    ) : (
                      <Chip label="Dispatched to Doctor" status="success" />
                    )}
                  </Card>
                ))}
              </div>

              {/* Right Panel: Result entry form */}
              <Card>
                <h3 style={{ fontSize: '16px', fontWeight: '700', marginBottom: '14px', borderBottom: `1px solid ${theme.border}`, paddingBottom: '8px' }}>
                  Result Parameter Input Form
                </h3>

                {activeUploadOrder ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                    <div style={{ fontSize: '13px', color: theme.textSecondary }}>
                      Active Patient: <strong>{activeUploadOrder.patient}</strong> <br />
                      Ordered Investigation: <strong>{activeUploadOrder.test}</strong>
                    </div>

                    <Input label="Measured Parameter Value" placeholder="e.g. 142 or 160" value={measuredValue} onChange={(e) => setMeasuredValue(e.target.value)} />
                    <Input label="Normal Reference Range" value={refRange} disabled />

                    <Btn style={{ width: '100%', marginTop: '10px' }} onClick={submitReportResults}>
                      Submit & Dispatch to Doctor
                    </Btn>
                  </div>
                ) : (
                  <div style={{ textAlign: 'center', padding: '40px', color: theme.textSecondary }}>
                    Select a pending diagnostics order from the left list to load the upload panel.
                  </div>
                )}
              </Card>

            </div>
          </div>
        )}

      </div>
    </div>
  );
}
window.HospitalPortal = HospitalPortal;

