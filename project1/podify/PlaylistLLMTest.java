package podify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import student.TestCase;

// -------------------------------------------------------------------------
/**
 * Tests the PlaylistLLM class.
 *
 * @author Dochan (Logan) Moon
 * @version 20 Sept 2026
 */
public class PlaylistLLMTest extends TestCase {

    //~ Fields ................................................................
    private static final String SAMPLE_FILE = "test_sample.csv";
    private static final String SAVED_FILE = "test_saved.csv";

    private PlaylistLLM llm;
    private Song song1;
    private Song song2;
    private Song song3;
    private UserPlaylist playlist;
    private List<String> createdFiles;


    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Sets up each test method.
     */
    public void setUp() {
        llm = new PlaylistLLM();
        createdFiles = new ArrayList<String>();

        song1 = new Song("Hotline Bling", "Drake", "4:27", 2, "Pop");
        song2 = new Song("Cruel Summer", "Taylor Swift", "2:58", 6, "Pop");
        song3 = new Song("HUMBLE.", "Kendrick Lamar", "2:57", 9, "Hip-Hop");

        playlist = new UserPlaylist();
        playlist.addSong(song1);
        playlist.addSong(song2);
        playlist.addSong(song3);
    }


    // ----------------------------------------------------------
    /**
     * Deletes the files each test made.
     */
    public void tearDown() {
        for (int i = 0; i < createdFiles.size(); i++) {
            File file = new File(createdFiles.get(i));
            if (file.exists()) {
                file.delete();
            }
        }
    }


    //~Public  Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Tests loadSongs() with a good file.
     */
    public void testLoadSongsValidFile() {
        writeSampleFile();

        assertTrue(llm.loadSongs(SAMPLE_FILE));
        assertEquals(3, llm.numberImportedSongs());
        assertEquals(0, llm.getSkippedRows());
        assertEquals("", llm.getLastError());

        Song first = llm.getImportedSongs().get(0);
        assertEquals("S001", first.getId());
        assertEquals("Blinding Lights", first.getName());
        assertEquals("The Weeknd", first.getArtist());
        assertEquals("3:20", first.getDuration());
        assertEquals("Pop", first.getGenre());
        assertEquals(7, first.getPlayCount());
    }


    // ----------------------------------------------------------
    /**
     * Tests loadSongs() when the file has no header line.
     */
    public void testLoadSongsWithoutHeader() {
        String[] lines = {"S001,Levels,Avicii,3:19,EDM,4"};
        writeFile("test_noheader.csv", lines);

        assertTrue(llm.loadSongs("test_noheader.csv"));
        assertEquals(1, llm.numberImportedSongs());
    }


    // ----------------------------------------------------------
    /**
     * Tests loadSongs() with blank lines in the file.
     */
    public void testLoadSongsIgnoresBlankLines() {
        String[] lines = {
            "id,title,artist,duration,genre,playCount",
            "S001,Yellow,Coldplay,4:26,Indie,1",
            "",
            "   "};
        writeFile("test_blanks.csv", lines);

        assertTrue(llm.loadSongs("test_blanks.csv"));
        assertEquals(1, llm.numberImportedSongs());
        // * blank lines are not damaged rows
        assertEquals(0, llm.getSkippedRows());
    }


    // ----------------------------------------------------------
    /**
     * Tests loadSongs() when the file is not there.
     */
    public void testLoadSongsFileNotFound() {
        assertFalse(llm.loadSongs("no_such_file_anywhere.csv"));
        assertEquals(0, llm.numberImportedSongs());
        assertTrue(llm.getLastError().contains("Could not open"));
    }


    // ----------------------------------------------------------
    /**
     * Tests loadSongs() with a missing or blank file name.
     */
    public void testLoadSongsBlankFileName() {
        assertFalse(llm.loadSongs(null));
        assertEquals("No file name was given.", llm.getLastError());

        assertFalse(llm.loadSongs("   "));
        assertEquals("No file name was given.", llm.getLastError());
    }


    // ----------------------------------------------------------
    /**
     * Tests loadSongs() when every row is broken.
     */
    public void testLoadSongsNoUsableRows() {
        String[] lines = {
            "id,title,artist,duration,genre,playCount",
            "S001,Missing Columns,Artist",
            ",Empty Id,Artist,3:20,Pop,1"};
        writeFile("test_allbad.csv", lines);

        assertFalse(llm.loadSongs("test_allbad.csv"));
        assertEquals(0, llm.numberImportedSongs());
        assertTrue(llm.getLastError().contains("did not contain any usable"));
    }


