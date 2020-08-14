package com.sme.multithreading.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Provides POJO message with delay option.
 */
public final class DelayedMessage extends Message
{
    private final int id;
    private final int delay;

    public DelayedMessage(int id, int delay, String message)
    {
        super(message);
        this.id = id;
        this.delay = delay;
    }

    public int getId()
    {
        return id;
    }

    public int getDelay()
    {
        return delay;
    }

    @Override
    public void setMessage(String message)
    {
        throw new UnsupportedOperationException("Immutable object does not allow to change a state");
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
