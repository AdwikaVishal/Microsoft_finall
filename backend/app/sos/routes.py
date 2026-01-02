from fastapi import APIRouter, Depends, status, Query
from sqlalchemy.orm import Session

from app.db.database import get_db
from app.core.security import require_user
from app.db.models import User
from app.sos.schemas import SOSCreate, SOSResponse, SOSListResponse
from app.sos.service import create_sos_alert, get_user_sos_alerts


router = APIRouter(prefix="/api/sos", tags=["SOS"])


@router.post("", response_model=SOSResponse, status_code=status.HTTP_201_CREATED)
def send_sos(
    sos_data: SOSCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_user)
):
    """
    Send an SOS emergency alert.
    
    - **ability**: User's accessibility requirement
    - **lat**: Current latitude
    - **lng**: Current longitude
    - **battery**: Current battery percentage (0-100)
    - **status**: Emergency status (TRAPPED, INJURED, NEED_HELP, SAFE)
    
    This endpoint is idempotent and retry-safe.
    Future updates will integrate Azure Notification Hub for real-time alerts.
    """
    return create_sos_alert(db, sos_data, current_user)


@router.get("/user", response_model=SOSListResponse)
def get_my_sos_alerts(
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db),
    current_user: User = Depends(require_user)
):
    """
    Get all SOS alerts sent by the current user.
    
    Supports pagination:
    - **page**: Page number (default: 1)
    - **page_size**: Items per page (default: 20, max: 100)
    """
    return get_user_sos_alerts(db, current_user, page, page_size)
