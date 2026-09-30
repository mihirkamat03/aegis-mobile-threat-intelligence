# AEGIS — Mobile Threat Intelligence & Privacy Monitoring

> **Comprehensive mobile security intelligence and privacy posture monitoring powered by deterministic multi-signal correlation.**

AEGIS is an enterprise-grade mobile threat intelligence and privacy auditing system developed for the Aether Hackathon (Problem Statement 10). Instead of treating application permissions, background telemetry anomalies, untrusted domain connections, and external threat feeds as isolated noise, AEGIS correlates these disparate telemetry vectors within a temporal sliding window to synthesize explainable, actionable threat intelligence.

---

## Problem

Mobile users and enterprise administrators face an acute visibility gap:
1. **Alert Fatigue & Isolated Signals:** Standard mobile OS security treats permissions, domain sockets, and telemetry in total isolation. An app requesting microphone access is common; an app contacting a content delivery network is common; an app transmitting periodic heartbeat packets is common. Individually, each signal appears benign or mildly elevated.
2. **Spyware & C2 Camouflage:** Sophisticated mobile spyware, surveillance tools, and aggressive adware deliberately spread their operational footprint across multiple capabilities (e.g., recording audio while the screen is off, waiting for device standby, and trickling exfiltration packets over dynamic DNS hosts).
3. **Black-Box Confusion:** When security tools do trigger warnings, they provide opaque risk scores or alarmist popups without explaining *why* the application is dangerous or providing an evidentiary trail.

---

## Solution

AEGIS solves this problem through **Deterministic Multi-Signal Correlation**:
* **Holistic Signal Synthesis:** Ingests 4 distinct telemetry dimensions:
  1. **Permission Invocations:** Identifies sensitive capabilities (`RECORD_AUDIO`, `ACCESS_FINE_LOCATION`, `READ_PHONE_STATE`) invoked outside active user interaction.
  2. **Behavioural Drift:** Monitors deviation from historical baselines (e.g., connection destinations surging from 3/hr to 14/hr, +367% drift).
  3. **Domain Intelligence:** Analyzes endpoint reputations, dynamic DNS infrastructure, and non-standard egress ports.
  4. **Threat Intelligence Corroboration:** Cross-references contacted infrastructure against curated open-source intelligence feeds (AbuseIPDB, OTX AlienVault).
* **Temporal Window Correlation:** Evaluates signal convergence within a 15-minute sliding window to confirm multi-hop attack chains.
* **Explainable Evidence Graphs:** Translates correlated findings into an intuitive causal graph and transparent score breakdown.
* **Proactive Remediation:** Recommends prioritized, actionable containment steps (e.g., revoking background permissions, severing C2 sockets).

---

## Architecture

```
┌────────────────────────────────────────────────────────┐
│               AEGIS ANDROID CLIENT                    │
│   • Pure Java (100% Java 17/21 Compatible)             │
│   • XML Layouts, Material Components & ViewBinding    │
│   • Custom Hardware-Accelerated Canvas Rendering      │
│   • Dual Mode: Live FastAPI Sync + Offline Fallback   │
└──────────────────────────┬─────────────────────────────┘
                           │ HTTP REST (10.0.2.2:8000 / Cleartext)
                           ▼
┌────────────────────────────────────────────────────────┐
│               FASTAPI BACKEND SERVICE                  │
│   • Python 3.11+ / Uvicorn ASGI Engine                │
│   • Comprehensive REST Endpoints (/health, /api/v1/*)  │
└───────┬──────────────────┬──────────────────┬──────────┘
        │                  │                  │
        ▼                  ▼                  ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────────┐
│ POSTGRESQL   │   │ INTELLIGENCE │   │ EVIDENCE GRAPH   │
│ aegis_db     │   │ ENGINE       │   │ & EXPLANATIONS   │
│ Relational   │   │ • Normalizer │   │ • Causal Graph   │
│ Schemas for  │   │ • Baseline   │   │ • Breakdown Card │
│ 7 Entities   │   │ • Multi-Corr │   │ • Recommendations│
└──────────────┘   └──────────────┘   └──────────────────┘
```

