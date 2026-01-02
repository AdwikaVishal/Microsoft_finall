import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Button,
  Box,
  Card,
  CardContent,
  TextField,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  Alert,
  Snackbar,
  Chip,
  Grid,
} from '@mui/material';
import {
  Report,
  LocationOn,
  Note,
  Send,
  Home,
  Warning,
} from '@mui/icons-material';
import { sendIncident } from '../../../../services/api.js';

function IncidentReport({ abilityProfile }) {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    incidentType: '',
    location: '',
    notes: '',
    severity: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState(null);
  const [incidentId, setIncidentId] = useState(null);

  const incidentTypes = [
    { id: 'fire', label: 'Fire', description: 'Building or area on fire', icon: '🔥' },
    { id: 'flood', label: 'Flood', description: 'Water damage or rising water', icon: '🌊' },
    { id: 'earthquake', label: 'Earthquake', description: 'Seismic activity', icon: '🌍' },
    { id: 'medical', label: 'Medical Emergency', description: 'Someone needs medical help', icon: '🚑' },
    { id: 'trapped', label: 'Trapped', description: 'People trapped in area', icon: '🚪' },
    { id: 'structural', label: 'Structural Damage', description: 'Building collapse or damage', icon: '🏢' },
    { id: 'other', label: 'Other', description: 'Different type of incident', icon: '⚠️' },
  ];

  const severityLevels = [
    { id: 'low', label: 'Low', description: 'Minor incident, no immediate danger', color: '#4caf50' },
    { id: 'medium', label: 'Medium', description: 'Moderate incident, some risk', color: '#ff9800' },
    { id: 'high', label: 'High', description: 'Serious incident, immediate risk', color: '#ff5722' },
    { id: 'critical', label: 'Critical', description: 'Life-threatening emergency', color: '#f44336' },
  ];

  const handleInputChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const getLocation = () => {
    return new Promise((resolve) => {
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);

    try {
      // Get current location
      const location = await getLocation();
      
      const incidentData = {
        title: `Incident Report: ${formData.incidentType}`,
        content: formData.notes || `Reported ${formData.incidentType} incident`,
        category: formData.incidentType,
        severity: formData.severity,
        lat: location.lat,
        lng: location.lng,
      };

      console.log('📡 Sending incident report to backend:', incidentData);
      
      const response = await sendIncident(incidentData);
      
      console.log('✅ Incident sent successfully:', response);
      
      setIncidentId(response.data?.id || 'Unknown');
      setSubmitted(true);
      setSnackbarOpen(true);
    } catch (err) {
      console.error('❌ Error submitting incident:', err);
      setError(err.message || 'Failed to submit incident. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const getCurrentLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          handleInputChange('location', `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`);
        },
        (error) => {
          console.error('Error getting location:', error);
          handleInputChange('location', 'Location access denied');
        }
      );
    } else {
      handleInputChange('location', 'Geolocation not supported');
    }
  };

  if (submitted) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Box textAlign="center">
          <Box sx={{ fontSize: '4rem', mb: 2 }}>✅</Box>
          <Typography 
            variant="h1" 
            component="h1" 
            sx={{ 
              fontSize: { xs: '2rem', sm: '3rem' },
              color: '#4caf50',
              mb: 2 
            }}
          >
            Report Submitted
          </Typography>
          <Typography variant="h6" sx={{ mb: 4, color: 'text.secondary' }}>
            Your incident report has been sent to emergency responders and the admin dashboard.
          </Typography>
          
          {incidentId && (
            <Alert severity="info" sx={{ mb: 4, maxWidth: 400, mx: 'auto' }}>
              Incident ID: {incidentId}
            </Alert>
          )}
          
          <Card sx={{ mb: 4, borderLeft: '5px solid #4caf50', maxWidth: 500, mx: 'auto' }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                What Happens Next?
              </Typography>
              <Box component="ul" sx={{ textAlign: 'left', pl: 3 }}>
                <li><Typography variant="body1" sx={{ mb: 1 }}>Emergency responders have been notified</Typography></li>
                <li><Typography variant="body1" sx={{ mb: 1 }}>Your location has been shared with rescue teams</Typography></li>
                <li><Typography variant="body1" sx={{ mb: 1 }}>Help is being dispatched to your area</Typography></li>
                <li><Typography variant="body1" sx={{ mb: 1 }}>Stay in a safe location and await assistance</Typography></li>
              </Box>
            </CardContent>
          </Card>

          <Box display="flex" flexDirection="column" gap={2}>
            <Button
              variant="contained"
              size="large"
              startIcon={<Home />}
              onClick={() => navigate('/')}
              sx={{ minHeight: 60, fontSize: '1.2rem', maxWidth: 300, mx: 'auto' }}
            >
              Return to Home
            </Button>
          </Box>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header */}
      <Box textAlign="center" mb={4}>
        <Box display="flex" alignItems="center" justifyContent="center" mb={2}>
          <Report sx={{ fontSize: 48, color: '#1976d2', mr: 2 }} />
          <Typography 
            variant="h1" 
            component="h1" 
            sx={{ 
              fontSize: { xs: '2rem', sm: '3rem' },
              color: '#1976d2',
            }}
          >
            Incident Report
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
          Tell us what happened so we can send the right help
        </Typography>
        
        {/* Profile Badge */}
        <Chip
          label={`Tailored for ${abilityProfile.type} users`}
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

      {error && (
        <Alert severity="error" sx={{ mb: 4 }}>
          {error}
        </Alert>
      )}

      <form onSubmit={handleSubmit}>
        {/* Incident Type */}
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              What type of incident occurred?
            </Typography>
            <FormControl component="fieldset" fullWidth>
              <RadioGroup
                value={formData.incidentType}
                onChange={(e) => handleInputChange('incidentType', e.target.value)}
              >
                <Grid container spacing={2}>
                  {incidentTypes.map((type) => (
                    <Grid item xs={12} sm={6} key={type.id}>
                      <FormControlLabel
                        value={type.id}
                        control={<Radio />}
                        label={
                          <Box>
                            <Box display="flex" alignItems="center" mb={1}>
                              <Typography variant="h6" sx={{ mr: 1 }}>
                                {type.icon}
                              </Typography>
                              <Typography variant="subtitle1" fontWeight="bold">
                                {type.label}
                              </Typography>
                            </Box>
                            <Typography variant="body2" color="text.secondary">
                              {type.description}
                            </Typography>
                          </Box>
                        }
                        sx={{
                          display: 'flex',
                          alignItems: 'flex-start',
                          p: 2,
                          border: formData.incidentType === type.id ? '2px solid #1976d2' : '1px solid #e0e0e0',
                          borderRadius: 1,
                          width: '100%',
                          m: 0,
                          '&:hover': {
                            backgroundColor: '#f5f5f5',
                          },
                        }}
                      />
                    </Grid>
                  ))}
                </Grid>
              </RadioGroup>
            </FormControl>
          </CardContent>
        </Card>

        {/* Severity Level */}
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              How severe is the situation?
            </Typography>
            <FormControl component="fieldset" fullWidth>
              <RadioGroup
                value={formData.severity}
                onChange={(e) => handleInputChange('severity', e.target.value)}
              >
                <Box display="flex" flexDirection="column" gap={2}>
                  {severityLevels.map((level) => (
                    <FormControlLabel
                      key={level.id}
                      value={level.id}
                      control={<Radio />}
                      label={
                        <Box>
                          <Typography 
                            variant="subtitle1" 
                            fontWeight="bold"
                            sx={{ color: level.color }}
                          >
                            {level.label}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {level.description}
                          </Typography>
                        </Box>
                      }
                      sx={{
                        display: 'flex',
                        alignItems: 'flex-start',
                        p: 2,
                        border: formData.severity === level.id ? '2px solid #1976d2' : '1px solid #e0e0e0',
                        borderRadius: 1,
                        width: '100%',
                        m: 0,
                        '&:hover': {
                          backgroundColor: '#f5f5f5',
                        },
                      }}
                    />
                  ))}
                </Box>
              </RadioGroup>
            </FormControl>
          </CardContent>
        </Card>

        {/* Location */}
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Box display="flex" alignItems="center" mb={2}>
              <LocationOn sx={{ mr: 1, color: 'text.secondary' }} />
              <Typography variant="h6">
                Where are you located?
              </Typography>
            </Box>
            <TextField
              fullWidth
              multiline
              rows={2}
              placeholder="Enter your location or address. You can also use the button below to get your current location."
              value={formData.location}
              onChange={(e) => handleInputChange('location', e.target.value)}
              sx={{ mb: 2 }}
              inputProps={{
                'aria-label': 'Location information',
              }}
            />
            <Button
              variant="outlined"
              startIcon={<LocationOn />}
              onClick={getCurrentLocation}
              size="small"
            >
              Get Current Location
            </Button>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Your GPS coordinates will be sent with the report
            </Typography>
          </CardContent>
        </Card>

        {/* Additional Notes */}
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Box display="flex" alignItems="center" mb={2}>
              <Note sx={{ mr: 1, color: 'text.secondary' }} />
              <Typography variant="h6">
                Additional Information (Optional)
              </Typography>
            </Box>
            <TextField
              fullWidth
              multiline
              rows={4}
              placeholder="Any additional details that might help responders: number of people, specific hazards, accessibility needs, etc."
              value={formData.notes}
              onChange={(e) => handleInputChange('notes', e.target.value)}
              inputProps={{
                'aria-label': 'Additional notes and details',
              }}
            />
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Include any special needs or accessibility requirements
            </Typography>
          </CardContent>
        </Card>

        {/* Submit Button */}
        <Box textAlign="center" mb={4}>
          <Button
            type="submit"
            variant="contained"
            size="large"
            disabled={!formData.incidentType || !formData.severity || isSubmitting}
            startIcon={<Send />}
            sx={{
              minWidth: 250,
              minHeight: 60,
              fontSize: '1.2rem',
              fontWeight: 'bold',
            }}
            aria-label="Submit incident report"
          >
            {isSubmitting ? 'Sending...' : 'Send Report'}
          </Button>
        </Box>

        {/* Accessibility Notice */}
        {abilityProfile.type !== 'normal' && (
          <Alert severity="info" sx={{ mb: 4 }}>
            <Typography variant="body2">
              Your accessibility needs have been noted and will be shared with responders:
            </Typography>
            <Box display="flex" flexWrap="wrap" gap={1} mt={1}>
              <Chip size="small" label={`Profile: ${abilityProfile.type}`} />
              {abilityProfile.highContrast && <Chip size="small" label="High Contrast" />}
              {abilityProfile.largeText && <Chip size="small" label="Large Text" />}
            </Box>
          </Alert>
        )}
      </form>

      {/* Success Snackbar */}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={4000}
        onClose={() => setSnackbarOpen(false)}
        message="Incident report submitted successfully!"
        action={
          <Button color="inherit" size="small" onClick={() => setSnackbarOpen(false)}>
            OK
          </Button>
        }
      />
    </Container>
  );
}

export default IncidentReport;

