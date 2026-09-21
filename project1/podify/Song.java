package podify;

// -------------------------------------------------------------------------
/**
 *  Represents a song.
 *  Describes the parameters and fields that the song class uses
 *  and sets up getters for them, also has an in playlist method
 *  that returns true or false depending on if it finds the song.
 *
 *  Every song also carries the id that the LLM gave it in the dataset.
 *  The id is what tells two songs apart, because two different songs can
 *  easily share a title.
 *
 *  @author miguel33
 *  @version 20 Sept 2026
 */
public class Song {

    //~ Fields ................................................................
    private String id;
    private String name;
    private String artist;
    private String duration;
    private int playCount;
    private String genre;

    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new Song object that knows the id the LLM gave it.
     *
     * @param id        the id from the LLM dataset, for example "S001"
     * @param name      the title of the song
     * @param artist    the name of the artist
     * @param duration  the length of the song as "M:SS"
     * @param playCount how many times the song has been played
     * @param genre     the genre of the song
     */
    public Song(String id, String name, String artist, String duration,
                int playCount, String genre) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.duration = duration;
        this.playCount = playCount;
        this.genre = genre;
    }


    // ----------------------------------------------------------
    /**
     * Create a new Song object without an id. This is used by the tests and
     * by any code that builds a song by hand instead of reading it from the
     * LLM file. The id is left null, and equals() then falls back to
     * comparing the title and the artist.
     *
     * @param name      the title of the song
     * @param artist    the name of the artist
     * @param duration  the length of the song as "M:SS"
     * @param playCount how many times the song has been played
     * @param genre     the genre of the song
     */
    public Song(String name, String artist, String duration,
                int playCount, String genre) {
        // Calling the other constructor keeps the field assignments in one
        // place, so there is only one spot to change if a field is added.
        this(null, name, artist, duration, playCount, genre);
    }

    //~Public  Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Gets the id that the LLM gave this song.
     *
     * @return the id, or null if this song was not read from the LLM file.
     */
    public String getId() {
        return id;
    }

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
     * Plays the song once. Every call adds one to the play count, which is
     * what the Ranking class sorts on. This is the only way the play count
     * is allowed to change, so a song can never lose plays.
     *
     * @return the new play count after this play.
     */
    public int play() {
        playCount++;
        return playCount;
    }

    // ----------------------------------------------------------
    /**
     * Determines if the song is in the given playlist.
     *
     * @param playlist the playlist to look in
     * @return true if in playlist, false if not or if the playlist is null.
     */
    public boolean inPlaylist(UserPlaylist playlist) {
        if (playlist == null) {
            return false;
        }
        return playlist.search(this);
    }

    // ----------------------------------------------------------
    /**
     * Two songs are the same song when the LLM gave them the same id. When
     * either song has no id, the title and the artist are compared instead,
     * because that is the best we can do for a hand-made song.
     *
     * @param other the object to compare against
     * @return true if both objects stand for the same song.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !(other instanceof Song)) {
            return false;
        }

        Song that = (Song)other;

        if (id != null && that.id != null) {
            return id.equalsIgnoreCase(that.id);
        }

        return name.equalsIgnoreCase(that.name)
            && artist.equalsIgnoreCase(that.artist);
    }

    // ----------------------------------------------------------
    /**
     * Java asks for hashCode whenever equals is overridden. The title is
     * used because it is the one field that every song always has.
     *
     * @return a hash code built from the lower case title.
     */
    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    // ----------------------------------------------------------
    /**
     * Builds the line that the console menu shows for this song. The scope
     * document asks for the title, the artist and the play count.
     *
     * @return the song as one line of text.
     */
    @Override
    public String toString() {
        return name + " - " + artist + " (" + duration + ", " + genre
            + ") - " + playCount + " plays";
    }
}
