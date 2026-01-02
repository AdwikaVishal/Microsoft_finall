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
  getAllAlertsForAdmin, 
  markMessageAsRead 
} from '../../../../services/api.js';

function Messages() {
  const [messages, setMessages] = useState([]);
  const [filteredMessages, setFilteredMessages] = useState([]);

  const [stats, setStats] = useState({
    total: 0,
    unread: 0,
    sos_count: 0,
    incident_count: 0
  });

  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('all');
  const [readFilter, setReadFilter] = useState('all');
  const [selectedMessage, setSelectedMessage] = useState(null);

  // ================= FETCH DATA =================
  const fetchData = useCallback(async () => {
    setIsLoading(true);

    try {
      console.log('🔄 Fetching messages from backend...');

      const data = await getAllAlertsForAdmin();

      // ALWAYS use fallback
      const messagesList = Array.isArray(data?.messages) ? data.messages : [];

      console.log(`📨 Received ${messagesList.length} messages`);

      setMessages(messagesList);

      setStats({
        total: data?.stats?.total || messagesList.length,
        unread: data?.stats?.unread || messagesList.filter(m => !m.is_read).length,
        sos_count: data?.stats?.by_type?.SOS || messagesList.filter(m => m.message_type === 'SOS').length,
        incident_count: data?.stats?.by_type?.INCIDENT || messagesList.filter(m => m.message_type === 'INCIDENT').length
      });

    } catch (err) {
      console.error('❌ Error fetching messages:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // ================= FILTERING =================
  useEffect(() => {
    let filtered = [...messages];

    if (searchTerm) {
      filtered = filtered.filter(msg =>
        (msg.title || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (msg.content || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (msg.user_name || '').toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (typeFilter !== 'all') {
      filtered = filtered.filter(msg => msg.message_type === typeFilter);
    }

    if (readFilter !== 'all') {
      const isRead = readFilter === 'read';
      filtered = filtered.filter(msg => msg.is_read === isRead);
    }

    filtered.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));

    setFilteredMessages(filtered);
  }, [messages, searchTerm, typeFilter, readFilter]);

  // ================= MARK READ =================
  const handleMarkAsRead = async (messageId) => {
    try {
      await markMessageAsRead(messageId);

      setMessages(prev =>
        prev.map(msg =>
          msg.id === messageId ? { ...msg, is_read: true } : msg
        )
      );

      setStats(prev => ({
        ...prev,
        unread: Math.max(prev.unread - 1, 0)
      }));
    } catch (err) {
      console.error('Error marking message as read:', err);
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

  return (
    <div className="p-6">
      {/* HEADER */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold">Messages</h1>
          <p className="text-gray-600">
            SOS + Incident reports coming from the Android app
          </p>
        </div>

        <button
          onClick={fetchData}
          disabled={isLoading}
          className="inline-flex items-center px-4 py-2 border rounded-md"
        >
          <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* LIST */}
      <div className="space-y-4">
        {isLoading ? (
          <div className="text-center py-12">
            <RefreshCw className="h-10 w-10 animate-spin mx-auto text-indigo-500" />
            <p className="mt-2 text-gray-600">Loading messages…</p>
          </div>
        ) : filteredMessages.length === 0 ? (
          <div className="text-center py-12 bg-white rounded shadow">
            <MessageCircle className="h-10 w-10 mx-auto text-gray-400" />
            <p className="mt-2 text-gray-600">
              No messages yet — Android alerts will appear here.
            </p>
          </div>
        ) : (
          filteredMessages.map(message => (
            <div
              key={message.id}
              className={`bg-white rounded-lg shadow p-6 border-l-4 ${getMessageColor(message.message_type)}`}
            >
              <div className="flex justify-between">
                <div>
                  <div className="flex items-center space-x-2">
                    {getMessageIcon(message.message_type)}
                    <h3 className="font-semibold">{message.title || 'Alert'}</h3>
                  </div>

                  <p className="mt-2 text-gray-700">
                    {message.content || '(No content)'}
                  </p>

                  {(message.lat && message.lng) && (
                    <p className="text-sm text-gray-600 mt-2">
                      📍 {message.lat}, {message.lng}
                    </p>
                  )}

                  <p className="text-xs text-gray-500 mt-1">
                    {formatTimestamp(message.created_at)}
                  </p>
                </div>

                {!message.is_read && (
                  <button
                    onClick={() => handleMarkAsRead(message.id)}
                    className="px-3 py-1 bg-green-600 text-white rounded"
                  >
                    Mark read
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default Messages;
