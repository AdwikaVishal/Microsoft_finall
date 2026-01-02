# -------------------------------
# DATABASE SETUP
# -------------------------------
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

from app.core.config import settings

# Create SQLAlchemy engine
engine = create_engine(
    settings.DATABASE_URL,
    pool_pre_ping=True,   # Verify connections before using
    echo=settings.DEBUG,  # Log SQL queries in debug mode
)

# Create SessionLocal class
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Base class
Base = declarative_base()


def get_db():
    """Dependency to get database session."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# -------------------------------
# MODELS
# -------------------------------
import uuid
import enum
from datetime import datetime
from sqlalchemy import Column, String, DateTime, ForeignKey, Float, Integer, Enum, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship


# Enums
class UserRole(str, enum.Enum):
    USER = "USER"
    ADMIN = "ADMIN"


class UserAbility(str, enum.Enum):
    BLIND = "BLIND"
    DEAF = "DEAF"
    NON_VERBAL = "NON_VERBAL"
    ELDERLY = "ELDERLY"
    OTHER = "OTHER"
    NONE = "NONE"


class IncidentStatus(str, enum.Enum):
    PENDING = "PENDING"
    UNDER_REVIEW = "UNDER_REVIEW"
    VERIFIED = "VERIFIED"
    HELP_ASSIGNED = "HELP_ASSIGNED"
    RESOLVED = "RESOLVED"


class SOSStatus(str, enum.Enum):
    TRAPPED = "TRAPPED"
    INJURED = "INJURED"
    NEED_HELP = "NEED_HELP"
    SAFE = "SAFE"


class AlertSeverity(str, enum.Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"


class User(Base):
    __tablename__ = "users"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    name = Column(String(255), nullable=False)
    email = Column(String(255), unique=True, nullable=False, index=True)
    password_hash = Column(String(255), nullable=False)
    role = Column(Enum(UserRole), default=UserRole.USER, nullable=False)
    ability = Column(Enum(UserAbility), default=UserAbility.NONE, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)

    incidents = relationship("Incident", back_populates="user", cascade="all, delete-orphan")
    sos_alerts = relationship("SOS", back_populates="user", cascade="all, delete-orphan")


class Incident(Base):
    __tablename__ = "incidents"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id"), nullable=True)
    type = Column(String(100), nullable=False)
    description = Column(Text, nullable=False)
    lat = Column(Float, nullable=False)
    lng = Column(Float, nullable=False)
    status = Column(Enum(IncidentStatus), default=IncidentStatus.PENDING, nullable=False)
    image_url = Column(String(500), nullable=True)
    risk_score = Column(Float, nullable=True)
    risk_level = Column(String(50), nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)

    user = relationship("User", back_populates="incidents")


class SOS(Base):
    __tablename__ = "sos"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id"), nullable=False)
    ability = Column(Enum(UserAbility), nullable=False)
    lat = Column(Float, nullable=False)
    lng = Column(Float, nullable=False)
    battery = Column(Integer, nullable=False)
    status = Column(Enum(SOSStatus), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)

    user = relationship("User", back_populates="sos_alerts")


class Alert(Base):
    __tablename__ = "alerts"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    title = Column(String(255), nullable=False)
    message = Column(Text, nullable=False)
    severity = Column(Enum(AlertSeverity), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)


class MessageType(str, enum.Enum):
    SOS = "SOS"
    INCIDENT = "INCIDENT"
    GENERAL = "GENERAL"


class Message(Base):
    __tablename__ = "messages"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id"), nullable=False)
    message_type = Column(Enum(MessageType), nullable=False)
    title = Column(String(255), nullable=False)
    content = Column(Text, nullable=False)
    lat = Column(Float, nullable=True)
    lng = Column(Float, nullable=True)
    category = Column(String(100), nullable=True)
    severity = Column(String(50), nullable=True)
    ability = Column(Enum(UserAbility), nullable=True)
    battery = Column(Integer, nullable=True)
    is_read = Column(Integer, default=0, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)

    user = relationship("User", back_populates="messages")


User.messages = relationship("Message", back_populates="user", cascade="all, delete-orphan")


# -------------------------------
# CREATE TABLES (FIRST RUN ONLY)
# -------------------------------
Base.metadata.create_all(bind=engine)