---

## Core Features

* **Permission Risk Analysis:** Categorizes permissions into `SAFE`, `SENSITIVE`, and `ANOMALY`, auditing background execution timestamps and usage context.
* **Behaviour Analysis & Anomaly Detection:** Calculates statistical deviation against baseline metrics, classifying drift as `NORMAL`, `ELEVATED`, or `ANOMALOUS`.
* **Threat Intelligence Matching:** Cross-references destination IPs and domains against AbuseIPDB and AlienVault OTX indicator feeds with confidence scoring.
* **Multi-Signal Correlation:** Evaluates 5 deterministic correlation rules within a 15-minute sliding temporal window.
* **Explainable Risk Scoring:** 4-pillar weighted model (Permissions 25%, Network 25%, Threat Intel 30%, Behaviour 20%) clamped strictly to 0–100 with dynamic diagnostic summaries.
* **Evidence Graph:** Generates and renders interactive node-and-edge graphs (`APP` $\to$ `PERMISSION` $\to$ `DOMAIN` $\to$ `THREAT_INTEL` $\to$ `RISK`) with touch inspection.
* **Risk History:** Historical audit trail tracking risk progression across baseline evaluations and telemetry triggers.
* **Actionable Recommendations:** Mapped remediation actions with instant resolution and de-escalation tracking.
* **Deterministic Demo Mode:** Interactive 5-step storyline demonstrating live multi-hop attack correlation on target app QuickPDF Reader.

---

## Technology Stack

### Android Mobile Client
* **Programming Language:** Java (100% Java 17/21 compatible)
* **UI & Views:** XML Layouts, ViewBinding, Android SDK 34 (AndroidX)
* **Components:** Material Components for Android (`MaterialCardView`, `MaterialButton`, `BottomNavigationView`, `Toolbar`)
* **Custom Graphics:** Double-buffered Canvas views (`EvidenceGraphView`, `RiskRingView`)
* **Strict Exclusions:** Zero Kotlin, Zero Jetpack Compose, Zero Kotlin Coroutines/DSL

### Backend & Data Layer
* **Language & Framework:** Python 3.11+ / FastAPI / Uvicorn
* **Database & ORM:** PostgreSQL 15+ / SQLAlchemy 2.0
* **Data Validation:** Pydantic v2
* **Automated Testing:** Pytest (42 automated unit and integration tests)

---

## Running the Backend

### Prerequisites
* Python 3.11+ installed and on system `PATH`
* PostgreSQL running locally on port `5432`

### 1. Database Setup
```bash
# Using PostgreSQL CLI:
psql -U postgres -c "CREATE DATABASE aegis_db;"
```

### 2. Environment Configuration
Copy `backend/.env.example` to `backend/.env`:
```bash
cd backend
cp .env.example .env
```
Default connection string:
```
DATABASE_URL=postgresql+psycopg2://postgres@localhost:5432/aegis_db
```

### 3. Install Dependencies & Launch
```bash
pip install -r requirements.txt
python -m app.main
```
*The server initializes database tables, seeds 10 baseline applications with rich telemetry, and listens on `http://localhost:8000`.*

### 4. Run Automated Test Suite
```bash
python -m pytest tests -v
```
*Executes all 42 automated tests covering REST endpoints, scoring rules, temporal correlation, evidence graph generation, and demo lifecycle.*

---

## Running the Android App

### Prerequisites
* **Android Studio** (Hedgehog 2023.1.1 or newer recommended)
* Android SDK Platform 34 installed
* Android Virtual Device (AVD) running Android 8.0+ (API 26+) or a physical Android handset

