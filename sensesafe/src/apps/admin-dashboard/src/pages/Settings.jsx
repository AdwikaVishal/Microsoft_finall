import React, { useState } from 'react';
import { Save, Bell, Shield, Database, User } from 'lucide-react';

function Settings() {
    const [settings, setSettings] = useState({
        notifications: {
            emailAlerts: true,
            pushNotifications: false,
            smsAlerts: true,
        },
        security: {
            twoFactorAuth: false,
            sessionTimeout: '30',
            passwordExpiry: '90',
        },
        system: {
            maintenanceMode: false,
            debugMode: false,
            logLevel: 'info',
        },
    });

    const handleSettingChange = (category, setting, value) => {
        setSettings(prev => ({
            ...prev,
            [category]: {
                ...prev[category],
                [setting]: value,
            },
        }));
    };

    const handleSave = () => {
        // Mock save functionality
        alert('Settings saved successfully!');
    };

    return (
        <div className="p-6">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Settings</h1>
                <button
                    onClick={handleSave}
                    className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center"
                >
                    <Save className="h-4 w-4 mr-2" />
                    Save Changes
                </button>
            </div>

            <div className="space-y-6">
                {/* Notifications Settings */}
                <div className="bg-white rounded-lg shadow p-6">
                    <div className="flex items-center mb-4">
                        <Bell className="h-5 w-5 text-gray-600 mr-2" />
                        <h2 className="text-lg font-semibold">Notifications</h2>
                    </div>
                    <div className="space-y-4">
                        <div className="flex items-center justify-between">
                            <div>
                                <label className="text-sm font-medium text-gray-700">Email Alerts</label>
                                <p className="text-sm text-gray-500">Receive alerts via email</p>
                            </div>
                            <label className="relative inline-flex items-center cursor-pointer">
                                <input
                                    type="checkbox"
                                    className="sr-only peer"
                                    checked={settings.notifications.emailAlerts}
                                    onChange={(e) => handleSettingChange('notifications', 'emailAlerts', e.target.checked)}
                                />
                                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                            </label>
                        </div>
                        <div className="flex items-center justify-between">
                            <div>
                                <label className="text-sm font-medium text-gray-700">Push Notifications</label>
                                <p className="text-sm text-gray-500">Receive push notifications in browser</p>
                            </div>
                            <label className="relative inline-flex items-center cursor-pointer">
                                <input
                                    type="checkbox"
                                    className="sr-only peer"
                                    checked={settings.notifications.pushNotifications}
                                    onChange={(e) => handleSettingChange('notifications', 'pushNotifications', e.target.checked)}
                                />
                                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                            </label>
                        </div>
                        <div className="flex items-center justify-between">
                            <div>
                                <label className="text-sm font-medium text-gray-700">SMS Alerts</label>
                                <p className="text-sm text-gray-500">Receive critical alerts via SMS</p>
                            </div>
                            <label className="relative inline-flex items-center cursor-pointer">
                                <input
                                    type="checkbox"
                                    className="sr-only peer"
                                    checked={settings.notifications.smsAlerts}
                                    onChange={(e) => handleSettingChange('notifications', 'smsAlerts', e.target.checked)}
                                />
                                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                            </label>
                        </div>
                    </div>
                </div>

                {/* Security Settings */}
                <div className="bg-white rounded-lg shadow p-6">
                    <div className="flex items-center mb-4">
                        <Shield className="h-5 w-5 text-gray-600 mr-2" />
                        <h2 className="text-lg font-semibold">Security</h2>
                    </div>
                    <div className="space-y-4">
                        <div className="flex items-center justify-between">
                            <div>
                                <label className="text-sm font-medium text-gray-700">Two-Factor Authentication</label>
                                <p className="text-sm text-gray-500">Add an extra layer of security</p>
                            </div>
                            <label className="relative inline-flex items-center cursor-pointer">
                                <input
                                    type="checkbox"
                                    className="sr-only peer"
                                    checked={settings.security.twoFactorAuth}
                                    onChange={(e) => handleSettingChange('security', 'twoFactorAuth', e.target.checked)}
                                />
                                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                            </label>
                        </div>
                        <div className="flex items-center justify-between">
                            <div>
                                <label className="text-sm font-medium text-gray-700">Session Timeout (minutes)</label>
                                <p className="text-sm text-gray-500">Auto-logout after inactivity</p>
                            </div>
                            <select
                                value={settings.security.sessionTimeout}
                                onChange={(e) => handleSettingChange('security', 'sessionTimeout', e.target.value)}
                                className="border border-gray-300 rounded px-3 py-1 text-sm"
                            >
                                <option value="15">15</option>
                                <option value="30">30</option>
                                <option value="60">60</option>
                                <option value="120">120</option>
                            </select>
                        </div>
                        <div className="flex items-center justify-between">
                            <div>
                                <label className="text-sm font-medium text-gray-700">Password Expiry (days)</label>
                                <p className="text-sm text-gray-500">Force password change interval</p>
                            </div>
                            <select
                                value={settings.security.passwordExpiry}
                                onChange={(e) => handleSettingChange('security', 'passwordExpiry', e.target.value)}
                                className="border border-gray-300 rounded px-3 py-1 text-sm"
                            >
                                <option value="30">30</option>
                                <option value="60">60</option>
                                <option value="90">90</option>
                                <option value="180">180</option>
                            </select>
                        </div>
                    </div>
                </div>

                {/* System Settings */}
                <div className="bg-white rounded-lg shadow p-6">
                    <div className="flex items-center mb-4">
                        <Database className="h-5 w-5 text-gray-600 mr-2" />
                        <h2 className="text-lg font-semibold">System</h2>
                    </div>
                    <div className="space-y-4">
                        <div className="flex items-center justify-between">
                            <div>
                                <label className="text-sm font-medium text-gray-700">Maintenance Mode</label>
                                <p className="text-sm text-gray-500">Put system in maintenance mode</p>
                            </div>
                            <label className="relative inline-flex items-center cursor-pointer">
                                <input
                                    type="checkbox"
                                    className="sr-only peer"
                                    checked={settings.system.maintenanceMode}
                                    onChange={(e) => handleSettingChange('system', 'maintenanceMode', e.target.checked)}
                                />
                                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                            </label>
                        </div>
                        <div className="flex items-center justify-between">
                            <div>
                                <label className="text-sm font-medium text-gray-700">Debug Mode</label>
                                <p className="text-sm text-gray-500">Enable detailed logging</p>
                            </div>
                            <label className="relative inline-flex items-center cursor-pointer">
                                <input
                                    type="checkbox"
                                    className="sr-only peer"
                                    checked={settings.system.debugMode}
                                    onChange={(e) => handleSettingChange('system', 'debugMode', e.target.checked)}
                                />
                                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                            </label>
                        </div>
                        <div className="flex items-center justify-between">
                            <div>
                                <label className="text-sm font-medium text-gray-700">Log Level</label>
                                <p className="text-sm text-gray-500">Set logging verbosity</p>
                            </div>
                            <select
                                value={settings.system.logLevel}
                                onChange={(e) => handleSettingChange('system', 'logLevel', e.target.value)}
                                className="border border-gray-300 rounded px-3 py-1 text-sm"
                            >
                                <option value="error">Error</option>
                                <option value="warn">Warning</option>
                                <option value="info">Info</option>
                                <option value="debug">Debug</option>
                            </select>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Settings;
