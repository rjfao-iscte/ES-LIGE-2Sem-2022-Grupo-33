/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2022, by David Gilbert and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ------------------
 * MovingAverage.java
 * ------------------
 * (C) Copyright 2003-2022, by David Gilbert.
 *
 * Original Author:  David Gilbert;
 * Contributor(s):   Benoit Xhenseval;
 *
 */

package org.jfree.data.time;

import org.jfree.chart.internal.Args;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A utility class for calculating moving averages of time series data.
 */
public class MovingAverage {

    /**
     * Creates a new {@link TimeSeriesCollection} containing a moving average
     * series for each series in the source collection.
     *
     * @param source  the source collection.
     * @param suffix  the suffix added to each source series name to create the
     *                corresponding moving average series name.
     * @param periodCount  the number of periods in the moving average
     *                     calculation.
     * @param skip  the number of initial periods to skip.
     *
     * @return A collection of moving average time series.
     */
    public static TimeSeriesCollection createMovingAverage(
            TimeSeriesCollection source, String suffix, int periodCount,
            int skip) {

        Args.nullNotPermitted(source, "source");
        if (periodCount < 1) {
            throw new IllegalArgumentException("periodCount must be greater "
                    + "than or equal to 1.");
        }

        TimeSeriesCollection result = new TimeSeriesCollection();
        for (int i = 0; i < source.getSeriesCount(); i++) {
            TimeSeries maSeries = maSeries(source, suffix, periodCount, skip, result, i);
        }
        return result;

    }

	private static TimeSeries maSeries(TimeSeriesCollection source, String suffix, int periodCount, int skip,
			TimeSeriesCollection result, int i) {
		TimeSeries sourceSeries = source.getSeries(i);
		TimeSeries maSeries = createMovingAverage(sourceSeries, sourceSeries.getKey() + suffix, periodCount, skip);
		result.addSeries(maSeries);
		return maSeries;
	}

    /**
     * Creates a new {@link TimeSeries} containing moving average values for
     * the given series.  If the series is empty (contains zero items), the
     * result is an empty series.
     *
     * @param source  the source series.
     * @param name  the series key ({@code null} not permitted).
     * @param periodCount  the number of periods used in the average
     *                     calculation.
     * @param skip  the number of initial periods to skip.
     *
     * @param <S>  the type for the series keys.
     * 
     * @return The moving average series.
     */
    public static <S extends Comparable<S>> TimeSeries<S> createMovingAverage(
            TimeSeries<S> source, S name, int periodCount, int skip) {

        Args.nullNotPermitted(source, "source");
        if (periodCount < 1) {
            throw new IllegalArgumentException("periodCount must be greater " 
                    + "than or equal to 1.");
        }

        TimeSeries<S> result = new TimeSeries<>(name);
        if (source.getItemCount() > 0) {

            // if the initial averaging period is to be excluded, then
            // calculate the index of the
            // first data item to have an average calculated...
            long firstSerial = source.getTimePeriod(0).getSerialIndex() + skip;

            for (int i = source.getItemCount() - 1; i >= 0; i--) {

                // get the current data item...
                RegularTimePeriod period = source.getTimePeriod(i);
                long serial = period.getSerialIndex();

                if (serial >= firstSerial) {
                    int n = n(source, periodCount, i, period);
					double sum = sum1(source, periodCount, i, period);
					if (n > 0) {
                        result.add(period, sum / n);
                    }
                    else {
                        result.add(period, null);
                    }
                }

            }
        }
        return result;
    }

	private static <S extends Comparable<S>> double sum1(TimeSeries<S> source, int periodCount, int i,
			RegularTimePeriod period) {
		double sum = 0.0;
		long serialLimit = period.getSerialIndex() - periodCount;
		int offset = 0;
		boolean finished = false;
		while ((offset < periodCount) && (!finished)) {
			finished = finished_(source, i, serialLimit, offset, finished);
			if ((i - offset) >= 0) {
				TimeSeriesDataItem item = source.getRawDataItem(i - offset);
				RegularTimePeriod p = item.getPeriod();
				Number v = item.getValue();
				long currentIndex = p.getSerialIndex();
				if (currentIndex > serialLimit) {
					if (v != null) {
						sum = sum + v.doubleValue();
					}
				} else {
				}
			}
			offset = offset + 1;
		}
		return sum;
	}

