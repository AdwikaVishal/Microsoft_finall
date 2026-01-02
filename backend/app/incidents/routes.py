from fastapi import APIRouter, Depends, status, Query
from sqlalchemy.orm import Session
from uuid import UUID

from app.db.database import get_db
# from app.core.security import require_user   <-- removed for now
from app.db.models import User
from app.incidents.schemas import (
    IncidentCreate,
    IncidentResponse,
    IncidentListResponse
)
from app.incidents.service import (
    create_incident,
    get_user_incidents,
    get_incident_by_id
)

router = APIRouter(prefix="/api/incidents", tags=["Incidents"])


@router.post("", response_model=IncidentResponse, status_code=status.HTTP_201_CREATED)
def report_incident(
    incident_data: IncidentCreate,
    db: Session = Depends(get_db)
):
    """
    Report a new incident.

    Returns the created incident with PENDING status.
    """
    # For testing, we pass None as user since auth is disabled
    return create_incident(db, incident_data, None)


@router.get("/user", response_model=IncidentListResponse)
def get_my_incidents(
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db)
):
    """
    Get incidents (testing mode: NOT filtered by user)
    """
    return get_user_incidents(db, None, page, page_size)


@router.get("/{incident_id}", response_model=IncidentResponse)
def get_incident(
    incident_id: UUID,
    db: Session = Depends(get_db)
):
    """
    Get incident by ID (testing mode, no auth check)
    """
    return get_incident_by_id(db, incident_id, None)