    // ----------------------------------------------------------
    /**
     * Tests that a failed loadSongs() keeps the songs already loaded.
     */
    public void testFailedLoadKeepsOldSongs() {
        writeSampleFile();

        assertTrue(llm.loadSongs(SAMPLE_FILE));
        assertEquals(3, llm.numberImportedSongs());

        assertFalse(llm.loadSongs("gone.csv"));

        assertEquals(3, llm.numberImportedSongs());
        assertEquals("Blinding Lights",
            llm.getImportedSongs().get(0).getName());
    }


    // ----------------------------------------------------------
    /**
     * Tests loadSongs() with every kind of damaged row.
     */
    public void testLoadSongsSkipsDamagedRows() {
        String[] lines = {
            "id,title,artist,duration,genre,playCount",
            "S001,Good Song,Good Artist,3:20,Pop,5",
            // * too few columns
            "S002,Too Few Columns,Artist,3:20,Pop",
            // * empty title
            "S003,,Artist,3:20,Pop,1",
            // * play count is not a number
            "S004,Bad Count,Artist,3:20,Pop,abc",
            // * negative play count
            "S005,Negative Count,Artist,3:20,Pop,-4",
            // * title over the 30 character limit
            "S006,This Title Is Far Too Long To Accept,Artist,3:20,Pop,1",
            "S007,Still Good,Other Artist,4:00,Rock,2"};
        writeFile("test_damaged.csv", lines);

        assertTrue(llm.loadSongs("test_damaged.csv"));
        assertEquals(2, llm.numberImportedSongs());
        assertEquals(5, llm.getDamagedRows());
        assertEquals(0, llm.getDuplicateRows());
        assertTrue(llm.getLastError().contains("damaged"));
    }


    // ----------------------------------------------------------
    /**
     * Tests loadSongs() when the file repeats an id.
     */
    public void testLoadSongsCountsDuplicatesSeparately() {
        String[] lines = {
            "id,title,artist,duration,genre,playCount",
            "S001,First Copy,The Weeknd,3:20,Pop,5",
            "S001,Second Copy,The Weeknd,3:20,Pop,9",
            "s001,Third Copy,The Weeknd,3:20,Pop,9"};
        writeFile("test_dupes.csv", lines);

        assertTrue(llm.loadSongs("test_dupes.csv"));
        assertEquals(1, llm.numberImportedSongs());
        // * duplicates are counted apart from damaged rows
        assertEquals(2, llm.getDuplicateRows());
        assertEquals(0, llm.getDamagedRows());
        assertEquals(2, llm.getSkippedRows());
        // * the first row wins, so the play count of 9 is thrown away
        assertEquals("First Copy", llm.getImportedSongs().get(0).getName());
        assertTrue(llm.getLastError().contains("duplicate"));
    }


    // ----------------------------------------------------------
    /**
     * Tests loadSongs() with bad durations.
     */
    public void testLoadSongsChecksDuration() {
        String[] lines = {
            "id,title,artist,duration,genre,playCount",
            // * more than 59 seconds
            "S001,Seconds Too Big,Artist,3:75,Pop,1",
            // * only one digit after the colon
            "S002,One Digit Seconds,Artist,3:5,Pop,1",
            // * no colon at all
            "S003,No Colon,Artist,351,Pop,1",
            // * nothing before the colon
            "S004,Starts With Colon,Artist,:51,Pop,1",
            // * a letter instead of a digit
            "S005,Letters Inside,Artist,3:5x,Pop,1",
            "S006,Just Right,Artist,3:59,Pop,1",
            "S007,Zero Seconds,Artist,4:00,Pop,1"};
        writeFile("test_durations.csv", lines);

        assertTrue(llm.loadSongs("test_durations.csv"));
        assertEquals(2, llm.numberImportedSongs());
        assertEquals(5, llm.getDamagedRows());
    }


    // ----------------------------------------------------------
    /**
     * Tests searchSongs()
     */
    public void testSearchSongsNormal() {
        loadSampleFile();

        // * a whole title
        assertEquals(1, llm.searchSongs("Blinding Lights").size());
        // * part of an artist name, in lower case
        assertEquals(2, llm.searchSongs("weeknd").size());
        // * part of a title
        assertEquals(1, llm.searchSongs("clock").size());
        assertEquals("", llm.getLastError());
    }


