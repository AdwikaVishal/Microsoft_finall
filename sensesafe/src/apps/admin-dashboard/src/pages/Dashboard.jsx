import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { AlertTriangle, Users, CheckCircle, Clock } from 'lucide-react';

function Dashboard({ alerts, newAlertId }) {
    const [stats, setStats] = useState({
        totalIncidents: 0,
        activeSOS: 0,
        highRiskUsers: 0,
        resolved: 0
    });

    useEffect(() => {
        // Calculate stats from alerts
        const totalIncidents = alerts.length;
        const activeSOS = alerts.filter(a => a.status === 'Active').length;
        const highRiskUsers = alerts.filter(a => a.isVulnerable && a.status !== 'Resolved').length;
        const resolved = alerts.filter(a => a.status === 'Resolved').length;

        setStats({ totalIncidents, activeSOS, highRiskUsers, resolved });
    }, [alerts]);

    return (
        <div className="p-6">
            <h1 className="text-3xl font-bold text-gray-900 mb-6">Dashboard Overview</h1>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                <div className="bg-white p-6 rounded-lg shadow">
                    <div className="flex items-center">
                        <AlertTriangle className="h-8 w-8 text-red-500" />
                        <div className="ml-4">
                            <p className="text-sm font-medium text-gray-600">Total Incidents</p>
                            <p className="text-2xl font-bold text-gray-900">{stats.totalIncidents}</p>
                        </div>
                    </div>
                </div>
                <div className="bg-white p-6 rounded-lg shadow">
                    <div className="flex items-center">
                        <Users className="h-8 w-8 text-orange-500" />
                        <div className="ml-4">
                            <p className="text-sm font-medium text-gray-600">Active SOS</p>
                            <p className="text-2xl font-bold text-gray-900">{stats.activeSOS}</p>
                        </div>
                    </div>
                </div>
                <div className="bg-white p-6 rounded-lg shadow">
                    <div className="flex items-center">
                        <AlertTriangle className="h-8 w-8 text-yellow-500" />
                        <div className="ml-4">
                            <p className="text-sm font-medium text-gray-600">High Risk Users</p>
                            <p className="text-2xl font-bold text-gray-900">{stats.highRiskUsers}</p>
                        </div>
                    </div>
                </div>
                <div className="bg-white p-6 rounded-lg shadow">
                    <div className="flex items-center">
                        <CheckCircle className="h-8 w-8 text-green-500" />
                        <div className="ml-4">
                            <p className="text-sm font-medium text-gray-600">Resolved</p>
                            <p className="text-2xl font-bold text-gray-900">{stats.resolved}</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Live Map Placeholder */}
            <div className="bg-white p-6 rounded-lg shadow mb-8">
                <h2 className="text-xl font-bold text-gray-900 mb-4">Live Map</h2>
                <div className="h-64 bg-gray-200 rounded-lg flex items-center justify-center">
                    <p className="text-gray-500">Map Placeholder</p>
                    {/* TODO: INSERT AZURE MAPS SDK HERE */}
                    {/* TODO: Add API key and map initialization */}
                </div>
            </div>

            {/* SOS List Table */}
            <div className="bg-white p-6 rounded-lg shadow mb-8">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-bold text-gray-900">SOS Alerts</h2>
                    <Link to="/alerts" className="text-indigo-600 hover:text-indigo-900">View All</Link>
                </div>
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">User</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ability</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Risk Score</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Battery</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {alerts.slice(0, 5).map((alert) => (
                                <tr key={alert.id} className={newAlertId === alert.id ? 'bg-yellow-50' : ''}>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{alert.userName}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{alert.userCategory}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {/* TODO: PLACEHOLDER: AI RISK API (Azure ML) */}
                                        {Math.floor(Math.random() * 100)}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{Math.floor(Math.random() * 100)}%</td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                            alert.status === 'Active' ? 'bg-red-100 text-red-800' :
                                            alert.status === 'Pending' ? 'bg-yellow-100 text-yellow-800' :
                                            'bg-green-100 text-green-800'
                                        }`}>
                                            {alert.status}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        <Link to={`/alerts/${alert.id}`} className="text-indigo-600 hover:text-indigo-900">View Details</Link>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Incident Management Table */}
            <div className="bg-white p-6 rounded-lg shadow">
                <h2 className="text-xl font-bold text-gray-900 mb-4">Incident Management</h2>
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Title</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Severity</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Reporter</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {alerts.slice(0, 5).map((alert) => (
                                <tr key={alert.id}>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{alert.alertType}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                            alert.alertType === 'SOS' ? 'bg-red-100 text-red-800' :
                                            alert.alertType === 'Injured' ? 'bg-orange-100 text-orange-800' :
                                            'bg-yellow-100 text-yellow-800'
                                        }`}>
                                            {alert.alertType === 'SOS' ? 'High' : alert.alertType === 'Injured' ? 'Medium' : 'Low'}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{alert.userName}</td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                            alert.status === 'Active' ? 'bg-red-100 text-red-800' :
                                            alert.status === 'Pending' ? 'bg-yellow-100 text-yellow-800' :
                                            'bg-green-100 text-green-800'
                                        }`}>
                                            {alert.status}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        <button className="text-indigo-600 hover:text-indigo-900 mr-2">Verify</button>
                                        <button className="text-green-600 hover:text-green-900">Resolve</button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* AI Explanation Panel */}
            <div className="bg-white p-6 rounded-lg shadow mt-8">
                <h2 className="text-xl font-bold text-gray-900 mb-4">AI Risk Analysis</h2>
                <p className="text-gray-700">
                    {/* TODO: PLACEHOLDER: AZURE OPENAI CALL */}
                    High risk due to low battery, no movement, severe area risk.
                </p>
            </div>
        </div>
    );
}

export default Dashboard;
