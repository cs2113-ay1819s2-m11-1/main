package seedu.addressbook.parser;

import seedu.addressbook.commands.*;
import seedu.addressbook.commands.finance.FinanceCommand;
import seedu.addressbook.commands.finance.ListFinanceCommand;
import seedu.addressbook.commands.match.*;
import seedu.addressbook.commands.player.*;
import seedu.addressbook.commands.team.*;
import seedu.addressbook.data.exception.IllegalValueException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static seedu.addressbook.common.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

/**
 * Parses user input.
 */
public class Parser {

    public static final Pattern INDEX_ARGS_FORMAT = Pattern.compile("(?<targetIndex>.+)");

    public static final Pattern KEYWORDS_ARGS_FORMAT =
            Pattern.compile("(?<keywords>\\S+(?:\\s+\\S+)*)"); // one or more keywords separated by whitespace

    public static final Pattern PLAYER_DATA_ARGS_FORMAT = // '/' forward slashes are reserved for delimiter prefixes
            Pattern.compile("(?<name>[^/]+)"
                    + "p/(?<position>[^/]+)"
                    + "a/(?<age>[^/]+)"
                    + "sal/(?<salary>[^/]+)"
                    + "gs/(?<goalsScored>[^/]+)"
                    + "ga/(?<goalsAssisted>[^/]+)"
                    + "tm/(?<team>[^/]+)"
                    + "ctry/(?<country>[^/]+)"
                    + "jn/(?<jerseyNumber>[^/]+)"
                    + "app/(?<appearance>[^/]+)"
                    + "hs/(?<healthStatus>[^/]+)"
                    + "(?<tagArguments>(?: t/[^/]+)*)"); // variable number of tags

    public static final Pattern PLAYERFAST_DATA_ARGS_FORMAT = // '/' forward slashes are reserved for delimiter prefixes
            Pattern.compile("(?<name>[^/]+)"
                    + "p/(?<position>[^/]+)"
                    + "a/(?<age>[^/]+)"
                    + "sal/(?<salary>[^/]+)"
                    + "tm/(?<team>[^/]+)"
                    + "ctry/(?<country>[^/]+)"
                    + "jn/(?<jerseyNumber>[^/]+)"
                    + "(?<tagArguments>(?: t/[^/]+)*)");

    public static final Pattern MATCH_DATA_ARGS_FORMAT = // '/' forward slashes are reserved for delimiter prefixes
            Pattern.compile("(?<date>[^/]+)"
                    + "h/(?<home>[^/]+)"
                    + "a/(?<away>[^/]+)"
                    + "(?<tagArguments>(?: t/[^/]+)*)"); // variable number of tags

    public static final Pattern TEAM_DATA_ARGS_FORMAT =
            Pattern.compile("(?<name>[^/]+)"
                    + "c/(?<country>[^/]+)"
                    + "s/(?<sponsor>[^/]+)"
                    + "(?<tagArguments>(?: t/[^/]+)*)"); // variable number of tags;


    public static final Pattern TEAM_EDIT_DATA_ARGS_FORMAT =
            Pattern.compile("(?<targetIndex>\\d+)"
                    + "(( n/(?<name>[^/]+))?)"
                    + "(( c/(?<country>[^/]+))?)"
                    + "(( s/(?<sponsor>[^/]+))?)"
                    + "(?<tagArguments>(?: t/[^/]+)*)"); // variable number of tags;

    public static final Pattern TEAM_EDIT_DATA_NOARGS_FORMAT =
            Pattern.compile("(?<targetIndex>\\d+)");

    /**
     * Signals that the user input could not be parsed.
     */
    public static class ParseException extends Exception {
        ParseException(String message) {
            super(message);
        }
    }

