package sqlite.model;

/**
 * PipSession.java
 * Model for PipSession table
 * Created by DED8IRD on 11/7/2016.
 */

public class PipSession {

    private int id;
    private String participant;
    private double GSR_raw_val;
    private String current_trend;
    private double accum_trend;

    // constructors
    public PipSession() {
    }

    public PipSession(String participant) {
        this.participant = participant;
    }

    public PipSession(int id, String participant) {
        this.id = id;
        this.participant = participant;
    }

    public PipSession(String participant, double GSR_raw_val, String current_trend, double accum_trend) {
        this.participant = participant;
        this.GSR_raw_val = GSR_raw_val;
        this.current_trend = current_trend;
        this.accum_trend = accum_trend;
    }

    // setters
    public void setId(int id) {
        this.id = id;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public void setGSR(int GSR) {
        this.GSR_raw_val = GSR;
    }

    public void setCurrentTrend(String current_trend){
        this.current_trend = current_trend;
    }

    public void setAccumTrend(double accum_trend){
        this.accum_trend = accum_trend;
    }

    // getters
    public long getId() {
        return this.id;
    }

    public String getParticipant() {
        return this.participant;
    }

    public double getGSR() {
        return this.GSR_raw_val;
    }

    public String getCurrentTrend() {
        return this.current_trend;
    }

    public double getAccumTrend() {
        return this.accum_trend;
    }
}