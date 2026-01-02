import React, { useState } from 'react';
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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
} from '@mui/material';
import {
  Home as HomeIcon,
  Warning,
  Report,
  Help,
  Shield,
  Send,
} from '@mui/icons-material';
import { sendSOS, sendIncident } from '../../../../services/api.js';

function Home({ abilityProfile }) {
  const navigate = useNavigate();

  // SOS Dialog State
  const [sosOpen, setSosOpen] = useState(false);
  const [sosData, setSosData] = useState({
    content: 'Emergency SOS - I need immediate help',
    ability: abilityProfile.type?.toUpperCase() || 'NONE',
    battery: 100,
  });
  const [sosSending, setSosSending] = useState(false);
  const [sosSuccess, setSosSuccess] = useState(false);
  const [sosError, setSosError] = useState(null);

  // Get location helper
  const getLocation = () => {
    return new Promise((resolve, reject) => {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            resolve({
              lat: position.coords.latitude,
              lng: position.coords.longitude,
            });
          },
          (error) => {
            console.error('Location error:', error);
            // Return default location
            resolve({ lat: 40.7128, lng: -74.0060 });
          }
        );
      } else {
        resolve({ lat: 40.7128, lng: -74.0060 });
      }
    });
  };

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  };

  const getProfileColor = (profileType) => {
    const colors = {
      blind: '#9c27b0',
      deaf: '#2196f3',
      nonverbal: '#ff9800',
      elderly: '#795548',
      normal: '#4caf50',
    };
    return colors[profileType] || '#4caf50';
  };

  const handleSOS = async () => {
    setSosSending(true);
    setSosError(null);
    
    try {
      const location = await getLocation();
      
      console.log('🚨 Sending SOS alert...');
      const response = await sendSOS({
        title: 'SOS Emergency Alert',
        content: sosData.content,
        ability: sosData.ability,
        lat: location.lat,
        lng: location.lng,
        battery: sosData.battery,
      });

      console.log('✅ SOS sent successfully:', response);
      setSosSuccess(true);
      
      // Close dialog after success
      setTimeout(() => {
        setSosOpen(false);
        setSosSuccess(false);
        setSosData({
          content: 'Emergency SOS - I need immediate help',
          ability: abilityProfile.type?.toUpperCase() || 'NONE',
          battery: 100,
        });
      }, 2000);
      
    } catch (error) {
      console.error('❌ SOS send error:', error);
      setSosError(error.message || 'Failed to send SOS. Please try again.');
    } finally {
      setSosSending(false);
    }
  };

  const quickActions = [
    {
      title: 'Report Incident',
      description: 'Report what happened during the disaster',
      icon: <Report sx={{ fontSize: 40 }} />,
      color: '#f44336',
      action: () => navigate('/report'),
    },
    {
      title: 'Get Guidance',
      description: 'Get personalized help and instructions',
      icon: <Help sx={{ fontSize: 40 }} />,
      color: '#2196f3',
      action: () => navigate('/guidance'),
    },
    {
      title: 'Emergency SOS',
      description: 'Send emergency alert for immediate help',
      icon: <Warning sx={{ fontSize: 40 }} />,
      color: '#ff5722',
      action: () => setSosOpen(true),
    },
  ];

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header */}
      <Box textAlign="center" mb={4}>
        <Box display="flex" alignItems="center" justifyContent="center" mb={2}>
          <HomeIcon sx={{ fontSize: 48, color: '#1976d2', mr: 2 }} />
          <Typography 
            variant="h1" 
            component="h1" 
            sx={{ 
              fontSize: { xs: '2rem', sm: '3rem' },
              color: '#1976d2',
            }}
          >
            SenseSafe
          </Typography>
        </Box>
        <Typography 
          variant="h5" 
          component="p" 
          sx={{ 
            fontSize: { xs: '1.2rem', sm: '1.5rem' },
            color: 'text.secondary',
            mb: 2 
          }}
        >
          {getGreeting()}, {abilityProfile.type} user
        </Typography>
        
        {/* Profile Badge */}
        <Chip
          label={`Profile: ${abilityProfile.type.charAt(0).toUpperCase() + abilityProfile.type.slice(1)}`}
          sx={{
            backgroundColor: getProfileColor(abilityProfile.type),
            color: 'white',
            fontSize: '1rem',
            px: 2,
            py: 3,
            height: 'auto',
            '& .MuiChip-label': {
              padding: '8px 16px',
            },
          }}
        />
      </Box>

      {/* Status Card */}
      <Card sx={{ mb: 4, borderLeft: '5px solid #4caf50' }}>
        <CardContent>
          <Box display="flex" alignItems="center" mb={2}>
            <Shield sx={{ fontSize: 32, color: '#4caf50', mr: 2 }} />
            <Typography variant="h6" component="h2">
              System Status
            </Typography>
          </Box>
          <Typography variant="body1" sx={{ mb: 2 }}>
            ✅ All systems operational
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Last updated: {new Date().toLocaleString()}
          </Typography>
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <Typography variant="h5" component="h2" gutterBottom sx={{ mb: 3 }}>
        Quick Actions
      </Typography>
      
      <Grid container spacing={3}>
        {quickActions.map((action, index) => (
          <Grid item xs={12} sm={6} key={index}>
            <Card 
              sx={{ 
                cursor: 'pointer',
                transition: 'all 0.3s ease',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 4,
                },
                height: '100%',
              }}
              onClick={action.action}
            >
              <CardContent sx={{ textAlign: 'center', py: 4 }}>
                <Box sx={{ color: action.color, mb: 2 }}>
                  {action.icon}
                </Box>
                <Typography 
                  variant="h6" 
                  component="h3" 
                  gutterBottom
                  sx={{ fontSize: '1.3rem' }}
                >
                  {action.title}
                </Typography>
                <Typography 
                  variant="body2" 
                  color="text.secondary"
                  sx={{ fontSize: '1rem' }}
                >
                  {action.description}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Accessibility Features Reminder */}
      {abilityProfile.type !== 'normal' && (
        <Card sx={{ mt: 4, backgroundColor: '#e3f2fd' }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Accessibility Features Active
            </Typography>
            <Box display="flex" flexWrap="wrap" gap={1}>
              {abilityProfile.highContrast && (
                <Chip label="High Contrast" color="primary" />
              )}
              {abilityProfile.largeText && (
                <Chip label="Large Text" color="primary" />
              )}
              <Chip label={`${abilityProfile.type.charAt(0).toUpperCase() + abilityProfile.type.slice(1)} Mode`} color="secondary" />
            </Box>
          </CardContent>
        </Card>
      )}

      {/* Emergency Contact Info */}
      <Card sx={{ mt: 4, borderLeft: '5px solid #ff9800' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Emergency Contacts
          </Typography>
          <Typography variant="body1" sx={{ mb: 1 }}>
            🚨 Emergency: 911
          </Typography>
          <Typography variant="body1" sx={{ mb: 1 }}>
            📞 SenseSafe Support: 1-800-SENSE-SAFE
          </Typography>
          <Typography variant="body2" color="text.secondary">
            These numbers are always available, even when the app is offline.
          </Typography>
        </CardContent>
      </Card>

      {/* SOS Dialog */}
      <Dialog 
        open={sosOpen} 
        onClose={() => !sosSending && setSosOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle sx={{ bgcolor: '#ff5722', color: 'white' }}>
          <Warning sx={{ mr: 1, verticalAlign: 'middle' }} />
          Emergency SOS Alert
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          {sosSuccess ? (
            <Alert severity="success" sx={{ mt: 2 }}>
              ✅ SOS sent successfully! Emergency responders have been notified.
            </Alert>
          ) : (
            <>
              {sosError && (
                <Alert severity="error" sx={{ mb: 2 }}>
                  {sosError}
                </Alert>
              )}
              
              <Alert severity="warning" sx={{ mb: 2 }}>
                This will send an emergency SOS alert to all administrators and emergency responders.
              </Alert>
              
              <TextField
                fullWidth
                multiline
                rows={3}
                label="SOS Message"
                value={sosData.content}
                onChange={(e) => setSosData({ ...sosData, content: e.target.value })}
                placeholder="Describe your emergency situation..."
                sx={{ mb: 2 }}
              />
              
              <FormControl fullWidth sx={{ mb: 2 }}>
                <InputLabel>Accessibility Need</InputLabel>
                <Select
                  value={sosData.ability}
                  label="Accessibility Need"
                  onChange={(e) => setSosData({ ...sosData, ability: e.target.value })}
                >
                  <MenuItem value="NONE">None</MenuItem>
                  <MenuItem value="BLIND">Blind</MenuItem>
                  <MenuItem value="DEAF">Deaf</MenuItem>
                  <MenuItem value="NON_VERBAL">Non-Verbal</MenuItem>
                  <MenuItem value="ELDERLY">Elderly</MenuItem>
                  <MenuItem value="OTHER">Other</MenuItem>
                </Select>
              </FormControl>
              
              <TextField
                fullWidth
                type="number"
                label="Battery Level (%)"
                value={sosData.battery}
                onChange={(e) => setSosData({ ...sosData, battery: parseInt(e.target.value) || 0 })}
                inputProps={{ min: 0, max: 100 }}
                sx={{ mb: 2 }}
              />
            </>
          )}
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button 
            onClick={() => setSosOpen(false)}
            disabled={sosSending}
          >
            Cancel
          </Button>
          <Button 
            variant="contained"
            color="error"
            onClick={handleSOS}
            disabled={sosSending || sosSuccess}
            startIcon={<Send />}
            sx={{ bgcolor: '#ff5722', '&:hover': { bgcolor: '#e64a19' } }}
          >
            {sosSending ? 'Sending...' : 'Send SOS'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
}

export default Home;

