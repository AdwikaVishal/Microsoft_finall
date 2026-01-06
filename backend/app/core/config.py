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
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30

    # Admin bootstrap user
    ADMIN_EMAIL: str = "admin@sensesafe.com"
    ADMIN_PASSWORD: str = "admin123"   # keep under 72 chars (bcrypt requirement)

    # Azure Computer Vision (placeholder)
    AZURE_CV_KEY: Optional[str] = None
    AZURE_CV_ENDPOINT: Optional[str] = None

    # CORS
    CORS_ORIGINS: List[str] = [
    # Local dev
    "http://localhost:3000",
    "http://localhost:3001",
    "http://localhost:5173",

    # Backend (API)
    "https://sensesafe-c9c8bpend7cceeh7.eastasia-01.azurewebsites.net",

    # Frontend (UI)
    "https://sensesafe-ghhuawctbmcxgnbb.centralindia-01.azurewebsites.net",
    ]


    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()
