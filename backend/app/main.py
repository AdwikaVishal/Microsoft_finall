from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.core.config import settings
from app.auth.routes import router as auth_router
from app.incidents.routes import router as incidents_router
from app.sos.routes import router as sos_router
from app.alerts.routes import router as alerts_router
from app.admin.routes import router as admin_router
from app.messages.routes import router as messages_router


# Create FastAPI application
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="""
    SenseSafe Emergency Application Backend
    
    A comprehensive emergency response system with:
    - 🔐 JWT Authentication
    - 👥 Role-based Access Control (USER, ADMIN)
    - 🚨 Incident Reporting
    - 🆘 SOS Emergency Alerts
    - 🔔 Disaster Alerts
    - 🖥️ Admin Dashboard Support
    - 🤖 ML Integration Ready (Azure Computer Vision)
    
    Built for Microsoft Imagine Cup with FastAPI, PostgreSQL, and Azure services.
    """,
    docs_url="/docs",
    redoc_url="/redoc"
)

# Startup event to create default admin
from app.db.database import SessionLocal
from app.db.models import User, UserRole, UserAbility
from app.core.security import hash_password

@app.on_event("startup")
async def create_default_admin():
    db = SessionLocal()
    try:
        # Check if admin user exists
        admin = db.query(User).filter(User.role == UserRole.ADMIN).first()
        if not admin:
            print("Creating default admin user...")
            hashed_pw = hash_password("admin123")
            new_admin = User(
                name="System Admin",
                email="admin@sensesafe.com",
                password_hash=hashed_pw,
                role=UserRole.ADMIN,
                ability=UserAbility.NONE
            )
            db.add(new_admin)
            db.commit()
            print("Default admin created: admin@sensesafe.com / admin123")
        else:
            print(f"Admin user exists: {admin.email}")
    except Exception as e:
        print(f"Error checking/creating admin: {e}")
    finally:
        db.close()


# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Include routers
app.include_router(auth_router)
app.include_router(incidents_router)
app.include_router(sos_router)
app.include_router(alerts_router)
app.include_router(admin_router)
app.include_router(messages_router)


# Root endpoint
@app.get("/")
def root():
    """Root endpoint with API information."""
    return {
        "name": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "status": "running",
        "docs": "/docs",
        "redoc": "/redoc"
    }


# Health check endpoint
@app.get("/health")
def health_check():
    """Health check endpoint for monitoring."""
    return {
        "status": "healthy",
        "service": settings.APP_NAME,
        "version": settings.APP_VERSION
    }


# Global exception handler
@app.exception_handler(Exception)
async def global_exception_handler(request, exc):
    """Handle all uncaught exceptions."""
    return JSONResponse(
        status_code=500,
        content={
            "detail": "Internal server error",
            "type": type(exc).__name__
        }
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.DEBUG
    )
