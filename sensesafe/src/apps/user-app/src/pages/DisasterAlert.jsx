import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Button,
  Box,
  Card,
  CardContent,
  Alert,
  Chip,
  Divider,
} from '@mui/material';
import {
  Warning,
  VolumeUp,
  VolumeOff,
  Navigation,
  Home,
  Shield,
  LocationOn,
} from '@mui/icons-material';

function DisasterAlert({ abilityProfile }) {
  const navigate = useNavigate();
  const [alert, setAlert] = useState(null);
  const [audioEnabled, setAudioEnabled] = useState(false);

  // Mock disaster alert - in real app this would come from Azure Notification Hub
  useEffect(() => {
    const mockAlert = {
      id: 'alert_001',
      title: 'Severe Weather Alert',
      level: 'High', // Low, Medium, High, Critical
      message: 'Severe thunderstorm warning in your area. Seek shelter immediately.',
      location: 'Your current location',
      timestamp: new Date().toISOString(),
      instructions: [
        'Move to the lowest floor of your building',
        'Stay away from windows and outside walls',
        'If you are outdoors, move to a sturdy building immediately',
        'Do not use elevators',
        'Keep your phone charged and stay connected',
      ],
      emergencyContacts: [
        { name: 'Emergency Services', number: '911' },
        { name: 'Local Emergency Management', number: '555-0100' },
      ]
    };

    setAlert(mockAlert);

    // Auto-enable audio for certain profiles
    if (abilityProfile.type === 'blind' || abilityProfile.type === 'elderly') {
      setAudioEnabled(true);
      speakAlert(mockAlert);
    }
  }, [abilityProfile.type]);

  const speakAlert = (alertData) => {
    if (!audioEnabled) return;
    
    // TODO: AZURE SPEECH SERVICE HERE
    // This will be replaced with Azure Speech Service integration
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel(); // Stop any ongoing speech
      const utterance = new SpeechSynthesisUtterance(
        `Emergency Alert: ${alertData.message}. Please follow the instructions provided.`
      );
      utterance.rate = 0.8;
      utterance.pitch = 1;
      utterance.volume = 1;
      window.speechSynthesis.speak(utterance);
    }
  };

  const getAlertColor = (level) => {
    const colors = {
      Low: '#4caf50',
      Medium: '#ff9800',
      High: '#ff5722',
      Critical: '#f44336',
    };
    return colors[level] || '#4caf50';
  };

  const getAlertIcon = (level) => {
    switch (level) {
      case 'Critical':
        return '🚨';
      case 'High':
        return '⚠️';
      case 'Medium':
        return '⚡';
      default:
        return 'ℹ️';
    }
  };

  if (!alert) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Typography variant="h5">Loading alert information...</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header */}
      <Box textAlign="center" mb={4}>
        <Box 
          sx={{ 
            fontSize: '4rem',
            mb: 2,
            animation: 'pulse 2s infinite'
          }}
        >
          {getAlertIcon(alert.level)}
        </Box>
        <Typography 
          variant="h1" 
          component="h1" 
          sx={{ 
            fontSize: { xs: '2rem', sm: '3rem' },
            color: getAlertColor(alert.level),
            fontWeight: 'bold'
          }}
        >
          Emergency Alert
        </Typography>
      </Box>

      {/* Alert Card */}
      <Card sx={{ mb: 4, borderLeft: `5px solid ${getAlertColor(alert.level)}` }}>
        <CardContent>
          <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
            <Typography variant="h5" component="h2">
              {alert.title}
            </Typography>
            <Chip
              label={alert.level}
              sx={{
                backgroundColor: getAlertColor(alert.level),
                color: 'white',
                fontWeight: 'bold',
                fontSize: '1rem',
                px: 2,
                py: 1,
              }}
            />
          </Box>

          <Typography 
            variant="body1" 
            sx={{ 
              fontSize: '1.2rem',
              lineHeight: 1.6,
              mb: 2,
              fontWeight: abilityProfile.type === 'blind' ? 'bold' : 'normal'
            }}
          >
            {alert.message}
          </Typography>

          <Box display="flex" alignItems="center" mb={2}>
            <LocationOn sx={{ mr: 1, color: 'text.secondary' }} />
            <Typography variant="body2" color="text.secondary">
              {alert.location}
            </Typography>
          </Box>

          <Typography variant="body2" color="text.secondary">
            Alert issued: {new Date(alert.timestamp).toLocaleString()}
          </Typography>
        </CardContent>
      </Card>

      {/* Audio Controls */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Box display="flex" alignItems="center" justifyContent="space-between">
            <Typography variant="h6">
              Audio Alert
            </Typography>
            <Button
              variant={audioEnabled ? 'contained' : 'outlined'}
              onClick={() => {
                setAudioEnabled(!audioEnabled);
                if (!audioEnabled) {
                  speakAlert(alert);
                } else {
                  window.speechSynthesis.cancel();
                }
              }}
              startIcon={audioEnabled ? <VolumeUp /> : <VolumeOff />}
            >
              {audioEnabled ? 'Audio On' : 'Audio Off'}
            </Button>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            {/* TODO: AZURE SPEECH SERVICE HERE */}
            This will use Azure Speech Service for high-quality audio alerts
          </Typography>
        </CardContent>
      </Card>

      {/* Instructions */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            What You Should Do
          </Typography>
          <Divider sx={{ mb: 2 }} />
          <Box component="ol" sx={{ pl: 3 }}>
            {alert.instructions.map((instruction, index) => (
              <li key={index}>
                <Typography 
                  variant="body1" 
                  sx={{ 
                    mb: 1,
                    fontSize: abilityProfile.largeText ? '1.2rem' : '1rem',
                    lineHeight: 1.6
                  }}
                >
                  {instruction}
                </Typography>
              </li>
            ))}
          </Box>
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <Box display="flex" flexDirection="column" gap={2} sx={{ mb: 4 }}>
        <Button
          variant="contained"
          size="large"
          startIcon={<Navigation />}
          onClick={() => {
            // TODO: AZURE MAPS INTEGRATION HERE
            alert('Navigation to nearest shelter opened (Azure Maps integration needed)');
          }}
          sx={{
            minHeight: 60,
            fontSize: '1.2rem',
            backgroundColor: '#2196f3',
            '&:hover': {
              backgroundColor: '#1976d2',
            },
          }}
        >
          Find Nearest Shelter
        </Button>

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

      {/* Emergency Contacts */}
      <Card sx={{ borderLeft: '5px solid #ff9800' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Emergency Contacts
          </Typography>
          {alert.emergencyContacts.map((contact, index) => (
            <Box key={index} display="flex" justifyContent="space-between" alignItems="center" mb={1}>
              <Typography variant="body1">
                {contact.name}
              </Typography>
              <Button
                variant="contained"
                size="small"
                href={`tel:${contact.number}`}
                sx={{ minWidth: 'auto' }}
              >
                {contact.number}
              </Button>
            </Box>
          ))}
          <Divider sx={{ my: 2 }} />
          <Typography variant="body2" color="text.secondary">
            {/* TODO: AZURE NOTIFICATION HUB HERE */}
            These alerts are delivered via Azure Notification Hub in production
          </Typography>
        </CardContent>
      </Card>

      {/* Status Badge */}
      <Box textAlign="center" mt={4}>
        <Chip
          icon={<Shield />}
          label="Alert Active"
          color="error"
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
    </Container>
  );
}

export default DisasterAlert;
