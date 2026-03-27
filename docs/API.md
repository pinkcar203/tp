# MediTrack Internal API Documentation

## Introduction

MediTrack is a standalone Java desktop point-and-click GUI application engineered to streamline medical logistics and 
personnel readiness for military field units. To ensure the system remains maintainable, scalable, and easy to test, 
it is built upon a modular layered architecture.

The system relies on internal APIs to establish clear contracts between its core components: the **UI**, **Parser**, 
**Logic**, **Model**, **Storage**, and **PasswordManager**. This document outlines the primary methods that facilitate 
data flow, command execution, and state management across these module boundaries.

All user interactions are performed via clickable GUI controls (buttons, dropdowns, modals). The UI collects typed 
field values from forms and constructs Command objects directly — there is no command-line input box and no string 
encoding of user input.

---

## 1. Parser API

The Parser component serves strictly as a validation utility. Before the UI constructs a Command object, it passes the 
raw user input to the Parser to ensure data integrity. It does not parse strings or construct Command objects itself.

### `Parser.validate(CommandType commandType, Map<String, String> fields)`
* **Description:** Validates raw string inputs from the UI against the specific business rules of the requested 
command type. Checks include: non-empty required fields, positive integer quantities, future expiry dates, and 
valid status enum values.
* **Parameters / inputs:** 
  * `commandType` (CommandType): The type of action being requested (e.g., `ADD_SUPPLY`).
  * `fields` (Map<String, String>): A key-value map of the raw input strings from the UI form.
* **Return values:** `void` - Completes silently if validation passes.
* **Exceptions:** Throws a `ParseException` containing a user-friendly error message indicating which field failed 
and why.
* **Validation rules by command type:**

  | Command | Rules enforced                                                                             |
  |---|--------------------------------------------------------------------------------------------|
  | `ADD_SUPPLY` / `EDIT_SUPPLY` | Name non-empty, quantity > 0, expiry date is a valid future date, no duplicate name on add |
  | `DELETE_SUPPLY` / `REMOVE_PERSONNEL` | Index is a positive integer within list bounds                                             |
  | `ADD_PERSONNEL` | Name non-empty, status is a valid Status enum value                                        |
  | `UPDATE_STATUS` | Status is one of `FIT`, `LIGHT_DUTY`, `MC`, `CASUALTY`, `PENDING`                           |
  | `GENERATE_ROSTER` | At least one FIT personnel record exists                                                   |
  | `GENERATE_RESUPPLY_REPORT` | At least one supply record exists                                                          |

* **Example usage:**
```java
  // Called by the UI after the user clicks Confirm on the Add Supply modal
  try {
      parser.validate(CommandType.ADD_SUPPLY, Map.of(
          "name",   nameField.getText(),
          "qty",    qtyField.getText(),
          "expiry", expiryPicker.getValue().toString()
      ));
      // Validation passed — UI constructs and executes the command
      Command command = new AddSupplyCommand(
          nameField.getText(),
          Integer.parseInt(qtyField.getText()),
          expiryPicker.getValue()
      );
      logic.executeCommand(command);
  } catch (ParseException e) {
      errorLabel.setText(e.getMessage());
  }
```

---

## 2. Logic API

The Logic component is the main execution engine of the application. Utilizing the Command Pattern, it receives 
pre-validated Command objects from the UI, verifies role-based permissions via the Session, executes the action 
against the Model, and triggers a save via Storage after every state-changing operation.

### `Logic.executeCommand(Command command)`
* **Description:** Receives a fully constructed and pre-validated Command object from the UI layer. It calls 
`command.getRequiredRoles()` and checks the active role via `model.getSession()` to confirm the action is permitted 
for that role. It then executes the command against the Model, and saves the updated state to Storage.
* **Parameters:** 
  * `command` (Command): A fully constructed Command object built by the UI after successful Parser validation.
* **Return values:** `CommandResult` - An object containing the feedback message to display to the user and any 
specific UI update instructions.
* **Exceptions:** Throws a `CommandException` if the current Session role is not contained within the command's 
allowed roles list.
* **Example usage:**
```java
Command command = new AddSupplyCommand("Panadol", 50, LocalDate.of(2027, 6, 1));
CommandResult result = logic.executeCommand(command);
```

---

## 3. Model API

The Model component manages the complete in-memory state of the application, handling all Create, Read, Update, and 
Delete operations for the Supply inventory and the Personnel roster, as well as the active User Session.

### `Model.getSession()`
* **Description:** Retrieves the current Session object. Used by the Logic component for role enforcement and by the 
JavaFX UI for observable role binding.
* **Parameters / inputs:** None.
* **Return values:** `Session` - The active user session.
* **Example usage:** 
```java
Session currentSession = model.getSession();
```

