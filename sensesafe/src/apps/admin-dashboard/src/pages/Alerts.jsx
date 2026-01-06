import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { RefreshCw } from 'lucide-react';

import VulnerableBadge from '../components/VulnerableBadge';

import {
  getAllAlertsForAdmin,
  resolveIncident,
  deleteMessage,
  resolveAlert
} from '../services/api.js';

function Alerts() {

  const [alerts, setAlerts] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  const loadAlerts = async () => {
    setIsLoading(true);
    try {
      const data = await getAllAlertsForAdmin();
      const messages = Array.isArray(data?.messages) ? data.messages : [];

      const combined = messages.map(msg => ({
        id: msg.id,
        backendSource: msg.sourceType,
        userName: msg.user_name || "Unknown User",
        alertType: msg.message_type || "GENERAL",
        description: msg.content || msg.title,
        isVulnerable: !!msg.ability && msg.ability !== "NONE",
        status: msg.is_read ? "Resolved" : "Active",
        timestamp: msg.created_at
      }));

      setAlerts(combined.filter(alert => alert.status !== "Resolved"));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => { loadAlerts(); }, []);

  const handleResolve = async (alert) => {
    try {
      // INCIDENT — admin incidents API
      if (alert.alertType === "INCIDENT") {
        await resolveIncident(alert.id);
      }
      // SOS / GENERAL — delete message
      else if (alert.backendSource === "MESSAGE") {
        await deleteMessage(alert.id);
      }
      // broadcast alerts table
      else {
        await resolveAlert(alert.id);
      }

      setAlerts(prev => prev.filter(a => a.id !== alert.id));

    } catch (err) {
      console.error(err);
      alert("Failed to resolve");
    }
  };

  return (
    <div className="p-6">

      <div className="flex justify-between mb-6">
        <h1 className="text-3xl font-bold">SOS Alerts</h1>

        <button onClick={loadAlerts} className="flex items-center px-4 py-2 border rounded">
          <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      <div className="space-y-4">
        {alerts.map(alert => (
          <div key={alert.id} className="bg-white p-6 rounded shadow">

            <div className="flex justify-between">
              <h3 className="font-semibold">{alert.userName}</h3>
              <VulnerableBadge isVulnerable={alert.isVulnerable} />
            </div>

            <p className="mt-2">{alert.description}</p>

            <div className="mt-4 flex gap-3">
              <Link to={`/alerts/${alert.id}`} className="px-3 py-1 border rounded">
                View
              </Link>

              <button
                onClick={() => handleResolve(alert)}
                className="px-3 py-1 text-white bg-green-600 rounded"
              >
                Resolve
              </button>
            </div>

          </div>
        ))}
      </div>

    </div>
  );
}

export default Alerts;
