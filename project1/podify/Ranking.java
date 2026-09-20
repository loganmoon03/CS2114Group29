package podify;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the ranking of songs in a user's playlist. Ranks songs by play
 * count and uses artist name alphabetically when play counts are the EXACT
 * same.
 *
 * @author Daphne
 * @version 17 Sept 2026
 */
public class Ranking
{
    private UserPlaylist playlist;

    /**
     * Creates a new Ranking object.
     *
     * @param playlist
     *            the user's playlist
     */
    public Ranking(UserPlaylist playlist)
    {
        this.playlist = playlist;
    }


    /**
     * Lists all songs from highest to lowest play count. If two songs have the
     * same play count, they are ranked alphabetically by artist name. Similar
     * to Ranking class.
     *
     * @return the ranked list of songs
     */
    public List<Song> rankSongs()
    {
        List<Song> ranked = new ArrayList<Song>();

        // Copy the songs into a new list
        for (Song s : playlist.getSongs())
        {
            ranked.add(s);
        }

        // Bubble sort: highest play count first
        int n = ranked.size();

        for (int i = 0; i < n - 1; i++)
        {
            for (int j = 0; j < n - 1 - i; j++)
            {
                Song a = ranked.get(j);
                Song b = ranked.get(j + 1);

                boolean shouldSwap = false;

                // Higher play count should come first
                if (a.getPlayCount() < b.getPlayCount())
                {
                    shouldSwap = true;
                }
                // If tied, sort alphabetically by artist
                else if (a.getPlayCount() == b.getPlayCount())
                {
                    if (a.getArtist().compareToIgnoreCase(b.getArtist()) > 0)
                    {
                        shouldSwap = true;
                    }
                }

                if (shouldSwap)
                {
                    ranked.set(j, b);
                    ranked.set(j + 1, a);
                }
            }
        }

        return ranked;
    }


    /**
     * Returns the top three most played songs.
     *
     * @return the top three songs
     */
    public List<Song> showTop()
    {
        List<Song> ranked = rankSongs();
        List<Song> top = new ArrayList<Song>();

        int count = 0;

        for (Song s : ranked)
        {
            if (count == 3)
            {
                break;
            }

            top.add(s);
            count++;
        }

        return top;
    }


    /**
     * Returns the bottom three songs.
     *
     * @return the bottom three songs
     */
    public List<Song> showBottom()
    {
        List<Song> ranked = rankSongs();
        List<Song> bottom = new ArrayList<Song>();

        int size = ranked.size();

        if (size == 0)
        {
            return bottom;
        }

        int start = size - 3;

        if (start < 0)
        {
            start = 0;
        }

        for (int i = start; i < size; i++)
        {
            bottom.add(ranked.get(i));
        }

        return bottom;
    }


    /**
     * Returns the complete ranked playlist.
     *
     * @return the full ranked playlist
     */
    public List<Song> showFull()
    {
        return rankSongs();
    }
}
