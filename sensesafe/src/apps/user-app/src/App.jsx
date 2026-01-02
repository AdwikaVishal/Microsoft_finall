import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

// Pages
import Onboarding from './pages/Onboarding';
import DisasterAlert from './pages/DisasterAlert';
import Guidance from './pages/Guidance';
import IncidentReport from './pages/IncidentReport';
import Home from './pages/Home';

// Utils
import { getAbilityProfile, setAbilityProfile } from '../../utils/storage.js';

function App() {
  const [abilityProfile, setAbilityProfileState] = useState(null);
  const [darkMode, setDarkMode] = useState(false);

  // Create theme based on accessibility needs
  const theme = createTheme({
    palette: {
      mode: darkMode ? 'dark' : 'light',
      primary: {
        main: '#1976d2',
      },
      secondary: {
        main: '#dc004e',
      },
    },
    typography: {
      fontSize: 16, // Larger base font size for accessibility
      h1: {
        fontSize: '2.5rem', // Large headings
        fontWeight: 'bold',
      },
      h2: {
        fontSize: '2rem',
        fontWeight: 'bold',
      },
      button: {
        fontSize: '1.1rem', // Larger button text
        fontWeight: 'bold',
      },
    },
    components: {
      MuiButton: {
        styleOverrides: {
          root: {
            minHeight: 60, // Large touch targets
            padding: '16px 32px',
            fontSize: '1.1rem',
          },
        },
      },
      MuiTextField: {
        styleOverrides: {
          root: {
            '& .MuiInputBase-root': {
              fontSize: '1.1rem',
              minHeight: 60,
            },
          },
        },
      },
    },
  });

  useEffect(() => {
    // Load saved ability profile
    const savedProfile = getAbilityProfile();
    if (savedProfile) {
      setAbilityProfileState(savedProfile);
    }
  }, []);

  const handleAbilityProfileSave = (profile) => {
    setAbilityProfile(profile);
    setAbilityProfileState(profile);
  };

  // Show onboarding if no profile is set
  if (!abilityProfile) {
    return (
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Router>
          <Onboarding onSave={handleAbilityProfileSave} />
        </Router>
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <div className="app-container">
          {/* Debug Mode Button */}
          <DebugModeButton />
          
          <Routes>
            <Route path="/" element={<Home abilityProfile={abilityProfile} />} />
            <Route path="/disaster" element={<DisasterAlert abilityProfile={abilityProfile} />} />
            <Route path="/guidance" element={<Guidance abilityProfile={abilityProfile} />} />
            <Route path="/report" element={<IncidentReport abilityProfile={abilityProfile} />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </Router>
    </ThemeProvider>
  );
}

// Debug Mode Component
function DebugModeButton() {
  const [isVisible, setIsVisible] = React.useState(false);

  const simulateSOS = () => {
    console.log('🆘 Simulating SOS alert');
    // TODO: Call Azure Functions here
    alert('SOS Alert Simulated! (Check console)');
  };

  const simulateDisaster = () => {
    console.log('⚠️ Simulating disaster alert');
    // TODO: Connect to Azure Notification Hub here
    alert('Disaster Alert Simulated! (Check console)');
  };

  const simulateAdminVerify = () => {
    console.log('✅ Simulating admin verification');
    alert('Admin Verification Simulated! (Check console)');
  };

  return (
    <>
      {/* Small debug toggle button */}
      <button
        onClick={() => setIsVisible(!isVisible)}
        style={{
          position: 'fixed',
          top: '10px',
          right: '10px',
          zIndex: 9999,
          background: '#666',
          color: 'white',
          border: 'none',
          padding: '8px',
          borderRadius: '4px',
          fontSize: '12px',
          cursor: 'pointer'
        }}
        aria-label="Toggle debug mode"
      >
        DEBUG
      </button>

      {/* Debug panel */}
      {isVisible && (
        <div
          style={{
            position: 'fixed',
            top: '50px',
            right: '10px',
            zIndex: 9998,
            background: 'white',
            border: '1px solid #ccc',
            borderRadius: '4px',
            padding: '16px',
            boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
          }}
        >
          <h4 style={{ margin: '0 0 12px 0' }}>Debug Mode</h4>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <button onClick={simulateSOS} style={{ padding: '8px', fontSize: '12px' }}>
              Simulate SOS
            </button>
            <button onClick={simulateDisaster} style={{ padding: '8px', fontSize: '12px' }}>
              Simulate Disaster
            </button>
            <button onClick={simulateAdminVerify} style={{ padding: '8px', fontSize: '12px' }}>
              Simulate Admin Verify
            </button>
          </div>
        </div>
      )}
    </>
  );
}

export default App;
