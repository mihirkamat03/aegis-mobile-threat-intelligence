# AEGIS Backend & Intelligence Engine

AEGIS (**Mobile Threat Intelligence & Privacy Monitoring**) is an explainable cybersecurity intelligence platform built with **Python 3.11+**, **FastAPI**, **SQLAlchemy 2.0**, and **PostgreSQL**.

Core principle: **Do not just detect isolated signals. Correlate them into explainable findings.**

---

## 1. System Architecture & Intelligence Pipeline

```text
    ┌───────────────────────────┐
    │     Raw Mobile Signals    │  (Permissions, DNS Destinations, Anomalies, OSINT)
    └─────────────┬─────────────┘
                  │
                  ▼
    ┌───────────────────────────┐
    │   Signal Normalization    │  (Unified Signal Model: Type, Severity, Confidence)
    └─────────────┬─────────────┘
                  │
                  ▼
    ┌───────────────────────────┐
    │    Baseline Comparison    │  (Historical Baselines vs. Telemetry Deviation)
    └─────────────┬─────────────┘
                  │
                  ▼
    ┌───────────────────────────┐
    │   Temporal Correlation    │  (15-Minute Sliding Execution Window)
    └─────────────┬─────────────┘
                  │
                  ▼
    ┌───────────────────────────┐
    │ Multi-Signal Correlation  │  (Rules A–E: Multi-Hop Evidence Synthesis)
    └─────────────┬─────────────┘
                  │
                  ▼
    ┌───────────────────────────┐
    │   Explainable Findings    │  (Dynamic Narrative + Evidence Web)
    └─────────────┬─────────────┘
                  │
                  ▼
    ┌───────────────────────────┐
    │    Recommended Actions    │  (Non-destructive User Guidance)
    └───────────────────────────┘
```

---

## 2. Capability Honesty: Real vs. Simulated Systems

> **Note on Prototype Status**: This system operates using **rule-based, correlation-driven, explainable heuristics** and synthetic mobile telemetry for hackathon evaluation. It does NOT claim to deploy unrestricted kernel-level packet inspection or production-grade threat feeds.

| Capability / Subsystem | Status | Implementation Reality |
| :--- | :--- | :--- |
| **FastAPI REST Server** | **REAL** | Asynchronous REST endpoints with CORS, validation, and Swagger OpenAPI docs. |
| **PostgreSQL Database** | **REAL** | Relational database schema (`aegis_db`) with foreign keys and SQLAlchemy 2.0 ORM. |
| **Unified Signal Normalization** | **REAL** | Normalizes heterogeneous inputs into standardized signals (`NormalizedSignal`). |
| **Temporal Correlation** | **REAL** | Dynamically sequences events occurring within a configurable window (15 mins). |
| **Multi-Signal Correlation Engine**| **REAL** | Rule-based engine combining independent signals with configurable weights (0–100). |
| **Explainability Engine** | **REAL** | Dynamically synthesizes summary, evidence list, confidence, and risk impacts. |
| **Recommendation Engine** | **REAL** | Maps evidence severity to tailored actions (`REVIEW_PERMISSION`, `CONSIDER_REMOVING_APP`). |
| **Evidence Graph Engine** | **REAL** | Generates directed causality graphs connecting nodes (`APP`, `PERMISSION`, `DOMAIN`, etc.). |
| **Android Fallback Architecture** | **REAL** | Non-blocking Java client with failover to local memory if the backend is offline. |
| **Mobile Packet Interception** | **SIMULATED** | Network destinations and byte counts reflect realistic synthetic telemetry; no VPN or Accessibility hooks. |
| **External Threat Intelligence** | **SIMULATED** | Local mock threat pulses representing AbuseIPDB and AlienVault OTX feeds. |
| **Firewall DNS Blocking** | **SIMULATED** | Domain blocking in mobile prototype acts on application state rather than iptables / root. |

---

## 3. Unified Signal Model & Correlation Rules

### Unified Signal Model (`app/schemas/signal.py`)
All inputs are normalized into a unified model before correlation:
* `id`: Unique signal identifier
* `type`: `PERMISSION`, `DOMAIN`, `THREAT_INTELLIGENCE`, `BEHAVIOUR`, `APP_METADATA`
* `app_id`: Application identifier
* `timestamp`: Naive UTC timestamp
* `severity`: `SAFE`, `LOW`, `SUSPICIOUS`, `HIGH`, `CRITICAL`
* `confidence`: Float between 0.0 and 1.0
* `description`: Concrete observation statement
* `metadata`: Raw contextual attributes (e.g. permission category, deviation percentage)

### Correlation Rules
1. **Rule A — Suspicious Domain**: Destination host matches untrusted dynamic DNS or tracker $\to$ Network finding.
2. **Rule B — Behaviour Drift**: Telemetry exceeds baseline thresholds ($>150\%$ deviation) $\to$ Behaviour finding.
3. **Rule C — Permission + Behaviour**: Sensitive permission accessed concurrently with behavioral traffic anomaly within the correlation window $\to$ Elevated confidence boost.
4. **Rule D — Domain + Threat Intelligence**: Untrusted destination corroborated by active OSINT threat feed $\to$ High-confidence network finding.
5. **Rule E — Multi-Signal Correlation**: Sensitive permission + suspicious destination + behavioral anomaly + threat pulse $\to$ Correlated security finding with calculated composite score.