### `Model.setRole(Role role)`
* **Description:** Sets the active role in the Session after a successful login. Called by the UI login screen after 
`PasswordManager.checkPassword()` returns true.
* **Parameters / inputs:**
  * `role` (Role): The authenticated role (`FIELD_MEDIC`, `MEDICAL_OFFICER`, `LOGISTICS_OFFICER`, 
  or `PLATOON_COMMANDER`).
* **Return values:** `void`
* **Example usage:** 
```java
model.setRole(Role.MEDICAL_OFFICER);
```

### `Model.addSupply(Supply supply)`
* **Description:** Adds a new medical supply item to the active inventory.
* **Parameters:** 
  * `supply` (Supply): A valid Supply object containing name, quantity, and expiry date.
* **Return values:** `void`
* **Exceptions:** Throws `DuplicateSupplyException` if a supply with the same name already exists.
* **Example usage:** 
```java
model.addSupply(newSupply);
```

### `Model.editSupply(Index targetIndex, Supply editedSupply)`
* **Description:** Replaces the supply record at the given index with the provided updated Supply object. Used when 
the user confirms changes in the Edit Supply modal.
* **Parameters / inputs:**
  * `targetIndex` (Index): The 1-based index of the supply as shown in the UI table.
  * `editedSupply` (Supply): A new Supply object containing the updated field values.
* **Return values:** `void`
* **Example usage:** 
```java
model.editSupply(Index.fromOneBased(2), new Supply("Bandages", 80, LocalDate.of(2027, 6, 1)));
```

### `Model.deleteSupply(Index targetIndex)`
* **Description:** Removes a supply item from the inventory based on its displayed index in the UI table.
* **Parameters / inputs:** 
  * `targetIndex` (Index): The 1-based index of the item as shown in the GUI list.
* **Return values:** `Supply` - Returns the deleted supply object for logging or undo purposes.
* **Example usage:** 
```java
Supply removedItem = model.deleteSupply(Index.fromOneBased(1));
```

### `Model.getFilteredSupplyList()`
* **Description:** Retrieves the current list of supply items, applying any active filters. Used by the UI to populate 
the Inventory and Supply Levels screens.
* **Parameters / inputs:** None.
* **Return values:** `ObservableList<Supply>` - A live list that automatically triggers UI updates when modified.
* **Example usage:** 
```java
inventoryTableView.setItems(model.getFilteredSupplyList());
```

### `Model.getExpiringSupplies(int daysThreshold)`
* **Description:** Returns a filtered list of supply items whose expiry date falls within the given number of days 
from today's system date.
* **Parameters / inputs:**
  * `daysThreshold` (int): The number of days from today to use as the expiry cutoff (e.g., `30`).
