package net.cruciblesoftware.homingbacon;

public class Position {
    protected double latitude;
    protected double longitude;
    protected double accuracy;
    protected long epochTime;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public long getEpochTime() {
        return epochTime;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public void setEpochTime(long epochTime) {
        this.epochTime = epochTime;
    }

    @Override
    public String toString() {
        return "[lat=" + latitude + ", lon=" + longitude + ", accuracy=" + accuracy + ", time=" + epochTime + "]";
    }
}