*Negative Constraint Enforced*: A sensitive permission by itself (e.g., `RECORD_AUDIO`) contributes only standard weight (+15 pts) and **never** creates a critical finding.

---

## 4. Explainability & Action Recommendations

### Dynamic Narrative Generation (`app/services/explanation.py`)
Produces natural language explanations backed strictly by underlying data:
* **Summary**: One concise sentence describing the finding.
* **Contributing Signals**: Exact signals that elevated the score.
* **Evidence**: Concrete, factual observations from device telemetry.
* **Risk Impact**: Breakdown of contribution points per pillar.
* **Confidence**: Calculated correlation confidence (0.0 to 1.0).

### Recommendation Engine (`app/services/recommendation.py`)
Provides non-destructive, advisory guidance:
* `REVIEW_PERMISSION`: Review and revoke non-essential Android permissions.
* `INVESTIGATE_DOMAIN`: Audit suspicious destination hosts in the Evidence Graph.
* `MONITOR_APPLICATION`: Keep application under active behavioral surveillance.
* `REVIEW_BEHAVIOUR`: Investigate unexpected off-hours wakeups or background traffic.
* `CONSIDER_REMOVING_APP`: Strongly recommended for confirmed multi-signal spyware.

---

## 5. Hackathon Demonstration Scenario (`app/services/demo_engine.py`)

Deterministic multi-signal attack progression focusing on **QuickPDF Reader** (`app_quickpdf`):

* **Initial (Step 0)**: Baseline secure / low-risk posture.
  $$\text{Risk} = 28 \; (\text{LOW})$$
* **Step 1**: Sensitive microphone accessed while device screen was locked without user interaction.
  $$\text{Risk} = 43 \; (\text{LOW})$$
* **Step 2**: Destination traffic spike from $3/\text{hr}$ baseline to $14/\text{hr}$ (+367% deviation).
  $$\text{Risk} = 61 \; (\text{SUSPICIOUS})$$
* **Step 3**: Outbound connection initiated to dynamic DNS host `sync-audio-cdn.hopto.org`.
  $$\text{Risk} = 72 \; (\text{HIGH})$$
* **Step 4**: Destination host matches AbuseIPDB emerging C2 pulse (Confidence: 96%).
  $$\text{Risk} = 84 \; (\text{HIGH})$$
* **Step 5**: Correlation completes, generating **Correlated Privacy & Network Risk** with complete causality graph.
  $$\text{Risk} = 84 / 92 \; (\text{HIGH / CRITICAL})$$
* **Reset**: Restores QuickPDF Reader back to Risk 28 (`LOW`), clears demo artifacts, and restores baseline metrics.

---

## 6. API Reference

All API routes are served under `/api/v1` with JSON request/response formats.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/health` | Service health status and environment |
| `GET` | `/api/v1/overview` | Device security status, risk scores, monitored apps count, and recent activity |
| `GET` | `/api/v1/apps` | List all applications (supports `search`, `severity`, `sort_by`, `order`) |
| `GET` | `/api/v1/apps/{app_id}` | Comprehensive application profile with nested permissions, domains, and threats |
| `GET` | `/api/v1/apps/{app_id}/permissions` | List permissions declared/used by the application |
| `GET` | `/api/v1/apps/{app_id}/domains` | List destination domains contacted by the application |
| `GET` | `/api/v1/apps/{app_id}/risk` | Detailed score breakdown, contributors list, and diagnostic narrative |
| `GET` | `/api/v1/apps/{app_id}/risk-history` | Chronological risk evolution audit records over time |
| `GET` | `/api/v1/apps/{app_id}/findings` | Active correlated security findings and evidence items |
| `GET` | `/api/v1/apps/{app_id}/evidence` | Structured evidence items and contribution weights |
| `GET` | `/api/v1/apps/{app_id}/behaviour` | Behavioral telemetry report: baseline, observed, deviation, classification |
| `GET` | `/api/v1/apps/{app_id}/evidence-graph` | Directed Evidence Graph nodes and causal relationship edges |
| `GET` | `/api/v1/threats` | Active threat records (supports `severity`, `status`, `app_id` filters) |
| `GET` | `/api/v1/activity` | Behavioral event timeline (supports `app_id`, `severity`, `limit` filters) |
| `GET` | `/api/v1/domains` | Global domain destination intelligence (supports `reputation` filter) |
| `GET` | `/api/v1/demo/status` | Current step, risk score, active finding, and progression timeline |
| `POST`| `/api/v1/demo/start` | Initiates the deterministic demo scenario (advances to Step 1) |
| `POST`| `/api/v1/demo/step` | Advances the demo scenario to the next step (1 to 5) |
| `POST`| `/api/v1/demo/reset` | Resets QuickPDF Reader back to baseline (Risk 28, LOW) |

---

## 7. Installation & Execution

### Prerequisites
* Python 3.11+
* PostgreSQL 14+ running locally on port 5432

### Configuration
Create `.env` from `.env.example`:
```bash
POSTGRES_SERVER=localhost
POSTGRES_PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=
POSTGRES_DB=aegis_db
ENVIRONMENT=development
API_HOST=0.0.0.0
API_PORT=8000
```

### Install Dependencies
```bash
pip install -r backend/requirements.txt
```

### Run Server
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
Interactive Swagger API documentation is available at:
`http://localhost:8000/docs`

### Run Test Suite
```bash
python -m pytest backend/tests -v
```
All 42 unit and integration tests execute in ~1 second.
