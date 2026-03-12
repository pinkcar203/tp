# MediTrack Internal API Documentation

## Introduction
MediTrack is a standalone desktop GUI application engineered to streamline logistics and personnel readiness for 
military field medics. To ensure the system remains maintainable, scalable, and easy to test, it is built upon a 
modular architecture.

The system relies on internal APIs to establish clear contracts between its core components: the **UI**, **Logic**, 
**Model**, and **Storage**. This document outlines the primary methods that facilitate data flow, command execution, 
and state management across these module boundaries.

---

## 1. Logic API

The Logic component is the main execution engine of the application. It receives the parsed commands and executes them 
by interacting with the Model and Storage components.

### `Logic.executeCommand(String commandText)`
* **Description:** Receives the raw command string from the UI, utilizes the Parser to translate it, executes 
the resulting command, and handles saving the updated state.
* **Parameters / inputs:** 
  * `commandText` (String): The text inputted by the user in the GUI command box.
* **Return values:** `CommandResult` - An object containing the feedback message to be displayed to the user and 
any specific UI instructions.
* **Example usage:** `CommandResult result = logic.executeCommand("delete 1");`

---

## 2. Parser API

The Parser component is strictly responsible for making sense of user input. It translates raw strings into 
structured, executable command objects without executing them.

### `Parser.parseCommand(String userInput)`
* **Description:** Parses the raw string input from the user and translates it into an executable `Command` object.
* **Parameters / inputs:** 
  * `userInput` (String): The raw text entered by the user.
* **Return values:** `Command` - A specific command object (e.g., `AddSupplyCommand`, `DeletePersonnelCommand`) ready 
to be executed by the Logic component.
* **Example usage:** `Command command = parser.parseCommand("add supply panadol 50");`

---

## 3. Model API

The Model component manages the in-memory state of the application, handling all Create, Read, Update, and Delete 
operations for the Medical Inventory and the Personnel Roster.

### `Model.addSupply(Supply supply)`
* **Description:** Adds a new medical supply item to the active inventory tracker.
* **Parameters / inputs:** 
  * `supply` (Supply): A valid `Supply` object containing details like name, quantity, and expiry date.
* **Return values:** `void`
* **Example usage:** `model.addSupply(new Supply("Bandages", 100, "2026-12-01"));`

### `Model.deleteSupply(Index targetIndex)`
* **Description:** Removes a fully consumed or expired supply item from the inventory based on its displayed index 
in the UI.
* **Parameters / inputs:** 
  * `targetIndex` (Index): The 1-based index of the item as shown in the GUI list.
* **Return values:** `Supply` - Returns the deleted supply object for logging or undo purposes.
* **Example usage:** `Supply removedItem = model.deleteSupply(Index.fromOneBased(1));`

### `Model.setPersonnelStatus(Personnel target, Status newStatus)`
* **Description:** Updates the medical readiness status of a specific soldier without overwriting their entire profile.
* **Parameters / inputs:** 
  * `target` (Personnel): The specific personnel object to update.
  * `newStatus` (Status): The new medical status (e.g., `FIT`, `LIGHT_DUTIES`).
* **Return values:** `void`
* **Example usage:** `model.setPersonnelStatus(johnDoe, Status.LIGHT_DUTIES);`

### `Model.getFilteredPersonnelList()`
* **Description:** Retrieves the current list of personnel, applying any active filters. This is used by the UI to 
populate the main table or list view dynamically.
* **Parameters / inputs:** None.
* **Return values:** `ObservableList<Personnel>` - A list that automatically updates the UI when the underlying 
data changes.
* **Example usage:** `personnelListView.setItems(model.getFilteredPersonnelList());`

---

## 4. Storage API

The Storage component handles reading from and writing to the local hard drive, ensuring data persists between 
application sessions without relying on an external database.

### `Storage.readMediTrackData()`
* **Description:** Reads the local JSON file and loads the saved inventory and roster data into the application's 
memory during startup.
* **Parameters / inputs:** None.
* **Return values:** `Optional<ReadOnlyMediTrack>` - Returns the parsed data if the file exists and is valid, or 
an empty Optional if no previous save data is found.
* **Example usage:** `Optional<ReadOnlyMediTrack> data = storage.readMediTrackData();`

### `Storage.saveMediTrackData(ReadOnlyMediTrack data)`
* **Description:** Serializes the current state of the application and saves it to the local JSON file.
* **Parameters / inputs:** 
  * `data` (ReadOnlyMediTrack): A read-only snapshot of the current Model data.
* **Return values:** `void` (Throws `IOException` if the file cannot be written).
* **Example usage:** `storage.saveMediTrackData(model.getMediTrack());`