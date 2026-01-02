import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Home, AlertTriangle, MessageCircle, Settings, Users, BarChart3 } from 'lucide-react';

function Sidebar({ unreadCount = 0 }) {
    const location = useLocation();

    const menuItems = [
        { path: '/', icon: Home, label: 'Dashboard' },
        { path: '/alerts', icon: AlertTriangle, label: 'Alerts' },
        { path: '/messages', icon: MessageCircle, label: 'Messages', badge: unreadCount },
        { path: '/users', icon: Users, label: 'Users' },
        { path: '/analytics', icon: BarChart3, label: 'Analytics' },
        { path: '/settings', icon: Settings, label: 'Settings' },
    ];

    return (
        <div className="bg-gray-800 text-white w-64 min-h-screen">
            <div className="p-4">
                <h2 className="text-xl font-bold">SenseSafe Admin</h2>
                <p className="text-xs text-gray-400 mt-1">Emergency Response System</p>
            </div>
            <nav className="mt-8">
                <ul>
                    {menuItems.map((item) => {
                        const Icon = item.icon;
                        const isActive = location.pathname === item.path;
                        return (
                            <li key={item.path} className="mb-2">
                                <Link
                                    to={item.path}
                                    className={`flex items-center px-4 py-3 text-sm font-medium rounded-lg mx-2 transition-colors ${
                                        isActive
                                            ? 'bg-indigo-600 text-white'
                                            : 'text-gray-300 hover:bg-gray-700 hover:text-white'
                                    }`}
                                >
                                    <Icon className="h-5 w-5 mr-3" />
                                    {item.label}
                                    {item.badge > 0 && (
                                        <span className="ml-auto bg-red-500 text-white text-xs px-2 py-0.5 rounded-full">
                                            {item.badge}
                                        </span>
                                    )}
                                </Link>
                            </li>
                        );
                    })}
                </ul>
            </nav>
            
            <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-700">
                <div className="flex items-center text-xs text-gray-400">
                    <div className={`w-2 h-2 rounded-full mr-2 ${unreadCount > 0 ? 'bg-yellow-500 animate-pulse' : 'bg-green-500'}`}></div>
                    {unreadCount > 0 ? `${unreadCount} unread messages` : 'All messages read'}
                </div>
            </div>
        </div>
    );
}

export default Sidebar;

