#!/bin/bash

# SenseSafe Backend Setup Script

echo "🚀 Setting up SenseSafe Backend..."

# Create virtual environment
echo "📦 Creating virtual environment..."
python3 -m venv venv

# Activate virtual environment
echo "🔌 Activating virtual environment..."
source venv/bin/activate

# Upgrade pip
echo "⬆️  Upgrading pip..."
pip install --upgrade pip

# Install dependencies
echo "📥 Installing dependencies..."
pip install -r requirements.txt

# Copy environment file
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    cp .env.example .env
    echo "⚠️  Please update .env with your database credentials!"
else
    echo "✅ .env file already exists"
fi

echo ""
echo "✨ Setup complete!"
echo ""
echo "Next steps:"
echo "1. Update .env with your PostgreSQL credentials"
echo "2. Create database: createdb sensesafe"
echo "3. Run migrations: alembic upgrade head"
echo "4. Start server: uvicorn app.main:app --reload"
echo ""
