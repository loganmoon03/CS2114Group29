package podify;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
// -------------------------------------------------------------------------
/**
 *  Represents the tests for the song class.
 *  Tests the five getters for the name, artist, duration,
 *  playcount and genre as well as the in playlist method.
 * 
 *  @author miguel33
 *  @version 19 Sept 2026
 */
class SongTest {

    //~ Fields ................................................................
    private Song song1;

    //~ Constructors ..........................................................
    
    // ----------------------------------------------------------
    /**
     * Set up method that runs at the beginning of every test.
     */
    public void setUp() {
        song1 = new Song("Enter Sandman", "Metallica", "5:30", 3, "Rock");
    }
    
    
    //~Public  Methods ........................................................
    
    // ----------------------------------------------------------
    /**
     * Tests the five getters in song.
     */
    public void testGetters() {
        assertEquals("Enter Sandman", song1.getName());
        assertEquals("Metallica", song1.getArtist());
        assertEquals("5:30", song1.getDuration());
        assertEquals(3, song1.getPlayCount());
        assertEquals("Rock", song1.getGenre());
    }
    
    // ----------------------------------------------------------
    /**
     * Tests inPlaylist().
     */
    public void testInPlaylist() {
        //
    }
    
}