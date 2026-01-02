"""
Script to create an admin user for SenseSafe backend.
Run this after setting up the database.
"""

import sys
import os

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.db.database import SessionLocal
from app.db.models import User, UserRole, UserAbility
from app.core.security import hash_password


def create_admin():
    """Create an admin user."""
    db = SessionLocal()
    
    try:
        # Check if admin already exists
        existing_admin = db.query(User).filter(
            User.email == "admin@sensesafe.com"
        ).first()
        
        if existing_admin:
            print("❌ Admin user already exists!")
            print(f"   Email: {existing_admin.email}")
            return
        
        # Create admin user
        admin = User(
            name="Admin User",
            email="admin@sensesafe.com",
            password_hash=hash_password("admin123"),
            role=UserRole.ADMIN,
            ability=UserAbility.NONE
        )
        
        db.add(admin)
        db.commit()
        db.refresh(admin)
        
        print("✅ Admin user created successfully!")
        print(f"   Email: {admin.email}")
        print(f"   Password: admin123")
        print(f"   Role: {admin.role}")
        print(f"   ID: {admin.id}")
        print("\n⚠️  Please change the password after first login!")
        
    except Exception as e:
        print(f"❌ Error creating admin user: {e}")
        db.rollback()
    finally:
        db.close()


if __name__ == "__main__":
    create_admin()
