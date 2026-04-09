# MediTrack User Guide

## Table of Contents
1. [Introduction](#1-introduction)
2. [Quick Start](#2-quick-start)
3. [Authentication & Roles](#3-authentication--roles)
4. [General Navigation](#4-general-navigation)
5. [Feature Guide: Field Medic](#5-feature-guide-field-medic)
6. [Feature Guide: Medical Officer](#6-feature-guide-medical-officer)
7. [Feature Guide: Platoon Commander](#7-feature-guide-platoon-commander)
8. [Feature Guide: Logistics Officer](#8-feature-guide-logistics-officer)
9. [Global Utilities](#9-global-utilities)
10. [FAQ](#10-faq) 

---

## 1. Introduction

**MediTrack** is a standalone Java desktop application engineered for military field units. It replaces inefficient paper logs and spreadsheets by centralizing medical supply tracking, personnel readiness monitoring, and duty roster generation.

Designed for high-pressure environments, MediTrack operates completely offline and utilizes strict **Role-Based Access Control (RBAC)** to ensure that operators only see and interact with the data relevant to their specific operational duties.

---

## 2. Quick Start

1. Ensure you have **Java 17** or above installed on your computer.
2. Download the latest `MediTrack.jar` from the releases page.
3. Copy the file to an empty folder where you want to store your operational data.
4. Double-click the file, or open a command terminal, navigate to the folder, and run the command:`java -jar MediTrack.jar`
5. You will see the MediTrack Terminal login screen. 

---

## 3. Authentication & Roles

MediTrack uses Role-Based Access Control. Upon launching the application, you must select an `Operational Role` and enter the corresponding `Access Key`.

For demonstration and evaluation purposes, the following credentials are hardcoded into the system:

| **Operational**       | **RoleAccess Key (Password)** | **Primary Responsibility**                                                 |
|-----------------------|-------------------------------|----------------------------------------------------------------------------|
| **FIELD MEDIC**       | `fm123`                       | Managing physical supply inventory and reporting field casualties.         |
| **MEDICAL OFFICER**   | `mo123`                       | Assessing personnel and assigning medical statuses (e.g., MC, Light Duty). |
| **PLATOON COMMANDER** | `pc123`                       | Managing unit manpower and scheduling duty rosters.                        |
| **LOGISTICS OFFICER** | `lo123`                       | Auditing supply levels and generating resupply reports.                    |

**Note:** The system stores these securely as BCrypt hashes. Sessions are automatically destroyed upon clicking "Logout" or closing the application.

---

## 4. General Navigation

Once authenticated, the application is divided into three main areas:

- **Sidebar (Left):** Your primary navigation menu. It dynamically updates to only show the screens your current role is authorized to access.
- **Dashboard (Center):** The default landing page. It provides a macroscopic, role-specific summary of system health (e.g., total FIT personnel, critical supply warnings).
- **Footer (Bottom):** Contains pagination controls to navigate large lists and a summary of the current data view.

---

## 5. Feature Guide: Field Medic

The Field Medic is the primary operator for physical inventory and front-line triage.

### 5.1 Managing Supply Inventory

Navigate to **INVENTORY** via the sidebar.

- **Add Supply:** Click `+ ADD`. Enter the Nomenclature (name), Quantity, and Expiry Date (`YYYY-MM-DD`).
- **Edit Supply:** Click the `✎` (Edit) button on any row to update its quantity or expiry date.
- **Delete Supply:** Click the `✕` (Delete) button to permanently remove a consumed or ruined supply item.

### 5.2 Monitoring Expirations

Navigate to **EXPIRING SOON**.

- This screen automatically filters the inventory to display only items expiring within the next **30 days**. Items that have already expired are highlighted in critical red.

### 5.3 Reporting Field Casualties

Navigate to **PERSONNEL**.

- Field Medics have a restricted view of the roster (FIT and CASUALTY only).
- To flag a soldier who has fallen ill outfield, click the inline dropdown under the **STATUS** column and change it from `FIT` to `CASUALTY`. This immediately alerts the Medical Officer.

---

## 6. Feature Guide: Medical Officer

The Medical Officer has absolute authority over the medical readiness and profiles of all personnel.

### 6.1 Managing the Roster

Navigate to **PERSONNEL**.

- **Add Personnel:** Click `+ ADD`. MOs can assign any initial status and input highly specific medical details, including `Blood Group` and `Allergies`.
- **Update Status:** Use the inline dropdown to change a soldier's status.
    - If changing to `MC` or `LIGHT DUTY`, a prompt will ask for the **Duration (in days)**. The system will automatically calculate the expiration date and revert the soldier to `FIT` when the duration ends.

### 6.2 Medical Attention Dashboard

Navigate to **MEDICAL ATTENTION**.

- This triage dashboard automatically isolates personnel who are currently marked as `PENDING`, `CASUALTY`, `MC`, or `LIGHT DUTY`.
- Use this screen to quickly identify soldiers who require immediate medical review or follow-up.

--- 

## 7. Feature Guide: Platoon Commander

The Platoon Commander is responsible for drafting personnel into the system and managing duty schedules.

### 7.1 Drafting Personnel

Navigate to **PERSONNEL**.

- **Add Personnel:** Click `+ ADD`. Platoon Commanders can draft new soldiers into the system, but their medical status is strictly locked to `PENDING` until a Medical Officer clears them.

### 7.2 Managing the Duty Roster

Navigate to **DUTY ROSTER**.

- Use the `← PREV` and `NEXT →` buttons at the top to navigate between dates.
- **Manual Assignment:** Click `+ ADD SLOT`. Input the time (e.g., `08:00`), select a duty type, and assign a currently `FIT` soldier.
- **Clear Day:** Use the red `CLEAR DAY` button at the bottom to wipe the current date's schedule clean.

### 7.3 Auto-Generating a Roster

From the Duty Roster screen, click **AUTO-GENERATE**.

1. Select the Duty Types you require for the day.
2. Adjust the desired shift duration (in minutes) for each selected duty.
3. Click `GENERATE ROSTER`.
4. The algorithm will automatically assign `FIT` personnel to slots, mathematically guaranteeing no overlaps and ensuring every soldier receives a **mandatory 8-hour continuous break** between shifts. 

---

## 8. Feature Guide: Logistics Officer

The Logistics Officer audits the system to prepare for rear-echelon resupply.

### 8.1 Supply Levels Overview

Navigate to **SUPPLY LEVELS**.

- This provides a read-only, macroscopic view of the entire inventory, automatically sorting items by severity. Critical items (Quantity < 10) are pushed to the top and highlighted in red.

### 8.2 Generating Resupply Reports 

Navigate to **RESUPPLY REPORT**.

- The system automatically scans the database against internal thresholds (Quantity < 50, Expiry < 30 days).
- It generates a consolidated action report detailing exactly why an item was flagged (e.g., "LOW STOCK & EXPIRING"), streamlining the requisition process.

--- 

## 9. Global Utilities

### 9.1 Exporting Data to CSV

Any user can export their current operational data for reporting to higher headquarters.

1. Click **EXPORT CSV** in the bottom left of the Sidebar.
2. The system checks your current RBAC Role and generates a report containing only the data you are authorized to see.
3. A success popup will display the exact file path where the CSV was saved (e.g., `/exports/MEDICAL_OFFICER_Export_20261122_1430.csv`).

---

### 10. FAQ

**Q: Do I need an internet connection to use MediTrack?**

A: No. MediTrack is designed for austere environments. All data is saved instantly to a local `data.json` file on your hard drive.

**Q: I assigned a soldier an MC for 3 days. Do I need to manually change them back to FIT?**

A: No. The system registers the expiration date. Once the 3 days have passed according to your system clock, the soldier will automatically be reverted to `FIT` status.

**Q: I am a Platoon Commander, why can't I edit supply levels?**

A: MediTrack strictly enforces Role-Based Access Control (RBAC) to protect data integrity. Platoon Commanders manage manpower, while Field Medics and Logistics Officers manage supplies.

**⚠ Note for Evaluators: Developer Time-Travel Mode**

To facilitate the testing of time-dependent features (like MC expirations or supply expirations) without altering your system clock, MediTrack includes a hidden developer tool.

1. From any screen inside the application, press `Ctrl + Shift + D`.
2. A green "DEV MODE" panel will appear in the top right corner.
3. Click **TIME TRAVEL (DAYS)** and input a number (e.g., `3`) to fast-forward the application's internal clock. Watch as statuses automatically revert to FIT and supplies turn red!