    /**
     * Used for initial separation of command word and args.
     */
    public static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     */
    public Command parseCommand(String userInput) {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");
        switch (commandWord) {
            case AddCommand.COMMAND_WORD:
                return prepareAddPlayer(arguments);

            case AddFastCommand.COMMAND_WORD:
                return prepareAddFastPlayer(arguments);

            case AddTeam.COMMAND_WORD:
                return addTeam(arguments);

            case DeleteCommand.COMMAND_WORD:
                return prepareDeletePlayer(arguments);

            case DeleteTeam.COMMAND_WORD:
                return delTeam(arguments);

            case ClearCommand.COMMAND_WORD:
                return new ClearCommand();

            case ClearTeam.COMMAND_WORD:
                return new ClearTeam();

            case FindCommand.COMMAND_WORD:
                return prepareFindPerson(arguments);

            case FindTeam.COMMAND_WORD:
                return prepareFindTeam(arguments);

            case FinanceCommand.COMMAND_WORD:
                return prepareFinance(arguments);

            case ListCommand.COMMAND_WORD:
                return new ListCommand();

            case AddMatchCommand.COMMAND_WORD:
                return prepareAddMatch(arguments);

            case DeleteMatchCommand.COMMAND_WORD:
                return prepareDeleteMatch(arguments);

            case ClearMatchCommand.COMMAND_WORD:
                return new ClearMatchCommand();

            case FindMatchCommand.COMMAND_WORD:
                return prepareFindMatch(arguments);

            case ListMatchCommand.COMMAND_WORD:
                return new ListMatchCommand();

            case ListTeam.COMMAND_WORD:
                return new ListTeam();

            case ListFinanceCommand.COMMAND_WORD:
                return new ListFinanceCommand();

            case EditTeam.COMMAND_WORD:
                return prepareEditTeam(arguments);

            case SortCommand.COMMAND_WORD:
                return new SortCommand();

            case ViewAllCommand.COMMAND_WORD:
                return prepareViewAll(arguments);

            case ExitCommand.COMMAND_WORD:
                return new ExitCommand();

            case HelpCommand.COMMAND_WORD: // Fallthrough
            default:
                return new HelpCommand();
        }
    }

    /**
     * Parses arguments in the context of the add team command.
     */
    private Command addTeam(String args) {
        final Matcher matcher = TEAM_DATA_ARGS_FORMAT.matcher(args.trim());
        // Validate arg string format
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddTeam.MESSAGE_USAGE));
        }
        try {
            return new AddTeam(
                    matcher.group("name"),
                    matcher.group("country"),
                    matcher.group("sponsor"),
                    getTagsFromArgs(matcher.group("tagArguments"))
            );
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }
    }

    /**
     * Parses arguments in the context of the add player command.
     *
     * @param args full command args string
     * @return the prepared command
     */
    private Command prepareAddPlayer(String args) {
        final Matcher matcher = PLAYER_DATA_ARGS_FORMAT.matcher(args.trim());
        // Validate arg string format
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
        try {
            return new AddCommand(
                    matcher.group("name"),
                    matcher.group("position"),
                    matcher.group("age"),
                    matcher.group("salary"),
                    matcher.group("goalsScored"),
                    matcher.group("goalsAssisted"),
                    matcher.group("team"),
                    matcher.group("country"),
                    matcher.group("jerseyNumber"),
                    matcher.group("appearance"),
                    matcher.group("healthStatus"),
                    getTagsFromArgs(matcher.group("tagArguments"))
            );

        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }
    }


    /**
     * Parses arguments in the context of the addFast player command.
     *
     * @param args full command args string
     * @return the prepared command
     */
    private Command prepareAddFastPlayer(String args) {
        final Matcher matcher = PLAYERFAST_DATA_ARGS_FORMAT.matcher(args.trim());
        // Validate arg string format
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddFastCommand.MESSAGE_USAGE));
        }
        try {
            return new AddFastCommand(
                    matcher.group("name"),
                    matcher.group("position"),
                    matcher.group("age"),
                    matcher.group("salary"),
                    matcher.group("team"),
                    matcher.group("country"),
                    matcher.group("jerseyNumber"),
                    getTagsFromArgs(matcher.group("tagArguments"))
            );
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }
    }


    /**
     * Parses arguments in the context of the add match command.
     *
     * @param args full command args string
     * @return the prepared command
     */
    private Command prepareAddMatch(String args) {
        final Matcher matcher = MATCH_DATA_ARGS_FORMAT.matcher(args.trim());
        // Validate arg string format
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddMatchCommand.MESSAGE_USAGE));
        }
        try {
            return new AddMatchCommand(
                    matcher.group("date"),
                    matcher.group("home"),
                    matcher.group("away")
            );
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }
    }

