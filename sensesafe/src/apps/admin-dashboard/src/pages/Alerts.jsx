import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  Search, 
  Filter, 
  MapPin, 
  Clock, 
  AlertTriangle,
  CheckCircle,
  XCircle,
  RefreshCw
} from 'lucide-react';
import AlertCard from '../components/AlertCard';
import VulnerableBadge from '../components/VulnerableBadge';
import { getSOS, getIncidents } from '../../../../services/api.js';

function Alerts({ alerts: initialAlerts, newAlertId }) {
  const [alerts, setAlerts] = useState(initialAlerts || []);
  const [filteredAlerts, setFilteredAlerts] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [sortBy, setSortBy] = useState('timestamp');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // Update alerts when props change
    setAlerts(initialAlerts || []);
  }, [initialAlerts]);

  useEffect(() => {
    // Apply filters and search
    let filtered = [...alerts];

    // Search filter
    if (searchTerm) {
      filtered = filtered.filter(alert => 
        alert.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        alert.alertType.toLowerCase().includes(searchTerm.toLowerCase()) ||
        alert.description.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Status filter
    if (statusFilter !== 'all') {
      filtered = filtered.filter(alert => alert.status.toLowerCase() === statusFilter.toLowerCase());
    }

    // Category filter
    if (categoryFilter !== 'all') {
      filtered = filtered.filter(alert => alert.userCategory.toLowerCase() === categoryFilter.toLowerCase());
    }

    // Sort
    filtered.sort((a, b) => {
      switch (sortBy) {
        case 'timestamp':
          return new Date(b.timestamp) - new Date(a.timestamp);
        case 'user':
          return a.userName.localeCompare(b.userName);
        case 'status':
          return a.status.localeCompare(b.status);
        case 'risk':
          return (b.riskScore || 0) - (a.riskScore || 0);
        default:
          return 0;
      }
    });

    setFilteredAlerts(filtered);
  }, [alerts, searchTerm, statusFilter, categoryFilter, sortBy]);

  const handleRefresh = async () => {
    setIsLoading(true);
    try {
      const [sosData, incidentsData] = await Promise.all([
        getSOS(),
        getIncidents()
      ]);
      
      // Combine SOS alerts with incident data
      const combinedAlerts = [...sosData, ...incidentsData.map(inc => ({
        id: inc.id,
        userName: inc.reporter,
        alertType: inc.type,
        userCategory: 'Normal',
        isVulnerable: false,
        timestamp: inc.timestamp,
        status: inc.status,
        description: inc.description,
        riskScore: Math.floor(Math.random() * 100)
      }))];
      
      setAlerts(combinedAlerts);
    } catch (error) {
      console.error('Error refreshing alerts:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleStatusChange = (alertId, newStatus) => {
    setAlerts(prev => prev.map(alert => 
      alert.id === alertId 
        ? { ...alert, status: newStatus, timestamp: new Date().toISOString() }
        : alert
    ));
  };

  const getStats = () => {
    return {
      total: filteredAlerts.length,
      active: filteredAlerts.filter(a => a.status === 'Active').length,
      pending: filteredAlerts.filter(a => a.status === 'Pending').length,
      resolved: filteredAlerts.filter(a => a.status === 'Resolved').length,
      vulnerable: filteredAlerts.filter(a => a.isVulnerable).length,
    };
  };

  const stats = getStats();

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">SOS Alerts</h1>
          <p className="text-gray-600 mt-1">Monitor and manage emergency alerts</p>
        </div>
        <div className="flex space-x-3">
          <button
            onClick={handleRefresh}
            disabled={isLoading}
            className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-6">
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex items-center">
            <AlertTriangle className="h-8 w-8 text-red-500" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-600">Total</p>
              <p className="text-xl font-bold text-gray-900">{stats.total}</p>
            </div>
          </div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex items-center">
            <XCircle className="h-8 w-8 text-red-500" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-600">Active</p>
              <p className="text-xl font-bold text-red-600">{stats.active}</p>
            </div>
          </div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex items-center">
            <Clock className="h-8 w-8 text-yellow-500" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-600">Pending</p>
              <p className="text-xl font-bold text-yellow-600">{stats.pending}</p>
            </div>
          </div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex items-center">
            <CheckCircle className="h-8 w-8 text-green-500" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-600">Resolved</p>
              <p className="text-xl font-bold text-green-600">{stats.resolved}</p>
            </div>
          </div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex items-center">
            <AlertTriangle className="h-8 w-8 text-orange-500" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-600">Vulnerable</p>
              <p className="text-xl font-bold text-orange-600">{stats.vulnerable}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white p-4 rounded-lg shadow mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Search */}
          <div className="relative">
            <Search className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
            <input
              type="text"
              placeholder="Search alerts..."
              className="pl-10 w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          {/* Status Filter */}
          <select
            className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="all">All Status</option>
            <option value="pending">Pending</option>
            <option value="active">Active</option>
            <option value="resolved">Resolved</option>
          </select>

          {/* Category Filter */}
          <select
            className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
          >
            <option value="all">All Categories</option>
            <option value="blind">Blind</option>
            <option value="deaf">Deaf</option>
            <option value="elderly">Elderly</option>
            <option value="normal">Normal</option>
          </select>

          {/* Sort */}
          <select
            className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
          >
            <option value="timestamp">Sort by Time</option>
            <option value="user">Sort by User</option>
            <option value="status">Sort by Status</option>
            <option value="risk">Sort by Risk</option>
          </select>
        </div>
      </div>

      {/* Alerts List */}
      <div className="space-y-4">
        {filteredAlerts.length === 0 ? (
          <div className="text-center py-12">
            <AlertTriangle className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">No alerts found</h3>
            <p className="mt-1 text-sm text-gray-500">
              {searchTerm || statusFilter !== 'all' || categoryFilter !== 'all' 
                ? 'Try adjusting your filters' 
                : 'No SOS alerts have been received yet'}
            </p>
          </div>
        ) : (
          filteredAlerts.map((alert) => (
            <div key={alert.id} className="bg-white rounded-lg shadow p-6">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center space-x-3 mb-2">
                    <h3 className="text-lg font-medium text-gray-900">{alert.userName}</h3>
                    <VulnerableBadge isVulnerable={alert.isVulnerable} />
                    <span className={`px-2 py-1 text-xs font-medium rounded-full ${
                      alert.status === 'Active' ? 'bg-red-100 text-red-800' :
                      alert.status === 'Pending' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-green-100 text-green-800'
                    }`}>
                      {alert.status}
                    </span>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div>
                      <p className="text-sm text-gray-600 mb-1">Alert Type: <span className="font-medium">{alert.alertType}</span></p>
                      <p className="text-sm text-gray-600 mb-1">Category: <span className="font-medium">{alert.userCategory}</span></p>
                      <p className="text-sm text-gray-600">Risk Score: <span className="font-medium">{alert.riskScore || 'N/A'}</span></p>
                    </div>
                    <div>
                      <div className="flex items-center text-sm text-gray-600 mb-1">
                        <Clock className="h-4 w-4 mr-1" />
                        {new Date(alert.timestamp).toLocaleString()}
                      </div>
                      {alert.location && (
                        <div className="flex items-center text-sm text-gray-600">
                          <MapPin className="h-4 w-4 mr-1" />
                          {typeof alert.location === 'string' ? alert.location : 'Location available'}
                        </div>
                      )}
                    </div>
                  </div>

                  <p className="text-gray-700 mb-4">{alert.description}</p>

                  <div className="flex space-x-2">
                    <Link
                      to={`/alerts/${alert.id}`}
                      className="inline-flex items-center px-3 py-1 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                    >
                      View Details
                    </Link>
                    
                    {alert.status === 'Pending' && (
                      <button
                        onClick={() => handleStatusChange(alert.id, 'Active')}
                        className="inline-flex items-center px-3 py-1 border border-transparent rounded-md text-sm font-medium text-white bg-red-600 hover:bg-red-700"
                      >
                        Mark Active
                      </button>
                    )}
                    
                    {alert.status === 'Active' && (
                      <button
                        onClick={() => handleStatusChange(alert.id, 'Resolved')}
                        className="inline-flex items-center px-3 py-1 border border-transparent rounded-md text-sm font-medium text-white bg-green-600 hover:bg-green-700"
                      >
                        Resolve
                      </button>
                    )}
                  </div>
                </div>

                {newAlertId === alert.id && (
                  <div className="ml-4">
                    <div className="bg-yellow-100 border border-yellow-400 text-yellow-700 px-3 py-2 rounded">
                      NEW
                    </div>
                  </div>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default Alerts;
