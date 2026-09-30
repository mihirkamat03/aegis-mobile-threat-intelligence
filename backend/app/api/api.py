from fastapi import APIRouter
from app.api.routes import overview, apps, threats, activity, domains, demo

api_router = APIRouter(prefix="/api/v1")

api_router.include_router(overview.router)
api_router.include_router(apps.router)
api_router.include_router(threats.router)
api_router.include_router(activity.router)
api_router.include_router(domains.router)
api_router.include_router(demo.router)