    // ----------------------------------------------------------
    /**
     * Tests searchSongs() with a blank query.
     */
    public void testSearchSongsBlank() {
        loadSampleFile();

        List<Song> results = llm.searchSongs("   ");
        assertNotNull(results);
        assertTrue(results.isEmpty());
        assertTrue(llm.getLastError().contains("cannot be blank"));

        assertTrue(llm.searchSongs(null).isEmpty());
        assertTrue(llm.getLastError().contains("cannot be blank"));
    }


    // ----------------------------------------------------------
    /**
     * Tests searchSongs() with a query over the character limit.
     */
    public void testSearchSongsTooLong() {
        loadSampleFile();

        // * 47 characters, past the limit of 30
        String longQuery = "This search text is much too long to be accepted";

        assertTrue(llm.searchSongs(longQuery).isEmpty());
        assertTrue(llm.getLastError().contains("too long"));
        assertTrue(llm.getLastError().contains("30"));
    }


    // ----------------------------------------------------------
    /**
     * Tests searchSongs() when nothing matches.
     */
    public void testSearchSongsNoMatch() {
        loadSampleFile();

        assertTrue(llm.searchSongs("Nirvana").isEmpty());
        assertTrue(llm.getLastError().contains("Song not found"));
    }


    // ----------------------------------------------------------
    /**
     * Tests getSong()
     */
    public void testGetSongNormal() {
        loadSampleFile();

        // * upper and lower case are ignored
        Song song = llm.getSong("blinding lights");
        assertNotNull(song);
        assertEquals("The Weeknd", song.getArtist());
        assertEquals("", llm.getLastError());
    }


    // ----------------------------------------------------------
    /**
     * Tests getSong() with a blank or unknown title.
     */
    public void testGetSongBadInput() {
        loadSampleFile();

        assertNull(llm.getSong(null));
        assertTrue(llm.getLastError().contains("cannot be blank"));

        assertNull(llm.getSong("  "));
        assertTrue(llm.getLastError().contains("cannot be blank"));

        assertNull(llm.getSong("Not A Real Song"));
        assertTrue(llm.getLastError().contains("Song not found"));
    }


    // ----------------------------------------------------------
    /**
     * Tests getSongById()
     */
    public void testGetSongById() {
        String[] lines = {
            "id,title,artist,duration,genre,playCount",
            "S001,One Dance,Drake,2:54,Pop,5",
            "S002,One Dance,Cover Artist,2:54,Pop,1"};
        writeFile("test_sametitle.csv", lines);

        assertTrue(llm.loadSongs("test_sametitle.csv"));
        assertEquals(2, llm.numberImportedSongs());

        assertEquals("Drake", llm.getSongById("S001").getArtist());
        assertEquals("Cover Artist", llm.getSongById("S002").getArtist());
        // * getSong only finds the first one, which is why ids exist
        assertEquals("Drake", llm.getSong("One Dance").getArtist());

        assertNull(llm.getSongById("S999"));
        assertTrue(llm.getLastError().contains("Song not found"));

        assertNull(llm.getSongById(null));
        assertTrue(llm.getLastError().contains("cannot be blank"));
    }


    // ----------------------------------------------------------
    /**
     * Tests getImportedSongs()
     */
    public void testGetImportedSongsIsACopy() {
        loadSampleFile();

        // * changing the copy must not change the real list
        List<Song> songs = llm.getImportedSongs();
        songs.clear();
        assertEquals(3, llm.numberImportedSongs());

        // * but the Song objects inside are shared on purpose
        llm.getImportedSongs().get(0).play();
        assertEquals(8, llm.getImportedSongs().get(0).getPlayCount());
    }


    // ----------------------------------------------------------
    /**
     * Tests saveSongs() by loading the file back again.
     */
    public void testSaveSongsRoundTrip() {
        loadSampleFile();

        llm.getSong("Clocks").play();
        llm.getSong("Clocks").play();

        createdFiles.add(SAVED_FILE);
        assertTrue(llm.saveSongs(SAVED_FILE, llm.getImportedSongs()));

        PlaylistLLM reloaded = new PlaylistLLM();
        assertTrue(reloaded.loadSongs(SAVED_FILE));
        assertEquals(3, reloaded.numberImportedSongs());
        assertEquals(0, reloaded.getSkippedRows());

        Song clocks = reloaded.getSong("Clocks");
        assertEquals("S003", clocks.getId());
        assertEquals("Coldplay", clocks.getArtist());
        assertEquals("5:07", clocks.getDuration());
        assertEquals("Rock", clocks.getGenre());
        // * the two plays were saved
        assertEquals(4, clocks.getPlayCount());
    }


