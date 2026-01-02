import React, { useState, useEffect, useCallback } from 'react';
import { 
  Search, 
  Filter, 
  MessageCircle, 
  AlertTriangle, 
  Flame, 
  Clock,
  CheckCircle,
  Eye,
  RefreshCw,
  MapPin
} from 'lucide-react';
import VulnerableBadge from '../components/VulnerableBadge';
import StatusBadge from '../components/StatusBadge';
import { 
  getAllMessages, 
  getMessageStats, 
  markMessageAsRead, 
  getUnreadCount 
} from '../../../../services/api.js';

function Messages() {
  const [messages, setMessages] = useState([]);
  const [filteredMessages, setFilteredMessages] = useState([]);
  const [stats, setStats] = useState({
    total: 0,
    unread: 0,
    by_type: { SOS: 0, INCIDENT: 0, GENERAL: 0 }
  });
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('all');
  const [readFilter, setReadFilter] = useState('all');
  const [selectedMessage, setSelectedMessage] = useState(null);

  // Fetch messages and stats
  const fetchData = useCallback(async () => {
    setIsLoading(true);
    try {
      const [messagesData, statsData, unreadData] = await Promise.all([
        getAllMessages({}),
        getMessageStats(),
        getUnreadCount()
      ]);
      
      setMessages(messagesData.messages || []);
      setStats({
        ...statsData,
        unread: unreadData.unread_count || 0
      });
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // Filter messages
  useEffect(() => {
    let filtered = [...messages];

    // Search filter
    if (searchTerm) {
      filtered = filtered.filter(msg => 
        msg.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        msg.content?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        msg.user_name?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Type filter
    if (typeFilter !== 'all') {
      filtered = filtered.filter(msg => msg.message_type === typeFilter);
    }

    // Read filter
    if (readFilter !== 'all') {
      const isRead = readFilter === 'read';
      filtered = filtered.filter(msg => msg.is_read === isRead);
    }

    // Sort by date (newest first)
    filtered.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));

    setFilteredMessages(filtered);
  }, [messages, searchTerm, typeFilter, readFilter]);

  const handleMarkAsRead = async (messageId) => {
    try {
      await markMessageAsRead(messageId);
      setMessages(prev => prev.map(msg => 
        msg.id === messageId ? { ...msg, is_read: true } : msg
      ));
      setStats(prev => ({ ...prev, unread: Math.max(0, prev.unread - 1) }));
    } catch (error) {
      console.error('Error marking message as read:', error);
    }
  };

  const getMessageIcon = (type) => {
    switch (type) {
      case 'SOS':
        return <AlertTriangle className="h-5 w-5 text-red-500" />;
      case 'INCIDENT':
        return <Flame className="h-5 w-5 text-orange-500" />;
      default:
        return <MessageCircle className="h-5 w-5 text-blue-500" />;
    }
  };

  const getMessageColor = (type) => {
    switch (type) {
      case 'SOS':
        return 'bg-red-50 border-red-200';
      case 'INCIDENT':
        return 'bg-orange-50 border-orange-200';
      default:
        return 'bg-blue-50 border-blue-200';
    }
  };

  const formatTimestamp = (timestamp) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  };

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Messages</h1>
          <p className="text-gray-600 mt-1">User SOS alerts and incident reports</p>
        </div>
        <button
          onClick={fetchData}
          disabled={isLoading}
          className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
        >
          <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex items-center">
            <MessageCircle className="h-8 w-8 text-indigo-500" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-600">Total Messages</p>
              <p className="text-xl font-bold text-gray-900">{stats.total}</p>
            </div>
          </div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex items-center">
            <AlertTriangle className="h-8 w-8 text-red-500" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-600">SOS Alerts</p>
              <p className="text-xl font-bold text-gray-900">{stats.by_type?.SOS || 0}</p>
            </div>
          </div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex items-center">
            <Flame className="h-8 w-8 text-orange-500" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-600">Incidents</p>
              <p className="text-xl font-bold text-gray-900">{stats.by_type?.INCIDENT || 0}</p>
            </div>
          </div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow border-2 border-yellow-400">
          <div className="flex items-center">
            <Clock className="h-8 w-8 text-yellow-500" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-600">Unread</p>
              <p className="text-xl font-bold text-yellow-600">{stats.unread}</p>
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
              placeholder="Search messages..."
              className="pl-10 w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          {/* Type Filter */}
          <select
            className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
          >
            <option value="all">All Types</option>
            <option value="SOS">SOS Alerts</option>
            <option value="INCIDENT">Incidents</option>
            <option value="GENERAL">General</option>
          </select>

          {/* Read Filter */}
          <select
            className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            value={readFilter}
            onChange={(e) => setReadFilter(e.target.value)}
          >
            <option value="all">All Status</option>
            <option value="unread">Unread</option>
            <option value="read">Read</option>
          </select>

          {/* Count */}
          <div className="flex items-center px-3 py-2 text-sm text-gray-600">
            Showing {filteredMessages.length} of {messages.length} messages
          </div>
        </div>
      </div>

      {/* Messages List */}
      <div className="space-y-4">
        {isLoading ? (
          <div className="text-center py-12">
            <RefreshCw className="mx-auto h-12 w-12 text-indigo-500 animate-spin" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">Loading messages...</h3>
          </div>
        ) : filteredMessages.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-lg shadow">
            <MessageCircle className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">No messages found</h3>
            <p className="mt-1 text-sm text-gray-500">
              {searchTerm || typeFilter !== 'all' || readFilter !== 'all' 
                ? 'Try adjusting your filters' 
                : 'No messages have been received yet'}
            </p>
          </div>
        ) : (
          filteredMessages.map((message) => (
            <div 
              key={message.id} 
              className={`bg-white rounded-lg shadow p-6 border-l-4 ${getMessageColor(message.message_type)} ${!message.is_read ? 'ring-2 ring-yellow-400' : ''}`}
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center space-x-3 mb-3">
                    {getMessageIcon(message.message_type)}
                    <h3 className="text-lg font-medium text-gray-900">{message.title}</h3>
                    {!message.is_read && (
                      <span className="px-2 py-1 text-xs font-medium bg-yellow-100 text-yellow-800 rounded-full">
                        NEW
                      </span>
                    )}
                    <StatusBadge status={message.message_type === 'SOS' ? 'SOS' : message.message_type === 'INCIDENT' ? 'Incident' : 'Message'} />
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div>
                      <p className="text-sm text-gray-600 mb-1">
                        <span className="font-medium">From:</span> {message.user_name || 'Unknown User'}
                      </p>
                      {message.category && (
                        <p className="text-sm text-gray-600 mb-1">
                          <span className="font-medium">Category:</span> {message.category}
                        </p>
                      )}
                      {message.severity && (
                        <p className="text-sm text-gray-600 mb-1">
                          <span className="font-medium">Severity:</span> 
                          <span className={`ml-1 px-2 py-0.5 rounded text-xs font-medium ${
                            message.severity === 'critical' ? 'bg-red-100 text-red-800' :
                            message.severity === 'high' ? 'bg-orange-100 text-orange-800' :
                            message.severity === 'medium' ? 'bg-yellow-100 text-yellow-800' :
                            'bg-green-100 text-green-800'
                          }`}>
                            {message.severity.toUpperCase()}
                          </span>
                        </p>
                      )}
                      {message.ability && (
                        <p className="text-sm text-gray-600 mb-1">
                          <span className="font-medium">Accessibility Need:</span> {message.ability}
                        </p>
                      )}
                    </div>
                    <div>
                      <div className="flex items-center text-sm text-gray-600 mb-1">
                        <Clock className="h-4 w-4 mr-1" />
                        {formatTimestamp(message.created_at)}
                      </div>
                      {(message.lat && message.lng) && (
                        <div className="flex items-center text-sm text-gray-600">
                          <MapPin className="h-4 w-4 mr-1" />
                          {message.lat.toFixed(4)}, {message.lng.toFixed(4)}
                        </div>
                      )}
                      {message.battery !== null && message.battery !== undefined && (
                        <p className="text-sm text-gray-600">
                          <span className="font-medium">Battery:</span> {message.battery}%
                        </p>
                      )}
                    </div>
                  </div>

                  <p className="text-gray-700 mb-4 bg-gray-50 p-3 rounded-lg">
                    {message.content}
                  </p>

                  <div className="flex space-x-2">
                    <button
                      onClick={() => setSelectedMessage(message)}
                      className="inline-flex items-center px-3 py-1 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                    >
                      <Eye className="h-4 w-4 mr-1" />
                      View Details
                    </button>
                    
                    {!message.is_read && (
                      <button
                        onClick={() => handleMarkAsRead(message.id)}
                        className="inline-flex items-center px-3 py-1 border border-transparent rounded-md text-sm font-medium text-white bg-green-600 hover:bg-green-700"
                      >
                        <CheckCircle className="h-4 w-4 mr-1" />
                        Mark as Read
                      </button>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Message Detail Modal */}
      {selectedMessage && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className={`p-6 border-b ${getMessageColor(selectedMessage.message_type)}`}>
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  {getMessageIcon(selectedMessage.message_type)}
                  <h2 className="text-xl font-bold text-gray-900">{selectedMessage.title}</h2>
                </div>
                <button
                  onClick={() => setSelectedMessage(null)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  ✕
                </button>
              </div>
            </div>
            
            <div className="p-6">
              <div className="grid grid-cols-2 gap-4 mb-4">
                <div>
                  <p className="text-sm text-gray-500">From</p>
                  <p className="font-medium">{selectedMessage.user_name || 'Unknown User'}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Time</p>
                  <p className="font-medium">{new Date(selectedMessage.created_at).toLocaleString()}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Type</p>
                  <p className="font-medium">{selectedMessage.message_type}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Status</p>
                  <p className="font-medium">{selectedMessage.is_read ? 'Read' : 'Unread'}</p>
                </div>
                {selectedMessage.category && (
                  <div>
                    <p className="text-sm text-gray-500">Category</p>
                    <p className="font-medium">{selectedMessage.category}</p>
                  </div>
                )}
                {selectedMessage.severity && (
                  <div>
                    <p className="text-sm text-gray-500">Severity</p>
                    <p className="font-medium uppercase">{selectedMessage.severity}</p>
                  </div>
                )}
                {(selectedMessage.lat && selectedMessage.lng) && (
                  <div>
                    <p className="text-sm text-gray-500">Location</p>
                    <p className="font-medium">{selectedMessage.lat.toFixed(6)}, {selectedMessage.lng.toFixed(6)}</p>
                  </div>
                )}
                {selectedMessage.ability && (
                  <div>
                    <p className="text-sm text-gray-500">Accessibility Need</p>
                    <p className="font-medium">{selectedMessage.ability}</p>
                  </div>
                )}
                {selectedMessage.battery !== null && selectedMessage.battery !== undefined && (
                  <div>
                    <p className="text-sm text-gray-500">Battery</p>
                    <p className="font-medium">{selectedMessage.battery}%</p>
                  </div>
                )}
              </div>
              
              <div className="border-t pt-4">
                <p className="text-sm text-gray-500 mb-2">Message Content</p>
                <p className="text-gray-900 whitespace-pre-wrap bg-gray-50 p-4 rounded-lg">
                  {selectedMessage.content}
                </p>
              </div>
            </div>
            
            <div className="px-6 py-4 bg-gray-50 border-t flex justify-end space-x-3">
              {!selectedMessage.is_read && (
                <button
                  onClick={() => {
                    handleMarkAsRead(selectedMessage.id);
                    setSelectedMessage(null);
                  }}
                  className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
                >
                  Mark as Read & Close
                </button>
              )}
              <button
                onClick={() => setSelectedMessage(null)}
                className="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400"
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

export default Messages;

