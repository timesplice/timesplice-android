package com.example.m1032561.pomodoroprototype.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by M1032467 on 1/10/2017.
 */

public class LogModel implements Serializable {
    private String id;
    private String actualDur;
    private String ellapsedDur;
    private String rateDur;
    private String rate;
    private String note;
    private String breakTime;
    private Date created_at;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActualDur() {
        return actualDur;
    }

    public void setActualDur(String actualDur) {
        this.actualDur = actualDur;
    }

    public String getEllapsedDur() {
        return ellapsedDur;
    }

    public void setEllapsedDur(String ellapsedDur) {
        this.ellapsedDur = ellapsedDur;
    }

    public String getRateDur() {
        return rateDur;
    }

    public void setRateDur(String rateDur) {
        this.rateDur = rateDur;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getBreakTime() {
        return breakTime;
    }

    public void setBreakTime(String breakTime) {
        this.breakTime = breakTime;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
