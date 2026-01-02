// localStorage utility functions for SenseSafe

const STORAGE_KEYS = {
  ABILITY_PROFILE: 'sensesafe_ability_profile',
  USER_SETTINGS: 'sensesafe_user_settings',
  OFFLINE_DATA: 'sensesafe_offline_data',
};

/**
 * Save ability profile to localStorage
 * @param {Object} profile - The ability profile object
 */
export const setAbilityProfile = (profile) => {
  try {
    localStorage.setItem(STORAGE_KEYS.ABILITY_PROFILE, JSON.stringify(profile));
  } catch (error) {
    console.error('Error saving ability profile:', error);
  }
};

/**
 * Get ability profile from localStorage
 * @returns {Object|null} The saved ability profile or null if not found
 */
export const getAbilityProfile = () => {
  try {
    const profile = localStorage.getItem(STORAGE_KEYS.ABILITY_PROFILE);
    return profile ? JSON.parse(profile) : null;
  } catch (error) {
    console.error('Error loading ability profile:', error);
    return null;
  }
};

/**
 * Remove ability profile from localStorage
 */
export const removeAbilityProfile = () => {
  try {
    localStorage.removeItem(STORAGE_KEYS.ABILITY_PROFILE);
  } catch (error) {
    console.error('Error removing ability profile:', error);
  }
};

/**
 * Save user settings to localStorage
 * @param {Object} settings - User settings object
 */
export const setUserSettings = (settings) => {
  try {
    localStorage.setItem(STORAGE_KEYS.USER_SETTINGS, JSON.stringify(settings));
  } catch (error) {
    console.error('Error saving user settings:', error);
  }
};

/**
 * Get user settings from localStorage
 * @returns {Object|null} The saved user settings or null
 */
export const getUserSettings = () => {
  try {
    const settings = localStorage.getItem(STORAGE_KEYS.USER_SETTINGS);
    return settings ? JSON.parse(settings) : null;
  } catch (error) {
    console.error('Error loading user settings:', error);
    return null;
  }
};

/**
 * Save offline data (incidents, reports, etc.)
 * @param {string} key - Data key
 * @param {any} data - Data to save
 */
export const setOfflineData = (key, data) => {
  try {
    const offlineData = getOfflineData();
    offlineData[key] = {
      data,
      timestamp: new Date().toISOString(),
    };
    localStorage.setItem(STORAGE_KEYS.OFFLINE_DATA, JSON.stringify(offlineData));
  } catch (error) {
    console.error('Error saving offline data:', error);
  }
};

/**
 * Get offline data
 * @returns {Object} The offline data object
 */
export const getOfflineData = () => {
  try {
    const data = localStorage.getItem(STORAGE_KEYS.OFFLINE_DATA);
    return data ? JSON.parse(data) : {};
  } catch (error) {
    console.error('Error loading offline data:', error);
    return {};
  }
};

/**
 * Get specific offline data by key
 * @param {string} key - Data key
 * @returns {any|null} The saved data or null
 */
export const getOfflineDataByKey = (key) => {
  try {
    const offlineData = getOfflineData();
    return offlineData[key] ? offlineData[key].data : null;
  } catch (error) {
    console.error('Error loading offline data by key:', error);
    return null;
  }
};

/**
 * Clear all offline data
 */
export const clearOfflineData = () => {
  try {
    localStorage.removeItem(STORAGE_KEYS.OFFLINE_DATA);
  } catch (error) {
    console.error('Error clearing offline data:', error);
  }
};

/**
 * Check if storage is available
 * @returns {boolean} True if localStorage is available
 */
export const isStorageAvailable = () => {
  try {
    const test = '__storage_test__';
    localStorage.setItem(test, test);
    localStorage.removeItem(test);
    return true;
  } catch (error) {
    return false;
  }
};

/**
 * Get storage usage info
 * @returns {Object} Storage usage information
 */
export const getStorageInfo = () => {
  try {
    let totalSize = 0;
    const info = {};
    
    for (const key in localStorage) {
      if (localStorage.hasOwnProperty(key)) {
        const size = localStorage[key].length;
        totalSize += size;
        info[key] = {
          size: size,
          sizeKB: (size / 1024).toFixed(2),
        };
      }
    }
    
    return {
      totalSize,
      totalSizeKB: (totalSize / 1024).toFixed(2),
      items: info,
    };
  } catch (error) {
    console.error('Error getting storage info:', error);
    return null;
  }
};
