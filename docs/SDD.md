# MediTrack: Software Design Document

---

## 1. System Overview

MediTrack is a standalone JavaFX desktop application built to help military field units manage medical supply inventories and track the medical readiness of their personnel, all in one place, with no internet required.

The application supports four user roles: Field Medic, Medical Officer, Platoon Commander, and Logistics Officer. Each role gets a different view of the system after logging in, so users only see what's relevant to them. Everything is stored locally in a JSON file, meaning the app works completely offline with no external dependencies.

### 1.1 Design Goals

- Role-based access so each user type only sees what they need. This means no clutter and no unauthorised actions.
- Strict adherence to SOLID principles, heavily utilizing ***Dependency Inversion*** to ensure a clean separation between the UI, business logic, data model, and storage.
- Fully offline operation, since field environments can't always guarantee internet access.
- Input validation and rigorous exception handling at every entry point, ensuring bad data is safely rejected.

---

## 2. Architecture Design

MediTrack uses a heavily decoupled, layered architecture split into four main layers: UI, Logic, Model, and Storage. By programming against abstract interfaces (`Logic` and `Model`) rather than concrete implementations, the layers remain loosely coupled and easily testable.

![Architecture Diagram](system_architecture.png)

### 2.1 Layer Responsibilities

| Layer   | Responsibility                                                                                                                                  |
|---------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| UI      | Renders the interface, captures user actions, and displays results. Does not interact directly with Storage.                                   |
| Logic   | Acts as the central coordinator. Receives commands from the UI, executes them against the Model, and automatically triggers Storage auto-saves. |
| Model   | Holds the in-memory state of the session, Supply, and Personnel records. Exposes an abstract interface to the UI.                               |
| Storage | Reads from and writes to `data.json`. Isolated from the UI layer entirely.                                                                      |

### 2.2 Authentication Layer

There is a lightweight authentication layer that runs before the main UI loads. On every launch, a login screen presents a role dropdown and a password field. Each role uses a fixed demo password (see PRD); at runtime `LoginScreen` holds BCrypt hashes produced via `PasswordManager.hashPassword(...)` and verifies input with `PasswordManager.checkPassword()`. Credentials are **not** read from `data.json`. On success, the selected role is written into the in-memory `Session` managed within the `Model`, and the main application screen loads.

### 2.3 Parser Role in a GUI Context

The Parser's job is purely **validation**. It checks that the values collected from a form are valid before the UI constructs a Command. If something's wrong, it throws a `ParseException` and the UI shows the error message inline.

---

## 3. Major System Components

### 3.1 UI Component

Built entirely in JavaFX. The UI relies heavily on modular builder methods to prevent massive, bloated classes. Crucially, the UI talks to the backend through abstract interfaces (`Logic.executeCommand(Command)` and `Model`). It has zero direct coupling to the `Storage` layer or concrete `ModelManager` classes.

**Pre-login screens**
- Login Screen: password field, role dropdown, login button (shown on every launch; if `data.json` is missing the app still starts here with an empty in-memory dataset)

**Field Medic screens**
- Inventory: full supply table with add, edit, and delete actions
- Expiring Soon: automatically filtered view of supplies expiring within 30 days
- Personnel: read-only personnel table for situational awareness

**Medical Officer screens**
- Personnel: full personnel table with add, remove, and inline status update (any status)
- Medical Attention: filtered view of personnel requiring attention (PENDING, CASUALTY, MC, LIGHT_DUTY) with colour-coded status badges

**Platoon Commander screens**
- Personnel: personnel table with add (forced PENDING status) and remove
- FIT Personnel: filtered view of FIT personnel with a live headcount
- Duty Roster: date-navigated duty schedule with add, edit, remove, clear day, and auto-generate actions

**Logistics Officer screens**
- Supply Levels: read-only view of the full inventory with low-stock highlighting
- Resupply Report: auto-generated report flagging low stock and items nearing expiry

**Shared modal dialogs**
- Add Supply, Edit Supply, Delete Supply (with confirmation)
- Add Personnel, Remove Personnel (with confirmation)
- Add Slot (duty roster), Auto-Generate (duty roster), Clear Day confirmation

