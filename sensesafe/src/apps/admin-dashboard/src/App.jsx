import React, { useState, useEffect, useCallback } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Dashboard from './pages/Dashboard';
import Alerts from './pages/Alerts';
import AlertDetail from './pages/AlertDetail';
import Messages from './pages/Messages';
import Login from './pages/Login';
import Users from './pages/Users';
import Analytics from './pages/Analytics';
import Settings from './pages/Settings';
import { getAllAlertsForAdmin, getMessageStats, getSystemHealth } from '../../../services/api.js';

function App() {
  const [alerts, setAlerts] = useState([]);
  const [messages, setMessages] = useState([]);
  const [stats, setStats] = useState({
    total: 0,
    unread: 0,
    by_type: { SOS: 0, INCIDENT: 0, GENERAL: 0 }
  });
  const [newAlertId, setNewAlertId] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoggedIn, setIsLoggedIn] = useState(() => {
    return !!localStorage.getItem('token');
  });

  // Fetch real data from backend - includes SOS and incidents from their native endpoints
  // This ensures data sent from Android app via /api/sos and /api/incidents is visible
  const fetchData = useCallback(async () => {
    try {
      console.log('🔄 Fetching alerts from backend...');
      
      // Fetch from combined endpoint (fetches from /api/sos/user, /api/incidents/user, and /api/messages/admin/all)
      const data = await getAllAlertsForAdmin();
      
      const messagesList = data.messages || [];
      setMessages(messagesList);

      // Convert messages to alerts format for Dashboard/Alerts pages
      const alertsList = messagesList.map(msg => ({
        id: msg.id,
        userName: msg.user_name || 'Unknown User',
        alertType: msg.message_type === 'SOS' ? 'SOS Alert' : msg.message_type === 'INCIDENT' ? 'Incident' : 'Message',
        userCategory: msg.ability || msg.category || 'Normal',
        isVulnerable: msg.ability && msg.ability !== 'NONE',
        timestamp: msg.created_at,
        status: msg.is_read ? 'Resolved' : 'Active',
        description: msg.content || msg.title,
        riskScore: msg.severity === 'critical' ? 95 : msg.severity === 'high' ? 75 : msg.severity === 'medium' ? 50 : 25,
        location: msg.lat && msg.lng ? `${msg.lat.toFixed(4)}, ${msg.lng.toFixed(4)}` : null,
        category: msg.category,
        severity: msg.severity,
        ability: msg.ability,
        battery: msg.battery,
      }));
      
      setAlerts(alertsList);
      console.log(`✅ Loaded ${alertsList.length} alerts from backend`);

      // Use stats from combined data
      setStats({
        total: data.stats.total || alertsList.length,
        unread: data.stats.unread || alertsList.filter(m => !m.is_read).length,
        by_type: data.stats.by_type || {
          SOS: alertsList.filter(m => m.alertType === 'SOS Alert').length,
          INCIDENT: alertsList.filter(m => m.alertType === 'Incident').length,
          GENERAL: alertsList.filter(m => m.alertType === 'Message').length,
        }
      });

    } catch (error) {
      console.error('❌ Error fetching data from backend:', error);
      // Keep using empty data if backend is not available
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Initial data fetch
  useEffect(() => {
    if (isLoggedIn) {
      fetchData();
    }
  }, [isLoggedIn, fetchData]);

  // Refresh data periodically
  useEffect(() => {
    if (!isLoggedIn) return;

    const interval = setInterval(() => {
      console.log('🔄 Periodic refresh...');
      fetchData();
    }, 10000); // Refresh every 10 seconds for real-time updates

    return () => clearInterval(interval);
  }, [isLoggedIn, fetchData]);

  const handleLogin = (success) => {
    if (success) {
      setIsLoggedIn(true);
      fetchData();
    }
  };

  if (!isLoggedIn) {
    return <Login onLogin={handleLogin} />;
  }

  return (
    <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <div className="flex w-full min-h-screen bg-gray-100">
        <Sidebar unreadCount={stats.unread} />
        <div className="flex-1 flex flex-col">
          <main className="flex-1 p-6">
            {isLoading ? (
              <div className="flex items-center justify-center h-full">
                <div className="text-center">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
                  <p className="mt-4 text-gray-600">Loading data from backend...</p>
                  <p className="mt-2 text-sm text-gray-500">Fetching SOS alerts and incidents...</p>
                </div>
              </div>
            ) : (
              <Routes>
                <Route
                  path="/"
                  element={<Dashboard alerts={alerts} stats={stats} newAlertId={newAlertId} />}
                />
                <Route
                  path="/alerts"
                  element={<Alerts alerts={alerts} newAlertId={newAlertId} />}
                />
                <Route
                  path="/alerts/:id"
                  element={<AlertDetail alerts={alerts} />}
                />
                <Route
                  path="/messages"
                  element={<Messages />}
                />
                <Route path="/users" element={<Users />} />
                <Route path="/analytics" element={<Analytics />} />
                <Route path="/settings" element={<Settings />} />
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            )}
          </main>
        </div>
      </div>
    </Router>
  );
}

export default App;

