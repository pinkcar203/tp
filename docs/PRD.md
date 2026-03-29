# Product Requirements Document (PRD)

## 1. Product Overview

MediTrack is a standalone **Java desktop GUI application** designed to help military field medics manage medical logistics and personnel readiness during deployments and training exercises.

The system enables users to track medical supply inventories, monitor supply expiration dates, manage the medical readiness status of personnel, schedule duty rosters, and generate operational reports. By digitizing these administrative tasks, MediTrack reduces manual errors and improves operational efficiency in high-pressure environments.

---

## 2. Problem Statement

Military field medics often rely on manual tracking methods such as paper logs or spreadsheets to manage medical supplies and personnel readiness. These methods are inefficient and prone to errors.

Several operational problems arise from these manual processes:

- Expiring medical supplies may go unnoticed.
- Personnel medical readiness is difficult to track quickly.
- Determining deployable manpower requires manually checking individual records.
- Duty assignments are time-consuming to plan and enforce manually.
- Manual data management increases the risk of mistakes during high-stress situations.
- Sensitive medical and operational data is accessible to anyone without access controls.

MediTrack addresses these problems by providing a centralized digital system with role-based access control that allows users to efficiently track medical supplies, manage personnel readiness, and plan duty schedules.

---

## 3. Target Users / Stakeholders

### Primary Users

**Field Medics**

- Responsible for maintaining medical supply inventories.
- Need quick access to supply status and can flag personnel as casualties in the field.

### Secondary Users

**Medical Officers**

- Need a full overview of personnel who require medical attention.
- Responsible for assessing and updating all personnel medical statuses.

**Platoon Commanders**

- Need to determine deployable manpower quickly.
- Responsible for scheduling duty rosters and managing personnel records.

**Logistics Officers**

- Responsible for maintaining and tracking medical supply levels.
- Use the system to plan future resupply operations.

---

## 4. User Stories

**Inventory Management**

As a field medic, I want to add, update, and remove medical supplies so that the inventory accurately reflects current stock levels in the field at all times.

---

**Expiry Monitoring**

As a field medic, I want to view a list of supplies expiring within the next 30 days so that I can decide what to use before they become unusable during operations.

---

**Casualty Reporting**

As a field medic, I want to flag a personnel member as CASUALTY during an operation so that the medical officer is immediately alerted for triage, without me needing access to other status changes.

---

**Personnel Management**

As a medical officer, I want to add, edit, and remove personnel records and update any medical status so that the roster always reflects the current readiness of the unit.

---

**Medical Triage Dashboard**

As a medical officer, I want to view a filtered list of personnel who require medical attention (PENDING, CASUALTY, MC, LIGHT_DUTY) so that I can prioritise assessments efficiently.

---

**Operational Inspection**

As a platoon commander, I want to view a list of personnel who are marked as FIT so that I can quickly determine available manpower for deployment.

---

**Structured Duty Rostering**

As a platoon commander, I want to schedule personnel into named duty slots with specific time ranges and duty types so that I can plan and communicate duty assignments clearly.

---

**Auto-Generated Duty Roster**

As a platoon commander, I want the system to automatically assign FIT personnel to duty slots based on coverage rules and rest requirements so that I can generate a fair and valid roster quickly.

---

**Supply Management**

As a logistics officer, I want to add, edit, and delete supply records and generate a resupply report so that I can maintain accurate inventory and prepare restocking requests.

---

**CSV Data Export**

As any authorised user, I want to export my accessible data to a CSV file so that I can share or archive operational records outside the application.

---

## 5. Functional Requirements

### Authentication and First Launch

The system shall:

- Detect on startup whether a data file exists.
- On first launch, prompt the user to set a master password before any other screen is accessible. The password shall be stored immediately as a BCrypt hash — never in plain text.
- On every subsequent launch, require the user to enter the password and select a role before accessing the application.
- Restrict available screens and actions based on the selected role (see Role-Based Access Control below).
- Destroy the active session on logout or application close, requiring re-authentication on the next launch.

---

### Role-Based Access Control (RBAC)

Four roles are supported. Each role has a distinct set of permitted screens and actions:

| Role | Permitted Actions |
|---|---|
| **Field Medic** | Add / edit / delete supplies; view expiring supplies; view FIT and CASUALTY personnel (read-only); flag personnel status as CASUALTY |
| **Medical Officer** | Add / edit / delete personnel; update any personnel status; view medical attention dashboard (PENDING, CASUALTY, MC, LIGHT_DUTY) |
| **Platoon Commander** | Add personnel (forced to PENDING status) / delete personnel; view FIT personnel; manage and auto-generate duty roster |
| **Logistics Officer** | Add / edit / delete supplies; view supply levels; generate resupply report |

Role restrictions are enforced at both the UI level (screens and buttons are hidden or disabled if not permitted) and the command level (commands reject execution if the active role is not in the allowed roles list).

---

### Personnel Management

The system shall allow authorised users to:

- Add new personnel records.
- Update personnel medical status.
- Remove personnel records.
- View the list of personnel (filtered by role).

Each **Personnel** record includes:

- Name (unique, case-insensitive)
- Medical readiness status
- Blood group (A+, A−, B+, B−, AB+, AB−, O+, O−, or Unknown)
- Allergies (free-text field)

Personnel status values:

