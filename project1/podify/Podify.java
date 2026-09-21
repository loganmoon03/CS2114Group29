package podify;

import java.io.File;
import java.util.List;
import java.util.Scanner;

// -------------------------------------------------------------------------
/**
 * Runs the Podify console menu and connects PlaylistLLM, UserPlaylist and
 * Ranking into one program.
 *
 * @author Dochan (Logan) Moon
 * @version 20 Sept 2026
 */
public class Podify {

    //~ Fields ................................................................

    /** The song list made by the LLM. */
    public static final String LLM_FILE_NAME = "llm_playlist.csv";

    /** Where the user's playlist and play counts are saved. */
    public static final String SAVE_FILE_NAME = "my_playlist.csv";

    private PlaylistLLM llm;
    private UserPlaylist playlist;
    private Ranking ranking;
    private Scanner input;
    private String llmFile;
    private String saveFile;


    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a Podify that reads the user's typing from the given Scanner.
     *
     * @param input where the user's typing comes from
     */
    public Podify(Scanner input) {
        this.input = input;
        llm = new PlaylistLLM();
        playlist = new UserPlaylist();
        ranking = new Ranking(playlist);

        String folder = findDataFolder();
        llmFile = folder + "/" + LLM_FILE_NAME;
        saveFile = folder + "/" + SAVE_FILE_NAME;
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Starts the program.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        Podify podify = new Podify(new Scanner(System.in));
        podify.run();
    }


