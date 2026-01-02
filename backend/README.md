# SenseSafe Backend

FastAPI backend for the SenseSafe emergency application with PostgreSQL, JWT authentication, and role-based access control.

## 🚀 Quick Start

### Prerequisites
- Python 3.9+
- PostgreSQL 13+

### Installation

1. Create virtual environment:
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Configure environment:
```bash
cp .env.example .env
# Edit .env with your database credentials
```

4. Initialize database:
```bash
# Create PostgreSQL database
createdb sensesafe

# Run migrations
alembic upgrade head
```

5. Run the server:
```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## 📚 API Documentation

Once running, visit:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## 🗄️ Database Migrations

```bash
# Create new migration
alembic revision --autogenerate -m "description"

# Apply migrations
alembic upgrade head

# Rollback
alembic downgrade -1
```

## 🔐 Authentication

The API uses JWT tokens. Include in requests:
```
Authorization: Bearer <token>
```

## 👥 User Roles

- **USER**: Mobile app users (report incidents, send SOS)
- **ADMIN**: Dashboard users (manage incidents, verify reports)

## 📁 Project Structure

```
app/
  main.py              # FastAPI application entry
  core/                # Core configuration
  db/                  # Database models & migrations
  auth/                # Authentication endpoints
  users/               # User management
  incidents/           # Incident reporting
  sos/                 # SOS emergency alerts
  alerts/              # Disaster alerts
  admin/               # Admin dashboard endpoints
  ai/                  # ML placeholders
  utils/               # Utilities & exceptions
```

## 🌐 Deployment

Ready for deployment with:
- CORS enabled for mobile & web
- Environment-based configuration
- PostgreSQL production support
- Pagination for large datasets
- Comprehensive error handling
