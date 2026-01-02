import React from 'react';

const StatusBadge = ({ status }) => {
  const getStatusColor = (status) => {
    switch (status) {
      case 'Active':
        return 'bg-red-500';
      case 'Pending':
        return 'bg-yellow-500';
      case 'Resolved':
        return 'bg-green-500';
      default:
        return 'bg-gray-500';
    }
  };

  return (
    <span className={`inline-block px-2 py-1 text-xs font-semibold text-white rounded-full ${getStatusColor(status)}`}>
      {status}
    </span>
  );
};

export default StatusBadge;
