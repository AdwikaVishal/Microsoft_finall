import React from 'react';
import { Users, AlertTriangle, Activity, Clock } from 'lucide-react';

function Analytics() {
    // Mock analytics data
    const stats = [
        {
            title: 'Total Users',
            value: '1,234',
            change: '+12%',
            changeType: 'positive',
            icon: Users,
        },
        {
            title: 'Active Alerts',
            value: '23',
            change: '-5%',
            changeType: 'negative',
            icon: AlertTriangle,
        },
        {
            title: 'System Uptime',
            value: '99.9%',
            change: '+0.1%',
            changeType: 'positive',
            icon: Activity,
        },
        {
            title: 'Avg Response Time',
            value: '2.3s',
            change: '-0.2s',
            changeType: 'positive',
            icon: Clock,
        },
    ];

    const recentActivity = [
        { id: 1, action: 'New user registered', user: 'Alice Cooper', time: '2 minutes ago' },
        { id: 2, action: 'Alert resolved', user: 'System', time: '5 minutes ago' },
        { id: 3, action: 'System backup completed', user: 'System', time: '1 hour ago' },
        { id: 4, action: 'User updated profile', user: 'David Miller', time: '2 hours ago' },
        { id: 5, action: 'New alert triggered', user: 'System', time: '3 hours ago' },
    ];

    return (
        <div className="p-6">
            <div className="mb-8">
                <h1 className="text-2xl font-bold text-gray-900">Analytics Dashboard</h1>
                <p className="text-gray-600">Monitor system performance and user activity</p>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                {stats.map((stat, index) => {
                    const Icon = stat.icon;
                    return (
                        <div key={index} className="bg-white rounded-lg shadow p-6">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-sm font-medium text-gray-600">{stat.title}</p>
                                    <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
                                </div>
                                <Icon className="h-8 w-8 text-gray-400" />
                            </div>
                            <div className="mt-4">
                                <span className={`text-sm font-medium ${
                                    stat.changeType === 'positive' ? 'text-green-600' : 'text-red-600'
                                }`}>
                                    {stat.change}
                                </span>
                                <span className="text-sm text-gray-600 ml-1">from last month</span>
                            </div>
                        </div>
                    );
                })}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Chart Placeholder */}
                <div className="bg-white rounded-lg shadow p-6">
                    <h2 className="text-lg font-semibold mb-4">Alerts Over Time</h2>
                    <div className="h-64 bg-gray-100 rounded-lg flex items-center justify-center">
                        <p className="text-gray-500">Chart visualization would go here</p>
                    </div>
                </div>

                {/* Recent Activity */}
                <div className="bg-white rounded-lg shadow p-6">
                    <h2 className="text-lg font-semibold mb-4">Recent Activity</h2>
                    <div className="space-y-4">
                        {recentActivity.map((activity) => (
                            <div key={activity.id} className="flex items-start space-x-3">
                                <div className="flex-shrink-0">
                                    <div className="w-2 h-2 bg-blue-500 rounded-full mt-2"></div>
                                </div>
                                <div className="flex-1 min-w-0">
                                    <p className="text-sm font-medium text-gray-900">
                                        {activity.action}
                                    </p>
                                    <p className="text-sm text-gray-500">
                                        by {activity.user} • {activity.time}
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Additional Analytics Section */}
            <div className="mt-8 bg-white rounded-lg shadow p-6">
                <h2 className="text-lg font-semibold mb-4">System Performance</h2>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div>
                        <h3 className="text-sm font-medium text-gray-600 mb-2">CPU Usage</h3>
                        <div className="w-full bg-gray-200 rounded-full h-2">
                            <div className="bg-blue-600 h-2 rounded-full" style={{ width: '45%' }}></div>
                        </div>
                        <p className="text-sm text-gray-500 mt-1">45% average</p>
                    </div>
                    <div>
                        <h3 className="text-sm font-medium text-gray-600 mb-2">Memory Usage</h3>
                        <div className="w-full bg-gray-200 rounded-full h-2">
                            <div className="bg-green-600 h-2 rounded-full" style={{ width: '67%' }}></div>
                        </div>
                        <p className="text-sm text-gray-500 mt-1">67% average</p>
                    </div>
                    <div>
                        <h3 className="text-sm font-medium text-gray-600 mb-2">Storage Usage</h3>
                        <div className="w-full bg-gray-200 rounded-full h-2">
                            <div className="bg-yellow-600 h-2 rounded-full" style={{ width: '78%' }}></div>
                        </div>
                        <p className="text-sm text-gray-500 mt-1">78% used</p>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Analytics;
