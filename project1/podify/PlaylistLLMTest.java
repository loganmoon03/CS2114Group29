package podify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

// -------------------------------------------------------------------------
/**
 *  Tests the PlaylistLLM class.
 *
 *  The spec's test plan asks for one normal case and one bad-input case for
 *  loadSongs. The other public methods are tested the same way, because the
 *  whole point of this class is that a broken file must never crash the
 *  program or change songs that were already loaded.
 *
 *  The songs used by the save tests are built by hand in setUp. The tests
 *  that read a file write their own small CSV first, using the same File
 *  and PrintWriter classes that PlaylistLLM itself uses. Every file that a
 *  test makes is deleted again in tearDown, so the tests never leave
 *  anything behind and never depend on each other.
 *
 *  @author Dochan (Logan) Moon
 *  @version 20 Sept 2026
 */
public class PlaylistLLMTest extends student.TestCase {

    //~ Fields ................................................................

    /** The file most of the reading tests load. */
    private static final String SAMPLE_FILE = "test_sample.csv";

    /** The file the saving tests write to. */
    private static final String SAVED_FILE = "test_saved.csv";

    private PlaylistLLM llm;

    private Song song1;
    private Song song2;
    private Song song3;

    private UserPlaylist playlist;

    // Every file a test creates is remembered here so tearDown can delete
    // it again.
    private List<String> createdFiles;


    //~ Set Up ................................................................

    // ----------------------------------------------------------
    /**
     * Set up method that runs at the beginning of every test. It builds a
     * fresh PlaylistLLM, three songs, and a playlist holding those songs.
     * The songs have no id, because that is what a playlist the user built
     * by hand looks like.
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
     * Runs after every test and deletes the files that the test made.
     */
    public void tearDown() {
        for (int i = 0; i < createdFiles.size(); i++) {
            File file = new File(createdFiles.get(i));
            if (file.exists()) {
                file.delete();
            }
        }
    }


    //~ loadSongs: normal cases ...............................................

    // ----------------------------------------------------------
    /**
     * The normal case from the test plan: a file where every row is good.
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
     * A file that has no header still loads. The header is only skipped
     * when the first line actually looks like one.
     */
    public void testLoadSongsWithoutHeader() {
        String[] lines = {"S001,Levels,Avicii,3:19,EDM,4"};
        writeFile("test_noheader.csv", lines);

        assertTrue(llm.loadSongs("test_noheader.csv"));
        assertEquals(1, llm.numberImportedSongs());
    }


    // ----------------------------------------------------------
    /**
     * Blank lines are normal at the end of a text file, so they must not
     * count as damaged rows.
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
        assertEquals(0, llm.getSkippedRows());
    }


    //~ loadSongs: bad input ..................................................

    // ----------------------------------------------------------
    /**
     * The bad-input case from the test plan: the file is not there. The
     * scope document says this must show an error and let the user try
     * again, so loadSongs returns false instead of throwing.
     */
    public void testLoadSongsFileNotFound() {
        assertFalse(llm.loadSongs("no_such_file_anywhere.csv"));
        assertEquals(0, llm.numberImportedSongs());
        assertTrue(llm.getLastError().contains("Could not open"));
    }


    // ----------------------------------------------------------
    /**
     * A missing or blank file name is rejected before anything is opened.
     */
    public void testLoadSongsBlankFileName() {
        assertFalse(llm.loadSongs(null));
        assertEquals("No file name was given.", llm.getLastError());

        assertFalse(llm.loadSongs("   "));
        assertEquals("No file name was given.", llm.getLastError());
    }


