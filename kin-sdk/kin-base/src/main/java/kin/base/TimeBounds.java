package kin.base;

import kin.base.xdr.Uint64;

/**
 * <p>TimeBounds represents the time interval that a transaction is valid.</p>
 * @see Transaction
 */
final public class TimeBounds {
	final private long mMinTime;
	final private long mMaxTime;

	/**
	 * @param minTime 64bit Unix timestamp
	 * @param maxTime 64bit Unix timestamp
	 */
	public TimeBounds(long minTime, long maxTime) {
		if(minTime >= maxTime) {
			throw new IllegalArgumentException("minTime must be >= maxTime");
		}

		mMinTime = minTime;
		mMaxTime = maxTime;
	}

	public long getMinTime() {
		return mMinTime;
	}

	public long getMaxTime() {
		return mMaxTime;
	}

	public static TimeBounds fromXdr(kin.base.xdr.TimeBounds timeBounds) {
		if (timeBounds == null) {
			return null;
		}

		return new TimeBounds(
				timeBounds.getMinTime().getUint64(),
				timeBounds.getMaxTime().getUint64()
		);
	}

	public kin.base.xdr.TimeBounds toXdr() {
		kin.base.xdr.TimeBounds timeBounds = new kin.base.xdr.TimeBounds();
		Uint64 minTime = new Uint64();
		Uint64 maxTime = new Uint64();
		minTime.setUint64(mMinTime);
		maxTime.setUint64(mMaxTime);
		timeBounds.setMinTime(minTime);
		timeBounds.setMaxTime(maxTime);
		return timeBounds;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TimeBounds that = (TimeBounds) o;

		if (mMinTime != that.mMinTime) return false;
		return mMaxTime == that.mMaxTime;
	}
}
