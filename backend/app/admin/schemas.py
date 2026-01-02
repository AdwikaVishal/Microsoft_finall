from pydantic import BaseModel, Field
from typing import Optional


# Request Schemas
class AlertCreate(BaseModel):
    """Schema for creating a new alert (admin only)."""
    title: str = Field(..., min_length=1, max_length=255)
    message: str = Field(..., min_length=1)
    severity: str = Field(..., pattern="^(LOW|MEDIUM|HIGH|CRITICAL)$")


class IncidentUpdate(BaseModel):
    """Schema for updating incident status (admin only)."""
    status: Optional[str] = Field(None, pattern="^(PENDING|UNDER_REVIEW|VERIFIED|HELP_ASSIGNED|RESOLVED)$")
    risk_score: Optional[float] = Field(None, ge=0, le=100)
    risk_level: Optional[str] = None