//    /**
//     * Checks whether the private prefix of a contact detail in the add command's arguments string is present.
//     */
//    private static boolean isPrivatePrefixPresent(String matchedPrefix) {
//        return matchedPrefix.equals("p");
//    }

    /**
     * Extracts the new player's tags from the add command's tag arguments string.
     * Extracts the new team's tags from the addTeam command's tag arguments string.
     * Merges duplicate tag strings.
     */

    private static Set<String> getTagsFromArgs(String tagArguments) throws IllegalValueException {
        // no tags
        if (tagArguments.isEmpty()) {
            return Collections.emptySet();
        }
        // replace first delimiter prefix, then split
        final Collection<String> tagStrings = Arrays.asList(tagArguments.replaceFirst(" t/", "").split(" t/"));
        return new HashSet<>(tagStrings);
    }


    /**
     * Parses arguments in the context of the delete player command.
     *
     * @param args full command args string
     * @return the prepared command
     */
    private Command prepareDeletePlayer(String args) {
        try {
            final int targetIndex = parseArgsAsDisplayedIndex(args);
            return new DeleteCommand(targetIndex);
        } catch (ParseException | NumberFormatException e) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }
    }


    /**
     * Parses arguments in the context of the delete match command.
     *
     * @param args full command args string
     * @return the prepared command
     */
    private Command prepareDeleteMatch(String args) {
        try {
            final int targetIndex = parseArgsAsDisplayedIndex(args);
            return new DeleteMatchCommand(targetIndex);
        } catch (ParseException | NumberFormatException e) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    DeleteMatchCommand.MESSAGE_USAGE));
        }
    }

    /**
     * Parses arguments in the context of the delete team command.
     */
    private Command delTeam(String args) {
        try {
            final int targetIndex = parseArgsAsDisplayedIndex(args);
            return new DeleteTeam(targetIndex);
        } catch (ParseException | NumberFormatException e) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteTeam.MESSAGE_USAGE));
        }
    }


    /**
     * Parses arguments in the context of the finance command.
     *
     * @param args full command args string
     * @return the prepared command
     */
    private Command prepareFinance(String args) {
        try {
            final int targetIndex = parseArgsAsDisplayedIndex(args);
            return new FinanceCommand(targetIndex);
        } catch (ParseException | NumberFormatException e) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FinanceCommand.MESSAGE_USAGE));
        }
    }


    /**
     * Parses arguments in the context of the Edit command.
     */
    private Command prepareEditTeam(String args) {

        final Matcher checkForArgs = TEAM_EDIT_DATA_NOARGS_FORMAT.matcher(args.trim());
        if (checkForArgs.matches()) {
            return new IncorrectCommand(String.format(
                    EditTeam.MESSAGE_NOARGS,
                    EditTeam.MESSAGE_USAGE));
        }
        final Matcher matcher = TEAM_EDIT_DATA_ARGS_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT,
                    EditTeam.MESSAGE_USAGE));
        }
        try {
            final int targetIndex = parseArgsAsDisplayedIndex(matcher.group("targetIndex"));
            return new EditTeam(
                    targetIndex,
                    matcher.group("name"),
                    matcher.group("country"),
                    matcher.group("sponsor"),
                    getTagsFromArgs(matcher.group("tagArguments"))
            );
        } catch (ParseException | NumberFormatException e) {
            return new IncorrectCommand(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT,
                    EditTeam.MESSAGE_USAGE));
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }
    }


    /**
     * Parses arguments in the context of the view all command.
     *
     * @param args full command args string
     * @return the prepared command
     */
    private Command prepareViewAll(String args) {

        try {
            final int targetIndex = parseArgsAsDisplayedIndex(args);
            return new ViewAllCommand(targetIndex);
        } catch (ParseException | NumberFormatException e) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    ViewAllCommand.MESSAGE_USAGE));
        }
    }

    /**
     * Parses the given arguments string as a single index number.
     *
     * @param args arguments string to parse as index number
     * @return the parsed index number
     * @throws ParseException        if no region of the args string could be found for the index
     * @throws NumberFormatException the args string region is not a valid number
     */
    private int parseArgsAsDisplayedIndex(String args) throws ParseException, NumberFormatException {
        final Matcher matcher = INDEX_ARGS_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            throw new ParseException("Could not find index number to parse");
        }
        return Integer.parseInt(matcher.group("targetIndex"));
    }


    /**
     * Parses arguments in the context of the find player command.
     *
     * @param args full command args string
     * @return the prepared command
     */
    private Command prepareFindPerson(String args) {
        final Matcher matcher = KEYWORDS_ARGS_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    FindCommand.MESSAGE_USAGE));
        }

        // keywords delimited by whitespace
        final String[] keywords = matcher.group("keywords").split("\\s+");
        final Set<String> keywordSet = new HashSet<>(Arrays.asList(keywords));
        return new FindCommand(keywordSet);
    }

    /**
     * Parses arguments in the context of the find match command.
     *
     * @param args full command args string
     * @return the prepared command
     */
    private Command prepareFindMatch(String args) {
        final Matcher matcher = KEYWORDS_ARGS_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    FindMatchCommand.MESSAGE_USAGE));
        }

        // keywords delimited by whitespace
        final String[] keywords = matcher.group("keywords").split("\\s+");
        final Set<String> keywordSet = new HashSet<>(Arrays.asList(keywords));
        return new FindMatchCommand(keywordSet);
    }

    /**
     * Parses arguments in the context of the find team command.
     */
    private Command prepareFindTeam(String args) {
        final Matcher matcher = KEYWORDS_ARGS_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    FindTeam.MESSAGE_USAGE));
        }

        // keywords delimited by whitespace
        final String[] keywords = matcher.group("keywords").split("\\s+");
        final Set<String> keywordSet = new HashSet<>(Arrays.asList(keywords));
        return new FindTeam(keywordSet);
    }
}
