// Mock data for SenseSafe frontend development
// This data simulates what would come from Azure services in production

export const mockData = {
  sosAlerts: [
    {
      id: 'sos_001',
      userName: 'Alice Cooper',
      userId: 'user_001',
      alertType: 'SOS',
      userCategory: 'Blind',
      isVulnerable: true,
      timestamp: '2024-01-15T10:30:00Z',
      status: 'Active',
      location: {
        lat: 40.7128,
        lng: -74.0060,
        address: '123 Main St, New York, NY 10001',
      },
      description: 'Emergency SOS triggered by voice command. User trapped in building collapse.',
      batteryLevel: 23,
      riskScore: 85,
      deviceInfo: {
        type: 'Mobile',
        model: 'iPhone 14',
        os: 'iOS 17.1',
      },
      response: {
        dispatched: true,
        eta: '8 minutes',
        responder: 'Unit Alpha-3',
      }
    },
    {
      id: 'sos_002',
      userName: 'David Miller',
      userId: 'user_002',
      alertType: 'Injured',
      userCategory: 'Deaf',
      isVulnerable: false,
      timestamp: '2024-01-15T09:45:00Z',
      status: 'Pending',
      location: {
        lat: 40.7589,
        lng: -73.9851,
        address: '456 Broadway, New York, NY 10013',
      },
      description: 'User reported injury during earthquake. Cannot hear emergency sirens.',
      batteryLevel: 67,
      riskScore: 72,
      deviceInfo: {
        type: 'Mobile',
        model: 'Samsung Galaxy S23',
        os: 'Android 14',
      },
      response: {
        dispatched: false,
        eta: null,
        responder: null,
      }
    },
    {
      id: 'sos_003',
      userName: 'Sophie Chen',
      userId: 'user_003',
      alertType: 'Trapped',
      userCategory: 'Elderly',
      isVulnerable: true,
      timestamp: '2024-01-15T08:15:00Z',
      status: 'Resolved',
      location: {
        lat: 40.7505,
        lng: -73.9934,
        address: '789 5th Ave, New York, NY 10022',
      },
      description: 'Elderly user trapped in elevator during power outage.',
      batteryLevel: 45,
      riskScore: 91,
      deviceInfo: {
        type: 'Tablet',
        model: 'iPad Air',
        os: 'iPadOS 17.1',
      },
      response: {
        dispatched: true,
        eta: '5 minutes',
        responder: 'Unit Bravo-1',
        resolvedAt: '2024-01-15T08:45:00Z',
      }
    },
    {
      id: 'sos_004',
      userName: 'Marcus Thorne',
      userId: 'user_004',
      alertType: 'Safe',
      userCategory: 'Normal',
      isVulnerable: false,
      timestamp: '2024-01-15T07:30:00Z',
      status: 'Resolved',
      location: {
        lat: 40.7282,
        lng: -73.9942,
        address: '321 West St, New York, NY 10014',
      },
      description: 'User confirmed safe after evacuating to designated shelter.',
      batteryLevel: 89,
      riskScore: 15,
      deviceInfo: {
        type: 'Mobile',
        model: 'Google Pixel 8',
        os: 'Android 14',
      },
      response: {
        dispatched: false,
        eta: null,
        responder: null,
        resolvedAt: '2024-01-15T07:35:00Z',
      }
    },
    {
      id: 'sos_005',
      userName: 'Isabella Rodriguez',
      userId: 'user_005',
      alertType: 'Medical',
      userCategory: 'Non-verbal',
      isVulnerable: true,
      timestamp: '2024-01-15T11:00:00Z',
      status: 'Active',
      location: {
        lat: 40.7614,
        lng: -73.9776,
        address: '654 Lexington Ave, New York, NY 10065',
      },
      description: 'Medical emergency triggered by one-tap button. User cannot speak.',
      batteryLevel: 34,
      riskScore: 78,
      deviceInfo: {
        type: 'Mobile',
        model: 'iPhone 13',
        os: 'iOS 17.1',
      },
      response: {
        dispatched: true,
        eta: '12 minutes',
        responder: 'Unit Charlie-2',
      }
    },
  ],

  incidents: [
    {
      id: 'incident_001',
      title: 'Building Collapse - Midtown',
      type: 'structural',
      severity: 'High',
      reporter: 'Alice Cooper',
      reporterId: 'user_001',
      status: 'Active',
      timestamp: '2024-01-15T10:30:00Z',
      location: {
        lat: 40.7128,
        lng: -74.0060,
        address: '123 Main St, New York, NY 10001',
      },
      description: 'Partial building collapse reported. Multiple people trapped. Elderly and disabled residents need immediate assistance.',
      affectedPeople: 12,
      vulnerableCount: 4,
      resources: ['Fire Department', 'Medical Team', 'Rescue Squad'],
      updates: [
        {
          timestamp: '2024-01-15T10:45:00Z',
          message: 'Fire department on scene. Structural assessment ongoing.',
          author: 'Unit Alpha-3',
        },
        {
          timestamp: '2024-01-15T11:00:00Z',
          message: 'First victim rescued. Searching for additional survivors.',
          author: 'Rescue Coordinator',
        },
      ],
    },
    {
      id: 'incident_002',
      title: 'Earthquake Damage - Downtown',
      type: 'earthquake',
      severity: 'Critical',
      reporter: 'David Miller',
      reporterId: 'user_002',
      status: 'Active',
      timestamp: '2024-01-15T09:45:00Z',
      location: {
        lat: 40.7589,
        lng: -73.9851,
        address: '456 Broadway, New York, NY 10013',
      },
      description: 'Magnitude 6.2 earthquake. Several buildings damaged. Deaf community needs visual emergency alerts.',
      affectedPeople: 45,
      vulnerableCount: 8,
      resources: ['Emergency Management', 'Medical Teams', 'Search & Rescue', 'Deaf Services'],
      updates: [
        {
          timestamp: '2024-01-15T10:00:00Z',
          message: 'Aftershock detected. Evacuations in progress.',
          author: 'Emergency Management',
        },
        {
          timestamp: '2024-01-15T10:30:00Z',
          message: 'Shelter opened at Community Center. ASL interpreters on site.',
          author: 'Deaf Services Coordinator',
        },
      ],
    },
    {
      id: 'incident_003',
      title: 'Flooding - Lower East Side',
      type: 'flood',
      severity: 'Medium',
      reporter: 'System Alert',
      reporterId: 'system',
      status: 'Monitoring',
      timestamp: '2024-01-15T08:00:00Z',
      location: {
        lat: 40.7155,
        lng: -73.9860,
        address: 'Lower East Side, New York, NY',
      },
      description: 'Heavy rainfall causing street flooding. No immediate danger reported.',
      affectedPeople: 150,
      vulnerableCount: 12,
      resources: ['Public Works', 'Emergency Management'],
      updates: [
        {
          timestamp: '2024-01-15T08:30:00Z',
          message: 'Drainage systems working. Water levels receding.',
          author: 'Public Works',
        },
      ],
    },
  ],

  users: [
    {
      id: 'user_001',
      name: 'Alice Cooper',
      email: 'alice.cooper@email.com',
      category: 'Blind',
      profile: {
        type: 'blind',
        highContrast: true,
        largeText: true,
        registeredAt: '2024-01-10T14:00:00Z',
        emergencyContacts: [
          { name: 'John Cooper', relationship: 'Son', phone: '+1-555-0123' },
          { name: 'Support Worker', relationship: 'Caregiver', phone: '+1-555-0124' },
        ],
      },
      deviceInfo: {
        model: 'iPhone 14',
        os: 'iOS 17.1',
        accessibility: ['VoiceOver', 'Voice Control', 'Zoom'],
      },
      location: {
        lat: 40.7128,
        lng: -74.0060,
        lastUpdated: '2024-01-15T10:35:00Z',
      },
      status: 'Active SOS',
      batteryLevel: 23,
    },
    {
      id: 'user_002',
      name: 'David Miller',
      email: 'david.miller@email.com',
      category: 'Deaf',
      profile: {
        type: 'deaf',
        highContrast: false,
        largeText: true,
        registeredAt: '2024-01-12T09:30:00Z',
        emergencyContacts: [
          { name: 'Sarah Miller', relationship: 'Wife', phone: '+1-555-0125' },
          { name: 'ASL Interpreter', relationship: 'Support', phone: '+1-555-0126' },
        ],
      },
      deviceInfo: {
        model: 'Samsung Galaxy S23',
        os: 'Android 14',
        accessibility: ['Live Transcribe', 'Sound Notifications', 'Vibration Patterns'],
      },
      location: {
        lat: 40.7589,
        lng: -73.9851,
        lastUpdated: '2024-01-15T09:50:00Z',
      },
      status: 'Needs Medical Help',
      batteryLevel: 67,
    },
    {
      id: 'user_003',
      name: 'Sophie Chen',
      email: 'sophie.chen@email.com',
      category: 'Elderly',
      profile: {
        type: 'elderly',
        highContrast: true,
        largeText: true,
        registeredAt: '2024-01-08T16:45:00Z',
        emergencyContacts: [
          { name: 'Robert Chen', relationship: 'Son', phone: '+1-555-0127' },
          { name: 'Home Care', relationship: 'Caregiver', phone: '+1-555-0128' },
        ],
      },
      deviceInfo: {
        model: 'iPad Air',
        os: 'iPadOS 17.1',
        accessibility: ['Larger Text', 'High Contrast', 'Simplified Interface'],
      },
      location: {
        lat: 40.7505,
        lng: -73.9934,
        lastUpdated: '2024-01-15T08:20:00Z',
      },
      status: 'Safe - Resolved',
      batteryLevel: 45,
    },
  ],

  disasterAlerts: [
    {
      id: 'alert_001',
      title: 'Severe Thunderstorm Warning',
      level: 'High',
      message: 'Severe thunderstorm warning in your area. Seek shelter immediately.',
      location: 'New York City Metro Area',
      timestamp: '2024-01-15T10:00:00Z',
      expiresAt: '2024-01-15T14:00:00Z',
      instructions: [
        'Move to the lowest floor of your building',
        'Stay away from windows and outside walls',
        'If you are outdoors, move to a sturdy building immediately',
        'Do not use elevators',
        'Keep your phone charged and stay connected',
      ],
      affectedAreas: ['Manhattan', 'Brooklyn', 'Queens'],
      alertType: 'weather',
    },
    {
      id: 'alert_002',
      title: 'Earthquake Aftershock Warning',
      level: 'Critical',
      message: 'Strong aftershock possible within the next hour. Stay away from damaged structures.',
      location: 'Downtown Manhattan',
      timestamp: '2024-01-15T09:50:00Z',
      expiresAt: '2024-01-15T11:00:00Z',
      instructions: [
        'Remain outside damaged buildings',
        'Stay away from power lines and trees',
        'Be prepared for additional aftershocks',
        'Check on neighbors, especially elderly and disabled',
        'Do not re-enter damaged structures',
      ],
      affectedAreas: ['Financial District', 'SoHo', 'Lower East Side'],
      alertType: 'earthquake',
    },
  ],

  systemHealth: {
    overall: 'Operational',
    services: [
      {
        name: 'Azure Functions',
        status: 'Operational',
        uptime: '99.9%',
        responseTime: '150ms',
      },
      {
        name: 'Cosmos DB',
        status: 'Operational',
        uptime: '99.8%',
        responseTime: '45ms',
      },
      {
        name: 'Notification Hub',
        status: 'Operational',
        uptime: '99.7%',
        responseTime: '200ms',
      },
      {
        name: 'Azure Maps',
        status: 'Operational',
        uptime: '99.9%',
        responseTime: '180ms',
      },
      {
        name: 'Speech Services',
        status: 'Degraded',
        uptime: '95.2%',
        responseTime: '800ms',
      },
      {
        name: 'Machine Learning',
        status: 'Operational',
        uptime: '99.6%',
        responseTime: '1200ms',
      },
    ],
    timestamp: '2024-01-15T11:15:00Z',
    lastIncident: '2024-01-14T16:30:00Z',
  },
};

