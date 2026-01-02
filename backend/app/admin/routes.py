from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from uuid import UUID

from app.db.database import get_db
from app.core.security import require_admin
from app.db.models import User, Incident, Alert, IncidentStatus, AlertSeverity
from app.incidents.schemas import IncidentResponse, IncidentListResponse
from app.alerts.schemas import AlertResponse
from app.admin.schemas import AlertCreate, IncidentUpdate


router = APIRouter(prefix="/api/admin", tags=["Admin"])


@router.get("/incidents", response_model=IncidentListResponse)
def get_all_incidents(
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    status_filter: str = Query(None),
    db: Session = Depends(get_db),
    admin_user: User = Depends(require_admin)
):
    """
    Get all incidents (admin only).
    
    Supports filtering and pagination:
    - **page**: Page number (default: 1)
    - **page_size**: Items per page (default: 20, max: 100)
    - **status_filter**: Filter by status (optional)
    
    Admin access required.
    """
    # Calculate offset
    offset = (page - 1) * page_size
    
    # Build query
    query = db.query(Incident)
    
    # Apply status filter if provided
    if status_filter:
        try:
            status_enum = IncidentStatus[status_filter]
            query = query.filter(Incident.status == status_enum)
        except KeyError:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Invalid status: {status_filter}"
            )
    
    total = query.count()
    incidents = query.order_by(Incident.created_at.desc()).offset(offset).limit(page_size).all()
    
    # TODO: Log admin action to Azure Application Insights
    # logger.info(f"Admin {admin_user.id} accessed incidents list")
    
    return IncidentListResponse(
        incidents=[IncidentResponse.from_orm(inc) for inc in incidents],
        total=total,
        page=page,
        page_size=page_size
    )


@router.patch("/incidents/{incident_id}/verify", response_model=IncidentResponse)
def verify_incident(
    incident_id: UUID,
    db: Session = Depends(get_db),
    admin_user: User = Depends(require_admin)
):
    """
    Verify an incident (admin only).
    
    Changes incident status to VERIFIED.
    Admin access required.
    """
    incident = db.query(Incident).filter(Incident.id == incident_id).first()
    
    if not incident:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Incident not found"
        )
    
    incident.status = IncidentStatus.VERIFIED
    db.commit()
    db.refresh(incident)
    
    # TODO: Log admin action
    # logger.info(f"Admin {admin_user.id} verified incident {incident_id}")
    
    return IncidentResponse.from_orm(incident)


@router.patch("/incidents/{incident_id}/resolve", response_model=IncidentResponse)
def resolve_incident(
    incident_id: UUID,
    db: Session = Depends(get_db),
    admin_user: User = Depends(require_admin)
):
    """
    Resolve an incident (admin only).
    
    Changes incident status to RESOLVED.
    Admin access required.
    """
    incident = db.query(Incident).filter(Incident.id == incident_id).first()
    
    if not incident:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Incident not found"
        )
    
    incident.status = IncidentStatus.RESOLVED
    db.commit()
    db.refresh(incident)
    
    # TODO: Log admin action
    # logger.info(f"Admin {admin_user.id} resolved incident {incident_id}")
    
    return IncidentResponse.from_orm(incident)


@router.patch("/incidents/{incident_id}", response_model=IncidentResponse)
def update_incident(
    incident_id: UUID,
    update_data: IncidentUpdate,
    db: Session = Depends(get_db),
    admin_user: User = Depends(require_admin)
):
    """
    Update incident details (admin only).
    
    Allows updating status, risk_score, and risk_level.
    Admin access required.
    """
    incident = db.query(Incident).filter(Incident.id == incident_id).first()
    
    if not incident:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Incident not found"
        )
    
    # Update fields if provided
    if update_data.status:
        incident.status = IncidentStatus[update_data.status]
    if update_data.risk_score is not None:
        incident.risk_score = update_data.risk_score
    if update_data.risk_level:
        incident.risk_level = update_data.risk_level
    
    db.commit()
    db.refresh(incident)
    
    return IncidentResponse.from_orm(incident)


@router.post("/alerts", response_model=AlertResponse, status_code=status.HTTP_201_CREATED)
def create_alert(
    alert_data: AlertCreate,
    db: Session = Depends(get_db),
    admin_user: User = Depends(require_admin)
):
    """
    Create a new disaster alert (admin only).
    
    - **title**: Alert title
    - **message**: Alert message
    - **severity**: LOW, MEDIUM, HIGH, or CRITICAL
    
    Admin access required.
    """
    new_alert = Alert(
        title=alert_data.title,
        message=alert_data.message,
        severity=AlertSeverity[alert_data.severity]
    )
    
    db.add(new_alert)
    db.commit()
    db.refresh(new_alert)
    
    # TODO: Send push notifications to all users
    # TODO: Log admin action
    
    return AlertResponse.from_orm(new_alert)
