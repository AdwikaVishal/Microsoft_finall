import React from 'react';
import { Shield } from 'lucide-react';

function VulnerableBadge({ isVulnerable }) {
    if (!isVulnerable) return null;

    return (
        <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
            <Shield className="h-3 w-3 mr-1" />
            Vulnerable
        </span>
    );
}

export default VulnerableBadge;
