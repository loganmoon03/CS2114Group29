package podify;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Tests the Ranking class.
 *
 * @author Daphne
 * @version 18 Sept 2026
 */
public class RankingTest
{
    private UserPlaylist playlist;
    private Ranking ranking;
    private Song song1;
    private Song song2;
    private Song song3;
    private Song song4;
    private Song song5;
    private Song song6;

    /**
     * Sets up the playlist and songs for testing.
     */
    public void setUp()
    {
        playlist = new UserPlaylist();

        song1 = new Song("Blinding Lights", "The Weeknd", "3:20", 30, "Pop");

        song2 = new Song("Starboy", "The Weeknd", "3:50", 25, "R&B");

        song3 = new Song("Shape of You", "Ed Sheeran", "3:53", 20, "Pop");

        song4 =
            new Song("Smells Like Teen Spirit", "Nirvana", "5:01", 15, "Rock");

        song5 = new Song("Dynamite", "BTS", "3:19", 10, "KPop");

        song6 = new Song("Clocks", "Coldplay", "5:07", 5, "Rock");

        playlist.addSong(song1);
        playlist.addSong(song2);
        playlist.addSong(song3);
        playlist.addSong(song4);
        playlist.addSong(song5);
        playlist.addSong(song6);

        ranking = new Ranking(playlist);
    }


    /**
     * Tests that songs are ranked from highest to lowest play count.
     */
    public void testRankSongs()
    {
        List<Song> ranked = ranking.rankSongs();

        assertEquals(song1, ranked.get(0));
        assertEquals(song2, ranked.get(1));
        assertEquals(song3, ranked.get(2));
        assertEquals(song4, ranked.get(3));
        assertEquals(song5, ranked.get(4));
        assertEquals(song6, ranked.get(5));
    }


    /**
     * Tests the top three songs.
     */
    public void testShowTop()
    {
        List<Song> top = ranking.showTop();

        assertEquals(3, top.size());

        assertEquals(song1, top.get(0));
        assertEquals(song2, top.get(1));
        assertEquals(song3, top.get(2));
    }


    /**
     * Tests the bottom three songs.
     */
    public void testShowBottom()
    {
        List<Song> bottom = ranking.showBottom();

        assertEquals(3, bottom.size());

        assertEquals(song4, bottom.get(0));
        assertEquals(song5, bottom.get(1));
        assertEquals(song6, bottom.get(2));
    }


    /**
     * Tests the full ranked playlist.
     */
    public void testShowFull()
    {
        List<Song> full = ranking.showFull();

        assertEquals(6, full.size());

        assertEquals(song1, full.get(0));
        assertEquals(song2, full.get(1));
        assertEquals(song3, full.get(2));
        assertEquals(song4, full.get(3));
        assertEquals(song5, full.get(4));
        assertEquals(song6, full.get(5));
    }


    /**
     * Tests songs that have the same play count. They should be ordered
     * alphabetically by artist.
     */
    public void testSamePlayCount()
    {
        UserPlaylist tiePlaylist = new UserPlaylist();

        Song levels = new Song("Levels", "Avicii", "3:19", 10, "EDM");

        Song yellow = new Song("Yellow", "Coldplay", "4:26", 10, "Indie");

        tiePlaylist.addSong(yellow);
        tiePlaylist.addSong(levels);

        Ranking tieRanking = new Ranking(tiePlaylist);

        List<Song> ranked = tieRanking.rankSongs();

        assertEquals(levels, ranked.get(0));
        assertEquals(yellow, ranked.get(1));
    }


    /**
     * Tests an empty playlist.
     */
    public void testEmptyPlaylist()
    {
        UserPlaylist emptyPlaylist = new UserPlaylist();
        Ranking emptyRanking = new Ranking(emptyPlaylist);

        assertEquals(0, emptyRanking.rankSongs().size());
        assertEquals(0, emptyRanking.showTop().size());
        assertEquals(0, emptyRanking.showBottom().size());
        assertEquals(0, emptyRanking.showFull().size());
    }
}
