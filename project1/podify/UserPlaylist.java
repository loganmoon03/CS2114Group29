package podify;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// -------------------------------------------------------------------------
/**
 * Represents the user's current playlist.
 * Manages adding, removing, searching, and reordering songs using an ArrayList.
 * 
 * @author Suhana Chowdhury
 * @version 20 Sept 2026
 */
public class UserPlaylist {

    //~ Fields ................................................................
    
    /** Core storage for playlist songs */
    private List<Song> songs;

    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Constructs a new empty UserPlaylist using an ArrayList.
     */
    public UserPlaylist() {
        this.songs = new ArrayList<>();
    }

    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Adds a song to the playlist if it is not null or a duplicate.
     * 
     * @param song The Song object to add.
     * @return true if added successfully; false if null or already exists.
     */
    public boolean addSong(Song song) {
        if (song == null) {
            System.out.println("Error: Cannot add a null song.");
            return false;
        }

        // Checks for duplicates in the playlist
        if (containsSong(song)) {
            System.out.println("Error: Song already exists in playlist.");
            return false;
        }

        return songs.add(song);
    }

    // ----------------------------------------------------------
    /**
     * Removes a song from the playlist.
     * 
     * @param song The Song object to remove.
     * @return The removed Song object, or null if not found/invalid.
     */
    public Song removeSong(Song song) {
        if (song == null) {
            System.out.println("Error: Cannot remove null song.");
            return null;
        }

        int index = findSongIndex(song);
        if (index != -1) {
            return songs.remove(index);
        }

        System.out.println("Error: Song not found in playlist.");
        return null;
    }

    // ----------------------------------------------------------
    /**
     * Searches for a particular song in the playlist.
     * 
     * @param song The Song object to search for.
     * @return true if found, false if not found or parameter invalid.
     */
    public boolean search(Song song) {
        if (song == null) {
            System.out.println("Error: Search query parameter is null.");
            return false;
        }

        // Validation: Blank name check
        if (song.getName() == null || song.getName().trim().isEmpty()) {
            System.out.println("Error: Blank song name in search query.");
            return false;
        }

        // Validation: Character limit check
        if (song.getName().length() > 100) {
            System.out.println("Error: Query exceeds character limit (100 characters max).");
            return false;
        }

        return containsSong(song);
    }

    // ----------------------------------------------------------
    /**
     * Reorders the playlist alphabetically by song name.
     * 
     * @return A list sorted by song title.
     */
    public List<Song> orderBySong() {
        List<Song> sortedList = new ArrayList<>(songs);
        sortedList.sort(Comparator.comparing(Song::getName, String.CASE_INSENSITIVE_ORDER));
        return sortedList;
    }

    // ----------------------------------------------------------
    /**
     * Reorders the playlist alphabetically by artist name.
     * 
     * @return A list sorted by artist name.
     */
    public List<Song> orderByArtist() {
        List<Song> sortedList = new ArrayList<>(songs);
        sortedList.sort(Comparator.comparing(Song::getArtist, String.CASE_INSENSITIVE_ORDER));
        return sortedList;
    }

    // ----------------------------------------------------------
    /**
     * Gets the total number of songs in the playlist.
     * 
     * @return Number of songs in the playlist.
     */
    public int numberSongs() {
        return songs.size();
    }

    // ----------------------------------------------------------
    /**
     * Checks whether the playlist size is strictly smaller than 6.
     * 
     * @return true if size is less than 6, false otherwise.
     */
    public boolean lessThanSix() {
        return songs.size() < 6;
    }

    // ----------------------------------------------------------
    /**
     * Gets the internal song list (used by Ranking module).
     * 
     * @return List of songs in the playlist.
     */
    public List<Song> getSongs() {
        return this.songs;
    }

    //~ Helper Methods ........................................................

    /**
     * Helper method to check equality by content (Title & Artist).
     */
    private boolean containsSong(Song target) {
        return findSongIndex(target) != -1;
    }

    /**
     * Helper method to find index based on song title and artist.
     */
    private int findSongIndex(Song target) {
        if (target == null) return -1;
        for (int i = 0; i < songs.size(); i++) {
            Song current = songs.get(i);
            if (current.getName().equalsIgnoreCase(target.getName()) &&
                current.getArtist().equalsIgnoreCase(target.getArtist())) {
                return i;
            }
        }
        return -1;
    }
}
