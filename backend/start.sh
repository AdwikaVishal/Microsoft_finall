#!/bin/bash

# SenseSafe Backend Start Script

echo "🚀 Starting SenseSafe Backend..."

# Activate virtual environment
source venv/bin/activate

# Check if .env exists
if [ ! -f .env ]; then
    echo "❌ Error: .env file not found!"
    echo "Please run setup.sh first and configure your .env file"
    exit 1
fi

# Start the server
echo "🌐 Starting FastAPI server on http://localhost:8000"
echo "📚 API Documentation: http://localhost:8000/docs"
echo ""

uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
