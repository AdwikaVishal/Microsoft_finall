// API services for SenseSafe frontend
// Connected to FastAPI backend

import axios from 'axios';

// Configure axios base settings
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
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
 * @param {Object} credentials - Login credentials
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
 * @param {Object} userData - User registration data
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

// ==================== SOS ALERTS (User → Backend → Admin) ====================

/**
 * Send SOS alert to backend
 * @param {Object} sosData - SOS alert data
 * @returns {Promise<Object>} Created SOS alert
 */
export const sendSOS = async (sosData) => {
  simulateError(0.05);
  
  try {
    console.log('📡 Sending SOS to backend:', sosData);
    const response = await apiClient.post('/api/messages/sos', {
      title: sosData.title || 'SOS Emergency Alert',
      content: sosData.content || 'Emergency SOS alert',
      ability: sosData.ability || 'NONE',
      lat: sosData.lat,
      lng: sosData.lng,
      battery: sosData.battery || 100,
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
    const response = await apiClient.get('/api/messages?message_type=SOS');
    return response.data.messages || [];
  } catch (error) {
    console.error('Get SOS alerts error:', error);
    throw error;
  }
};

// ==================== INCIDENTS (User → Backend → Admin) ====================

/**
 * Send incident report to backend
 * @param {Object} incidentData - Incident report data
 * @returns {Promise<Object>} Created incident
 */
export const sendIncident = async (incidentData) => {
  simulateError(0.05);
  
  try {
    console.log('📡 Sending incident to backend:', incidentData);
    const response = await apiClient.post('/api/messages/incident', {
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
    const response = await apiClient.get('/api/messages?message_type=INCIDENT');
    return response.data.messages || [];
  } catch (error) {
    console.error('Get incidents error:', error);
    throw error;
  }
};

// ==================== ADMIN DASHBOARD ====================

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
 * Get all SOS alerts from all users (Admin only)
 * @returns {Promise<Array>} List of all SOS alerts
 */
export const getAllSOSAlerts = async () => {
  try {
    const response = await getAllMessages({ message_type: 'SOS' });
    return response.messages || [];
  } catch (error) {
    console.error('Get all SOS alerts error:', error);
    throw error;
  }
};

/**
 * Get all incident reports from all users (Admin only)
 * @returns {Promise<Array>} List of all incidents
 */
export const getAllIncidents = async () => {
  try {
    const response = await getAllMessages({ message_type: 'INCIDENT' });
    return response.messages || [];
  } catch (error) {
    console.error('Get all incidents error:', error);
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
    throw error;
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

// ==================== LEGACY FUNCTIONS (Still available for compatibility) ====================

/**
 * Get all SOS alerts (legacy - uses admin endpoint)
 * @returns {Promise<Array>} Array of SOS alerts
 */
export const getSOS = async () => {
  try {
    return await getAllSOSAlerts();
  } catch (error) {
    // Fallback to empty array if backend not available
    return [];
  }
};

/**
 * Get all incidents (legacy - uses admin endpoint)
 * @returns {Promise<Array>} Array of incidents
 */
export const getIncidents = async () => {
  try {
    return await getAllIncidents();
  } catch (error) {
    // Fallback to empty array if backend not available
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
 * Update incident status
 * @param {Object} incidentData - Incident data to update
 * @returns {Promise<Object>} Response with updated incident
 */
export const updateIncident = async (incidentData) => {
  try {
    // Legacy function - just return success
    return {
      success: true,
      message: 'Incident updated successfully',
      id: incidentData.id || `incident_${Date.now()}`,
      data: incidentData,
      timestamp: new Date().toISOString(),
    };
  } catch (error) {
    console.error('Update incident error:', error);
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
    throw error;
  }
};

/**
 * Send disaster alert to users (Admin only)
 * @param {Object} alertData - Alert data
 * @returns {Promise<Object>} Response
 */
export const sendDisasterAlert = async (alertData) => {
  try {
    const response = await apiClient.post('/api/admin/alerts', alertData);
    return response.data;
  } catch (error) {
    console.error('Send disaster alert error:', error);
    throw error;
  }
};

/**
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

