# AEGIS — Hackathon Demonstration Guide & Script

**Team:** Aether  
**Problem Statement:** PS 10 — Mobile Threat Intelligence & Privacy Monitoring  
**Core Motto:** *Do not just detect isolated signals. Correlate them.*

---

## 1. Quick Setup & Prerequisites

### A. Environment Architecture
* **Operating System:** Windows 10/11, macOS, or Linux
* **Backend:** Python 3.11+ (FastAPI, SQLAlchemy 2.0, Pydantic 2, Uvicorn)
* **Database:** PostgreSQL (local database named `aegis_db` on port `5432`)
* **Android Application:** Pure Java, Android XML Views, Material Components, ViewBinding (Target SDK 34, Min SDK 26)

### B. Starting the Database & Backend
1. **Database:**
   Ensure PostgreSQL is running and database exists:
   ```bash
   # From PowerShell or bash:
   psql -U postgres -c "CREATE DATABASE aegis_db;"
   ```
2. **Environment Variables:**
   Copy `backend/.env.example` to `backend/.env`:
   ```bash
   DATABASE_URL=postgresql://postgres:postgres@localhost:5432/aegis_db
   PORT=8000
   HOST=0.0.0.0
   CORS_ORIGINS=*
   ```
3. **Install Dependencies & Seed Database:**
   ```bash
   cd backend
   pip install -r requirements.txt
   python -m app.main
   ```
   *The server starts on `http://localhost:8000`. On first launch, it automatically initializes database tables and seeds 10 baseline applications with rich telemetry.*
4. **Run Verification Test Suite:**
   ```bash
   python -m pytest backend/tests -v
   ```
   *All 42 automated tests pass with 100% green coverage.*

### C. Launching the Android Application
* Open `Aether Hackathon` in **Android Studio**.
* The project connects to `http://10.0.2.2:8000` (standard Android emulator loopback). If testing on a physical handset, update `DEFAULT_BASE_URL` in `AegisApiClient.java` to your machine's local LAN IP.
* Build and run on an Android 8.0+ (API 26+) device or emulator.

---

## 2. Capability Honesty & Architecture Transparency

AEGIS distinguishes clearly between actual functional implementations and simulated mobile telemetry:

| Feature / Subsystem | Implementation Reality | Notes |
|:---|:---:|:---|
| **PostgreSQL Database** | **REAL** | Relational schemas for Applications, Permissions, Domains, Threats, Behaviour, and Risk |
| **FastAPI REST Service** | **REAL** | 18+ endpoints serving live telemetry, queries, filters, and dynamic calculations |
| **Deterministic Risk Engine** | **REAL** | 4-pillar weighted model (Permissions 25%, Network 25%, Threat Intel 30%, Behaviour 20%) clamped 0–100 |
| **Behaviour Baseline Engine** | **REAL** | Calculates absolute & percentage drift across metric baselines (`NORMAL`, `ELEVATED`, `ANOMALOUS`) |
| **Multi-Signal Correlation Engine** | **REAL** | Temporal windowing (15-min default), 5 correlation rules (Rules A–E), and synthesis |
| **Evidence Graph Synthesis** | **REAL** | Traverses typed nodes (`APP`, `PERMISSION`, `DOMAIN`, `THREAT_INTEL`, `BEHAVIOUR`, `RISK`) and causal edges |
| **Explainability Generator** | **REAL** | Produces natural diagnostic summaries, contributing signals, and confidence scores |
| **Action Recommendation Engine** | **REAL** | Prioritizes actionable remediation steps based on evidence severity |
| **Android Java/XML UI** | **REAL** | Custom `RiskRingView`, custom interactive `EvidenceGraphView`, ViewBinding, dark surface palette |
| **Mobile Packet Interception** | *Simulated Telemetry* | Honest prototype boundary: Android VpnService and low-level kernel packet interception are not active |
| **On-Device Firewall Sockets** | *Simulated Action* | Severing network streams is labeled `Simulate Block` / `DEMO` |
| **Exfiltration Volume Counters** | *Simulated Telemetry* | Network byte counters (e.g. 18.2 MB) represent simulated telemetry |

---

## 3. The 5-Step Hackathon Demonstration Script

### Step 0: Baseline Secure State (Initial Posture)
* **Visual State:**
  * Status indicator: `● LIVE` (or `● DEMO` offline)
  * Device Risk Score: **34 (SAFE / PROTECTED)**
  * QuickPDF Reader: Risk **28 (LOW)**
  * Action Button: **"Start Demo"**
* **Presenter Script:**
  > *"Judges, mobile security today is broken because security tools look at signals in isolation. Here on our AEGIS dashboard, we see QuickPDF Reader. It's a standard document utility with a low risk score of 28. It uses standard file storage, and everything appears completely normal."*

---

### Step 1: Sensitive Capability Drift
* **Action:** Tap **"Start Demo"** (or button advances to Step 1).
* **Visual State:**
  * QuickPDF Reader Risk: **28 → 43 (LOW)**
  * Action Button updates to: **"Next (2/5: Drift)"**
  * Timeline event: `DEMO (1/5): Background Microphone Access`
  * Explanation: *"RECORD_AUDIO invoked while screen locked without active user interface."*
