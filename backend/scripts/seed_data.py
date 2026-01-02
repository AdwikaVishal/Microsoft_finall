"""
Script to populate the database with test data.
Useful for development and testing.
"""

import sys
import os
from datetime import datetime, timedelta
import random

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.db.database import SessionLocal
from app.db.models import (
    User, Incident, SOS, Alert,
    UserRole, UserAbility, IncidentStatus, SOSStatus, AlertSeverity
)
from app.core.security import hash_password


def create_test_data():
    """Create test data for development."""
    db = SessionLocal()
    
    try:
        print("🌱 Seeding database with test data...")
        
        # Create test users
        print("\n👥 Creating test users...")
        users = []
        
        # Regular users
        for i in range(5):
            user = User(
                name=f"Test User {i+1}",
                email=f"user{i+1}@test.com",
                password_hash=hash_password("test123"),
                role=UserRole.USER,
                ability=random.choice(list(UserAbility))
            )
            users.append(user)
            db.add(user)
        
        # Admin user
        admin = User(
            name="Test Admin",
            email="admin@test.com",
            password_hash=hash_password("admin123"),
            role=UserRole.ADMIN,
            ability=UserAbility.NONE
        )
        users.append(admin)
        db.add(admin)
        
        db.commit()
        print(f"   ✅ Created {len(users)} users")
        
        # Create test incidents
        print("\n🚨 Creating test incidents...")
        incident_types = ["Fire", "Flood", "Accident", "Medical Emergency", "Building Collapse"]
        locations = [
            (40.7128, -74.0060),  # New York
            (34.0522, -118.2437),  # Los Angeles
            (41.8781, -87.6298),   # Chicago
            (29.7604, -95.3698),   # Houston
            (33.4484, -112.0740),  # Phoenix
        ]
        
        incidents = []
        for i in range(20):
            user = random.choice(users[:-1])  # Exclude admin
            lat, lng = random.choice(locations)
            
            incident = Incident(
                user_id=user.id,
                type=random.choice(incident_types),
                description=f"Test incident description {i+1}. This is a simulated emergency for testing purposes.",
                lat=lat + random.uniform(-0.1, 0.1),
                lng=lng + random.uniform(-0.1, 0.1),
                status=random.choice(list(IncidentStatus)),
                risk_score=random.uniform(0, 100) if random.random() > 0.5 else None,
                risk_level=random.choice(["LOW", "MEDIUM", "HIGH", "CRITICAL"]) if random.random() > 0.5 else None,
                created_at=datetime.utcnow() - timedelta(days=random.randint(0, 30))
            )
            incidents.append(incident)
            db.add(incident)
        
        db.commit()
        print(f"   ✅ Created {len(incidents)} incidents")
        
        # Create test SOS alerts
        print("\n🆘 Creating test SOS alerts...")
        sos_alerts = []
        for i in range(10):
            user = random.choice(users[:-1])
            lat, lng = random.choice(locations)
            
            sos = SOS(
                user_id=user.id,
                ability=user.ability,
                lat=lat + random.uniform(-0.1, 0.1),
                lng=lng + random.uniform(-0.1, 0.1),
                battery=random.randint(5, 100),
                status=random.choice(list(SOSStatus)),
                created_at=datetime.utcnow() - timedelta(hours=random.randint(0, 72))
            )
            sos_alerts.append(sos)
            db.add(sos)
        
        db.commit()
        print(f"   ✅ Created {len(sos_alerts)} SOS alerts")
        
        # Create test disaster alerts
        print("\n🔔 Creating test alerts...")
        alert_data = [
            ("Flood Warning", "Heavy rainfall expected in downtown area. Avoid low-lying areas.", AlertSeverity.HIGH),
            ("Heat Advisory", "Extreme heat conditions. Stay hydrated and avoid outdoor activities.", AlertSeverity.MEDIUM),
            ("Earthquake Alert", "Minor earthquake detected. Check for structural damage.", AlertSeverity.CRITICAL),
            ("Storm Warning", "Severe thunderstorm approaching. Seek shelter immediately.", AlertSeverity.HIGH),
            ("Air Quality Alert", "Poor air quality due to wildfire smoke. Limit outdoor exposure.", AlertSeverity.MEDIUM),
        ]
        
        alerts = []
        for title, message, severity in alert_data:
            alert = Alert(
                title=title,
                message=message,
                severity=severity,
                created_at=datetime.utcnow() - timedelta(days=random.randint(0, 7))
            )
            alerts.append(alert)
            db.add(alert)
        
        db.commit()
        print(f"   ✅ Created {len(alerts)} alerts")
        
        # Summary
        print("\n" + "="*50)
        print("✨ Database seeded successfully!")
        print("="*50)
        print(f"\n📊 Summary:")
        print(f"   Users: {len(users)}")
        print(f"   Incidents: {len(incidents)}")
        print(f"   SOS Alerts: {len(sos_alerts)}")
        print(f"   Disaster Alerts: {len(alerts)}")
        print(f"\n🔐 Test Credentials:")
        print(f"   Regular User: user1@test.com / test123")
        print(f"   Admin User: admin@test.com / admin123")
        print("\n")
        
    except Exception as e:
        print(f"\n❌ Error seeding database: {e}")
        db.rollback()
    finally:
        db.close()


if __name__ == "__main__":
    create_test_data()
