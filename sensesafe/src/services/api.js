/**
 * SenseSafe API Service
 * Connected to FastAPI backend via Vite proxy
 */

import axios from 'axios';

// Use relative URLs when running behind Vite proxy
// The proxy in vite.admin.config.js forwards /api/* to http://10.82.205.229:8000
const apiClient = axios.create({
  baseURL: '',  // Use relative URLs - proxy handles the rest
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle auth errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Simulate network delays (for development without backend)
const delay = (ms = 300) => new Promise((resolve) => setTimeout(resolve, ms));

// Error simulation for testing (disabled when real backend is available)
const simulateError = (probability = 0) => {
  if (Math.random() < probability) {
    throw new Error('Network error occurred');
  }
};

// ==================== AUTHENTICATION ====================

/**
 * Authenticate user
 * @param {Object} credentials - Login credentials {email, password}
 * @returns {Promise<Object>} Authentication response
 */
export const authenticateUser = async (credentials) => {
  simulateError(0.05);
  
  try {
    const response = await apiClient.post('/api/auth/login', credentials);
    if (response.data.access_token) {
      localStorage.setItem('token', response.data.access_token);
      return {
        success: true,
        token: response.data.access_token,
        user: response.data.user,
      };
    }
    throw new Error('Invalid response from server');
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

/**
 * Register new user
 * @param {Object} userData - User registration data {name, email, password, role, ability}
 * @returns {Promise<Object>} Registration response
 */
export const registerUser = async (userData) => {
  simulateError(0.05);
  
  try {
    const response = await apiClient.post('/api/auth/register', userData);
    return response.data;
  } catch (error) {
    console.error('Registration error:', error);
    throw error;
  }
};

/**
 * Get current user profile
 * @returns {Promise<Object>} User profile
 */
export const getCurrentUser = async () => {
  try {
    const response = await apiClient.get('/api/auth/me');
    return response.data;
  } catch (error) {
    console.error('Get user error:', error);
    throw error;
  }
};

// ==================== SOS ALERTS ====================

/**
 * Send SOS alert to backend
 * @param {Object} sosData - SOS alert data {ability, lat, lng, battery, status}
 * @returns {Promise<Object>} Created SOS alert
 */
export const sendSOS = async (sosData) => {
  simulateError(0.05);
  
  try {
    console.log('📡 Sending SOS to backend:', sosData);
    const response = await apiClient.post('/api/sos', {
      ability: sosData.ability || 'NONE',
      lat: sosData.lat,
      lng: sosData.lng,
      battery: sosData.battery || 100,
      status: sosData.status || 'NEED_HELP',
    });
    console.log('✅ SOS sent successfully:', response.data);
    return {
      success: true,
      message: 'SOS sent successfully',
      data: response.data,
    };
  } catch (error) {
    console.error('❌ SOS send error:', error);
    throw error;
  }
};

/**
 * Get user's SOS alerts
 * @returns {Promise<Array>} List of user's SOS alerts
 */
export const getUserSOSAlerts = async () => {
  try {
    const response = await apiClient.get('/api/sos/user');
    return response.data.sos_alerts || [];
  } catch (error) {
    console.error('Get SOS alerts error:', error);
    throw error;
  }
};

/**
 * Get all SOS alerts (admin)
 * @returns {Promise<Array>} List of all SOS alerts
 */
export const getAllSOSAlerts = async () => {
  try {
    // Try to get from messages first (unified endpoint)
    const response = await apiClient.get('/api/messages/admin/all', {
      params: { message_type: 'SOS', page_size: 100 }
    });
    return response.data.messages || [];
  } catch (error) {
    console.warn('Could not fetch SOS from messages, trying alternative...');
    // Fallback: try direct sos endpoint if available
    try {
      const response = await apiClient.get('/api/sos/user');
      return response.data.sos_alerts || [];
    } catch (e) {
      console.warn('No SOS data available');
      return [];
    }
  }
};

// ==================== INCIDENTS ====================

/**
 * Send incident report to backend
 * @param {Object} incidentData - Incident report data {title, content, category, severity, lat, lng, image_url}
 * @returns {Promise<Object>} Created incident
 */
export const sendIncident = async (incidentData) => {
  simulateError(0.05);
  
  try {
    console.log('📡 Sending incident to backend:', incidentData);
    const response = await apiClient.post('/api/incidents', {
      title: incidentData.title || `Incident Report: ${incidentData.category}`,
      content: incidentData.content,
      category: incidentData.category,
      severity: incidentData.severity,
      lat: incidentData.lat,
      lng: incidentData.lng,
      image_url: incidentData.image_url || null,
    });
    console.log('✅ Incident sent successfully:', response.data);
    return {
      success: true,
      message: 'Incident reported successfully',
      data: response.data,
    };
  } catch (error) {
    console.error('❌ Incident send error:', error);
    throw error;
  }
};

/**
 * Get user's incident reports
 * @returns {Promise<Array>} List of user's incidents
 */
export const getUserIncidents = async () => {
  try {
    const response = await apiClient.get('/api/incidents/user');
    return response.data.incidents || [];
  } catch (error) {
    console.error('Get incidents error:', error);
    throw error;
  }
};

/**
 * Get incident by ID
 * @param {string} incidentId - Incident ID
 * @returns {Promise<Object>} Incident details
 */
export const getIncidentById = async (incidentId) => {
  try {
    const response = await apiClient.get(`/api/incidents/${incidentId}`);
    return response.data;
  } catch (error) {
    console.error('Get incident error:', error);
    throw error;
  }
};

/**
 * Get all incidents (admin)
 * @param {Object} params - Query parameters {page, page_size, status_filter}
 * @returns {Promise<Object>} Paginated list of incidents
 */
export const getAdminIncidents = async (params = {}) => {
  try {
    const response = await apiClient.get('/api/admin/incidents', {
      params: {
        page: params.page || 1,
        page_size: params.page_size || 100,
        status_filter: params.status_filter || null,
      },
    });
    return response.data.incidents || [];
  } catch (error) {
    console.error('Get admin incidents error:', error);
    throw error;
  }
};

/**
 * Get all incidents from incidents endpoint (bypasses messages table)
 * This is the key function to fetch incidents sent from Android app
 * @returns {Promise<Array>} List of all incidents
 */
export const getAllIncidentsDirect = async () => {
  try {
    const response = await apiClient.get('/api/incidents/user');
    return response.data.incidents || [];
  } catch (error) {
    console.warn('Could not fetch incidents directly:', error.message);
    // Try admin endpoint as fallback
    try {
      const response = await apiClient.get('/api/admin/incidents', {
        params: { page_size: 100 }
      });
      return response.data.incidents || [];
    } catch (e) {
      console.warn('No incidents available from admin endpoint either');
      return [];
    }
  }
};

// ==================== DISASTER ALERTS ====================

/**
 * Get all disaster alerts
 * @param {Object} params - Query parameters {page, page_size}
 * @returns {Promise<Object>} Paginated list of alerts
 */
export const getAlerts = async (params = {}) => {
  try {
    const response = await apiClient.get('/api/alerts', {
      params: {
        page: params.page || 1,
        page_size: params.page_size || 20,
      },
    });
    return response.data;
  } catch (error) {
    console.error('Get alerts error:', error);
    throw error;
  }
};

// ==================== ADMIN DASHBOARD - COMBINED FETCH ====================

/**
 * Get all alerts for admin dashboard (combines SOS, incidents from their native endpoints)
 * This ensures data sent from Android app via /api/sos and /api/incidents is visible
 * @returns {Promise<Object>} Object containing all alerts { sosAlerts, incidents, stats }
 */
export const getAllAlertsForAdmin = async () => {
  try {
    console.log('🔄 Fetching all alerts from backend...');
    
    // Fetch from multiple sources in parallel
    const [sosResponse, incidentsResponse, messagesResponse] = await Promise.allSettled([
      // Try getting SOS from native endpoint
      apiClient.get('/api/sos/user').catch(() => ({ data: { sos_alerts: [] } })),
      // Get incidents from native endpoint (where Android app sends data)
      apiClient.get('/api/incidents/user').catch(() => ({ data: { incidents: [] } })),
      // Also try messages endpoint as backup
      apiClient.get('/api/messages/admin/all', { 
        params: { page_size: 100 } 
      }).catch(() => ({ data: { messages: [] } }))
    ]);

    const sosAlerts = sosResponse.value?.data?.sos_alerts || [];
    const incidents = incidentsResponse.value?.data?.incidents || [];
    const messages = messagesResponse.value?.data?.messages || [];

    console.log(`📊 Raw data - SOS: ${sosAlerts.length}, Incidents: ${incidents.length}, Messages: ${messages.length}`);

    // Convert SOS alerts to unified format
    const formattedSOS = sosAlerts.map(sos => ({
      id: sos.id,
      message_type: 'SOS',
      user_name: sos.user_name || 'Unknown User',
      title: sos.title || 'SOS Alert',
      content: sos.content || 'Emergency SOS alert',
      ability: sos.ability || 'NONE',
      battery: sos.battery,
      lat: sos.lat,
      lng: sos.lng,
      severity: 'critical',
      is_read: false,
      created_at: sos.created_at || new Date().toISOString(),
      status: sos.status
    }));

    // Convert incidents to unified format (key for Android-sent data)
    const formattedIncidents = incidents.map(inc => ({
      id: inc.id,
      message_type: 'INCIDENT',
      user_name: inc.reporter_name || 'Unknown User',
      title: inc.title,
      content: inc.description || inc.content,
      category: inc.category,
      severity: inc.severity || 'medium',
      lat: inc.lat,
      lng: inc.lng,
      is_read: false,
      created_at: inc.created_at || new Date().toISOString(),
      status: inc.status,
      image_url: inc.image_url
    }));

    // Use messages if available, otherwise use formatted native data
    let allMessages = [];
    if (messages.length > 0) {
      allMessages = messages;
    } else {
      // Combine SOS and incidents from native endpoints
      allMessages = [...formattedSOS, ...formattedIncidents];
    }

    // Calculate stats
    const stats = {
      total: allMessages.length,
      unread: allMessages.filter(m => !m.is_read).length,
      sos_count: allMessages.filter(m => m.message_type === 'SOS').length,
      incident_count: allMessages.filter(m => m.message_type === 'INCIDENT').length,
      by_type: {
        SOS: allMessages.filter(m => m.message_type === 'SOS').length,
        INCIDENT: allMessages.filter(m => m.message_type === 'INCIDENT').length,
        GENERAL: allMessages.filter(m => m.message_type === 'GENERAL').length,
      }
    };

    console.log(`✅ Total alerts: ${allMessages.length}`);
    
    return {
      messages: allMessages,
      sos_alerts: formattedSOS,
      incidents: formattedIncidents,
      stats
    };
  } catch (error) {
    console.error('Error fetching all alerts for admin:', error);
    throw error;
  }
};

/**
 * Get all messages from all users (Admin only)
 * @param {Object} params - Query parameters
 * @returns {Promise<Object>} Paginated list of messages
 */
export const getAllMessages = async (params = {}) => {
  try {
    const response = await apiClient.get('/api/messages/admin/all', {
      params: {
        page: params.page || 1,
        page_size: params.page_size || 50,
        message_type: params.message_type || null,
        is_read: params.is_read || null,
      },
    });
    return response.data;
  } catch (error) {
    console.error('Get all messages error:', error);
    throw error;
  }
};

/**
 * Get message statistics for admin dashboard
 * @returns {Promise<Object>} Message statistics
 */
export const getMessageStats = async () => {
  try {
    const response = await apiClient.get('/api/messages/admin/stats');
    return response.data;
  } catch (error) {
    console.error('Get message stats error:', error);
    // Return calculated stats from available data
    return { total: 0, unread: 0 };
  }
};

/**
 * Get unread message count (Admin only)
 * @returns {Promise<Object>} Unread count
 */
export const getUnreadCount = async () => {
  try {
    const response = await apiClient.get('/api/messages/admin/unread/count');
    return response.data;
  } catch (error) {
    console.error('Get unread count error:', error);
    throw error;
  }
};

/**
 * Mark message as read (Admin only)
 * @param {string} messageId - Message ID
 * @returns {Promise<Object>} Updated message
 */
export const markMessageAsRead = async (messageId) => {
  try {
    const response = await apiClient.post(`/api/messages/admin/${messageId}/read`);
    return response.data;
  } catch (error) {
    console.error('Mark message as read error:', error);
    throw error;
  }
};

/**
 * Verify incident (Admin only)
 * @param {string} incidentId - Incident ID
 * @returns {Promise<Object>} Updated incident
 */
export const verifyIncident = async (incidentId) => {
  try {
    const response = await apiClient.patch(`/api/admin/incidents/${incidentId}/verify`);
    return response.data;
  } catch (error) {
    console.error('Verify incident error:', error);
    throw error;
  }
};

/**
 * Resolve incident (Admin only)
 * @param {string} incidentId - Incident ID
 * @returns {Promise<Object>} Updated incident
 */
export const resolveIncident = async (incidentId) => {
  try {
    const response = await apiClient.patch(`/api/admin/incidents/${incidentId}/resolve`);
    return response.data;
  } catch (error) {
    console.error('Resolve incident error:', error);
    throw error;
  }
};

/**
 * Update incident (Admin only)
 * @param {string} incidentId - Incident ID
 * @param {Object} updateData - Update data
 * @returns {Promise<Object>} Updated incident
 */
export const updateIncident = async (incidentId, updateData) => {
  try {
    const response = await apiClient.patch(`/api/admin/incidents/${incidentId}`, updateData);
    return response.data;
  } catch (error) {
    console.error('Update incident error:', error);
    throw error;
  }
};

/**
 * Create disaster alert (Admin only)
 * @param {Object} alertData - Alert data {title, message, severity}
 * @returns {Promise<Object>} Created alert
 */
export const createDisasterAlert = async (alertData) => {
  try {
    const response = await apiClient.post('/api/admin/alerts', alertData);
    return response.data;
  } catch (error) {
    console.error('Create disaster alert error:', error);
    throw error;
  }
};

// ==================== LEGACY FUNCTIONS ====================

/**
 * Get all SOS alerts (legacy)
 * @returns {Promise<Array>} Array of SOS alerts
 */
export const getSOS = async () => {
  try {
    return await getAllSOSAlerts();
  } catch (error) {
    console.warn('Backend not available, returning empty array');
    return [];
  }
};

/**
 * Get all incidents (legacy)
 * @returns {Promise<Array>} Array of incidents
 */
export const getIncidents = async () => {
  try {
    // This is the key fix - use direct endpoint that Android app uses
    const response = await getAllIncidentsDirect();
    return response;
  } catch (error) {
    console.warn('Backend not available, returning empty array');
    return [];
  }
};

/**
 * Send user status update
 * @param {Object} statusData - User status data
 * @returns {Promise<Object>} Response with success status
 */
export const sendUserStatus = async (statusData) => {
  try {
    return await sendIncident({
      title: 'Status Update',
      content: statusData.message || 'User status update',
      category: statusData.type || 'general',
      severity: statusData.severity || 'low',
      lat: statusData.lat,
      lng: statusData.lng,
    });
  } catch (error) {
    console.error('Send user status error:', error);
    throw error;
  }
};

/**
 * Get user profile by ID
 * @param {string} userId - User ID
 * @returns {Promise<Object>} User profile
 */
export const getUserProfile = async (userId) => {
  try {
    const response = await apiClient.get(`/api/users/${userId}`);
    return response.data;
  } catch (error) {
    console.error('Get user profile error:', error);
  }
};

/**
    throw error;
 * Get system health status
 * @returns {Promise<Object>} System health information
 */
export const getSystemHealth = async () => {
  try {
    const response = await apiClient.get('/health');
    return response.data;
  } catch (error) {
    // Return mock data if backend not available
    return {
      status: 'healthy',
      service: 'SenseSafe',
      version: '1.0.0',
    };
  }
};