	private static <S extends Comparable<S>> boolean finished_(TimeSeries<S> source, int i, long serialLimit,
			int offset, boolean finished) {
		if ((i - offset) >= 0) {
			TimeSeriesDataItem item = source.getRawDataItem(i - offset);
			RegularTimePeriod p = item.getPeriod();
			long currentIndex = p.getSerialIndex();
			if (currentIndex > serialLimit) {
			} else {
				finished = true;
			}
		}
		return finished;
	}

	private static <S extends Comparable<S>> int n(TimeSeries<S> source, int periodCount, int i,
			RegularTimePeriod period) {
		int n = 0;
		long serialLimit = period.getSerialIndex() - periodCount;
		int offset = 0;
		boolean finished = false;
		while ((offset < periodCount) && (!finished)) {
			finished = finished_2(source, i, serialLimit, offset, finished);
			if ((i - offset) >= 0) {
				TimeSeriesDataItem item = source.getRawDataItem(i - offset);
				RegularTimePeriod p = item.getPeriod();
				Number v = item.getValue();
				long currentIndex = p.getSerialIndex();
				if (currentIndex > serialLimit) {
					if (v != null) {
						n = n + 1;
					}
				} else {
				}
			}
			offset = offset + 1;
		}
		return n;
	}

	private static <S extends Comparable<S>> boolean finished_2(TimeSeries<S> source, int i, long serialLimit,
			int offset, boolean finished) {
		if ((i - offset) >= 0) {
			TimeSeriesDataItem item = source.getRawDataItem(i - offset);
			RegularTimePeriod p = item.getPeriod();
			long currentIndex = p.getSerialIndex();
			if (currentIndex > serialLimit) {
			} else {
				finished = true;
			}
		}
		return finished;
	}

    /**
     * Creates a new {@link TimeSeries} containing moving average values for
     * the given series, calculated by number of points (irrespective of the
     * 'age' of those points).  If the series is empty (contains zero items),
     * the result is an empty series.
     * <p>
     * Developed by Benoit Xhenseval (www.ObjectLab.co.uk).
     *
     * @param source  the source series.
     * @param name  the series key ({@code null} not permitted).
     * @param pointCount  the number of POINTS used in the average calculation
     *                    (not periods!)
     * 
     * @param <S>  the type for the series keys.
     *
     * @return The moving average series.
     */
    public static <S extends Comparable<S>> TimeSeries<S> createPointMovingAverage(
            TimeSeries<S> source, S name, int pointCount) {

        Args.nullNotPermitted(source, "source");
        if (pointCount < 2) {
            throw new IllegalArgumentException("periodCount must be greater " 
                    + "than or equal to 2.");
        }

        TimeSeries<S> result = new TimeSeries<>(name);
        double rollingSumForPeriod = 0.0;
        for (int i = 0; i < source.getItemCount(); i++) {
            // get the current data item...
            TimeSeriesDataItem current = source.getRawDataItem(i);
            RegularTimePeriod period = current.getPeriod();
            // FIXME: what if value is null on next line?
            rollingSumForPeriod += current.getValue().doubleValue();

            if (i > pointCount - 1) {
                // remove the point i-periodCount out of the rolling sum.
                TimeSeriesDataItem startOfMovingAvg = source.getRawDataItem(
                        i - pointCount);
                rollingSumForPeriod -= startOfMovingAvg.getValue()
                        .doubleValue();
                result.add(period, rollingSumForPeriod / pointCount);
            }
            else if (i == pointCount - 1) {
                result.add(period, rollingSumForPeriod / pointCount);
            }
        }
        return result;
    }

