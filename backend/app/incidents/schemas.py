from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID
from datetime import datetime

from app.db.models import IncidentStatus


# Request Schemas
class IncidentCreate(BaseModel):
    """Schema for creating a new incident report."""
    type: str = Field(..., min_length=1, max_length=100)
    description: str = Field(..., min_length=10)
    lat: float = Field(..., ge=-90, le=90)
    lng: float = Field(..., ge=-180, le=180)
    image_url: Optional[str] = None


# Response Schemas
class IncidentResponse(BaseModel):
    """Schema for incident data response."""
    id: UUID
    user_id: Optional[UUID] = None
    type: str
    description: str
    lat: float
    lng: float
    status: IncidentStatus
    image_url: Optional[str]
    risk_score: Optional[float]
    risk_level: Optional[str]
    created_at: datetime
    
    class Config:
        from_attributes = True


class IncidentListResponse(BaseModel):
    """Schema for paginated incident list."""
    incidents: list[IncidentResponse]
    total: int
    page: int
    page_size: int
