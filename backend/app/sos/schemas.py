from pydantic import BaseModel, Field
from uuid import UUID
from datetime import datetime

from app.db.models import SOSStatus, UserAbility


# Request Schemas
class SOSCreate(BaseModel):
    """Schema for creating a new SOS alert."""
    ability: UserAbility
    lat: float = Field(..., ge=-90, le=90)
    lng: float = Field(..., ge=-180, le=180)
    battery: int = Field(..., ge=0, le=100)
    status: SOSStatus


# Response Schemas
class SOSResponse(BaseModel):
    """Schema for SOS alert response."""
    id: UUID
    user_id: UUID
    ability: UserAbility
    lat: float
    lng: float
    battery: int
    status: SOSStatus
    created_at: datetime
    
    class Config:
        from_attributes = True


class SOSListResponse(BaseModel):
    """Schema for paginated SOS list."""
    sos_alerts: list[SOSResponse]
    total: int
    page: int
    page_size: int
