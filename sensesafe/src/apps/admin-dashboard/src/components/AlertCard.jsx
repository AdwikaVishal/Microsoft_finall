import React from 'react';
import { AlertTriangle, Clock, User } from 'lucide-react';

function AlertCard({ alert, isNew }) {
    return (
        <div className={`p-4 border rounded-lg ${isNew ? 'bg-yellow-50 border-yellow-200' : 'bg-white border-gray-200'}`}>
            <div className="flex items-start justify-between">
                <div className="flex items-start">
                    <AlertTriangle className={`h-5 w-5 mt-0.5 ${alert.status === 'Active' ? 'text-red-500' : 'text-yellow-500'}`} />
                    <div className="ml-3">
                        <p className="text-sm font-medium text-gray-900">{alert.userName}</p>
                        <p className="text-sm text-gray-500">{alert.alertType}</p>
                        <p className="text-xs text-gray-400 mt-1">{alert.description}</p>
                    </div>
                </div>
                <div className="flex flex-col items-end">
                    <span className={`px-2 py-1 text-xs font-medium rounded-full ${
                        alert.status === 'Active' ? 'bg-red-100 text-red-800' :
                        alert.status === 'Pending' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-green-100 text-green-800'
                    }`}>
                        {alert.status}
                    </span>
                    <p className="text-xs text-gray-400 mt-1">{new Date(alert.timestamp).toLocaleTimeString()}</p>
                </div>
            </div>
        </div>
    );
}

export default AlertCard;
