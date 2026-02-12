package me.matistan05.minecraftmanhunt.classes;

public class Hunter extends ManhuntPlayer {
    private int compassMode;
    private String whichSpeedrunner;

    public Hunter(String name) {
        super(name);
        this.compassMode = 0;
        this.whichSpeedrunner = "";
    }

    public int getCompassMode() {
        return compassMode;
    }

    public void setCompassMode(int compassMode) {
        this.compassMode = compassMode;
    }

    public String getWhichSpeedrunner() {
        return whichSpeedrunner;
    }

    public void setWhichSpeedrunner(String whichSpeedrunner) {
        this.whichSpeedrunner = whichSpeedrunner;
    }

    private long lastWarpShadows = 0;

    public long getLastWarpShadows() {
        return lastWarpShadows;
    }

    public void setLastWarpShadows(long lastWarpShadows) {
        this.lastWarpShadows = lastWarpShadows;
    }
}
