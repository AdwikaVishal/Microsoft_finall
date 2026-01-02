import React, { useState } from 'react';
import {
  Container,
  Typography,
  Button,
  Box,
  Paper,
  Grid,
  FormControl,
  FormControlLabel,
  Radio,
  RadioGroup,
  Switch,
  Divider,
} from '@mui/material';
import {
  VisibilityOff,
  Hearing,
  RecordVoiceOver,
  Elderly,
  Person,
  VolumeUp,
} from '@mui/icons-material';

function Onboarding({ onSave }) {
  const [selectedProfile, setSelectedProfile] = useState('');
  const [highContrast, setHighContrast] = useState(false);
  const [largeText, setLargeText] = useState(true);

  const abilityProfiles = [
    {
      id: 'blind',
      label: 'Blind',
      icon: <VisibilityOff sx={{ fontSize: 48 }} />,
      description: 'Visual impairment - optimized for screen readers',
      accessibility: ['High contrast mode', 'Audio descriptions', 'Voice navigation'],
    },
    {
      id: 'deaf',
      label: 'Deaf',
      icon: <Hearing sx={{ fontSize: 48 }} />,
      description: 'Hearing impairment - visual and text-based alerts',
      accessibility: ['Text alerts', 'Visual indicators', 'Vibration patterns'],
    },
    {
      id: 'nonverbal',
      label: 'Non-verbal',
      icon: <RecordVoiceOver sx={{ fontSize: 48 }} />,
      description: 'Communication difficulties - simple one-tap responses',
      accessibility: ['Big buttons', 'Simple language', 'Icon-based interface'],
    },
    {
      id: 'elderly',
      label: 'Elderly',
      icon: <Elderly sx={{ fontSize: 48 }} />,
      description: 'Age-related considerations - larger text and buttons',
      accessibility: ['Large text', 'High contrast', 'Simple navigation'],
    },
    {
      id: 'normal',
      label: 'No disability',
      icon: <Person sx={{ fontSize: 48 }} />,
      description: 'Standard interface with accessibility options available',
      accessibility: ['All features', 'Optional accessibility modes'],
    },
  ];

  const handleSave = () => {
    if (selectedProfile) {
      const profile = {
        type: selectedProfile,
        highContrast,
        largeText,
        timestamp: new Date().toISOString(),
      };
      onSave(profile);
    }
  };

  const speakText = (text) => {
    // TODO: AZURE SPEECH SERVICE HERE
    // This will be replaced with Azure Speech Service integration
    if ('speechSynthesis' in window) {
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.rate = 0.8;
      utterance.pitch = 1;
      window.speechSynthesis.speak(utterance);
    }
  };

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Box textAlign="center" mb={4}>
        <Typography 
          variant="h1" 
          component="h1" 
          gutterBottom
          sx={{ 
            fontSize: { xs: '2rem', sm: '3rem' },
            color: '#1976d2',
            mb: 2 
          }}
        >
          Welcome to SenseSafe
        </Typography>
        <Typography 
          variant="h6" 
          component="p" 
          sx={{ 
            fontSize: { xs: '1.1rem', sm: '1.3rem' },
            color: 'text.secondary',
            maxWidth: '600px',
            mx: 'auto'
          }}
        >
          Help us customize your experience by telling us about your needs.
          This information will be stored locally on your device.
        </Typography>
      </Box>

      {/* Accessibility Settings */}
      <Paper elevation={3} sx={{ p: 3, mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          Accessibility Settings
        </Typography>
        <Box display="flex" flexDirection="column" gap={2}>
          <FormControlLabel
            control={
              <Switch
                checked={highContrast}
                onChange={(e) => setHighContrast(e.target.checked)}
                inputProps={{ 'aria-label': 'High contrast mode' }}
              />
            }
            label={
              <Box display="flex" alignItems="center" gap={1}>
                <span>High Contrast Mode</span>
              </Box>
            }
          />
          <FormControlLabel
            control={
              <Switch
                checked={largeText}
                onChange={(e) => setLargeText(e.target.checked)}
                inputProps={{ 'aria-label': 'Large text mode' }}
              />
            }
            label={
              <Box display="flex" alignItems="center" gap={1}>
                <span>Large Text</span>
              </Box>
            }
          />
        </Box>
      </Paper>

      {/* Ability Profile Selection */}
      <Typography variant="h6" gutterBottom sx={{ mb: 3 }}>
        Select Your Ability Profile
      </Typography>

      <Grid container spacing={3}>
        {abilityProfiles.map((profile) => (
          <Grid item xs={12} sm={6} key={profile.id}>
            <Paper
              elevation={selectedProfile === profile.id ? 8 : 2}
              sx={{
                p: 3,
                cursor: 'pointer',
                transition: 'all 0.3s ease',
                border: selectedProfile === profile.id ? '3px solid #1976d2' : '2px solid transparent',
                '&:hover': {
                  elevation: 4,
                  transform: 'translateY(-2px)',
                },
              }}
              onClick={() => setSelectedProfile(profile.id)}
              role="button"
              tabIndex={0}
              onKeyPress={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  setSelectedProfile(profile.id);
                }
              }}
              aria-label={`Select ${profile.label} profile: ${profile.description}`}
            >
              <Box textAlign="center" mb={2}>
                <Box 
                  sx={{ 
                    color: selectedProfile === profile.id ? '#1976d2' : 'text.secondary',
                    mb: 2 
                  }}
                >
                  {profile.icon}
                </Box>
                <Typography variant="h6" component="h3" gutterBottom>
                  {profile.label}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  {profile.description}
                </Typography>
              </Box>

              <Divider sx={{ my: 2 }} />

              <Typography variant="subtitle2" gutterBottom>
                Accessibility Features:
              </Typography>
              <ul style={{ paddingLeft: '20px', margin: 0 }}>
                {profile.accessibility.map((feature, index) => (
                  <li key={index}>
                    <Typography variant="body2" color="text.secondary">
                      {feature}
                    </Typography>
                  </li>
                ))}
              </ul>

              {selectedProfile === profile.id && (
                <Box mt={2} textAlign="center">
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={(e) => {
                      e.stopPropagation();
                      speakText(profile.description);
                    }}
                    startIcon={<VolumeUp />}
                    sx={{ mr: 1 }}
                  >
                    Listen
                  </Button>
                </Box>
              )}
            </Paper>
          </Grid>
        ))}
      </Grid>

      <Box textAlign="center" mt={4}>
        <Button
          variant="contained"
          size="large"
          onClick={handleSave}
          disabled={!selectedProfile}
          sx={{
            minWidth: 200,
            minHeight: 60,
            fontSize: '1.2rem',
            fontWeight: 'bold',
          }}
          aria-label="Save your ability profile and continue"
        >
          Continue to SenseSafe
        </Button>
      </Box>

      {/* Privacy Notice */}
      <Box mt={4} p={2} sx={{ backgroundColor: 'grey.100', borderRadius: 1 }}>
        <Typography variant="body2" color="text.secondary" textAlign="center">
          🔒 Your privacy is important to us. This information is stored locally on your device
          and never shared without your consent.
        </Typography>
      </Box>
    </Container>
  );
}

export default Onboarding;