    // ----------------------------------------------------------
    /**
     * Loads the songs, then shows the menu until the user quits.
     */
    public void run() {
        System.out.println("===== Welcome to Podify =====");
        printHelp();
        loadLLMSongs();
        restorePlaylist();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine ("Choose an option: ");

            // No more input at all, so save and stop instead of crashing.
            if (choice == null) {
                savePlaylist();
                return;
            }

            running = handleChoice(choice);

            // Wait here so the result is not pushed off screen by the menu.
            if (running) {
                String pause = readLine("Press Enter to go back to the "
                    + "menu...");
                if (pause == null) {
                    savePlaylist();
                    return;
                }
            }
        }
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    /**
     * Runs the menu option the user picked.
     *
     * @param choice what the user typed
     * @return false when the user wants to quit
     */
    private boolean handleChoice(String choice) {
        if (choice.equals("1")) {
            showImportedSongs();
        }
        else if (choice.equals("2")) {
            searchImportedSongs();
        }
        else if (choice.equals("3")) {
            addSong();
        }
        else if (choice.equals("4")) {
            removeSong();
        }
        else if (choice.equals("5")) {
            showPlaylist();
        }
        else if (choice.equals("6")) {
            playSong();
        }
        else if (choice.equals("7")) {
            showRanking();
        }
        else if (choice.equals("8")) {
            reviewBottomSongs();
        }
        else if (choice.equals("9")) {
            savePlaylist();
        }
        else if (choice.equals("10")) {
            loadLLMSongs();
        }
        else if (choice.equalsIgnoreCase("h")) {
            printHelp();
        }
        else if (choice.equals("0")) {
            savePlaylist();
            System.out.println("Goodbye!");
            return false;
        }
        else {
            System.out.println("\"" + choice + "\" is not an option. "
                + "Type a number from the menu, or h for help.");
        }
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Prints the main menu.
     */
    private void printMenu() {
        System.out.println();
        System.out.println("===== Podify Menu =====");
        System.out.println(" 1. Show imported songs");
        System.out.println(" 2. Search imported songs");
        System.out.println(" 3. Add a song to my playlist");
        System.out.println(" 4. Remove a song from my playlist");
        System.out.println(" 5. Show my playlist");
        System.out.println(" 6. Play a song");
        System.out.println(" 7. Show ranking");
        System.out.println(" 8. Review my 3 least played songs");
        System.out.println(" 9. Save my playlist");
        System.out.println("10. Reload the LLM song file");
        System.out.println(" h. Help");
        System.out.println(" 0. Save and quit");
    }


    // ----------------------------------------------------------
    /**
     * Prints a short guide on how to use the menu.
     */
    private void printHelp() {
        System.out.println();
        System.out.println("How to use Podify:");
        System.out.println(" - Type a menu number and press Enter.");
        System.out.println(" - Pick a song by its number in the list or "
            + "its full title.");
        System.out.println(" - Type 0 when asked for a song or a search to "
            + "cancel and go back.");
        System.out.println(" - Titles and searches can be up to "
            + PlaylistLLM.MAX_TEXT_LENGTH + " characters.");
        System.out.println(" - Your playlist and play counts are saved "
            + "when you quit.");
    }


    // ----------------------------------------------------------
    /**
     * Loads the LLM song file and says how it went.
     */
    private void loadLLMSongs() {
        if (llm.loadSongs(llmFile)) {
            System.out.println("Imported " + llm.numberImportedSongs()
                + " songs from " + llmFile + ".");
        }

        // After a good load this holds the skipped row warning, if any.
        if (!llm.getLastError().isEmpty()) {
            System.out.println(llm.getLastError());
        }
    }


    // ----------------------------------------------------------
    /**
     * Puts back the playlist that was saved the last time the program ran.
     */
    private void restorePlaylist() {
        // The first run has no saved file yet, which is not an error.
        if (!new File(saveFile).exists()) {
            return;
        }

        // A separate PlaylistLLM is used so the imported songs are not
        // replaced by the saved ones.
        PlaylistLLM saved = new PlaylistLLM();
        if (!saved.loadSongs(saveFile)) {
            // Nothing skipped means the file only had a header, so the
            // user simply saved an empty playlist last time.
            if (saved.getSkippedRows() > 0) {
                System.out.println("Could not restore your playlist: "
                    + saved.getLastError());
            }
            return;
        }

        List<Song> songs = saved.getImportedSongs();
        for (int i = 0; i < songs.size(); i++) {
            playlist.addSong(songs.get(i));
        }

        System.out.println("Restored " + playlist.numberSongs()
            + " songs to your playlist.");
        if (!saved.getLastError().isEmpty()) {
            System.out.println(saved.getLastError());
        }
    }


    // ----------------------------------------------------------
    /**
     * Shows every imported song with its number.
     */
    private void showImportedSongs() {
        List<Song> songs = llm.getImportedSongs();

        if (songs.isEmpty()) {
            System.out.println("No songs are imported. Choose 10 to try "
                + "loading the LLM song file again.");
            return;
        }

        System.out.println("Imported songs (" + songs.size() + " songs):");
        for (int i = 0; i < songs.size(); i++) {
            System.out.println("  " + (i + 1) + ". "
                + describeImported(songs.get(i)));
        }
    }


    // ----------------------------------------------------------
    /**
     * Searches the imported songs by title or artist.
     */
    private void searchImportedSongs() {
        List<Song> results = null;

        while (results == null || results.isEmpty()) {
            String query = readLine("Search for a title or artist "
                + "(0 to cancel): ");
            if (query == null) {
                return;
            }

            if (query.equals("0")) {
                System.out.println("Cancelled.");
                return;
            }

            results = llm.searchSongs(query);

            // searchSongs explains blank, too long and not found by itself.
            if (results.isEmpty()) {
                System.out.println(llm.getLastError() + " Please try again.");
            }
        }

        System.out.println("Found " + results.size() + " song(s):");
        for (int i = 0; i < results.size(); i++) {
            System.out.println("  " + describeImported(results.get(i)));
        }
    }


    // ----------------------------------------------------------
    /**
     * Adds an imported song to the user's playlist.
     */
    private void addSong() {
        // Without this check pickFromList could never find a song.
        if (llm.numberImportedSongs() == 0) {
            System.out.println("No songs are imported. Choose 10 to try "
                + "loading the LLM song file again.");
            return;
        }

        showImportedSongs();
        Song song = pickFromList(llm.getImportedSongs(), "add",
            "the imported songs");
        if (song == null) {
            return;
        }

        // addSong prints its own message when the song is a duplicate.
        if (playlist.addSong(song)) {
            System.out.println("Added \"" + song.getName()
                + "\" to your playlist.");
        }
        else {
            System.out.println("Your playlist was not changed.");
        }
    }


    // ----------------------------------------------------------
    /**
     * Removes a song from the user's playlist.
     */
    private void removeSong() {
        Song song = pickFromPlaylist("remove");
        if (song == null) {
            return;
        }

        playlist.removeSong(song);
        System.out.println("Removed \"" + song.getName()
            + "\" from your playlist.");
    }


    // ----------------------------------------------------------
    /**
     * Shows every song in the user's playlist with its play count.
     */
    private void showPlaylist() {
        if (isPlaylistEmpty()) {
            return;
        }

        List<Song> songs = playlist.getSongs();
        System.out.println("My playlist (" + songs.size() + " songs):");
        for (int i = 0; i < songs.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + songs.get(i));
        }
    }


