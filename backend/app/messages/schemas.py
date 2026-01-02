from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID
from datetime import datetime

from app.db.models import MessageType, UserAbility


# Request Schemas
class MessageCreate(BaseModel):
    """Schema for creating a new message (SOS or Incident report)."""
    message_type: MessageType
    title: str = Field(..., min_length=1, max_length=255)
    content: str = Field(..., min_length=1)
    lat: Optional[float] = Field(None, ge=-90, le=90)
    lng: Optional[float] = Field(None, ge=-180, le=180)
    category: Optional[str] = Field(None, max_length=100)  # For incidents
    severity: Optional[str] = Field(None, max_length=50)  # For incidents
    ability: Optional[UserAbility] = None  # For SOS
    battery: Optional[int] = Field(None, ge=0, le=100)  # For SOS


class SOSMessageCreate(BaseModel):
    """Schema for creating an SOS alert message."""
    title: str = Field(..., min_length=1, max_length=255)
    content: str = Field(..., min_length=1)
    ability: UserAbility
    lat: float = Field(..., ge=-90, le=90)
    lng: float = Field(..., ge=-180, le=180)
    battery: int = Field(..., ge=0, le=100)


class IncidentMessageCreate(BaseModel):
    """Schema for creating an incident report message."""
    title: str = Field(..., min_length=1, max_length=255)
    content: str = Field(..., min_length=1)
    category: str = Field(..., min_length=1, max_length=100)
    severity: str = Field(..., min_length=1, max_length=50)
    lat: float = Field(..., ge=-90, le=90)
    lng: float = Field(..., ge=-180, le=180)
    image_url: Optional[str] = None


# Response Schemas
class MessageResponse(BaseModel):
    """Schema for message response."""
    id: UUID
    user_id: UUID
    user_name: Optional[str] = None  # Include user name in response
    message_type: MessageType
    title: str
    content: str
    lat: Optional[float]
    lng: Optional[float]
    category: Optional[str]
    severity: Optional[str]
    ability: Optional[UserAbility]
    battery: Optional[int]
    is_read: bool
    created_at: datetime
    
    class Config:
        from_attributes = True


class MessageListResponse(BaseModel):
    """Schema for paginated message list."""
    messages: list[MessageResponse]
    total: int
    page: int
    page_size: int


class MessageCreateResponse(BaseModel):
    """Schema for message creation response."""
    id: UUID
    message_type: MessageType
    created_at: datetime
    success: bool = True
    message: str = "Message sent successfully"