// Helper functions for generating dynamic mock data
export const generateRandomAlert = () => {
  const names = ['Alice Cooper', 'David Miller', 'Sophie Chen', 'Marcus Thorne', 'Isabella Rodriguez'];
  const types = ['SOS', 'Injured', 'Trapped', 'Safe', 'Medical'];
  const categories = ['Blind', 'Deaf', 'Non-verbal', 'Elderly', 'Normal'];
  
  return {
    id: `sos_${Date.now()}`,
    userName: names[Math.floor(Math.random() * names.length)],
    alertType: types[Math.floor(Math.random() * types.length)],
    userCategory: categories[Math.floor(Math.random() * categories.length)],
    isVulnerable: Math.random() > 0.5,
    timestamp: new Date().toISOString(),
    status: 'Pending',
    description: `Emergency alert triggered at ${new Date().toLocaleTimeString()}`,
    batteryLevel: Math.floor(Math.random() * 100),
    riskScore: Math.floor(Math.random() * 100),
  };
};

export const updateAlertStatus = (alerts) => {
  return alerts.map(alert => {
    if (alert.status === 'Pending' && Math.random() > 0.7) {
      return { ...alert, status: 'Active' };
    } else if (alert.status === 'Active' && Math.random() > 0.8) {
      return { ...alert, status: 'Resolved' };
    }
    return alert;
  });
};
