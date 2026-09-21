package podify;

import java.util.List;

// -------------------------------------------------------------------------
/**
 * Test class for UserPlaylist.
 * Covers normal cases and bad-input test cases specified in the test plan
 * using mainstream American artists.
 * 
 * @author Suhana Chowdhury
 * @version 20 Sept 2026
 */
public class UserPlaylistTest {

    public static void main(String[] args) {
        System.out.println("=== STARTING USERPLAYLIST TESTS ===");

        testAddSong();
        testRemoveSong();
        testSearch();
        testOrderBySong();
        testOrderByArtist();
        testLessThanSix();

        System.out.println("\n=== ALL TESTS COMPLETED ===");
    }

    public static void testAddSong() {
        System.out.println("\n--- Testing addSong() ---");
        UserPlaylist playlist = new UserPlaylist();
        Song song1 = new Song("God's Plan", "Drake", "3:18", 500, "Hip-Hop");

        // Normal Case: Valid song
        boolean added = playlist.addSong(song1);
        System.out.println("Normal add [Expected: true]: " + added);

        // Bad-input Case 1: Null object parameter
        boolean addedNull = playlist.addSong(null);
        System.out.println("Bad-input (null) [Expected: false]: " + addedNull);

        // Bad-input Case 2: Duplicate song
        boolean addedDuplicate = playlist.addSong(song1);
        System.out.println("Bad-input (duplicate) [Expected: false]: " + addedDuplicate);
    }

    public static void testRemoveSong() {
        System.out.println("\n--- Testing removeSong() ---");
        UserPlaylist playlist = new UserPlaylist();
        Song song1 = new Song("Not Like Us", "Kendrick Lamar", "4:34", 800, "Hip-Hop");
        Song song2 = new Song("Snooze", "SZA", "3:21", 350, "R&B");

        playlist.addSong(song1);

        // Normal Case: Song exists in playlist
        Song removed = playlist.removeSong(song1);
        System.out.println("Normal remove [Expected: Not Like Us]: " + (removed != null ? removed.getName() : "null"));

        // Bad-input Case 1: Song is not in playlist
        Song removedMissing = playlist.removeSong(song2);
        System.out.println("Bad-input (missing song) [Expected: null]: " + removedMissing);

        // Bad-input Case 2: Parameter is null
        Song removedNull = playlist.removeSong(null);
        System.out.println("Bad-input (null) [Expected: null]: " + removedNull);
    }

    public static void testSearch() {
        System.out.println("\n--- Testing search() ---");
        UserPlaylist playlist = new UserPlaylist();
        Song song1 = new Song("Circles", "Post Malone", "3:35", 420, "Pop");
        Song emptyNameSong = new Song("   ", "Future", "2:40", 10, "Hip-Hop");
        
        // Creating song title > 100 characters max limit
        String longTitle = "A".repeat(105);
        Song longTitleSong = new Song(longTitle, "Travis Scott", "3:00", 15, "Hip-Hop");

        playlist.addSong(song1);

        // Normal Case: Song on playlist
        System.out.println("Normal search [Expected: true]: " + playlist.search(song1));

        // Bad-input Case 1: Null parameter
        System.out.println("Bad-input (null parameter) [Expected: false]: " + playlist.search(null));

        // Bad-input Case 2: Blank song name
        System.out.println("Bad-input (blank name) [Expected: false]: " + playlist.search(emptyNameSong));

        // Bad-input Case 3: Title exceeds character limit
        System.out.println("Bad-input (exceeds limit) [Expected: false]: " + playlist.search(longTitleSong));
    }

    public static void testOrderBySong() {
        System.out.println("\n--- Testing orderBySong() ---");
        UserPlaylist playlist = new UserPlaylist();
        
        // Edge case: Two songs with identical names
        Song songA1 = new Song("One Dance", "Drake", "2:54", 600, "Hip-Hop");
        Song songA2 = new Song("One Dance", "Cover Artist", "3:00", 5, "Pop");
        Song songB = new Song("HUMBLE.", "Kendrick Lamar", "2:57", 750, "Hip-Hop");

        playlist.addSong(songA1);
        playlist.addSong(songB);
        playlist.addSong(songA2);

        List<Song> sorted = playlist.orderBySong();
        System.out.println("Ordered by Song Title:");
        for (Song s : sorted) {
            System.out.println(" - " + s.getName() + " by " + s.getArtist());
        }
    }

    public static void testOrderByArtist() {
        System.out.println("\n--- Testing orderByArtist() ---");
        UserPlaylist playlist = new UserPlaylist();
        
        Song song1 = new Song("Hotline Bling", "Drake", "4:27", 550, "Hip-Hop");
        Song song2 = new Song("Cruel Summer", "Taylor Swift", "2:58", 900, "Pop");

        playlist.addSong(song1);
        playlist.addSong(song2);

        List<Song> sorted = playlist.orderByArtist();
        System.out.println("Ordered by Artist:");
        for (Song s : sorted) {
            System.out.println(" - " + s.getArtist() + ": " + s.getName());
        }
    }

    public static void testLessThanSix() {
        System.out.println("\n--- Testing lessThanSix() ---");
        UserPlaylist playlist = new UserPlaylist();

        // Normal Case 1: 0 songs (< 6)
        System.out.println("0 songs check [Expected: true]: " + playlist.lessThanSix());

        // Fill playlist with 6 mainstream tracks
        playlist.addSong(new Song("Rich Baby Daddy", "Drake", "5:19", 300, "Hip-Hop"));
        playlist.addSong(new Song("Feather", "Sabrina Carpenter", "3:05", 250, "Pop"));
        playlist.addSong(new Song("FE!N", "Travis Scott", "3:11", 400, "Hip-Hop"));
        playlist.addSong(new Song("Lovin On Me", "Jack Harlow", "2:18", 350, "Hip-Hop"));
        playlist.addSong(new Song("Kill Bill", "SZA", "2:33", 650, "R&B"));
        playlist.addSong(new Song("Sunflower", "Post Malone", "2:38", 950, "Pop"));

        // Normal Case 2: 6 songs (Not < 6)
        System.out.println("6 songs check [Expected: false]: " + playlist.lessThanSix());
    }
}