    // ----------------------------------------------------------
    /**
     * Plays a song from the user's playlist, adding one to its play count.
     */
    private void playSong() {
        Song song = pickFromPlaylist("play");
        if (song == null) {
            return;
        }

        int plays = song.play();
        System.out.println("Now playing \"" + song.getName() + "\" - "
            + song.getArtist() + ". Played " + plays + " time(s).");
    }


    // ----------------------------------------------------------
    /**
     * Shows the playlist and lets the user pick a song from it.
     *
     * @param action what will be done to the song, like "play"
     * @return the picked song, or null if nothing was picked
     */
    private Song pickFromPlaylist(String action) {
        if (isPlaylistEmpty()) {
            return null;
        }

        showPlaylist();
        return pickFromList(playlist.getSongs(), action, "your playlist");
    }


    // ----------------------------------------------------------
    /**
     * Keeps asking until the user picks a song by number or title.
     *
     * @param songs  the list that was just shown to the user
     * @param action what will be done to the song, like "play"
     * @param place  where the songs come from, used in the error message
     * @return the picked song, or null if the user typed 0 to cancel
     */
    private Song pickFromList(List<Song> songs, String action, String place) {
        while (true) {
            String text = readSongName("Type the number or title of the "
                + "song to " + action + " ");
            if (text == null) {
                return null;
            }

            int position = readPosition(text);

            // A number picks from the list shown above, counting from 1.
            if (position >= 1 && position <= songs.size()) {
                return songs.get(position - 1);
            }

            Song song = findInList(songs, text);
            if (song != null) {
                return song;
            }

            if (position > 0) {
                System.out.println("There is no song number " + position
                    + ". Pick a number from 1 to " + songs.size() + ".");
            }
            else {
                System.out.println("Song not found in " + place
                    + ". Please try again.");
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Shows the top and bottom three songs, or one full list when the
     * playlist has fewer than six songs.
     */
    private void showRanking() {
        if (isPlaylistEmpty()) {
            return;
        }

        // With fewer than six songs the top and bottom three would
        // overlap, so the scope document asks for one list instead.
        if (playlist.lessThanSix()) {
            System.out.println("Ranking (fewer than 6 songs, so showing "
                + "all of them):");
            printRankedList(ranking.showFull());
            return;
        }

        System.out.println("Top 3 most played:");
        printRankedList(ranking.showTop());

        System.out.println("Bottom 3 least played:");
        printRankedList(ranking.showBottom());
    }


    // ----------------------------------------------------------
    /**
     * Asks the user whether to keep or delete each of the three least
     * played songs.
     */
    private void reviewBottomSongs() {
        if (isPlaylistEmpty()) {
            return;
        }

        List<Song> bottom = ranking.showBottom();
        System.out.println("These are your least played songs. You might "
            + "not enjoy them as much.");

        for (int i = 0; i < bottom.size(); i++) {
            Song song = bottom.get(i);
            System.out.println();
            System.out.println("  " + song);

            String answer = readKeepOrDelete();
            if (answer == null) {
                return;
            }

            if (answer.equals("d")) {
                playlist.removeSong(song);
                System.out.println("Deleted \"" + song.getName() + "\".");
            }
            else if(answer.equals("k")){
                System.out.println("Kept \"" + song.getName() + "\".");
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Saves the user's playlist and play counts to a file.
     */
    private void savePlaylist() {
        if (llm.savePlaylist(saveFile, playlist)) {
            System.out.println("Saved " + playlist.numberSongs()
                + " songs to " + saveFile + ".");
        }
        else {
            System.out.println("Could not save: " + llm.getLastError());
        }
    }


    // ----------------------------------------------------------
    /**
     * Prints a ranked list with a place number in front of each song.
     *
     * @param songs the songs in ranked order
     */
    private void printRankedList(List<Song> songs) {
        for (int i = 0; i < songs.size(); i++) {
            System.out.println("  #" + (i + 1) + " " + songs.get(i));
        }
    }


    // ----------------------------------------------------------
    /**
     * Describes an imported song without a play count, since only the
     * playlist's play counts matter.
     *
     * @param song the song to describe
     * @return the song as one line of text
     */
    private String describeImported(Song song) {
        return song.getName() + " - "
            + song.getArtist() + " (" + song.getDuration() + ", "
            + song.getGenre() + ")";
    }


    // ----------------------------------------------------------
    /**
     * Finds a song in the list by title, ignoring case.
     *
     * @param songs the list to look in
     * @param text  the title the user typed
     * @return the song, or null if there is no match
     */
    private Song findInList(List<Song> songs, String text) {
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            if (text.equalsIgnoreCase(song.getName())) {
                return song;
            }
        }
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Turns the text into a list number.
     *
     * @param text what the user typed
     * @return the number, or 0 if the text is not a whole number
     */
    private int readPosition(String text) {
        try {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }


    // ----------------------------------------------------------
    /**
     * Prints a message and returns true when the playlist has no songs.
     *
     * @return true if the playlist is empty
     */
    private boolean isPlaylistEmpty() {
        if (playlist.numberSongs() == 0) {
            System.out.println("Your playlist is empty. Choose 3 to add a "
                + "song.");
            return true;
        }
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Keeps asking for a song until the text is not blank or too long.
     *
     * @param prompt the question to show
     * @return the text, or null if the user typed 0 to cancel
     */
    private String readSongName(String prompt) {
        while (true) {
            String text = readLine(prompt + "(0 to cancel): ");
            if (text == null) {
                return null;
            }

            if (text.equals("0")) {
                System.out.println("Cancelled.");
                return null;
            }

            if (text.isEmpty()) {
                System.out.println("The song cannot be blank. Please try "
                    + "again.");
            }
            else if (text.length() > PlaylistLLM.MAX_TEXT_LENGTH) {
                System.out.println("That is too long. The limit is "
                    + PlaylistLLM.MAX_TEXT_LENGTH + " characters. Please "
                    + "try again.");
            }
            else {
                return text;
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Keeps asking until the user types k or d.
     *
     * @return "k" or "d", or null if there is no more input
     */
    private String readKeepOrDelete() {
        while (true) {
            String answer = readLine("  Keep or delete? (k/d): ");
            if (answer == null) {
                return null;
            }

            answer = answer.trim().toLowerCase();
            if (answer.equals("k") || answer.equals("d")) {
                return answer;
            }

            System.out.println("  Please type k to keep or d to delete.");
        }
    }


    // ----------------------------------------------------------
    /**
     * Shows a prompt and reads one line.
     *
     * @param prompt the question to show
     * @return the line, or null if there is no more input
     */
    private String readLine(String prompt) {
        System.out.print(prompt);

        // Without this check nextLine throws when input runs out.
        if (!input.hasNextLine()) {
            System.out.println();
            return null;
        }
        return cleanInput(input.nextLine());
    }


    // ----------------------------------------------------------
    /**
     * Removes hidden characters that a Korean or other input method can
     * add, so "1" typed in any keyboard mode still matches "1".
     *
     * @param text the line the user typed
     * @return the cleaned and trimmed text
     */
    private String cleanInput(String text) {
        String cleaned = "";

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isDigit(c)) {
                // Turns a full-width digit like '１' into a normal '1'.
                cleaned = cleaned + Character.getNumericValue(c);
            }
            else if (Character.isSpaceChar(c) || Character.isWhitespace(c)) {
                cleaned = cleaned + " ";
            }
            else if (Character.getType(c) != Character.FORMAT) {
                // FORMAT characters are invisible, like a zero-width space.
                cleaned = cleaned + c;
            }
        }
        return cleaned.trim();
    }


    // ----------------------------------------------------------
    /**
     * Finds the data folder. Eclipse runs from the project root, but a
     * terminal usually runs from inside project1.
     *
     * @return the path of the data folder
     */
    private String findDataFolder() {
        String[] choices = {"project1/data", "data"};

        for (int i = 0; i < choices.length; i++) {
            if (new File(choices[i]).isDirectory()) {
                return choices[i];
            }
        }
        return "data";
    }
}
