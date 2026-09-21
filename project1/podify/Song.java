package podify;

// -------------------------------------------------------------------------
/**
 *  Represents a song.
 *  Describes the parameters and fields that the song class uses
 *  and sets up getters for them, also has an in playlist method
 *  that returns true or false depending on if it finds the song.
 * 
 *  @author miguel33
 *  @version 19 Sept 2026
 */
public class Song {
    
    //~ Fields ................................................................
    private String name;
    private String artist;
    private String duration;
    private int playCount;
    private String genre;
    //~ Constructors ..........................................................
    
    // ----------------------------------------------------------
    /**
     * Create a new Song object.
     * @param name
     * @param artist
     * @param duration
     * @param playCount
     * @param genre
     */
    public Song(String name, String artist, String duration,
                int playCount, String genre) {
        this.name = name;
        this.artist = artist;
        this.duration = duration;
        this.playCount = playCount;
        this.genre = genre;
    }
    
    //~Public  Methods ........................................................
    
    // ----------------------------------------------------------
    /**
     * Gets the song's name.
     * @return name of song.
     */
    public String getName() {
        return name;
    }
    
    // ----------------------------------------------------------
    /**
     * Gets the name of the song's artist.
     * @return artist.
     */
    public String getArtist() {
        return artist;
    }
    
    // ----------------------------------------------------------
    /**
     * Gets the song's duration in the format x:xx (minutes:seconds).
     * @return duration of song.
     */
    public String getDuration() {
        return duration;
    }
    
    // ----------------------------------------------------------
    /**
     * Gets the number of times the song has been played.
     * @return playcount.
     */
    public int getPlayCount() {
        return playCount;
    }
    
    // ----------------------------------------------------------
    /**
     * Gets the song's genre.
     * @return genre.
     */
    public String getGenre() {
        return genre;
    }
    
    // ----------------------------------------------------------
    /**
     * Determines if the song is in the playlist.
     * @return true if in playlist, false if not.
     */
    public boolean inPlaylist(Playlist playlist) {
        if (playlist == null) {
            return false;
        }
        return playlist.search(this);
    }
}