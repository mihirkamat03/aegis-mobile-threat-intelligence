import pytest
from app.core.database import SessionLocal, Base, engine
from app.seed.demo_data import seed_database

@pytest.fixture(autouse=True, scope="module")
def setup_clean_database():
    """Ensure database has the clean seeded dataset before each test module runs."""
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    seed_database(db)
    db.close()
    yield
