from sqlalchemy.orm import Session
from fastapi import HTTPException, status

from app.db.models import User
from app.core.security import hash_password, verify_password, create_access_token
from app.auth.schemas import UserRegister, UserLogin, AuthResponse, UserResponse


def register_user(db: Session, user_data: UserRegister) -> AuthResponse:
    """Register a new user and return auth token."""
    
    # Check if user already exists
    existing_user = db.query(User).filter(User.email == user_data.email).first()
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email already registered"
        )
    
    # Create new user
    new_user = User(
        name=user_data.name,
        email=user_data.email,
        password_hash=hash_password(user_data.password),
        role=user_data.role,
        ability=user_data.ability
    )
    
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    
    # Generate token
    access_token = create_access_token(data={"sub": str(new_user.id)})
    
    return AuthResponse(
        access_token=access_token,
        user=UserResponse.from_orm(new_user)
    )


def login_user(db: Session, credentials: UserLogin) -> AuthResponse:
    """Authenticate user and return auth token."""
    
    # Find user by email
    user = db.query(User).filter(User.email == credentials.email).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password"
        )
    
    # Verify password
    if not verify_password(credentials.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password"
        )
    
    # Generate token
    access_token = create_access_token(data={"sub": str(user.id)})
    
    return AuthResponse(
        access_token=access_token,
        user=UserResponse.from_orm(user)
    )


def get_current_user_info(user: User) -> UserResponse:
    """Get current user information."""
    return UserResponse.from_orm(user)
