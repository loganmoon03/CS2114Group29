package podify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// -------------------------------------------------------------------------
/**
 * Reads and writes the CSV song list made by an LLM, and holds the valid
 * songs. The columns are id,title,artist,duration,genre,playCount. Broken
 * rows are skipped instead of crashing the program.
 *
 * @author Dochan (Logan) Moon
 * @version 20 Sept 2026
 */
public class PlaylistLLM {

    //~ Fields ................................................................

    /** The longest title or artist name we accept. */
    public static final int MAX_TEXT_LENGTH = 30;

    /** The header line that every saved file starts with. */
    public static final String HEADER = "id,title,artist,duration,genre,"
        + "playCount";

    /** Number of columns every row of the CSV file must have. */
    private static final int COLUMN_COUNT = 6;

    /** The largest number of seconds a duration may show. */
    private static final int SECONDS_PER_MINUTE = 60;

    // An ArrayList keeps the songs in file order and lets us reach them by
    // index when the list is shown to the user.
    private List<Song> importedSongs;

    private String lastError;

    private int damagedRows;

    // Duplicates are counted apart from damaged rows because the scope
    // document asks for a duplicate warning, not an error.
    private int duplicateRows;


    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new PlaylistLLM with no songs imported yet.
     */
    public PlaylistLLM() {
        importedSongs = new ArrayList<Song>();
        lastError = "";
        damagedRows = 0;
        duplicateRows = 0;
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Reads the LLM file and creates a Song for every valid row.
     *
     * @param fileName the path of the CSV file
     * @return true if at least one song was loaded
     */
    public boolean loadSongs(String fileName) {
        lastError = "";
        damagedRows = 0;
        duplicateRows = 0;

        if (fileName == null || fileName.trim().isEmpty()) {
            lastError = "No file name was given.";
            return false;
        }

        // Songs go into a temporary list, so a bad file cannot damage the
        // songs that were already loaded.
        List<Song> loaded = new ArrayList<Song>();
        List<String> usedIds = new ArrayList<String>();

        try (Scanner reader = new Scanner(new File(fileName))) {
            boolean firstLine = true;

            while (reader.hasNextLine()) {
                String line = reader.nextLine();

                // Lower case so "playCount" and "playcount" both work.
                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("id,")) {
                        continue;
                    }
                }

                // Text files often end with a blank line, so it is not an
                // error.
                if (line.trim().isEmpty()) {
                    continue;
                }

                readRow(line, usedIds, loaded);
            }
        }
        catch (FileNotFoundException e) {
            lastError = "Could not open the file \"" + fileName
                + "\". Check that the name is spelled correctly and that "
                + "the file is in the right folder, then try again.";
            return false;
        }

        if (loaded.isEmpty()) {
            lastError = "The file \"" + fileName
                + "\" did not contain any usable songs.";
            return false;
        }

        importedSongs = loaded;
        buildSkippedMessage(loaded.size());
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Writes songs in the same format loadSongs reads, so the playlist and
     * its play counts survive closing the program.
     *
     * @param fileName the path to write to
     * @param songs    the songs to write
     * @return true when the file was written
     */
    public boolean saveSongs(String fileName, List<Song> songs) {
        lastError = "";

        if (fileName == null || fileName.trim().isEmpty()) {
            lastError = "No file name was given.";
            return false;
        }

        if (songs == null) {
            lastError = "There was no song list to save.";
            return false;
        }

        // The whole list is checked before anything is written, so a bad
        // song leaves the file on disk untouched.
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            if (song == null) {
                lastError = "The song list contains an empty slot, so "
                    + "nothing was saved.";
                return false;
            }
            if (hasComma(song)) {
                lastError = "\"" + song.getName() + "\" contains a comma, "
                    + "which cannot be saved in a CSV file. Rename the song "
                    + "and try again.";
                return false;
            }
        }

        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.println(HEADER);

