#!/usr/bin/env python3
"""
Test script to verify admin dashboard backend connectivity
"""

import requests
import json
from datetime import datetime

BASE_URL = "http://192.168.0.130:8000"

def test_endpoint(endpoint, method="GET", data=None, headers=None):
    """Test a single endpoint"""
    url = f"{BASE_URL}{endpoint}"
    
    try:
        if method == "GET":
            response = requests.get(url, headers=headers)
        elif method == "POST":
            response = requests.post(url, json=data, headers=headers)
        elif method == "PATCH":
            response = requests.patch(url, json=data, headers=headers)
        
        print(f"✅ {method} {endpoint}: {response.status_code}")
        if response.status_code < 400:
            try:
                result = response.json()
                if isinstance(result, dict):
                    if 'sos_alerts' in result:
                        print(f"   📊 Found {len(result['sos_alerts'])} SOS alerts")
                    elif 'incidents' in result:
                        print(f"   📊 Found {len(result['incidents'])} incidents")
                    elif 'messages' in result:
                        print(f"   📊 Found {len(result['messages'])} messages")
                    else:
                        print(f"   📊 Response keys: {list(result.keys())}")
                return True
            except:
                print(f"   📄 Non-JSON response")
                return True
        else:
            print(f"   ❌ Error: {response.text[:100]}")
            return False
    except Exception as e:
        print(f"❌ {method} {endpoint}: Connection failed - {e}")
        return False

def main():
    print("🔍 Testing SenseSafe Backend Connectivity")
    print("=" * 50)
    
    # Test basic endpoints
    endpoints = [
        "/health",
        "/api/sos/user",
        "/api/incidents/user", 
        "/api/messages/admin/all",
        "/api/admin/incidents",
        "/api/admin/sos"
    ]
    
    success_count = 0
    total_count = len(endpoints)
    
    for endpoint in endpoints:
        if test_endpoint(endpoint):
            success_count += 1
    
    print("\n" + "=" * 50)
    print(f"📊 Results: {success_count}/{total_count} endpoints working")
    
    if success_count == total_count:
        print("✅ All endpoints are working! Admin dashboard should connect properly.")
    else:
        print("⚠️  Some endpoints failed. Check backend server status.")
    
    print(f"\n🌐 Backend URL: {BASE_URL}")
    print("🔧 Make sure backend server is running with: python -m uvicorn app.main:app --host 0.0.0.0 --port 8000")

if __name__ == "__main__":
    main()