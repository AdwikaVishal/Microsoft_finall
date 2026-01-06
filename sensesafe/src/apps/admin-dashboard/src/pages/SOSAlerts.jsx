import React, { useState, useEffect, useCallback } from 'react';
import { 
  Search, 
  Filter, 
  AlertTriangle, 
  Clock, 
  CheckCircle, 
  MapPin, 
  Battery,
  User,
  RefreshCw,
  Eye
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { getAllSOSAlerts, resolveSOS, getSOSStats } from '../services/api.js';

function SOSAlerts() {
  const [sosAlerts, setSOSAlerts] = useState([]);
  const [filteredAlerts, setFilteredAlerts] = useState([]);
  const [stats, setStats] = useState({ active_sos: 0, total: 0 });
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [selectedAlert, setSelectedAlert] = useState(null);

  // Fetch SOS data
  const fetchData = useCallback(async () => {
    setIsLoading(true);
    try {
      console.log('🔄 Fetching SOS alerts...');
      
      // Fetch both SOS alerts and stats in parallel
      const [sosData, statsData] = await Promise.all([
        getAllSOSAlerts(),
        getSOSStats()
      ]);

      console.log(`📨 Received ${sosData.length} SOS alerts`);
      
      setSOSAlerts(sosData);
      setStats({
        active_sos: statsData.active_sos || sosData.filter(s => s.status !== 'SAFE').length,
        total: sosData.length
      });
    } catch (error) {
      console.error('❌ Error fetching SOS alerts:', error);
      setSOSAlerts([]);
      setStats({ active_sos: 0, total: 0 });
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // Filter SOS alerts
  useEffect(() => {
    let filtered = [...sosAlerts];

    // Search filter
    if (searchTerm) {
      filtered = filtered.filter(alert =>
        alert.id?.toString().toLowerCase().includes(searchTerm.toLowerCase()) ||
        alert.user_id?.toString().toLowerCase().includes(searchTerm.toLowerCase()) ||
        (alert.status && alert.status.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (alert.ability && alert.ability.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    }

    // Status filter
    if (statusFilter !== 'all') {
      filtered = filtered.filter(alert => alert.status === statusFilter);
    }

    // Sort by created_at descending (newest first)
    filtered.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));

    setFilteredAlerts(filtered);
  }, [sosAlerts, searchTerm, statusFilter]);

  // Resolve SOS
  const handleResolve = async (sosId) => {
    try {
      console.log(`🔧 Resolving SOS ${sosId}...`);
      await resolveSOS(sosId);
      
      // Update local state
      setSOSAlerts(prev =>
        prev.map(sos =>
          sos.id === sosId ? { ...sos, status: 'SAFE' } : sos
        )
      );
      
      setStats(prev => ({
        ...prev,
        active_sos: Math.max(prev.active_sos - 1, 0)
      }));
      
      console.log('✅ SOS resolved successfully');
    } catch (error) {
      console.error('❌ Error resolving SOS:', error);
    }
  };

  // Get status badge color
  const getStatusColor = (status) => {
    switch (status) {
      case 'TRAPPED':
        return 'bg-red-100 text-red-800';
      case 'INJURED':
        return 'bg-orange-100 text-orange-800';
      case 'NEED_HELP':
        return 'bg-yellow-100 text-yellow-800';
      case 'SAFE':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  // Format timestamp
  const formatTimestamp = (timestamp) => {
    if (!timestamp) return 'Unknown';
    
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;
    
    const mins = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);
    
    if (mins < 1) return 'Just now';
    if (mins < 60) return `${mins}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
    
    return date.toLocaleString();
  };

  // Get ability label
  const getAbilityLabel = (ability) => {
    const labels = {
      'BLIND': 'Blind',
      'LOW_VISION': 'Low Vision',
      'DEAF': 'Deaf',
      'HARD_OF_HEARING': 'Hard of Hearing',
      'NON_VERBAL': 'Non-Verbal',
      'ELDERLY': 'Elderly',
      'OTHER': 'Other',
      'NONE': 'None'
    };
    return labels[ability] || ability || 'Unknown';
  };

  return (
    <div className="p-6">
      {/* HEADER */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">SOS Alerts</h1>
          <p className="text-gray-600">
            Monitor and respond to emergency SOS signals from users
          </p>
        </div>

        <button
          onClick={fetchData}
          disabled={isLoading}
          className="inline-flex items-center px-4 py-2 border rounded-md bg-red-600 text-white hover:bg-red-700"
        >
          <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* STATS CARDS */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-white p-4 rounded-lg shadow border-l-4 border-red-500">
          <div className="flex items-center">
            <AlertTriangle className="h-8 w-8 text-red-500" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-600">Active SOS</p>
              <p className="text-2xl font-bold text-gray-900">{stats.active_sos}</p>
            </div>
          </div>
        </div>
        
        <div className="bg-white p-4 rounded-lg shadow border-l-4 border-gray-500">
          <div className="flex items-center">
            <Clock className="h-8 w-8 text-gray-500" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-600">Total SOS</p>
              <p className="text-2xl font-bold text-gray-900">{stats.total}</p>
            </div>
          </div>
        </div>
        
        <div className="bg-white p-4 rounded-lg shadow border-l-4 border-green-500">
          <div className="flex items-center">
            <CheckCircle className="h-8 w-8 text-green-500" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-600">Resolved</p>
              <p className="text-2xl font-bold text-gray-900">{stats.total - stats.active_sos}</p>
            </div>
          </div>
        </div>
      </div>

      {/* FILTERS */}
      <div className="bg-white p-4 rounded-lg shadow mb-6">
        <div className="flex flex-wrap gap-4">
          <div className="flex-1 min-w-64">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <input
                type="text"
                placeholder="Search by ID, user, status, or ability..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 pr-4 py-2 w-full border rounded-md focus:ring-2 focus:ring-red-500 focus:border-red-500"
              />
            </div>
          </div>
          
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 border rounded-md focus:ring-2 focus:ring-red-500 focus:border-red-500"
          >
            <option value="all">All Statuses</option>
            <option value="TRAPPED">Trapped</option>
            <option value="INJURED">Injured</option>
            <option value="NEED_HELP">Need Help</option>
            <option value="SAFE">Safe (Resolved)</option>
          </select>
        </div>
      </div>

      {/* SOS TABLE */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        {isLoading ? (
          <div className="text-center py-12">
            <RefreshCw className="h-10 w-10 animate-spin mx-auto text-red-500" />
            <p className="mt-2 text-gray-600">Loading SOS alerts...</p>
          </div>
        ) : filteredAlerts.length === 0 ? (
          <div className="text-center py-12">
            <AlertTriangle className="h-10 w-10 mx-auto text-gray-400" />
            <p className="mt-2 text-gray-600">
              No SOS alerts found
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Time
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    User ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Ability
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Battery
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Location (lat, lng)
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredAlerts.map((alert) => (
                  <tr key={alert.id} className={alert.status !== 'SAFE' ? 'bg-red-50' : ''}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      <div className="flex items-center">
                        <Clock className="h-4 w-4 text-gray-400 mr-2" />
                        {formatTimestamp(alert.created_at)}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      <div className="flex items-center">
                        <User className="h-4 w-4 text-gray-400 mr-2" />
                        {alert.user_id ? (
                          <span className="font-mono text-xs">
                            {alert.user_id.substring(0, 8)}...
                          </span>
                        ) : (
                          <span className="text-gray-400">Anonymous</span>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(alert.status)}`}>
                        {alert.status || 'UNKNOWN'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {getAbilityLabel(alert.ability)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <div className="flex items-center">
                        <Battery className={`h-4 w-4 mr-1 ${alert.battery < 20 ? 'text-red-500' : 'text-green-500'}`} />
                        {alert.battery !== undefined ? `${alert.battery}%` : 'N/A'}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <div className="flex items-center">
                        <MapPin className="h-4 w-4 text-gray-400 mr-1" />
                        <span className="font-mono text-xs">
                          {alert.lat?.toFixed(4)}, {alert.lng?.toFixed(4)}
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <div className="flex items-center space-x-2">
                        {alert.status !== 'SAFE' && (
                          <button
                            onClick={() => handleResolve(alert.id)}
                            className="inline-flex items-center px-2 py-1 bg-green-600 text-white text-xs rounded hover:bg-green-700"
                          >
                            <CheckCircle className="h-3 w-3 mr-1" />
                            Resolve
                          </button>
                        )}
                        <Link
                          to={`/messages?highlight=${alert.id}`}
                          className="inline-flex items-center px-2 py-1 bg-blue-600 text-white text-xs rounded hover:bg-blue-700"
                        >
                          <Eye className="h-3 w-3 mr-1" />
                          View
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* DETAIL PANEL */}
      {selectedAlert && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-lg w-full mx-4">
            <h3 className="text-lg font-bold mb-4">SOS Alert Details</h3>
            <pre className="bg-gray-100 p-4 rounded overflow-auto max-h-64 text-sm">
              {JSON.stringify(selectedAlert, null, 2)}
            </pre>
            <div className="mt-4 flex justify-end">
              <button
                onClick={() => setSelectedAlert(null)}
                className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default SOSAlerts;