            for (int i = 0; i < songs.size(); i++) {
                writer.println(buildRow(songs.get(i), i));
            }
        }
        catch (FileNotFoundException e) {
            lastError = "Could not write to the file \"" + fileName
                + "\". Check that the folder exists and that the file is "
                + "not read-only, then try again.";
            return false;
        }

        return true;
    }


    // ----------------------------------------------------------
    /**
     * Saves the user's own playlist, whose play counts change as the
     * program runs.
     *
     * @param fileName the path to write to
     * @param playlist the user's playlist
     * @return true when the file was written
     */
    public boolean savePlaylist(String fileName, UserPlaylist playlist) {
        lastError = "";

        if (playlist == null) {
            lastError = "There was no playlist to save.";
            return false;
        }

        return saveSongs(fileName, playlist.getSongs());
    }


    // ----------------------------------------------------------
    /**
     * Finds every imported song whose title or artist contains the query,
     * ignoring case.
     *
     * @param query the text to look for
     * @return the matching songs, or an empty list
     */
    public List<Song> searchSongs(String query) {
        lastError = "";
        List<Song> results = new ArrayList<Song>();

        if (query == null || query.trim().isEmpty()) {
            lastError = "Search cannot be blank. Type a title or an artist.";
            return results;
        }

        if (query.length() > MAX_TEXT_LENGTH) {
            lastError = "Search text is too long. The limit is "
                + MAX_TEXT_LENGTH + " characters.";
            return results;
        }

        String wanted = query.trim().toLowerCase();

        for (int i = 0; i < importedSongs.size(); i++) {
            Song song = importedSongs.get(i);
            String title = song.getName().toLowerCase();
            String artist = song.getArtist().toLowerCase();

            if (title.contains(wanted) || artist.contains(wanted)) {
                results.add(song);
            }
        }

        if (results.isEmpty()) {
            lastError = "Song not found. No title or artist contains \""
                + query.trim() + "\".";
        }
        return results;
    }


    // ----------------------------------------------------------
    /**
     * Finds the imported song with this title, ignoring case.
     *
     * @param title the full title of the song
     * @return the matching Song, or null
     */
    public Song getSong(String title) {
        lastError = "";

        if (title == null || title.trim().isEmpty()) {
            lastError = "Song title cannot be blank.";
            return null;
        }

        String wanted = title.trim();

        for (int i = 0; i < importedSongs.size(); i++) {
            Song song = importedSongs.get(i);
            if (song.getName().equalsIgnoreCase(wanted)) {
                return song;
            }
        }

        lastError = "Song not found: \"" + wanted + "\".";
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Finds the imported song with this id. Ids are unique, titles are not.
     *
     * @param songId the id from the file
     * @return the matching Song, or null
     */
    public Song getSongById(String songId) {
        lastError = "";

        if (songId == null || songId.trim().isEmpty()) {
            lastError = "Song id cannot be blank.";
            return null;
        }

        String wanted = songId.trim();

        for (int i = 0; i < importedSongs.size(); i++) {
            Song song = importedSongs.get(i);
            if (wanted.equalsIgnoreCase(song.getId())) {
                return song;
            }
        }

        lastError = "Song not found for id \"" + wanted + "\".";
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Gets the songs that were imported from the file.
     *
     * @return a copy of the imported song list
     */
    public List<Song> getImportedSongs() {
        // Only the list is copied. The Song objects are shared on purpose,
        // so playing a song counts everywhere.
        return new ArrayList<Song>(importedSongs);
    }


    // ----------------------------------------------------------
    /**
     * Gets how many songs are currently imported.
     *
     * @return the number of imported songs
     */
    public int numberImportedSongs() {
        return importedSongs.size();
    }


    // ----------------------------------------------------------
    /**
     * Gets the message explaining the last problem, or an empty string.
     *
     * @return the error message for the user
     */
    public String getLastError() {
        return lastError;
    }


    // ----------------------------------------------------------
    /**
     * Gets how many rows the last load threw away, damaged plus duplicate.
     *
     * @return the number of skipped rows
     */
    public int getSkippedRows() {
        return damagedRows + duplicateRows;
    }


    // ----------------------------------------------------------
    /**
     * Gets how many rows were broken, not counting duplicates.
     *
     * @return the number of damaged rows
     */
    public int getDamagedRows() {
        return damagedRows;
    }


    // ----------------------------------------------------------
    /**
     * Gets how many rows repeated an id already read.
     *
     * @return the number of duplicate rows
     */
    public int getDuplicateRows() {
        return duplicateRows;
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    /**
     * Turns one CSV line into a Song, or counts it as a bad row.
     *
     * @param line    one line from the file
     * @param usedIds the ids already seen in this file
     * @param loaded  the list the new Song is added to
     */
    private void readRow(String line, List<String> usedIds,
        List<Song> loaded) {

        // The -1 keeps empty fields at the end. Without it a row ending in
        // a comma would look like it has 5 columns instead of 6.
        String[] parts = line.split(",", -1);

        if (parts.length != COLUMN_COUNT) {
            damagedRows++;
            return;
        }

        String id = parts[0].trim();
        String title = parts[1].trim();
        String artist = parts[2].trim();
        String duration = parts[3].trim();
        String genre = parts[4].trim();
        String countText = parts[5].trim();

        if (id.isEmpty() || title.isEmpty() || artist.isEmpty()
            || duration.isEmpty() || genre.isEmpty() || countText.isEmpty()) {
            damagedRows++;
            return;
        }

        if (title.length() > MAX_TEXT_LENGTH
            || artist.length() > MAX_TEXT_LENGTH) {
            damagedRows++;
            return;
        }

        if (!isValidDuration(duration)) {
            damagedRows++;
            return;
        }

        int playCount;
        try {
            playCount = Integer.parseInt(countText);
        }
        catch (NumberFormatException e) {
            damagedRows++;
            return;
        }

        if (playCount < 0) {
            damagedRows++;
            return;
        }

        // Checked last so a row that is both broken and a duplicate is
        // reported as broken. The first copy of an id is the one kept.
        if (containsIgnoreCase(usedIds, id)) {
            duplicateRows++;
            return;
        }

        usedIds.add(id);

        // Song takes playCount BEFORE genre, which is not the column order.
        loaded.add(new Song(id, title, artist, duration, playCount, genre));
    }


    // ----------------------------------------------------------
    /**
     * Builds the message shown after a load that skipped rows.
     *
     * @param loadedCount how many songs were loaded
     */
    private void buildSkippedMessage(int loadedCount) {
        if (getSkippedRows() == 0) {
            return;
        }

        String message = "Loaded " + loadedCount + " songs";

        if (damagedRows > 0) {
            message = message + ", skipped " + damagedRows
                + " damaged row(s)";
        }

        if (duplicateRows > 0) {
            message = message + ", ignored " + duplicateRows
                + " duplicate song(s) already listed in the file";
        }

        lastError = message + ".";
    }


    // ----------------------------------------------------------
    /**
     * Turns one Song back into a CSV line.
     *
     * @param song     the song to write
     * @param position where the song sits in the list
     * @return the line to write to the file
     */
    private String buildRow(Song song, int position) {
        String id = song.getId();

        // A hand-made song has no id, and loadSongs treats an empty id as a
        // damaged row, so one is made up here.
        if (id == null || id.trim().isEmpty()) {
            id = "S" + (position + 1);
        }

        return id + "," + song.getName() + "," + song.getArtist() + ","
            + song.getDuration() + "," + song.getGenre() + ","
            + song.getPlayCount();
    }


    // ----------------------------------------------------------
    /**
     * Checks whether any field holds a comma, which a CSV file cannot
     * store.
     *
     * @param song the song to check
     * @return true if the song cannot be written safely
     */
    private boolean hasComma(Song song) {
        return contains(song.getId()) || contains(song.getName())
            || contains(song.getArtist()) || contains(song.getDuration())
            || contains(song.getGenre());
    }


    // ----------------------------------------------------------
    /**
     * Checks one field for a comma. A null field counts as safe.
     *
     * @param field the text to check
     * @return true if the text holds a comma
     */
    private boolean contains(String field) {
        return field != null && field.indexOf(',') >= 0;
    }


    // ----------------------------------------------------------
    /**
     * Looks for an id already seen, ignoring case.
     *
     * @param ids    the ids seen so far
     * @param wanted the id to look for
     * @return true if the id was already used
     */
    private boolean containsIgnoreCase(List<String> ids, String wanted) {
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i).equalsIgnoreCase(wanted)) {
                return true;
            }
        }
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Checks that a duration looks like M:SS and holds real seconds, so
     * "3:75" is refused.
     *
     * @param text the duration from the file
     * @return true if the format is correct
     */
    private boolean isValidDuration(String text) {
        // indexOf gives the position of the first ':', or -1 if there is
        // none.
        int colon = text.indexOf(':');

        // One digit before the colon and exactly two after it.
        // "3:51" is length 4, colon at 1, and 4 - 3 = 1.
        if (colon < 1 || colon != text.length() - 3) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            if (i != colon && !Character.isDigit(text.charAt(i))) {
                return false;
            }
        }

        // The last two characters are digits, so this cannot fail.
        int seconds = Integer.parseInt(text.substring(colon + 1));
        return seconds < SECONDS_PER_MINUTE;
    }
}