* **Presenter Script:**
  > *"Now an event occurs: QuickPDF accesses the microphone while the device screen is off. Traditional permission monitors either spam the user with an alert or ignore it as an allowed permission. In AEGIS, this single signal only nudges the score from 28 to 43. We do not cry wolf yet."*

---

### Step 2: Telemetry Destination Drift
* **Action:** Tap **"Next (2/5: Drift)"**.
* **Visual State:**
  * QuickPDF Reader Risk: **43 → 61 (SUSPICIOUS)**
  * Action Button updates to: **"Next (3/5: Host)"**
  * Timeline event: `DEMO (2/5): Telemetry Destination Spike`
  * Diagnostic text: *"Network destinations spiked from 3/hr baseline to 14/hr immediately following background audio invocation (+367% anomaly)."*
* **Presenter Script:**
  > *"Moments later, the app’s background network behaviour anomalies trigger. Network connection destinations surge from a historical baseline of 3 per hour to 14 per hour—a 367% spike. The risk score rises to 61 (Elevated/Suspicious). Still, we don't have definitive proof of malice."*

---

### Step 3: Untrusted Network Destination Contacted
* **Action:** Tap **"Next (3/5: Host)"**.
* **Visual State:**
  * QuickPDF Reader Risk: **61 → 72 (HIGH)**
  * Action Button updates to: **"Next (4/5: Intel)"**
  * Timeline event: `DEMO (3/5): Untrusted Dynamic DNS Contacted`
  * Domain listed: `sync-audio-cdn.hopto.org`
* **Presenter Script:**
  > *"In step 3, our network intelligence detects that QuickPDF is transmitting to an untrusted Dynamic DNS host: sync-audio-cdn.hopto.org. The risk enters HIGH at 72."*

---

### Step 4: External Threat Intelligence Pulse Corroboration
* **Action:** Tap **"Next (4/5: Intel)"**.
* **Visual State:**
  * QuickPDF Reader Risk: **72 → 84 (HIGH)**
  * Action Button updates to: **"Correlate (5/5)"**
  * Timeline event: `DEMO (4/5): AbuseIPDB C2 Match Corroborated`
  * Threat Intel Confidence: **96% match** to active spyware command-and-control pulse.
* **Presenter Script:**
  > *"In step 4, AEGIS queries our Threat Intelligence feed. The dynamic DNS domain matches an active AbuseIPDB C2 indicator with 96% confidence."*

---

### Step 5: Multi-Signal Correlation Complete (The "Aha!" Moment)
* **Action:** Tap **"Correlate (5/5)"**.
* **Visual State:**
  * QuickPDF Reader Risk: **84 (CRITICAL)**
  * Global Risk: **86 (ATTENTION REQUIRED)**
  * Threat Center: New active threat `Unauthorized C2 Audio Exfiltration` (ACTIVE · CORRELATED)
  * Action Button updates to: **"Reset Demo"** (Icon switches to reset)
* **Key Screen to Show: App Detail Investigation Workspace:**
  1. **"Why this is risky" Card:**
     * Badge: `MULTI-SIGNAL CORRELATED`
     * Diagnostic Summary: Full correlated explanation.
     * Signal Breakdown:
       * Microphone Access: `+15`
       * Destination Spike: `+25`
       * Untrusted Dynamic DNS: `+20`
       * Threat Intel Match: `+30`
       * Total Correlated Impact: `84 / 100`
     * Risk Evolution: `28` → `43` → `61` → `72` → `84`
  2. **Interactive Evidence Graph:**
     * Nodes: `QuickPDF` (App) → `RECORD_AUDIO` (Permission) → `sync-audio-cdn.hopto.org` (Domain) → `AbuseIPDB Match` (Threat Intel) → `Critical Threat` (Risk)
     * Tap any node to inspect real-time diagnostic metadata in the inspector callout below!
* **Presenter Script:**
  > *"Now, look at what happens when our Correlation Engine connects all the dots. Instead of 4 disconnected warnings, AEGIS synthesizes a single, explainable investigation graph. We prove that QuickPDF Reader is recording audio in the background and exfiltrating it to an identified C2 server. 
  > You can tap each node in the Evidence Graph to inspect the exact causal chain. And right here in the Breakdown Card, you can see how each individual signal contributed to the final score."*

---

### Step 6: Interactive Remediation & Reset
* **Action:**
  * Tap the **Network** tab in App Detail $\to$ Tap **"Simulate Block"** on `sync-audio-cdn.hopto.org`.
  * The risk de-escalates immediately, proving responsive containment.
  * Tap **"Reset Demo"** on the top toolbar (or long-press the demo button) to return all data and backend state to baseline Risk 28.
* **Presenter Script:**
  > *"With one tap, the user can sever the connection. AEGIS recalculates risk in real time, restoring trust to the device."*

---

## 4. Hackathon Presentation Backup Plan (Failsafe)

If the venue WiFi drops, database connection is severed, or backend is unreachable:
1. **Zero Downtime Offline Mode:** The Android application automatically detects connection loss and switches the top badge to `● DEMO` (Amber).
2. **Local Deterministic Fallback:** All 5 steps, the Evidence Graph, the diagnostic breakdown, and risk evolutions work seamlessly offline through local in-memory fallback.
3. **Reconnecting Live:** Tap the `● DEMO` status badge anytime to ping `/health`. When the backend returns online, the badge turns green (`● LIVE`) and resynchronizes with the FastAPI server.
