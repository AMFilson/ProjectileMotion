/* 
 * Name:    MatchRecord.java (ProjectileMotion / BIT-REKT)
 * Author:  Andrew Filson
 * Date:    April 24th 2026
 * Desc:    Data model representing a completed game session.
 *          Used for logging match history to persistent storage.
 */

public class MatchRecord {
    private int gameNumber;
    private String p1Name;
    private int p1TankIndex;
    private int p1Score;
    private String p2Name;
    private int p2TankIndex;
    private int p2Score;

    public MatchRecord(int gameNumber, String p1Name, int p1TankIndex, int p1Score, 
                       String p2Name, int p2TankIndex, int p2Score) {
        this.gameNumber = gameNumber;
        this.p1Name = p1Name;
        this.p1TankIndex = p1TankIndex;
        this.p1Score = p1Score;
        this.p2Name = p2Name;
        this.p2TankIndex = p2TankIndex;
        this.p2Score = p2Score;
    }

    /** Converts the record to a CSV line: gameNum,p1Name,p1TankIdx,p1Score,p2Name,p2TankIdx,p2Score */
    public String toCSV() {
        return String.format("%d,%s,%d,%d,%s,%d,%d", 
            gameNumber, p1Name, p1TankIndex, p1Score, p2Name, p2TankIndex, p2Score);
    }

    // Getters
    public int getGameNumber() { return gameNumber; }
    public String getP1Name() { return p1Name; }
    public int getP1TankIndex() { return p1TankIndex; }
    public int getP1Score() { return p1Score; }
    public String getP2Name() { return p2Name; }
    public int getP2TankIndex() { return p2TankIndex; }
    public int getP2Score() { return p2Score; }
}