### 3.2 Logic Component

This is the execution engine of the app. It receives Command objects from the UI, checks the current session role, runs the command against the Model, and automatically triggers the Storage layer to save the data.

Key classes:
- `LogicManager`: the central coordinator, implements the `Logic` interface
- `Command` (abstract): base class for all commands
- Concrete commands: `AddSupplyCommand`, `EditSupplyCommand`, `DeleteSupplyCommand`, `AddPersonnelCommand`, `RemovePersonnelCommand`, `UpdateStatusCommand`, `GenerateResupplyReportCommand`
- `RosterAutoGenerator`: static utility that generates randomised duty rosters from FIT personnel with constraint satisfaction (no overlap + 8-hour break rule)

If the current role accessed via `model.getSession().getRole()` does not have permission, a `CommandException` is thrown and the UI gracefully handles the error.

### 3.3 Parser Component

The Parser is a validation-only utility. The UI calls it after the user hits Confirm on any form, before building the Command object. It checks each field against the rules for that command type and throws a ParseException with a clear message if anything fails.

Key classes:
- `Parser`: exposes `validate(CommandType, Map<String, String>)`
- `CommandType`: enum covering all supported command types
- `ParseException`: thrown with a descriptive message on validation failure

Validation rules by command:

| Command | Rules enforced |
|---------|----------------|
| ADD_SUPPLY / EDIT_SUPPLY | Name is non-empty, quantity > 0, expiry date is valid and in the future, no duplicate name on add |
| DELETE_SUPPLY / REMOVE_PERSONNEL | Index is a positive integer within the current list bounds |
| ADD_PERSONNEL | Name is non-empty, status is a valid `Status` enum value |
| UPDATE_STATUS | Status must be one of: `FIT`, `LIGHT_DUTY`, `MC`, `CASUALTY`, `PENDING` |
| GENERATE_RESUPPLY_REPORT | At least one supply record must exist |

### 3.4 Model Component

The Model component is responsible for holding the application's in-memory state. It acts as a facade, exposing an abstract `Model` interface to the UI while keeping the concrete implementations hidden.

Key classes:
- `Session`: a POJO securely managed by the Model that tracks which Role is currently logged in. (Refactored away from a Singleton to eliminate hidden global state).
- `ModelManager`: the concrete implementation of the `Model` interface that acts as the central point of access for all data operations.
- `MediTrack`: the root data container, holding the supply list, personnel list, and duty slot list.
- `Supply`: represents a medical supply item with a name (String), quantity (int), and expiryDate (LocalDate).
- `Personnel`: represents a person with a name (String), status (Status), bloodGroup (BloodGroup), and allergies (String).
- `DutySlot`: represents a scheduled duty assignment with date (LocalDate), startTime/endTime (LocalTime), dutyType (DutyType), and personnelName (String).
- `Status`: enum with five values: `FIT`, `LIGHT_DUTY`, `MC`, `CASUALTY`, `PENDING`.
- `Role`: enum with four values: `FIELD_MEDIC`, `MEDICAL_OFFICER`, `PLATOON_COMMANDER`, `LOGISTICS_OFFICER`.
- `DutyType`: enum with five values: `GUARD_DUTY`, `MEDICAL_COVER`, `PATROL`, `STANDBY`, `SENTRY`.
- `BloodGroup`: enum with nine values: `A_POS`, `A_NEG`, `B_POS`, `B_NEG`, `AB_POS`, `AB_NEG`, `O_POS`, `O_NEG`, `UNKNOWN`.

### 3.5 Storage Component

The Storage component deals with reading from and writing to disk. It uses the Jackson library to serialise the Model's state into `data.json`. This file is automatically rewritten by the Logic engine after every successful command execution.

Key classes:
- `StorageManager`: the concrete implementation of the `Storage` interface.
- `JsonMediTrackStorage`: handles the actual reading and writing of `data.json`, utilizing Dependency Injection to allow safe file-path overrides during automated unit testing.
- `JsonAdaptedSupply`: a JSON-friendly wrapper around `Supply`, used during serialisation and deserialisation.
- `JsonAdaptedPersonnel`: same idea, but for `Personnel` (includes bloodGroup and allergies).
- `JsonAdaptedDutySlot`: same idea, but for `DutySlot`.
- `CsvExportUtility`: static utility for role-filtered CSV data export with flags for medical attention and low/expiring stock.

