from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from uuid import UUID

from app.db.database import get_db
from app.core.security import require_user, require_admin
from app.db.models import User
from app.messages.schemas import (
    MessageCreate,
    MessageResponse,
    MessageListResponse,
    MessageCreateResponse,
    SOSMessageCreate,
    IncidentMessageCreate
)
from app.messages.service import (
    create_message,
    create_sos_message,
    create_incident_message,
    get_user_messages,
    get_all_messages,
    mark_message_read,
    get_message_stats
)


router = APIRouter(prefix="/api/messages", tags=["Messages"])


@router.post("", response_model=MessageResponse, status_code=status.HTTP_201_CREATED)
def send_message(
    message_data: MessageCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_user)
):
    """
    Send a new message (SOS, Incident report, or General message).
    
    - **message_type**: Type of message (SOS, INCIDENT, or GENERAL)
    - **title**: Message title
    - **content**: Message content
    - **lat/lng**: Optional location coordinates
    - **category**: For incidents (fire, flood, etc.)
    - **severity**: For incidents (low, medium, high, critical)
    - **ability**: For SOS (user's accessibility requirement)
    - **battery**: For SOS (device battery percentage)
    
    Returns the created message with ID and timestamp.
    """
    return create_message(db, message_data, current_user)


@router.post("/sos", response_model=MessageResponse, status_code=status.HTTP_201_CREATED)
def send_sos_alert(
    sos_data: SOSMessageCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_user)
):
    """
    Send an SOS emergency alert.
    
    This endpoint:
    - Creates a message for the admin dashboard
    - Stores the SOS alert for emergency response
    
    - **title**: Alert title
    - **content**: Additional details about the emergency
    - **ability**: User's accessibility requirement
    - **lat**: Current latitude
    - **lng**: Current longitude
    - **battery**: Device battery percentage (0-100)
    
    Returns the created SOS alert with ID and timestamp.
    """
    return create_sos_message(db, sos_data, current_user)


@router.post("/incident", response_model=MessageResponse, status_code=status.HTTP_201_CREATED)
def report_incident_message(
    incident_data: IncidentMessageCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_user)
):
    """
    Report an incident.
    
    This endpoint:
    - Creates a message for the admin dashboard
    - Stores the incident report for response teams
    
    - **title**: Incident title
    - **content**: Detailed description of the incident
    - **category**: Type of incident (fire, flood, earthquake, etc.)
    - **severity**: Severity level (low, medium, high, critical)
    - **lat**: Location latitude
    - **lng**: Location longitude
    - **image_url**: Optional URL to incident photo
    
    Returns the created incident report with ID and timestamp.
    """
    return create_incident_message(db, incident_data, current_user)


@router.get("", response_model=MessageListResponse)
def get_my_messages(
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db),
    current_user: User = Depends(require_user)
):
    """
    Get all messages sent by the current user.
    
    Supports pagination:
    - **page**: Page number (default: 1)
    - **page_size**: Items per page (default: 20, max: 100)
    
    Returns a paginated list of messages with details.
    """
    return get_user_messages(db, current_user, page, page_size)


@router.get("/{message_id}", response_model=MessageResponse)
def get_message(
    message_id: UUID,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_user)
):
    """
    Get a specific message by ID.
    
    Only returns messages created by the current user.
    """
    message = db.query(Message).filter(
        Message.id == message_id,
        Message.user_id == current_user.id
    ).first()
    
    if not message:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Message not found"
        )
    
    return MessageResponse(
        id=message.id,
        user_id=message.user_id,
        user_name=current_user.name,
        message_type=message.message_type,
        title=message.title,
        content=message.content,
        lat=message.lat,
        lng=message.lng,
        category=message.category,
        severity=message.severity,
        ability=message.ability,
        battery=message.battery,
        is_read=bool(message.is_read),
        created_at=message.created_at
    )


@router.post("/{message_id}/read", response_model=MessageResponse)
def mark_as_read(
    message_id: UUID,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_user)
):
    """
    Mark a message as read.
    
    Users can only mark their own messages as read.
    """
    return mark_message_read(db, message_id, current_user, is_admin=False)


# Admin-only routes below
from app.db.models import Message


@router.get("/admin/all", response_model=MessageListResponse)
def get_all_messages_admin(
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    message_type: str = Query(None),
    is_read: str = Query(None),
    db: Session = Depends(get_db),
    admin_user: User = Depends(require_admin)
):
    """
    Get all messages from all users (admin only).
    
    Supports filtering and pagination:
    - **page**: Page number (default: 1)
    - **page_size**: Items per page (default: 20, max: 100)
    - **message_type**: Filter by type (SOS, INCIDENT, GENERAL)
    - **is_read**: Filter by read status (true/false)
    
    Admin access required.
    """
    return get_all_messages(db, page, page_size, message_type, is_read)


@router.get("/admin/stats")
def get_message_stats_admin(
    db: Session = Depends(get_db),
    admin_user: User = Depends(require_admin)
):
    """
    Get message statistics for admin dashboard (admin only).
    
    Returns:
    - Total messages
    - Unread messages
    - Read messages
    - Count by message type (SOS, INCIDENT, GENERAL)
    
    Admin access required.
    """
    return get_message_stats(db)


@router.post("/admin/{message_id}/read", response_model=MessageResponse)
def admin_mark_read(
    message_id: UUID,
    db: Session = Depends(get_db),
    admin_user: User = Depends(require_admin)
):
    """
    Mark a message as read (admin only).
    
    Admins can mark any message as read.
    """
    return mark_message_read(db, message_id, admin_user, is_admin=True)


@router.get("/admin/unread/count")
def get_unread_count_admin(
    db: Session = Depends(get_db),
    admin_user: User = Depends(require_admin)
):
    """
    Get count of unread messages (admin only).
    
    Admin access required.
    """
    from app.db.models import Message
    count = db.query(Message).filter(Message.is_read == 0).count()
    return {"unread_count": count}

