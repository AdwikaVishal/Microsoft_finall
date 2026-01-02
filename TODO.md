# Implementation Plan: User Messages → Backend → Admin Dashboard

## Phase 1: Backend - Complete Message Flow
- [x] 1.1 Create `Message` model in `backend/app/db/models.py` for user messages
- [x] 1.2 Create message schemas in `backend/app/messages/schemas.py`
- [x] 1.3 Create message service in `backend/app/messages/service.py`
- [x] 1.4 Create message routes for users in `backend/app/messages/routes.py`
- [x] 1.5 Create admin routes for viewing messages in `backend/app/messages/routes.py`
- [x] 1.6 Update `backend/app/main.py` to include message router

## Phase 2: Update API Service (sensesafe)
- [x] 2.1 Add `sendSOS()` function for sending SOS alerts to backend
- [x] 2.2 Add `sendIncident()` function for sending incident reports
- [x] 2.3 Add `getAllMessages()` function for admin dashboard
- [x] 2.4 Add `getMessageStats()` for admin dashboard stats
- [x] 2.5 Replace mock data with actual backend calls
- [x] 2.6 Add axios configuration with auth token support

## Phase 3: Update Admin Dashboard
- [x] 3.1 Create Messages page in `sensesafe/src/apps/admin-dashboard/src/pages/Messages.jsx`
- [x] 3.2 Update `sensesafe/src/apps/admin-dashboard/src/App.jsx` to fetch real data
- [x] 3.3 Update `sensesafe/src/apps/admin-dashboard/src/components/Sidebar.jsx` to include Messages nav with unread count
- [x] 3.4 Dashboard and Alerts pages now use real backend data

## Phase 4: Connect User App to Backend
- [x] 4.1 Update Home.jsx SOS button to call backend API with dialog
- [x] 4.2 Update IncidentReport.jsx to send incidents to backend
- [x] 4.3 Add message sending functionality in user-app

## Phase 5: Android App (Not Updated)
- [ ] 5.1 Add message sending capability (future enhancement)
- [ ] 5.2 Connect to real backend instead of mock API (future enhancement)

## Summary
✅ User SOS alerts → Backend → Admin Dashboard (Messages page)
✅ User Incident reports → Backend → Admin Dashboard (Messages page)
✅ Real-time data fetching for admin dashboard
✅ Message statistics and unread counts
✅ Admin can view, filter, and mark messages as read

