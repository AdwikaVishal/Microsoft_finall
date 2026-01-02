from pydantic import BaseModel, EmailStr, Field
from typing import Optional
from uuid import UUID

from app.db.models import UserRole, UserAbility


# Request Schemas
class UserRegister(BaseModel):
    """Schema for user registration."""
    name: str = Field(..., min_length=2, max_length=255)
    email: EmailStr
    password: str = Field(..., min_length=6)
    role: UserRole = UserRole.USER
    ability: UserAbility = UserAbility.NONE


class UserLogin(BaseModel):
    """Schema for user login."""
    email: EmailStr
    password: str


# Response Schemas
class Token(BaseModel):
    """Schema for JWT token response."""
    access_token: str
    token_type: str = "bearer"


class UserResponse(BaseModel):
    """Schema for user data response."""
    id: UUID
    name: str
    email: str
    role: UserRole
    ability: UserAbility
    
    class Config:
        from_attributes = True


class AuthResponse(BaseModel):
    """Schema for authentication response with token and user data."""
    access_token: str
    token_type: str = "bearer"
    user: UserResponse
