package app.library.indicators;

import app.library.indicators.enums.InputPrice;

// Average True Range
public class ATR extends SMA
{
	public ATR()
	{
		this(14);
	}

	public ATR(int period)
	{
		super(period, InputPrice.TRUE_RANGE);
	}
}
