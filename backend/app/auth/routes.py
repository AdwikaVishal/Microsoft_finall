from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.core.security import get_current_user
from app.db.models import User
from app.auth.schemas import UserRegister, UserLogin, AuthResponse, UserResponse
from app.auth.service import register_user, login_user, get_current_user_info


router = APIRouter(prefix="/api/auth", tags=["Authentication"])


@router.post("/register", response_model=AuthResponse, status_code=status.HTTP_201_CREATED)
def register(user_data: UserRegister, db: Session = Depends(get_db)):
    """
    Register a new user.
    
    - **name**: User's full name
    - **email**: Valid email address
    - **password**: Minimum 6 characters
    - **role**: USER or ADMIN (default: USER)
    - **ability**: Accessibility requirement (default: NONE)
    
    Returns JWT token and user information.
    """
    return register_user(db, user_data)


@router.post("/login", response_model=AuthResponse)
def login(credentials: UserLogin, db: Session = Depends(get_db)):
    """
    Login with email and password.
    
    - **email**: Registered email address
    - **password**: User's password
    
    Returns JWT token and user information.
    """
    return login_user(db, credentials)


@router.get("/me", response_model=UserResponse)
def get_me(current_user: User = Depends(get_current_user)):
    """
    Get current authenticated user information.
    
    Requires valid JWT token in Authorization header.
    """
    return get_current_user_info(current_user)