| Status | Description |
|---|---|
| **PENDING** | Newly added by Platoon Commander; awaiting Medical Officer assessment |
| **FIT** | Medically cleared for full duty |
| **LIGHT_DUTY** | Cleared for limited duty; requires MO monitoring |
| **MC** | On medical leave; not deployable |
| **CASUALTY** | Flagged unwell outfield by Field Medic; requires immediate MO assessment |

Status update permissions by role:

- **Medical Officer**: may set any status on any personnel
- **Platoon Commander**: may add personnel (status forced to PENDING); cannot edit status
- **Field Medic**: may only flag a personnel member as CASUALTY

---

### Medical Attention Dashboard

The system shall provide Medical Officers with a filtered view of personnel who require attention — those with status PENDING, CASUALTY, MC, or LIGHT_DUTY — so that triage and follow-up can be prioritised efficiently.

---

### Inventory Management

The system shall allow authorised users (Field Medics and Logistics Officers) to:

- Add new medical supply items.
- Update supply information such as quantity and expiry date.
- Delete supply records when items are consumed or expired.
- View a list of all medical supplies.

Each **Supply** item includes:

- Name (unique, case-insensitive)
- Quantity
- Expiry date

---

### Expiration Monitoring

The system shall:

- Retrieve the system date from the user's computer.
- Identify and display supplies that will expire within 30 days, sorted by expiry date ascending.

---

### Supply Levels Monitoring

The system shall:

- Identify supplies whose quantity falls below a threshold of 20 units.
- Display these items sorted by quantity ascending so the most critical shortages appear first.

---

### Resupply Report

The system shall allow Logistics Officers to:

- Analyse the inventory and flag supplies that are low in stock (quantity < 20) or expiring within 30 days.
- Display a consolidated report indicating the reason each item was flagged (low stock, expiring, or both).

---

### Duty Roster — Structured Scheduling

The system shall allow Platoon Commanders to build a persistent duty schedule by:

- Adding individual duty slots, each specifying:
  - Start time and end time (HH:mm format)
  - Duty type: Guard Duty, Medical Cover, Patrol, Standby, or Sentry
  - Assigned personnel (selected from FIT personnel)
- Editing an existing slot's duty type or assigned personnel while keeping the time window fixed.
- Removing individual slots.
- Clearing the entire roster.
- Persisting all duty slots to the local data file so the schedule survives application restarts.

---

### Duty Roster — Auto-Generation

The system shall provide an auto-generation function that:

- Accepts a user selection of duty types to schedule.
- Applies the following coverage windows per duty type:
  - **Guard Duty, Patrol**: 00:00 – 00:00 (24-hour coverage)
  - **Medical Cover, Standby, Sentry**: 08:00 – 20:00 (12-hour coverage)
- Calculates slot duration by dividing the coverage window by the number of FIT personnel, rounded to the nearest :00 or :30, clamped between 30 minutes and 4 hours.
- Assigns FIT personnel to slots using a round-robin strategy, enforcing two hard constraints per person per day:
  - No two duty slots may overlap.
  - At least one 8-hour continuous break must remain after each assignment.
- Allows one person to hold multiple duty slots of different types provided the above constraints are met.
- Offers a choice to replace the existing roster or append to it.
- Requires a minimum of **3 FIT personnel** for full 24-hour coverage; fewer personnel result in some slots going unassigned without an application error.

---

### CSV Data Export

The system shall allow users to export their accessible data to a CSV file. The content is determined by the user's role:

| Role | Exported Sections |
|---|---|
| **Field Medic** | Personnel roster + Supply inventory |
| **Medical Officer** | Personnel roster |
| **Platoon Commander** | Personnel roster + Duty roster |
| **Logistics Officer** | Supply inventory |

Export behaviour:

- Files are saved to an auto-created `/exports` directory with a timestamped filename.
- Personnel records requiring medical attention are flagged with `⚠ MEDICAL ATTENTION`.
- Supply records that are low in stock or expiring soon are flagged with `⚠ LOW / EXPIRING`.
- Duty roster exports include Time Slot, Duty Type, and Personnel columns.

---

## 6. Non-Functional Requirements

### Data Persistence

All application data (supplies, personnel, duty slots, and the master password hash) must be stored in a local JSON file and automatically loaded when the application starts, ensuring the system works without internet connectivity. Duty slot data is backward-compatible — existing data files without duty slots load without error.

---

### Performance

The system should process user actions and update the user interface within 1 second when managing up to 200 supply or personnel records.

---

### Reliability

The system should detect invalid inputs and display appropriate error messages without crashing the application. Corrupt individual records in the data file are skipped with a warning rather than preventing application startup.

---

### Data Integrity

The system should prevent invalid data entries such as:

- Negative supply quantities
- Invalid or past expiration dates
- Missing required fields
- Duplicate personnel or supply names (case-insensitive)

---

### Security

MediTrack uses a single shared password to protect access to the application, reflecting the reality that field teams typically operate from one shared device.

The system shall:

- Require a password at application launch before any features are accessible. On first launch, prompt the user to set the password. The password shall be stored immediately as a BCrypt hash (cost factor 12) in the local JSON data file — never in plain text.
- Present a role selection screen upon successful authentication, where the user selects their operating role.
- Restrict available screens and actions to those permitted for the selected role.
- Destroy the active session when the user logs out or the application is closed, requiring re-authentication on the next launch.