    /**
     * Creates a new {@link XYDataset} containing the moving averages of each
     * series in the {@code source} dataset.
     *
     * @param source  the source dataset.
     * @param suffix  the string to append to source series names to create
     *                target series names.
     * @param period  the averaging period.
     * @param skip  the length of the initial skip period.
     *
     * @return The dataset.
     */
    public static XYDataset createMovingAverage(XYDataset source, String suffix,
            long period, long skip) {

        return createMovingAverage(source, suffix, (double) period,
                (double) skip);

    }


    /**
     * Creates a new {@link XYDataset} containing the moving averages of each
     * series in the {@code source} dataset.
     *
     * @param source  the source dataset.
     * @param suffix  the string to append to source series names to create
     *                target series names.
     * @param period  the averaging period.
     * @param skip  the length of the initial skip period.
     *
     * @return The dataset.
     */
    public static XYDataset createMovingAverage(XYDataset source,
            String suffix, double period, double skip) {

        Args.nullNotPermitted(source, "source");
        XYSeriesCollection result = new XYSeriesCollection();
        for (int i = 0; i < source.getSeriesCount(); i++) {
            XYSeries s = createMovingAverage(source, i, source.getSeriesKey(i)
                    + suffix, period, skip);
            result.addSeries(s);
        }
        return result;
    }

    /**
     * Creates a new {@link XYSeries} containing the moving averages of one
     * series in the {@code source} dataset.
     *
     * @param source  the source dataset.
     * @param series  the series index (zero based).
     * @param name  the name for the new series.
     * @param period  the averaging period.
     * @param skip  the length of the initial skip period.
     *
     * @return The dataset.
     */
    public static XYSeries createMovingAverage(XYDataset source,
            int series, String name, double period, double skip) {

        Args.nullNotPermitted(source, "source");
        if (period < Double.MIN_VALUE) {
            throw new IllegalArgumentException("period must be positive.");
        }
        if (skip < 0.0) {
            throw new IllegalArgumentException("skip must be >= 0.0.");
        }

        XYSeries result = new XYSeries(name);

        if (source.getItemCount(series) > 0) {

            // if the initial averaging period is to be excluded, then
            // calculate the lowest x-value to have an average calculated...
            double first = source.getXValue(series, 0) + skip;

            for (int i = source.getItemCount(series) - 1; i >= 0; i--) {

                // get the current data item...
                double x = source.getXValue(series, i);

                if (x >= first) {
                    int n = n2(source, series, period, i, x);
					double sum = sum(source, series, period, i, x);
					if (n > 0) {
                        result.add(x, sum / n);
                    }
                    else {
                        result.add(x, null);
                    }
                }

            }
        }

        return result;

    }

	private static int n2(XYDataset source, int series, double period, int i, double x) {
		int n = 0;
		double limit = x - period;
		int offset = 0;
		boolean finished = false;
		while (!finished) {
			finished = finished(source, series, i, limit, offset, finished);
			if ((i - offset) >= 0) {
				double xx = source.getXValue(series, i - offset);
				Number yy = source.getY(series, i - offset);
				if (xx > limit) {
					if (yy != null) {
						n = n + 1;
					}
				} else {
				}
			} else {
			}
			offset = offset + 1;
		}
		return n;
	}

	private static double sum(XYDataset source, int series, double period, int i, double x) {
		double sum = 0.0;
		double limit = x - period;
		int offset = 0;
		boolean finished = false;
		while (!finished) {
			finished = finished(source, series, i, limit, offset, finished);
			if ((i - offset) >= 0) {
				double xx = source.getXValue(series, i - offset);
				Number yy = source.getY(series, i - offset);
				if (xx > limit) {
					if (yy != null) {
						sum = sum + yy.doubleValue();
					}
				} else {
				}
			} else {
			}
			offset = offset + 1;
		}
		return sum;
	}

	private static boolean finished(XYDataset source, int series, int i, double limit, int offset, boolean finished) {
		if ((i - offset) >= 0) {
			double xx = source.getXValue(series, i - offset);
			if (xx > limit) {
			} else {
				finished = true;
			}
		} else {
			finished = true;
		}
		return finished;
	}

}
