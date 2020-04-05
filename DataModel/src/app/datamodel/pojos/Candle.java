package app.datamodel.pojos;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

public class Candle extends Pojo
{

	protected Instant time;
	protected double open;
	protected double high;
	protected double low;
	protected double close;
	protected double volume;
	
	
	public Candle(Instant time, double open, double high, double low, double close, double volume)
	{
		super(PojoState.STAGED);
		this.time = time;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
	}
	
	public Candle()
	{
		super();
	}
	
	public void setTime(Instant time)
	{
		updateField("time", time);
	}

	public void setOpen(double open)
	{
		updateField("open", open);
	}

	public void setHigh(double high)
	{
		updateField("high", high);
	}

	public void setLow(double low)
	{
		updateField("low", low);
	}

	public void setClose(double close)
	{
		updateField("close", close);
	}

	public void setVolume(double volume)
	{
		updateField("volume", volume);
	}

	public Instant getTime()
	{
		return time;
	}

	public double getOpen()
	{
		return open;
	}

	public double getHigh()
	{
		return high;
	}

	public double getLow()
	{
		return low;
	}

	public double getClose()
	{
		return close;
	}

	public double getVolume()
	{
		return volume;
	}
}
