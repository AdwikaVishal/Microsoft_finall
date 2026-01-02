from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.db.models import Alert
from app.alerts.schemas import AlertResponse, AlertListResponse


router = APIRouter(prefix="/api/alerts", tags=["Alerts"])


@router.get("", response_model=AlertListResponse)
def get_alerts(
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db)
):
    """
    Get all disaster alerts.
    
    Returns active alerts ordered by severity and creation time.
    
    Supports pagination:
    - **page**: Page number (default: 1)
    - **page_size**: Items per page (default: 20, max: 100)
    """
    # Calculate offset
    offset = (page - 1) * page_size
    
    # Query alerts
    query = db.query(Alert)
    total = query.count()
    alerts = query.order_by(Alert.created_at.desc()).offset(offset).limit(page_size).all()
    
    return AlertListResponse(
        alerts=[AlertResponse.from_orm(alert) for alert in alerts],
        total=total,
        page=page,
        page_size=page_size
    )
