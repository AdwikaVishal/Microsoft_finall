import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Button,
  Box,
  Card,
  CardContent,
  Grid,
  Chip,
  Alert,
  Snackbar,
} from '@mui/material';
import {
  Help,
  VolumeUp,
  Visibility,
  RecordVoiceOver,
  Accessibility,
  Send,
  Home,
  Warning,
} from '@mui/icons-material';
import { sendUserStatus } from '../../../services/api.js';

function Guidance({ abilityProfile }) {
  const navigate = useNavigate();
  const [selectedGuidance, setSelectedGuidance] = useState(null);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const guidanceOptions = {
    blind: [
      {
        id: 'trapped',
        text: "I'm trapped",
        description: 'You are trapped and need immediate rescue',
        audioText: "Help! I am trapped and need immediate rescue.",
      },
      {
        id: 'injured',
        text: "I'm injured",
        description: 'You are injured and need medical attention',
        audioText: "Help! I am injured and need medical attention.",
      },
      {
        id: 'help_needed',
        text: "I need help",
        description: 'You need assistance but are not in immediate danger',
        audioText: "I need help and assistance.",
      },
      {
        id: 'safe',
        text: "I'm safe",
        description: 'You are safe and do not need immediate assistance',
        audioText: "I am safe and do not need assistance at this time.",
      },
    ],
    deaf: [
      {
        id: 'trapped',
        text: "TRAPPED",
        icon: "🚪",
        description: 'Use simple words and visual cues',
        visualCue: "🔴",
      },
      {
        id: 'injured',
        text: "INJURED",
        icon: "🩹",
        description: 'Point to injury location if possible',
        visualCue: "🟠",
      },
      {
        id: 'help_needed',
        text: "HELP NEEDED",
        icon: "🤝",
        description: 'Use gestures to communicate',
        visualCue: "🟡",
      },
      {
        id: 'safe',
        text: "SAFE",
        icon: "✅",
        description: 'Use thumbs up or OK sign',
        visualCue: "🟢",
      },
    ],
    nonverbal: [
      {
        id: 'trapped',
        text: "TRAPPED",
        description: 'Tap once for immediate help',
        action: () => handleQuickAction('trapped'),
      },
      {
        id: 'injured',
        text: "INJURED",
        description: 'Tap once for medical help',
        action: () => handleQuickAction('injured'),
      },
      {
        id: 'help_needed',
        text: "HELP",
        description: 'Tap once for assistance',
        action: () => handleQuickAction('help_needed'),
      },
      {
        id: 'safe',
        text: "SAFE",
        description: 'Tap once to confirm safety',
        action: () => handleQuickAction('safe'),
      },
    ],
    elderly: [
      {
        id: 'trapped',
        text: "I'm trapped",
        description: 'Stay calm and press this button for help',
        action: () => handleQuickAction('trapped'),
      },
      {
        id: 'injured',
        text: "I'm injured",
        description: 'Let us know if you need medical help',
        action: () => handleQuickAction('injured'),
      },
      {
        id: 'help_needed',
        text: "I need help",
        description: 'Press this if you need any assistance',
        action: () => handleQuickAction('help_needed'),
      },
      {
        id: 'safe',
        text: "I'm safe",
        description: 'Let us know you are safe',
        action: () => handleQuickAction('safe'),
      },
    ],
    normal: [
      {
        id: 'trapped',
        text: "I'm trapped",
        description: 'You are trapped and need immediate rescue',
        action: () => handleQuickAction('trapped'),
      },
      {
        id: 'injured',
        text: "I'm injured",
        description: 'You are injured and need medical attention',
        action: () => handleQuickAction('injured'),
      },
      {
        id: 'help_needed',
        text: "I need help",
        description: 'You need assistance but are not in immediate danger',
        action: () => handleQuickAction('help_needed'),
      },
      {
        id: 'safe',
        text: "I'm safe",
        description: 'You are safe and do not need immediate assistance',
        action: () => handleQuickAction('safe'),
      },
    ],
  };

  const handleQuickAction = async (status) => {
    setIsLoading(true);
    try {
      // TODO: AZURE FUNCTIONS CALL HERE
      // This will connect to Azure Functions in production
      await sendUserStatus({
        status,
        abilityProfile: abilityProfile.type,
        location: 'Current location', // TODO: Get real location
        timestamp: new Date().toISOString(),
      });
      
      setSnackbarOpen(true);
      setSelectedGuidance(status);
    } catch (error) {
      console.error('Error sending status:', error);
      alert('Error sending status. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const speakText = (text) => {
    if (abilityProfile.type === 'blind') {
      // TODO: AZURE SPEECH SERVICE HERE
      if ('speechSynthesis' in window) {
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.rate = 0.8;
        window.speechSynthesis.speak(utterance);
      }
    }
  };

  const renderGuidanceContent = () => {
    const options = guidanceOptions[abilityProfile.type] || guidanceOptions.normal;

    if (abilityProfile.type === 'blind') {
      return (
        <Box>
          <Typography variant="h6" gutterBottom>
            Audio Guidance for Blind Users
          </Typography>
          <Alert severity="info" sx={{ mb: 3 }}>
            Use the audio controls to hear guidance. Large buttons with audio feedback.
          </Alert>
          <Grid container spacing={3}>
            {options.map((option) => (
              <Grid item xs={12} sm={6} key={option.id}>
                <Button
                  variant="contained"
                  size="large"
                  fullWidth
                  onClick={() => speakText(option.audioText)}
                  startIcon={<VolumeUp />}
                  sx={{
                    minHeight: 80,
                    fontSize: '1.3rem',
                    fontWeight: 'bold',
                    display: 'flex',
                    flexDirection: 'column',
                    py: 3,
                  }}
                  aria-label={`${option.description}. Click to hear audio description`}
                >
                  <Typography variant="h6" component="div">
                    {option.text}
                  </Typography>
                  <Typography variant="body2" sx={{ mt: 1, opacity: 0.8 }}>
                    Tap to hear description
                  </Typography>
                </Button>
              </Grid>
            ))}
          </Grid>
        </Box>
      );
    }

    if (abilityProfile.type === 'deaf') {
      return (
        <Box>
          <Typography variant="h6" gutterBottom>
            Visual Guidance for Deaf Users
          </Typography>
          <Alert severity="info" sx={{ mb: 3 }}>
            High contrast visual cues and simple text. No audio required.
          </Alert>
          <Grid container spacing={3}>
            {options.map((option) => (
              <Grid item xs={12} sm={6} key={option.id}>
                <Card
                  sx={{
                    cursor: 'pointer',
                    transition: 'all 0.3s ease',
                    '&:hover': {
                      transform: 'scale(1.05)',
                      boxShadow: 4,
                    },
                    border: `3px solid ${option.visualCue === '🔴' ? '#f44336' : 
                                   option.visualCue === '🟠' ? '#ff9800' : 
                                   option.visualCue === '🟡' ? '#ffeb3b' : '#4caf50'}`,
                  }}
                  onClick={() => handleQuickAction(option.id)}
                >
                  <CardContent sx={{ textAlign: 'center', py: 4 }}>
                    <Box sx={{ fontSize: '4rem', mb: 2 }}>
                      {option.icon}
                    </Box>
                    <Typography 
                      variant="h5" 
                      component="h3" 
                      sx={{ 
                        fontWeight: 'bold',
                        mb: 1,
                        color: option.visualCue === '🔴' ? '#f44336' : 
                               option.visualCue === '🟠' ? '#ff9800' : 
                               option.visualCue === '🟡' ? '#ffeb3b' : '#4caf50'
                      }}
                    >
                      {option.text}
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                      {option.description}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>
      );
    }

    // For non-verbal, elderly, and normal users
    return (
      <Box>
        <Typography variant="h6" gutterBottom>
          {abilityProfile.type === 'nonverbal' ? 'One-Tap Actions' : 
           abilityProfile.type === 'elderly' ? 'Simple Guidance' : 
           'Quick Status Updates'}
        </Typography>
        {abilityProfile.type === 'nonverbal' && (
          <Alert severity="info" sx={{ mb: 3 }}>
            Large buttons for easy tapping. Each button sends an immediate status update.
          </Alert>
        )}
        <Grid container spacing={3}>
          {options.map((option) => (
            <Grid item xs={12} sm={6} key={option.id}>
              <Button
                variant="contained"
                size="large"
                fullWidth
                onClick={option.action}
                disabled={isLoading}
                sx={{
                  minHeight: abilityProfile.type === 'nonverbal' ? 120 : 80,
                  fontSize: abilityProfile.type === 'nonverbal' ? '1.5rem' : '1.2rem',
                  fontWeight: 'bold',
                  display: 'flex',
                  flexDirection: 'column',
                  py: abilityProfile.type === 'nonverbal' ? 4 : 3,
                }}
                aria-label={option.description}
              >
                {abilityProfile.type === 'nonverbal' && (
                  <Typography variant="h4" component="div" sx={{ mb: 1 }}>
                    {option.id === 'trapped' ? '🚪' : 
                     option.id === 'injured' ? '🩹' : 
                     option.id === 'help_needed' ? '🤝' : '✅'}
                  </Typography>
                )}
                <Typography variant="h6" component="div">
                  {option.text}
                </Typography>
                {abilityProfile.type !== 'nonverbal' && (
                  <Typography variant="body2" sx={{ mt: 1, opacity: 0.8 }}>
                    {option.description}
                  </Typography>
                )}
              </Button>
            </Grid>
          ))}
        </Grid>
      </Box>
    );
  };

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header */}
      <Box textAlign="center" mb={4}>
        <Box display="flex" alignItems="center" justifyContent="center" mb={2}>
          <Help sx={{ fontSize: 48, color: '#1976d2', mr: 2 }} />
          <Typography 
            variant="h1" 
            component="h1" 
            sx={{ 
              fontSize: { xs: '2rem', sm: '3rem' },
              color: '#1976d2',
            }}
          >
            Guidance
          </Typography>
        </Box>
        <Typography 
          variant="h6" 
          component="p" 
          sx={{ 
            fontSize: { xs: '1.1rem', sm: '1.3rem' },
            color: 'text.secondary',
            mb: 2 
          }}
        >
          {abilityProfile.type === 'blind' ? 'Audio-optimized guidance' :
           abilityProfile.type === 'deaf' ? 'Visual guidance with high contrast' :
           abilityProfile.type === 'nonverbal' ? 'Simple one-tap responses' :
           abilityProfile.type === 'elderly' ? 'Clear, easy-to-read guidance' :
           'Quick status updates'}
        </Typography>
        
        {/* Profile Badge */}
        <Chip
          label={`Optimized for ${abilityProfile.type}`}
          icon={<Accessibility />}
          color="primary"
          sx={{
            fontSize: '1rem',
            px: 2,
            py: 1,
            height: 'auto',
            '& .MuiChip-label': {
              padding: '8px 16px',
            },
          }}
        />
      </Box>

      {/* Current Status */}
      {selectedGuidance && (
        <Card sx={{ mb: 4, borderLeft: '5px solid #4caf50' }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Status Sent
            </Typography>
            <Typography variant="body1">
              Your status "{selectedGuidance}" has been sent to emergency responders.
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Help is on the way. Stay where you are if safe to do so.
            </Typography>
          </CardContent>
        </Card>
      )}

      {/* Guidance Content */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          {renderGuidanceContent()}
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <Box display="flex" flexDirection="column" gap={2} sx={{ mb: 4 }}>
        <Button
          variant="outlined"
          size="large"
          startIcon={<Home />}
          onClick={() => navigate('/')}
          sx={{
            minHeight: 60,
            fontSize: '1.2rem',
          }}
        >
          Return to Home
        </Button>
      </Box>

      {/* Status Info */}
      <Card sx={{ borderLeft: '5px solid #2196f3' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            What Happens Next?
          </Typography>
          <Box component="ul" sx={{ pl: 3 }}>
            <li>
              <Typography variant="body1" sx={{ mb: 1 }}>
                Your status is sent to emergency responders immediately
              </Typography>
            </li>
            <li>
              <Typography variant="body1" sx={{ mb: 1 }}>
                {/* TODO: AZURE FUNCTIONS CALL HERE */}
                Azure Functions will process and route your request
              </Typography>
            </li>
            <li>
              <Typography variant="body1" sx={{ mb: 1 }}>
                Help will be dispatched based on your location and situation
              </Typography>
            </li>
            <li>
              <Typography variant="body1" sx={{ mb: 1 }}>
                You can update your status at any time
              </Typography>
            </li>
          </Box>
        </CardContent>
      </Card>

      {/* Success Snackbar */}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={4000}
        onClose={() => setSnackbarOpen(false)}
        message="Status sent successfully!"
        action={
          <Button color="inherit" size="small" onClick={() => setSnackbarOpen(false)}>
            OK
          </Button>
        }
      />
    </Container>
  );
}

export default Guidance;
