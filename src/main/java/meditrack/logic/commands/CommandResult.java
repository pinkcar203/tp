package meditrack.logic.commands;

import java.util.Objects;

/**
 * Represents the result of a command execution.
 * Contains the feedback message to be displayed to the user.
 */
public class CommandResult {

    private final String feedbackToUser;

    /**
     * Constructs a CommandResult with the specified feedback message.
     *
     * @param feedbackToUser The message to display to the user. Must not be null.
     */
    public CommandResult(String feedbackToUser) {
        this.feedbackToUser = Objects.requireNonNull(feedbackToUser);
    }

    /**
     * Shows the feedback message.
     *
     * @return The feedback string.
     */
    public String getFeedbackToUser() {
        return feedbackToUser;
    }
}