#!/usr/bin/env python3
"""Script to create admin user"""

from app.db.database import SessionLocal
from app.db.models import User, UserRole, UserAbility
from app.core.security import hash_password

def create_admin():
    db = SessionLocal()
    try:
        # Check if admin exists
        admin = db.query(User).filter(User.email == "admin@sensesafe.com").first()
        
        # Hash password using the same method as the app
        hashed_pw = hash_password("admin123")
        
        if admin:
            print(f"Admin user already exists: {admin.email}")
            # Update password
            admin.password_hash = hashed_pw
            db.commit()
            print("✅ Password updated successfully!")
        else:
            # Create new admin
            print("Creating new admin user...")
            new_admin = User(
                name="System Admin",
                email="admin@sensesafe.com",
                password_hash=hashed_pw,
                role=UserRole.ADMIN,
                ability=UserAbility.NONE
            )
            db.add(new_admin)
            db.commit()
            print("✅ Admin user created successfully!")
            print("Email: admin@sensesafe.com")
            print("Password: admin123")
    except Exception as e:
        print(f"❌ Error: {e}")
        import traceback
        traceback.print_exc()
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    create_admin()