### Steps
1. Launch Android Studio and select **Open**.
2. Browse to and open the project root directory: `Aether Hackathon`.
3. Allow Gradle to synchronize dependencies.
4. **Network Target:**
   * **Android Emulator:** Automatically connects to the FastAPI backend at `http://10.0.2.2:8000` with cleartext traffic enabled in `AndroidManifest.xml`.
   * **Physical Handset:** Open `app/src/main/java/com/aether/aegis/data/api/AegisApiClient.java` and set `DEFAULT_BASE_URL` to your computer's local Wi-Fi IP (e.g., `http://192.168.1.15:8000`).
5. Select your target device and click **Run 'app'** (`Shift + F10`).

---

## Demo Flow (The 5-Step Hackathon Story)

| Step | State / Action | QuickPDF Risk | Narrative & Presentation Point |
|:---:|:---|:---:|:---|
| **0** | **Baseline Normal** | **28 (LOW)** | QuickPDF Reader operates within expected document utility limits. Standard file access active; no anomalous network egress. |
| **1** | Tap **"Start Demo"** | **43 (LOW)** | Sensitive microphone access (`RECORD_AUDIO`) invoked in background while screen locked. Single signal is not enough to condemn the app. |
| **2** | Tap **"Next (2/5: Drift)"** | **61 (SUSP)** | Network destinations spike from 3/hr baseline to 14/hr (+367% drift). Behavioural drift engine flags elevated anomaly. |
| **3** | Tap **"Next (3/5: Host)"** | **72 (HIGH)** | Outbound sockets opened to untrusted dynamic DNS host `sync-audio-cdn.hopto.org`. Network pillar flags suspicious destination. |
| **4** | Tap **"Next (4/5: Intel)"** | **84 (HIGH)** | AbuseIPDB threat intelligence feed matches destination to known C2 infrastructure with 96% confidence. |
| **5** | Tap **"Correlate (5/5)"** | **84 (CRIT)** | **The "Aha!" Moment:** Correlation Engine synthesizes all 4 vectors into confirmed spyware incident. Evidence Graph illuminates full causal chain. |
| **Reset** | Tap **"Reset Demo"** | **28 (LOW)** | Tapping reset (or long-pressing the demo button) restores all database state and UI telemetry cleanly back to baseline. |

---

## Limitations & Capability Honesty

AEGIS transparently distinguishes between fully implemented code and prototype-simulated mobile vectors:

* **Real & Functional:**
  * Complete relational PostgreSQL database with 7 core entities and foreign key constraints.
  * Complete FastAPI service with 18+ REST endpoints.
  * Deterministic 4-pillar risk calculation model clamped to 0–100.
  * Behaviour baseline comparator engine with percentage drift calculations.
  * Multi-signal correlation engine enforcing 5 correlation rules within a 15-minute sliding temporal window.
  * Dynamic evidence graph generation and natural language diagnostic explanations.
  * 100% pure Java Android client with custom Canvas components (`RiskRingView`, `EvidenceGraphView`).
* **Simulated Mobile Telemetry:**
  * **Packet Interception:** Android `VpnService` and low-level kernel packet sniffing are simulated via structured event feeds rather than kernel hooks.
  * **On-Device Firewall Actions:** Severing active sockets is simulated on-device and explicitly labeled `Simulate Block` / `DEMO`.
  * **Exfiltration Byte Volume:** Byte counters (e.g. 18.2 MB exfiltrated) represent simulated telemetry payloads.

---

## Future Scope

1. **Real-Device Local Packet Inspection:** Integration of Android `VpnService` or eBPF tracing on rooted audit devices to capture real-time DNS requests and TLS Server Name Indication (SNI) headers.
2. **On-Device Machine Learning:** Lightweight TensorFlow Lite / ONNX models running directly on-device to model per-application battery and packet arrival distributions.
3. **Direct Threat Intelligence Feed Sync:** Automated streaming connectors for real-time STIX/TAXII threat intel feeds, VirusTotal, and URLhaus.
4. **Device-Wide Endpoint Fleet Management:** Enterprise dashboard console supporting fleet-wide telemetry aggregation, compliance policies, and automated device quarantine.
