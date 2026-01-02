from sqlalchemy.orm import Session
from fastapi import HTTPException, status
from uuid import UUID

from app.db.models import Incident, User, IncidentStatus
from app.incidents.schemas import IncidentCreate, IncidentResponse, IncidentListResponse


def create_incident(db: Session, incident_data: IncidentCreate, user: User) -> IncidentResponse:
    """Create a new incident report."""
    
    # Create incident
    new_incident = Incident(
        user_id = user.id if user is not None else None,
        type=incident_data.type,
        description=incident_data.description,
        lat=incident_data.lat,
        lng=incident_data.lng,
        image_url=incident_data.image_url,
        status=IncidentStatus.PENDING
    )
    
    # TODO: Later call ML model to assign risk_score and risk_level
    # from app.ai.computer_vision import analyze_image
    # if incident_data.image_url:
    #     ml_result = await analyze_image(incident_data.image_url)
    #     new_incident.risk_score = ml_result.get('risk_score')
    #     new_incident.risk_level = ml_result.get('risk_level')
    
    db.add(new_incident)
    db.commit()
    db.refresh(new_incident)
    
    return IncidentResponse.from_orm(new_incident)


def get_user_incidents(db: Session, user: User, page: int = 1, page_size: int = 20) -> IncidentListResponse:
    """Get all incidents reported by the current user."""
    
    # Calculate offset
    offset = (page - 1) * page_size
    
    # Query incidents
    query = db.query(Incident).filter(Incident.user_id == user.id)
    total = query.count()
    incidents = query.order_by(Incident.created_at.desc()).offset(offset).limit(page_size).all()
    
    return IncidentListResponse(
        incidents=[IncidentResponse.from_orm(inc) for inc in incidents],
        total=total,
        page=page,
        page_size=page_size
    )


def get_incident_by_id(db: Session, incident_id: UUID, user: User) -> IncidentResponse:
    """Get a specific incident by ID."""
    
    incident = db.query(Incident).filter(
        Incident.id == incident_id,
        Incident.user_id == user.id
    ).first()
    
    if not incident:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Incident not found"
        )
    
    return IncidentResponse.from_orm(incident)
