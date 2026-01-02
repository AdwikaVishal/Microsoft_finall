from sqlalchemy.orm import Session

from app.db.models import SOS, User
from app.sos.schemas import SOSCreate, SOSResponse, SOSListResponse


def create_sos_alert(db: Session, sos_data: SOSCreate, user: User) -> SOSResponse:
    """Create a new SOS emergency alert."""
    
    # Create SOS alert
    new_sos = SOS(
        user_id=user.id,
        ability=sos_data.ability,
        lat=sos_data.lat,
        lng=sos_data.lng,
        battery=sos_data.battery,
        status=sos_data.status
    )
    
    db.add(new_sos)
    db.commit()
    db.refresh(new_sos)
    
    # TODO: Integrate Azure Notification Hub later
    # This will send push notifications to nearby responders
    # from app.utils.notifications import send_sos_notification
    # await send_sos_notification(new_sos)
    
    return SOSResponse.from_orm(new_sos)


def get_user_sos_alerts(db: Session, user: User, page: int = 1, page_size: int = 20) -> SOSListResponse:
    """Get all SOS alerts sent by the current user."""
    
    # Calculate offset
    offset = (page - 1) * page_size
    
    # Query SOS alerts
    query = db.query(SOS).filter(SOS.user_id == user.id)
    total = query.count()
    sos_alerts = query.order_by(SOS.created_at.desc()).offset(offset).limit(page_size).all()
    
    return SOSListResponse(
        sos_alerts=[SOSResponse.from_orm(sos) for sos in sos_alerts],
        total=total,
        page=page,
        page_size=page_size
    )
