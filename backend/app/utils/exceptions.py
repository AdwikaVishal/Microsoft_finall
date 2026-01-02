"""
Custom exceptions for the SenseSafe application.
"""

from fastapi import HTTPException, status


class SenseSafeException(Exception):
    """Base exception for SenseSafe application."""
    pass


class AuthenticationError(SenseSafeException):
    """Raised when authentication fails."""
    pass


class AuthorizationError(SenseSafeException):
    """Raised when user lacks required permissions."""
    pass


class ResourceNotFoundError(SenseSafeException):
    """Raised when a requested resource is not found."""
    pass


class ValidationError(SenseSafeException):
    """Raised when data validation fails."""
    pass


class DatabaseError(SenseSafeException):
    """Raised when database operation fails."""
    pass


# HTTP Exception helpers
def not_found(detail: str = "Resource not found"):
    """Return a 404 Not Found exception."""
    return HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail=detail
    )


def unauthorized(detail: str = "Authentication required"):
    """Return a 401 Unauthorized exception."""
    return HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail=detail,
        headers={"WWW-Authenticate": "Bearer"}
    )


def forbidden(detail: str = "Access forbidden"):
    """Return a 403 Forbidden exception."""
    return HTTPException(
        status_code=status.HTTP_403_FORBIDDEN,
        detail=detail
    )


def bad_request(detail: str = "Invalid request"):
    """Return a 400 Bad Request exception."""
    return HTTPException(
        status_code=status.HTTP_400_BAD_REQUEST,
        detail=detail
    )


def internal_server_error(detail: str = "Internal server error"):
    """Return a 500 Internal Server Error exception."""
    return HTTPException(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        detail=detail
    )
