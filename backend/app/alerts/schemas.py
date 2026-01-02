from pydantic import BaseModel, Field
from uuid import UUID
from datetime import datetime

from app.db.models import AlertSeverity


# Response Schemas
class AlertResponse(BaseModel):
    """Schema for alert response."""
    id: UUID
    title: str
    message: str
    severity: AlertSeverity
    created_at: datetime
    
    class Config:
        from_attributes = True


class AlertListResponse(BaseModel):
    """Schema for paginated alert list."""
    alerts: list[AlertResponse]
    total: int
    page: int
    page_size: int