### 3.6 PasswordManager Component

`PasswordManager` is a simple stateless utility class providing `hashPassword(String)` and `checkPassword(String, String)`. Keeping it separate avoids mixing authentication logic into `Logic` or `Storage`.

---

## 4. UML Diagrams

### 4.1 Class Diagram

![Class Diagram](classdiag.svg)

### 4.2 Sequence Diagram — Add Supply

![Add Supply](sequencediag.svg)

**Note:** If `Parser.validate()` throws a `ParseException`, the UI catches it and displays the error without ever calling `Logic.executeCommand()`. The sequence diagram only covers the happy path.


### 4.4 Use Case Diagram

![Use Case Diagram](use.svg)

---

## 5. Key Design Decisions

### 5.1 Eradication of Global State (Singleton Removal)

**Decision:** The application's `Session` tracking is modeled as a standard Plain Old Java Object (POJO) whose lifecycle is instantiated and managed strictly within the `Model` facade, rather than utilizing a global `Session.getInstance()` Singleton.

**Rationale:** Singletons introduce hidden global state, creating invisible dependencies across the codebase that make testing incredibly difficult. By making the session an object managed by the Model, session state becomes predictable, explicitly injected, and easily mockable for automated tests.

---

### 5.2 Dependency Inversion & Loose Coupling

**Decision:** The UI layer is strictly prohibited from interacting with concrete implementation classes (like `ModelManager` or `StorageManager`). Instead, the UI depends entirely on the abstract `Model` and `Logic` interfaces. Furthermore, the UI layer is completely decoupled from the Storage layer.

**Rationale:** This adheres perfectly to the Dependency Inversion Principle (the 'D' in SOLID). By preventing the UI from calling `storage.save()`, we eliminate scattered persistence calls across the codebase. The UI simply asks the `Logic` engine to execute a command, and the `Logic` engine orchestrates the resulting data save.

---

### 5.3 Layered Architecture (UI → Logic → Model / Storage)

**Decision:** Use a four-layer architecture where each layer only depends on the layer directly below it.

**Rationale:** This keeps each layer focused on a single responsibility. The UI does not touch the data directly, it validates input through the Parser, builds a Command object, and hands it off to `Logic.executeCommand()`. This separation makes individual layers much easier to test in isolation.

---

### 5.4 Duty Roster as a Persisted Schedule

**Decision:** The duty roster is persisted to `data.json` as a list of structured `DutySlot` objects. Platoon Commanders can manually add individual slots, auto-generate slots using constraint-based logic, edit, remove, and clear slots by date.

**Rationale:** Persisting the roster allows Platoon Commanders to build up a schedule over multiple sessions and share the duty plan with other roles via CSV export. The auto-generation algorithm uses round-robin assignment with two hard constraints (no overlapping slots per person, minimum 8-hour break between assignments) to produce fair rosters automatically.

---

### 5.5 BCrypt Password Hashing

**Decision:** Store the application password as a BCrypt hash in `data.json`, using the `jbcrypt` library with a cost factor of 12.

**Rationale:** BCrypt was chosen because it is deliberately slow to compute and makes brute-force attacks much harder. Storing the password in plain text would be a significant security flaw.

---

### 5.6 Per-Role Hardcoded Credentials

**Decision:** Each role has a fixed password stored as a BCrypt hash. The login screen asks the user to select a role and enter its credential.

**Rationale:** Giving each role its own password makes it immediately clear which operational context the user is entering, reduces the chance of accidentally operating under the wrong role, and aligns with the reality of MediTrack being a demonstration application running on a single shared field device.

---

### 5.7 Hidden Developer Panel

**Decision:** Testing utilities (Fast Forward) are placed behind a `Ctrl + Shift + D` keyboard shortcut that toggles a hidden side panel, rather than being exposed as visible UI controls.

**Rationale:** Keeping test controls invisible in normal operation prevents accidental use during a live demonstration and keeps the UI uncluttered.

---