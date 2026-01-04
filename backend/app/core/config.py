from pydantic_settings import BaseSettings
from typing import Optional, List


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""
    
    # Application
    APP_NAME: str = "SenseSafe"
    APP_VERSION: str = "1.0.0"
    DEBUG: bool = True

    # Database
    DATABASE_URL: str

    # JWT
    JWT_SECRET: str
    JWT_ALGORITHM: str = "HSHS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30

    # Admin bootstrap user
    ADMIN_EMAIL: str = "admin@sensesafe.com"
    ADMIN_PASSWORD: str = "admin123"   # keep under 72 chars (bcrypt requirement)

    # Azure Computer Vision (placeholder)
    AZURE_CV_KEY: Optional[str] = None
    AZURE_CV_ENDPOINT: Optional[str] = None

    # CORS
    CORS_ORIGINS: List[str] = [
        "http://localhost:3000",  # User app
        "http://localhost:3001",  # Admin dashboard
        "http://localhost:3002",  # Actual Vite port
        "http://localhost:5173",  # Vite dev server
        "http://192.168.0.130:3000",  # User app on network IP
        "http://192.168.0.130:3001",  # Admin dashboard on network IP
        "http://192.168.0.130:3002",  # Actual Vite port on network IP
        # Note: Removed "*" wildcard because allow_credentials=True 
        # requires specific origins (browser security requirement)
    ]

    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()
