# CS2114Group29
# Podify - Playlist Management & Ranking System

Podify is a Java-based application designed to manage custom user playlists, track song playback statistics and rank songs by play count.

---

## Project Structure

The project consists of four core modules and the primary driver class:

- `Song.java` - Data model representing individual tracks, artists, duration, genre and play count metrics.
- `UserPlaylist.java` - Manages song collections and playlist operations.
- `Ranking.java` - Utility class for sorting and displaying top tracks based on play count.
- `PlaylistLLM.java` - Generates recommendations and natural-language summaries based on listener habits.
- `Podify.java` - Main driver class containing the `public static void main(String[] args)` entry point.

---

## How to Import, Compile, and Run in Eclipse

### Step 1: Import the Project into Eclipse

1. Open **Eclipse IDE**.
2. Go to top menu: **File** > **Import...**
3. Expand **General**, select **Projects from Folder or Archive**, and click **Next**.
4. Click **Directory...**, select the `podify` root folder on your computer, and click **Finish**.

---

### Step 2: Compile the Code

Eclipse automatically compiles your Java code every time you save a file (**Project** > **Build Automatically** is enabled by default). 

*If there are red build markers:*
1. Go to **Project** > **Clean...**
2. Select **Clean all projects** and click **Clean**.

---

### Step 3: Run the Application

1. In the **Package Explorer** window on the left, expand the `podify` project and open the `src` folder.
2. Right-click **`Podify.java`**.
3. Select **Run As** > **Java Application**.
4. View the output in the **Console** panel at the bottom of Eclipse.

---