* **Return values:** `List<Supply>` - Supplies expiring within the threshold, sorted by expiry date ascending.`
* **Example usage:** 
```java
List<Supply> expiring = model.getExpiringSupplies(30);
```

### `Model.getLowStockSupplies(int quantityThreshold)`
* **Description:** Returns a filtered list of supply items whose current quantity is below the given threshold.
* **Parameters / inputs:**
  * `quantityThreshold` (int): The minimum quantity below which a supply is considered low stock (e.g., `20`).
* **Return values:** `List<Supply>` - Supplies with quantity below the threshold, sorted by quantity ascending.
* **Example usage:** 
```java
List<Supply> lowStock = model.getLowStockSupplies(20);
```

### `Model.addPersonnel(Personnel personnel)`
* **Description:** Adds a new personnel record to the roster.
* **Parameters / inputs:**
  * `personnel` (Personnel): A valid `Personnel` object containing name and initial status.
* **Return values:** `void`
* **Example usage:** 
```java
model.addPersonnel(new Personnel("John Doe", Status.PENDING));
```

### `Model.deletePersonnel(Index targetIndex)`
* **Description:** Removes a personnel record from the roster based on its displayed index in the UI table.
* **Parameters / inputs:**
  * `targetIndex` (Index): The 1-based index of the personnel record as shown in the UI table.
* **Return values:** `Personnel` - Returns the deleted personnel object.
* **Example usage:** 
```java
Personnel removed = model.deletePersonnel(Index.fromOneBased(3));
```

### `Model.setPersonnelStatus(Personnel target, Status newStatus)`
* **Description:** Updates the medical readiness status of a specific personnel record without modifying any 
other fields. Valid statuses include `FIT`, `LIGHT_DUTY`, `MC`, `CASUALTY`, or `PENDING`.
* **Parameters / inputs:** 
  * `target` (Personnel): The specific personnel object to update.
  * `newStatus` (Status): The new medical status (e.g., `FIT`, `LIGHT_DUTY`, `MC`, `CASUALTY`, or `PENDING`).
* **Return values:** `void`
* **Example usage:** 
```java
model.setPersonnelStatus(johnDoe, Status.LIGHT_DUTY);
```

### `Model.getFilteredPersonnelList(Status statusFilter)`
* **Description:** Retrieves the list of personnel based on the provided status filter. Passing `null` returns 
the entire unfiltered roster. The UI may further filter this list based on the active session role 
(e.g., Field Medics only seeing `FIT` and `CASUALTY`).
* **Parameters / inputs:** `statusFilter` (Status): The specific status to filter by, or `null` for all.
* **Return values:** `List<Personnel>` - The requested personnel list.
* **Example usage:** 
```java
List<Personnel> allPersonnel = model.getFilteredPersonnelList(null);
```

### `Model.generateResupplyReport()`
* **Description:** Analyzes the inventory and generates a report flagging items with a quantity below 20 or an expiry 
within 30 days.
* **Parameters:** None.
* **Return values:** `List<ReportItem>` - A structured list containing the flagged items and the reason 
they were flagged.
* **Example usage:** 
```java
List<ReportItem> report = model.generateResupplyReport();
```

---

## 4. Storage API

The Storage component handles reading from and writing to the local hard drive, ensuring data (including security 
credentials) persists between application sessions without relying on a network or external database.

### `Storage.isFirstLaunch()`
* **Description:** Checks whether the local data file exists. Called at application startup to determine whether to 
show the first-launch password setup screen or proceed directly to the login screen.
* **Parameters / inputs:** None.
* **Return values:** `boolean` - `true` if data.json does not exist, `false` if it does.
* **Example usage:** 
```java
if (storage.isFirstLaunch()) {
    ui.showPasswordSetupScreen();
} else {
    ui.showLoginScreen();
}
```

### `Storage.readMediTrackData()`
* **Description:** Reads the local JSON file during startup to load the saved inventory, roster data, and the 
application's master BCrypt password hash into memory.
* **Parameters / inputs:** None.
* **Return values:** `Optional<ReadOnlyMediTrack>` - Returns the parsed data if the file exists and is valid, or an 
empty Optional if no previous save data is found.
* **Example usage:** 
```java
Optional<ReadOnlyMediTrack> data = storage.readMediTrackData();
```

### `Storage.saveMediTrackData(ReadOnlyMediTrack data)`
* **Description:** Serializes the current state of the application (inventory, roster, and password hash) and saves 
it to the local JSON file. Called automatically by the Logic layer after every state-changing command.
* **Parameters / inputs:** 
  * `data` (ReadOnlyMediTrack): A read-only snapshot of the current Model data.
* **Return values:** `void` (Throws `IOException` if the file cannot be written).
* **Example usage:** 
```java
storage.saveMediTrackData(model.getMediTrack());
```

### `CsvExportUtility.exportData(ReadOnlyMediTrack data, Role currentRole)`
* **Description:** A static utility method that exports the application data to a CSV file. It implements 
Role-Based Access Control (RBAC): Medical Officers and Platoon Commanders export the Personnel Roster, 
Logistics Officers export the Supply Inventory, and Field Medics export both. It automatically flags items 
requiring medical attention or restocking.
* **Parameters:** * `data` (ReadOnlyMediTrack): A read-only snapshot of the current state of the application.
  * `currentRole` (Role): The active role of the user requesting the export.
* **Return values:** `Path` - The file path where the CSV was successfully saved (saved in an 
auto-generated `/exports` directory).
* **Exceptions:** Throws `IOException` if the application lacks permission to create the directory or write the file.
* **Example usage:** 
```java
  Path savedPath = CsvExportUtility.exportData(model.getMediTrack(), model.getSession().getRole());
```

---

## 5. PasswordManager API

The PasswordManager is a stateless utility component dedicated solely to application security, ensuring that sensitive 
credentials are not processed directly by general logic classes. It does not store the password itself — it only 
provides methods to hash and verify it.

### `PasswordManager.hashPassword(String plainText)`
* **Description:** Takes a plain-text password and returns a BCrypt hash string suitable for storage. Uses a BCrypt 
cost factor of 12. Called once when the user sets their password on first launch. The plain-text password is never 
stored or logged.
* **Parameters / inputs:**
  * `plainText` (String): The plain-text password entered by the user during the first-launch setup screen.
* **Return values:** `String` - A BCrypt hash string.
* * **Example usage:** 
```java
String hash = PasswordManager.hashPassword("mySecurePassword");
```

### `PasswordManager.checkPassword(String plainTextPassword, String storedHash)`
* **Description:** Compares the plain text password entered by the user at launch against the BCrypt hash stored in 
the local data file. The plain-text password is never stored or logged.
* **Parameters:** 
  * `plainTextPassword` (String): The password entered in the UI.
  * `storedHash` (String): The BCrypt hash retrieved from Storage.
* **Return values:** `boolean` - Returns `true` if the password matches the hash, `false` otherwise.
* **Example usage:** 
```java
boolean isAuth = PasswordManager.checkPassword(inputPassword, savedHash);
```