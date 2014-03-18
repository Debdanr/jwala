package com.siemens.cto.aem.domain.model.audit;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class AuditDateTime implements Serializable {

    public static AuditDateTime now() {
        return new AuditDateTime(new Date());
    }

    private static final long serialVersionUID = 1L;

    private final Date date;

    public AuditDateTime(final Date theDateTime) {
        this.date = theDateTime;
    }

    public Date getDate() {
        return date;
    }

    public Calendar getCalendar() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AuditDateTime that = (AuditDateTime) o;

        if (date != null ? !date.equals(that.date) : that.date != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return date != null ? date.hashCode() : 0;
    }
}