    // ----------------------------------------------------------
    /**
     * A file where every row is broken loads nothing, so loadSongs reports
     * failure rather than pretending it worked.
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
     * The scope document says invalid input must not change saved data. A
     * failed load has to leave the songs from the last good load alone.
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
     * Each kind of broken row is skipped and counted, and the good rows
     * around it are still loaded.
     */
    public void testLoadSongsSkipsDamagedRows() {
        String[] lines = {
            "id,title,artist,duration,genre,playCount",
            "S001,Good Song,Good Artist,3:20,Pop,5",
            "S002,Too Few Columns,Artist,3:20,Pop",
            "S003,,Artist,3:20,Pop,1",
            "S004,Bad Count,Artist,3:20,Pop,abc",
            "S005,Negative Count,Artist,3:20,Pop,-4",
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
     * The scope document asks for a duplicate warning, which is a different
     * message from "the file is damaged". Duplicates are matched on the id
     * the LLM assigned, and the first song listed is the one that is kept.
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
        assertEquals(2, llm.getDuplicateRows());
        assertEquals(0, llm.getDamagedRows());
        assertEquals(2, llm.getSkippedRows());

        // The first row wins, so the later play count of 9 is thrown away.
        assertEquals("First Copy", llm.getImportedSongs().get(0).getName());
        assertTrue(llm.getLastError().contains("duplicate"));
    }


    // ----------------------------------------------------------
    /**
     * A duration must look like M:SS and the seconds must really be
     * seconds, so "3:75" is refused even though its shape is right.
     */
    public void testLoadSongsChecksDuration() {
        String[] lines = {
            "id,title,artist,duration,genre,playCount",
            "S001,Seconds Too Big,Artist,3:75,Pop,1",
            "S002,One Digit Seconds,Artist,3:5,Pop,1",
            "S003,No Colon,Artist,351,Pop,1",
            "S004,Starts With Colon,Artist,:51,Pop,1",
            "S005,Letters Inside,Artist,3:5x,Pop,1",
            "S006,Just Right,Artist,3:59,Pop,1",
            "S007,Zero Seconds,Artist,4:00,Pop,1"};
        writeFile("test_durations.csv", lines);

        assertTrue(llm.loadSongs("test_durations.csv"));
        assertEquals(2, llm.numberImportedSongs());
        assertEquals(5, llm.getDamagedRows());
    }


    //~ searchSongs ...........................................................

    // ----------------------------------------------------------
    /**
     * A search matches part of a title or part of an artist name, and does
     * not care about upper or lower case.
     */
    public void testSearchSongsNormal() {
        loadSampleFile();

        assertEquals(1, llm.searchSongs("Blinding Lights").size());
        assertEquals(2, llm.searchSongs("weeknd").size());
        assertEquals(1, llm.searchSongs("clock").size());
        assertEquals("", llm.getLastError());
    }


    // ----------------------------------------------------------
    /**
     * A blank search is rejected and the user is told what to type. The
     * list that comes back is empty, never null.
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
     * The scope document says an over-long search must be refused with a
     * message that explains the limit. This query is 47 characters, which
     * is past the 30 character limit.
     */
    public void testSearchSongsTooLong() {
        loadSampleFile();

        String longQuery = "This search text is much too long to be accepted";

        assertTrue(llm.searchSongs(longQuery).isEmpty());
        assertTrue(llm.getLastError().contains("too long"));
        assertTrue(llm.getLastError().contains("30"));
    }


    // ----------------------------------------------------------
    /**
     * Searching for something that is not there gives the "song not found"
     * message the scope document asks for.
     */
    public void testSearchSongsNoMatch() {
        loadSampleFile();

        assertTrue(llm.searchSongs("Nirvana").isEmpty());
        assertTrue(llm.getLastError().contains("Song not found"));
    }


    //~ getSong and getSongById ...............................................

    // ----------------------------------------------------------
    /**
     * getSong finds a song by its whole title, ignoring case.
     */
    public void testGetSongNormal() {
        loadSampleFile();

        Song song = llm.getSong("blinding lights");
        assertNotNull(song);
        assertEquals("The Weeknd", song.getArtist());
        assertEquals("", llm.getLastError());
    }


    // ----------------------------------------------------------
    /**
     * A blank title and a title that is not in the list both give null
     * instead of throwing, so the menu can print the message and carry on.
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
     * Two songs can share a title, so the menu picks songs by the id the
     * LLM gave them instead.
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

        // getSong only ever finds the first one, which is why ids exist.
        assertEquals("Drake", llm.getSong("One Dance").getArtist());

        assertNull(llm.getSongById("S999"));
        assertTrue(llm.getLastError().contains("Song not found"));

        assertNull(llm.getSongById(null));
        assertTrue(llm.getLastError().contains("cannot be blank"));
    }


    //~ getImportedSongs ......................................................

    // ----------------------------------------------------------
    /**
     * The list that comes back is a copy, so changing it must not change
     * what PlaylistLLM holds. The Song objects inside are shared on
     * purpose, because playing a song has to count everywhere.
     */
    public void testGetImportedSongsIsACopy() {
        loadSampleFile();

        List<Song> songs = llm.getImportedSongs();
        songs.clear();

        assertEquals(3, llm.numberImportedSongs());

        llm.getImportedSongs().get(0).play();
        assertEquals(8, llm.getImportedSongs().get(0).getPlayCount());
    }


    //~ saveSongs and savePlaylist ............................................

    // ----------------------------------------------------------
    /**
     * The normal case: songs that are saved and then loaded again come back
     * exactly as they were, play counts included. This is what makes the
     * data survive closing the program.
     */
    public void testSaveSongsRoundTrip() {
        loadSampleFile();

        // Play one song twice so the saved counts are not all the same.
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
        assertEquals(4, clocks.getPlayCount());
    }


    // ----------------------------------------------------------
    /**
     * The playlist built in setUp holds songs with no id, which is what a
     * playlist the user made by hand looks like. Saving still has to
     * produce a file that can be read back, so an id is made up.
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
        assertNotNull(reloaded.getSong("Hotline Bling").getId());
    }


    // ----------------------------------------------------------
    /**
     * Playing a song and then saving keeps the new count, which is the
     * whole point of saving the playlist between runs.
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
     * Bad input to the save methods is refused, and nothing is written.
     */
    public void testSaveSongsBadInput() {
        assertFalse(llm.saveSongs(null, playlist.getSongs()));
        assertEquals("No file name was given.", llm.getLastError());

        assertFalse(llm.saveSongs("  ", playlist.getSongs()));
        assertEquals("No file name was given.", llm.getLastError());

        assertFalse(llm.saveSongs(SAVED_FILE, null));
        assertTrue(llm.getLastError().contains("no song list"));
        assertFalse(new File(SAVED_FILE).exists());

        assertFalse(llm.savePlaylist(SAVED_FILE, null));
        assertTrue(llm.getLastError().contains("no playlist"));
        assertFalse(new File(SAVED_FILE).exists());
    }


    // ----------------------------------------------------------
    /**
     * A comma inside a title would split one column into two and make the
     * saved file unreadable, so the save is refused and the file on disk is
     * left exactly as it was.
     */
    public void testSaveSongsRefusesComma() {
        createdFiles.add(SAVED_FILE);
        assertTrue(llm.savePlaylist(SAVED_FILE, playlist));

        UserPlaylist withComma = new UserPlaylist();
        withComma.addSong(
            new Song("Hello, Goodbye", "The Beatles", "3:27", 1, "Rock"));

        assertFalse(llm.savePlaylist(SAVED_FILE, withComma));
        assertTrue(llm.getLastError().contains("comma"));

        // The good file that was already there is untouched.
        PlaylistLLM reloaded = new PlaylistLLM();
        assertTrue(reloaded.loadSongs(SAVED_FILE));
        assertEquals(3, reloaded.numberImportedSongs());
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    /**
     * Writes the small file that most of the reading tests share, then
     * loads it.
     */
    private void loadSampleFile() {
        writeSampleFile();
        assertTrue(llm.loadSongs(SAMPLE_FILE));
    }


    // ----------------------------------------------------------
    /**
     * Writes the sample file used by several tests. Two of the songs share
     * an artist so that a search can find more than one row.
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
     * Writes one CSV file for a test to read, and remembers the name so
     * that tearDown can delete it afterwards.
     *
     * @param fileName the name of the file to create
     * @param lines    the lines to write, one per row of the file
     */
    private void writeFile(String fileName, String[] lines) {
        createdFiles.add(fileName);

        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            for (int i = 0; i < lines.length; i++) {
                writer.println(lines[i]);
            }
        }
        catch (FileNotFoundException e) {
            // Writing into the project folder should always work. If it
            // ever does not, the test has to fail rather than pass by
            // accident on a file that was never created.
            fail("Could not write the test file " + fileName);
        }
    }
}
