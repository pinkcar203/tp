package meditrack.logic.parser.personnel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import meditrack.logic.commands.personnel.AddPersonnelCommand;
import meditrack.logic.commands.personnel.RemovePersonnelCommand;
import meditrack.logic.commands.personnel.UpdateStatusCommand;
import meditrack.logic.parser.exceptions.ParseException;
import meditrack.model.Status;

class PersonnelParserTest {

    // POSITIVE TESTS 

    @Test
    void test_parseAddPersonnel_validNameAndFitStatus_returnsCommand() throws ParseException {
        // Arrange
        String validArgs = "n/John Tan s/FIT";

        // Act
        AddPersonnelCommand command = PersonnelParser.parseAddPersonnel(validArgs);

        // Assert
        assertNotNull(command);
        assertEquals("John Tan", command.getName());
        assertEquals(Status.FIT, command.getStatus());
    }

    @Test
    void test_parseAddPersonnel_validNameAndMcStatus_returnsCommand() throws ParseException {
        // Arrange
        String validArgs = "n/Alice Wong s/MC";

        // Act
        AddPersonnelCommand command = PersonnelParser.parseAddPersonnel(validArgs);

        // Assert
        assertEquals("Alice Wong", command.getName());
        assertEquals(Status.MC, command.getStatus());
    }

    @Test
    void test_parseAddPersonnel_validNameAndCasualtyStatus_returnsCommand() throws ParseException {
        // Arrange
        String validArgs = "n/Bob Lee s/CASUALTY";

        // Act
        AddPersonnelCommand command = PersonnelParser.parseAddPersonnel(validArgs);

        // Assert
        assertEquals("Bob Lee", command.getName());
        assertEquals(Status.CASUALTY, command.getStatus());
    }

    @Test
    void test_parseAddPersonnel_validNameAndLightDutyStatus_returnsCommand() throws ParseException {
        // Arrange
        String validArgs = "n/Charlie s/LIGHT_DUTY";

        // Act
        AddPersonnelCommand command = PersonnelParser.parseAddPersonnel(validArgs);

        // Assert
        assertEquals("Charlie", command.getName());
        assertEquals(Status.LIGHT_DUTY, command.getStatus());
    }

    @Test
    void test_parseAddPersonnel_validNameAndPendingStatus_returnsCommand() throws ParseException {
        // Arrange
        String validArgs = "n/Dave s/PENDING";

        // Act
        AddPersonnelCommand command = PersonnelParser.parseAddPersonnel(validArgs);

        // Assert
        assertEquals("Dave", command.getName());
        assertEquals(Status.PENDING, command.getStatus());
    }

    @Test
    void test_parseAddPersonnel_caseInsensitiveStatus_returnsCommand() throws ParseException {
        // Arrange
        String validArgs = "n/Eve s/fit";

        // Act
        AddPersonnelCommand command = PersonnelParser.parseAddPersonnel(validArgs);

        // Assert
        assertEquals(Status.FIT, command.getStatus());
    }

    // NEGATIVE TESTS 

    @Test
    void test_parseAddPersonnel_nullArgs_throwsParseException() {
        // Arrange
        String nullArgs = null;

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseAddPersonnel(nullArgs));
    }

    @Test
    void test_parseAddPersonnel_missingNamePrefix_throwsParseException() {
        // Arrange
        String argsWithoutNamePrefix = "John Tan s/FIT";

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseAddPersonnel(argsWithoutNamePrefix));
    }

    @Test
    void test_parseAddPersonnel_blankNameValue_throwsParseException() {
        // Arrange
        String argsWithBlankName = "n/   s/FIT";

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseAddPersonnel(argsWithBlankName));
    }

    @Test
    void test_parseAddPersonnel_missingStatusPrefix_throwsParseException() {
        // Arrange
        String argsWithoutStatusPrefix = "n/John Tan";

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseAddPersonnel(argsWithoutStatusPrefix));
    }

    @Test
    void test_parseAddPersonnel_invalidStatusValue_throwsParseException() {
        // Arrange
        String argsWithInvalidStatus = "n/John Tan s/SUPER_FIT";

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseAddPersonnel(argsWithInvalidStatus));
    }

    // POSITIVE TESTS 

    @Test
    void test_parseRemovePersonnel_validPositiveIndex_returnsCommand() throws ParseException {
        // Arrange
        String validIndex = "1";

        // Act
        RemovePersonnelCommand command = PersonnelParser.parseRemovePersonnel(validIndex);

        // Assert
        assertNotNull(command);
        assertEquals(1, command.getOneBasedIndex());
    }

    @Test
    void test_parseRemovePersonnel_largeValidIndex_returnsCommand() throws ParseException {
        // Arrange
        String largeIndex = "999";

        // Act
        RemovePersonnelCommand command = PersonnelParser.parseRemovePersonnel(largeIndex);

        // Assert
        assertEquals(999, command.getOneBasedIndex());
    }

    // NEGATIVE TESTS 

    @Test
    void test_parseRemovePersonnel_zeroIndex_throwsParseException() {
        // Arrange
        String zeroIndex = "0";

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseRemovePersonnel(zeroIndex));
    }

    @Test
    void test_parseRemovePersonnel_negativeIndex_throwsParseException() {
        // Arrange
        String negativeIndex = "-1";

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseRemovePersonnel(negativeIndex));
    }

    @Test
    void test_parseRemovePersonnel_nonNumericIndex_throwsParseException() {
        // Arrange
        String nonNumericIndex = "abc";

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseRemovePersonnel(nonNumericIndex));
    }

    // POSITIVE TESTS 

    @Test
    void test_parseUpdateStatus_validIndexAndFitStatus_returnsCommand() throws ParseException {
        // Arrange
        String validArgs = "1 s/FIT";

        // Act
        UpdateStatusCommand command = PersonnelParser.parseUpdateStatus(validArgs);

        // Assert
        assertNotNull(command);
        assertEquals(1, command.getOneBasedIndex());
        assertEquals(Status.FIT, command.getNewStatus());
    }

    @Test
    void test_parseUpdateStatus_validIndexAndCasualtyStatus_returnsCommand() throws ParseException {
        // Arrange
        String validArgs = "3 s/CASUALTY";

        // Act
        UpdateStatusCommand command = PersonnelParser.parseUpdateStatus(validArgs);

        // Assert
        assertEquals(3, command.getOneBasedIndex());
        assertEquals(Status.CASUALTY, command.getNewStatus());
    }

    // NEGATIVE TESTS 

    @Test
    void test_parseUpdateStatus_missingStatusPart_throwsParseException() {
        // Arrange
        String argsMissingStatus = "1";

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseUpdateStatus(argsMissingStatus));
    }

    @Test
    void test_parseUpdateStatus_zeroIndex_throwsParseException() {
        // Arrange
        String argsWithZeroIndex = "0 s/FIT";

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseUpdateStatus(argsWithZeroIndex));
    }

    @Test
    void test_parseUpdateStatus_nonNumericIndex_throwsParseException() {
        // Arrange
        String argsWithBadIndex = "abc s/FIT";

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseUpdateStatus(argsWithBadIndex));
    }

    @Test
    void test_parseUpdateStatus_invalidStatus_throwsParseException() {
        // Arrange
        String argsWithBadStatus = "1 s/INVALID";

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseUpdateStatus(argsWithBadStatus));
    }

    @Test
    void test_parseUpdateStatus_blankStatus_throwsParseException() {
        // Arrange
        String argsWithBlankStatus = "1 s/";

        // Act & Assert
        assertThrows(ParseException.class, () -> PersonnelParser.parseUpdateStatus(argsWithBlankStatus));
    }
}
