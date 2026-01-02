export const mockAlerts = [
    {
        id: '1',
        userName: 'Alice Cooper',
        alertType: 'SOS',
        userCategory: 'Blind',
        isVulnerable: true,
        timestamp: '2023-10-01T10:00:00Z',
        status: 'Active',
        description: 'Emergency SOS triggered. User reported being trapped.'
    },
    {
        id: '2',
        userName: 'David Miller',
        alertType: 'Injured',
        userCategory: 'Deaf',
        isVulnerable: false,
        timestamp: '2023-10-01T09:30:00Z',
        status: 'Pending',
        description: 'User reported injury in sector 5.'
    },
    {
        id: '3',
        userName: 'Sophie Chen',
        alertType: 'Trapped',
        userCategory: 'Elderly',
        isVulnerable: true,
        timestamp: '2023-10-01T08:45:00Z',
        status: 'Resolved',
        description: 'User was trapped but has been rescued.'
    },
    {
        id: '4',
        userName: 'Marcus Thorne',
        alertType: 'Safe',
        userCategory: 'Normal',
        isVulnerable: false,
        timestamp: '2023-10-01T08:00:00Z',
        status: 'Resolved',
        description: 'User confirmed safe after incident.'
    }
];