    // ----------------------------------------------------------
    /**
     * Tests savePlaylist() with songs that have no id.
     */
    public void testSavePlaylistWithoutIds() {
        createdFiles.add(SAVED_FILE);
        assertTrue(llm.savePlaylist(SAVED_FILE, playlist));

        PlaylistLLM reloaded = new PlaylistLLM();
        assertTrue(reloaded.loadSongs(SAVED_FILE));
        assertEquals(3, reloaded.numberImportedSongs());
        assertEquals(6, reloaded.getSong("Cruel Summer").getPlayCount());
        assertEquals("Kendrick Lamar",
            reloaded.getSong("HUMBLE.").getArtist());
        // * an id was made up so the file can be read back
        assertNotNull(reloaded.getSong("Hotline Bling").getId());
    }


    // ----------------------------------------------------------
    /**
     * Tests savePlaylist() after a song has been played.
     */
    public void testSavePlaylistKeepsNewPlayCounts() {
        song1.play();
        song1.play();
        song1.play();

        createdFiles.add(SAVED_FILE);
        assertTrue(llm.savePlaylist(SAVED_FILE, playlist));

        PlaylistLLM reloaded = new PlaylistLLM();
        assertTrue(reloaded.loadSongs(SAVED_FILE));
        assertEquals(5, reloaded.getSong("Hotline Bling").getPlayCount());
    }


    // ----------------------------------------------------------
    /**
     * Tests saveSongs() and savePlaylist() with bad input.
     */
    public void testSaveSongsBadInput() {
        assertFalse(llm.saveSongs(null, playlist.getSongs()));
        assertEquals("No file name was given.", llm.getLastError());

        assertFalse(llm.saveSongs("  ", playlist.getSongs()));
        assertEquals("No file name was given.", llm.getLastError());

        assertFalse(llm.saveSongs(SAVED_FILE, null));
        assertTrue(llm.getLastError().contains("no song list"));
        // * nothing was written
        assertFalse(new File(SAVED_FILE).exists());

        assertFalse(llm.savePlaylist(SAVED_FILE, null));
        assertTrue(llm.getLastError().contains("no playlist"));
        assertFalse(new File(SAVED_FILE).exists());
    }


    // ----------------------------------------------------------
    /**
     * Tests saveSongs() when a title holds a comma.
     */
    public void testSaveSongsRefusesComma() {
        createdFiles.add(SAVED_FILE);
        assertTrue(llm.savePlaylist(SAVED_FILE, playlist));

        UserPlaylist withComma = new UserPlaylist();
        withComma.addSong(
            new Song("Hello, Goodbye", "The Beatles", "3:27", 1, "Rock"));

        assertFalse(llm.savePlaylist(SAVED_FILE, withComma));
        assertTrue(llm.getLastError().contains("comma"));

        // * the good file that was already there is untouched
        PlaylistLLM reloaded = new PlaylistLLM();
        assertTrue(reloaded.loadSongs(SAVED_FILE));
        assertEquals(3, reloaded.numberImportedSongs());
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    /**
     * Writes the sample file and loads it.
     */
    private void loadSampleFile() {
        writeSampleFile();
        assertTrue(llm.loadSongs(SAMPLE_FILE));
    }


    // ----------------------------------------------------------
    /**
     * Writes the sample file the reading tests share.
     */
    private void writeSampleFile() {
        String[] lines = {
            "id,title,artist,duration,genre,playCount",
            "S001,Blinding Lights,The Weeknd,3:20,Pop,7",
            "S002,Starboy,The Weeknd,3:50,R&B,3",
            "S003,Clocks,Coldplay,5:07,Rock,2"};
        writeFile(SAMPLE_FILE, lines);
    }


    // ----------------------------------------------------------
    /**
     * Writes one CSV file and remembers it for tearDown.
     *
     * @param fileName the file to create
     * @param lines    the lines to write
     */
    private void writeFile(String fileName, String[] lines) {
        createdFiles.add(fileName);

        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            for (int i = 0; i < lines.length; i++) {
                writer.println(lines[i]);
            }
        }
        catch (FileNotFoundException e) {
            fail("Could not write the test file " + fileName);
        }
    }
}
