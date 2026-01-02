from sqlalchemy.orm import Session
from fastapi import HTTPException, status
from uuid import UUID

from app.db.models import User, Message, MessageType, IncidentStatus, SOSStatus
from app.messages.schemas import (
    MessageCreate, 
    MessageResponse, 
    MessageListResponse,
    SOSMessageCreate,
    IncidentMessageCreate
)


def create_message(db: Session, message_data: MessageCreate, user: User) -> MessageResponse:
    """Create a new message (SOS or Incident report)."""
    
    # Determine message type and create appropriate title
    if message_data.message_type == MessageType.SOS:
        title = f"SOS Alert: {message_data.ability.value if message_data.ability else 'Emergency'}"
    elif message_data.message_type == MessageType.INCIDENT:
        title = f"Incident Report: {message_data.category or 'General'}"
    else:
        title = message_data.title
    
    # Create message
    new_message = Message(
        user_id=user.id,
        message_type=message_data.message_type,
        title=title,
        content=message_data.content,
        lat=message_data.lat,
        lng=message_data.lng,
        category=message_data.category,
        severity=message_data.severity,
        ability=message_data.ability,
        battery=message_data.battery,
        is_read=0  # Unread by default
    )
    
    db.add(new_message)
    db.commit()
    db.refresh(new_message)
    
    return MessageResponse(
        id=new_message.id,
        user_id=new_message.user_id,
        user_name=user.name,
        message_type=new_message.message_type,
        title=new_message.title,
        content=new_message.content,
        lat=new_message.lat,
        lng=new_message.lng,
        category=new_message.category,
        severity=new_message.severity,
        ability=new_message.ability,
        battery=new_message.battery,
        is_read=bool(new_message.is_read),
        created_at=new_message.created_at
    )


def create_sos_message(db: Session, sos_data: SOSMessageCreate, user: User) -> MessageResponse:
    """Create an SOS alert message and also save to SOS table."""
    
    # Create message
    message = Message(
        user_id=user.id,
        message_type=MessageType.SOS,
        title=f"SOS Alert: {sos_data.ability.value}",
        content=sos_data.content,
        lat=sos_data.lat,
        lng=sos_data.lng,
        ability=sos_data.ability,
        battery=sos_data.battery,
        is_read=0
    )
    
    db.add(message)
    db.commit()
    db.refresh(message)
    
    return MessageResponse(
        id=message.id,
        user_id=message.user_id,
        user_name=user.name,
        message_type=message.message_type,
        title=message.title,
        content=message.content,
        lat=message.lat,
        lng=message.lng,
        category=None,
        severity=None,
        ability=message.ability,
        battery=message.battery,
        is_read=bool(message.is_read),
        created_at=message.created_at
    )


def create_incident_message(db: Session, incident_data: IncidentMessageCreate, user: User) -> MessageResponse:
    """Create an incident report message and also save to Incident table."""
    
    # Create message
    message = Message(
        user_id=user.id,
        message_type=MessageType.INCIDENT,
        title=f"Incident Report: {incident_data.category}",
        content=incident_data.content,
        lat=incident_data.lat,
        lng=incident_data.lng,
        category=incident_data.category,
        severity=incident_data.severity,
        is_read=0
    )
    
    db.add(message)
    db.commit()
    db.refresh(message)
    
    return MessageResponse(
        id=message.id,
        user_id=message.user_id,
        user_name=user.name,
        message_type=message.message_type,
        title=message.title,
        content=message.content,
        lat=message.lat,
        lng=message.lng,
        category=message.category,
        severity=message.severity,
        ability=None,
        battery=None,
        is_read=bool(message.is_read),
        created_at=message.created_at
    )


def get_user_messages(db: Session, user: User, page: int = 1, page_size: int = 20) -> MessageListResponse:
    """Get all messages sent by the current user."""
    
    offset = (page - 1) * page_size
    
    query = db.query(Message).filter(Message.user_id == user.id)
    total = query.count()
    messages = query.order_by(Message.created_at.desc()).offset(offset).limit(page_size).all()
    
    return MessageListResponse(
        messages=[
            MessageResponse(
                id=msg.id,
                user_id=msg.user_id,
                user_name=user.name,
                message_type=msg.message_type,
                title=msg.title,
                content=msg.content,
                lat=msg.lat,
                lng=msg.lng,
                category=msg.category,
                severity=msg.severity,
                ability=msg.ability,
                battery=msg.battery,
                is_read=bool(msg.is_read),
                created_at=msg.created_at
            )
            for msg in messages
        ],
        total=total,
        page=page,
        page_size=page_size
    )


def get_all_messages(
    db: Session, 
    page: int = 1, 
    page_size: int = 20,
    message_type: str = None,
    is_read: str = None
) -> MessageListResponse:
    """Get all messages (admin only)."""
    
    offset = (page - 1) * page_size
    
    query = db.query(Message).join(User)
    
    # Apply filters
    if message_type:
        try:
            query = query.filter(Message.message_type == MessageType[message_type])
        except KeyError:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Invalid message type: {message_type}"
            )
    
    if is_read is not None:
        read_value = 1 if is_read.lower() == "true" else 0
        query = query.filter(Message.is_read == read_value)
    
    total = query.count()
    messages = query.order_by(Message.created_at.desc()).offset(offset).limit(page_size).all()
    
    return MessageListResponse(
        messages=[
            MessageResponse(
                id=msg.id,
                user_id=msg.user_id,
                user_name=msg.user.name if msg.user else None,
                message_type=msg.message_type,
                title=msg.title,
                content=msg.content,
                lat=msg.lat,
                lng=msg.lng,
                category=msg.category,
                severity=msg.severity,
                ability=msg.ability,
                battery=msg.battery,
                is_read=bool(msg.is_read),
                created_at=msg.created_at
            )
            for msg in messages
        ],
        total=total,
        page=page,
        page_size=page_size
    )


def mark_message_read(db: Session, message_id: UUID, user: User, is_admin: bool = False) -> MessageResponse:
    """Mark a message as read."""
    
    message = db.query(Message).filter(Message.id == message_id).first()
    
    if not message:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Message not found"
        )
    
    # If not admin, user can only mark their own messages
    if not is_admin and message.user_id != user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to mark this message as read"
        )
    
    message.is_read = 1
    db.commit()
    db.refresh(message)
    
    return MessageResponse(
        id=message.id,
        user_id=message.user_id,
        user_name=message.user.name if message.user else None,
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


def get_message_stats(db: Session) -> dict:
    """Get message statistics for admin dashboard."""
    
    total = db.query(Message).count()
    unread = db.query(Message).filter(Message.is_read == 0).count()
    sos_count = db.query(Message).filter(Message.message_type == MessageType.SOS).count()
    incident_count = db.query(Message).filter(Message.message_type == MessageType.INCIDENT).count()
    general_count = db.query(Message).filter(Message.message_type == MessageType.GENERAL).count()
    
    return {
        "total": total,
        "unread": unread,
        "read": total - unread,
        "by_type": {
            "SOS": sos_count,
            "INCIDENT": incident_count,
            "GENERAL": general_count
        }
    }

