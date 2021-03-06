package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

public class GroupDataSeries {

	private String label;

	private int population;

	ArrayList<DataPoint> points = new ArrayList<DataPoint>();

	public GroupDataSeries(String label, int population) {
		this.label = label;
		this.population = population;
	}

	public String getLabel() {
		return label;
	}

	public ArrayList<DataPoint> getPoints() {
		return points;
	}

	private DataPoint createDataPoint(List<EnumMetric> metrics, DataPoint.EnumDataPointType type, Long timestamp) {
		switch (type) {
			case METER:
				MeterDataPoint mp = (timestamp == null ? new MeterDataPoint() : new MeterDataPoint(timestamp));

				for (EnumMetric m : metrics) {
					if (m == EnumMetric.MIN) {
						mp.getVolume().put(m, Double.MAX_VALUE);
					} else {
						mp.getVolume().put(m, 0.0);
					}
				}

				this.points.add(mp);

				return mp;
			case AMPHIRO:
				AmphiroDataPoint ap = (timestamp == null ? new AmphiroDataPoint() : new AmphiroDataPoint(timestamp));

				for (EnumMetric m : metrics) {
					if (m == EnumMetric.MIN) {
						ap.getVolume().put(m, Double.MAX_VALUE);
						ap.getEnergy().put(m, Double.MAX_VALUE);
						ap.getTemperature().put(m, Double.MAX_VALUE);
						ap.getDuration().put(m, Double.MAX_VALUE);
						ap.getFlow().put(m, Double.MAX_VALUE);
					} else {
						ap.getVolume().put(m, 0.0);
						ap.getEnergy().put(m, 0.0);
						ap.getTemperature().put(m, 0.0);
						ap.getDuration().put(m, 0.0);
						ap.getFlow().put(m, 0.0);
					}
				}

				this.points.add(ap);

				return ap;
			default:
				throw new IllegalArgumentException("Data point type is not supported.");

		}
	}

	private DataPoint getDataPoint(EnumTimeAggregation granularity, long timestamp, List<EnumMetric> metrics,
					DataPoint.EnumDataPointType type, DateTimeZone timezone) {
		DateTime date = new DateTime(timestamp, timezone);

		switch (granularity) {
			case HOUR:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(),
								0, 0, timezone);
				break;
			case DAY:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0, 0, timezone);
				break;
			case WEEK:
				DateTime sunday = date.withDayOfWeek(DateTimeConstants.SUNDAY);

				date = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 0, 0, 0,
								timezone);
				break;
			case MONTH:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), 1, 0, 0, 0, timezone);
				break;
			case YEAR:
				date = new DateTime(date.getYear(), 1, 1, 0, 0, 0, timezone);
				break;
			case ALL:
				break;
			default:
				throw new IllegalArgumentException("Granularity level not supported.");
		}

		DataPoint p = null;

		if (granularity == EnumTimeAggregation.ALL) {
			if (this.points.size() == 0) {
				p = createDataPoint(metrics, type, null);
			} else {
				p = this.points.get(0);
			}
		} else {
			timestamp = date.getMillis();

			for (int i = 0, count = this.points.size(); i < count; i++) {
				if (timestamp == this.points.get(i).getTimestamp()) {
					p = this.points.get(i);

					break;
				}
			}

			if (p == null) {
				p = createDataPoint(metrics, type, timestamp);
			}
		}
		return p;
	}

	public void addAmhiroDataPoint(EnumTimeAggregation granularity, long timestamp, double volume, double energy,
					double duration, double temperature, double flow, List<EnumMetric> metrics, DateTimeZone timezone) {
		AmphiroDataPoint point = (AmphiroDataPoint) this.getDataPoint(granularity, timestamp, metrics,
						DataPoint.EnumDataPointType.AMPHIRO, timezone);

		boolean avg = false;
		for (EnumMetric m : metrics) {
			switch (m) {
				case COUNT:
					point.getVolume().put(m, point.getVolume().get(m) + 1);
					point.getEnergy().put(m, point.getEnergy().get(m) + 1);
					point.getDuration().put(m, point.getDuration().get(m) + 1);
					point.getTemperature().put(m, point.getTemperature().get(m) + 1);
					point.getFlow().put(m, point.getFlow().get(m) + 1);
					break;
				case SUM:
					point.getVolume().put(m, point.getVolume().get(m) + volume);
					point.getEnergy().put(m, point.getEnergy().get(m) + energy);
					point.getDuration().put(m, point.getDuration().get(m) + duration);
					point.getTemperature().put(m, point.getTemperature().get(m) + temperature);
					point.getFlow().put(m, point.getFlow().get(m) + flow);
					break;
				case MIN:
					if (point.getVolume().get(m) > volume) {
						point.getVolume().put(m, volume);
					}
					if (point.getEnergy().get(m) > energy) {
						point.getEnergy().put(m, energy);
					}
					if (point.getDuration().get(m) > duration) {
						point.getDuration().put(m, duration);
					}
					if (point.getTemperature().get(m) > temperature) {
						point.getTemperature().put(m, temperature);
					}
					if (point.getFlow().get(m) > flow) {
						point.getFlow().put(m, flow);
					}
					break;
				case MAX:
					if (point.getVolume().get(m) < volume) {
						point.getVolume().put(m, volume);
					}
					if (point.getEnergy().get(m) < energy) {
						point.getEnergy().put(m, energy);
					}
					if (point.getDuration().get(m) < duration) {
						point.getDuration().put(m, duration);
					}
					if (point.getTemperature().get(m) < temperature) {
						point.getTemperature().put(m, temperature);
					}
					if (point.getFlow().get(m) < flow) {
						point.getFlow().put(m, flow);
					}
					break;
				case AVERAGE:
					avg = true;
				default:
					// Ignore
			}
		}
		if (avg) {
			double count = point.getVolume().get(EnumMetric.COUNT);
			if (count == 0) {
				point.getVolume().put(EnumMetric.AVERAGE, 0.0);
				point.getEnergy().put(EnumMetric.AVERAGE, 0.0);
				point.getDuration().put(EnumMetric.AVERAGE, 0.0);
				point.getTemperature().put(EnumMetric.AVERAGE, 0.0);
				point.getFlow().put(EnumMetric.AVERAGE, 0.0);
			} else {
				point.getVolume().put(EnumMetric.AVERAGE, point.getVolume().get(EnumMetric.SUM) / count);
				point.getEnergy().put(EnumMetric.AVERAGE, point.getEnergy().get(EnumMetric.SUM) / count);
				point.getDuration().put(EnumMetric.AVERAGE, point.getDuration().get(EnumMetric.SUM) / count);
				point.getTemperature().put(EnumMetric.AVERAGE, point.getTemperature().get(EnumMetric.SUM) / count);
				point.getFlow().put(EnumMetric.AVERAGE, point.getFlow().get(EnumMetric.SUM) / count);
			}
		}
	}

	public void addMeterRankingDataPoint(EnumTimeAggregation granularity, UUID key, String label, long timestamp,
					double difference, double volume, List<EnumMetric> metrics, DateTimeZone timezone) {
		MeterUserDataPoint point = (MeterUserDataPoint) this.getUserDataPoint(granularity, key, label, timestamp,
						metrics, DataPoint.EnumDataPointType.METER, timezone);

		for (EnumMetric m : metrics) {
			switch (m) {
				case COUNT:
					point.getVolume().put(m, point.getVolume().get(m) + 1);
					break;
				case SUM:
					point.getVolume().put(m, point.getVolume().get(m) + difference);
					break;
				case MIN:
					if (point.getVolume().get(m) > volume) {
						point.getVolume().put(m, volume);
					}
					break;
				case MAX:
					if (point.getVolume().get(m) < volume) {
						point.getVolume().put(m, volume);
					}
					break;
				default:
					// Ignore
			}
		}
	}

	public void addAmphiroRankingDataPoint(EnumTimeAggregation granularity, UUID key, String label, long timestamp,
					double volume, double energy, double duration, double temperature, double flow,
					List<EnumMetric> metrics, DateTimeZone timezone) {
		AmphiroUserDataPoint point = (AmphiroUserDataPoint) this.getUserDataPoint(granularity, key, label, timestamp,
						metrics, DataPoint.EnumDataPointType.AMPHIRO, timezone);

		boolean avg = false;
		for (EnumMetric m : metrics) {
			switch (m) {
				case COUNT:
					point.getVolume().put(m, point.getVolume().get(m) + 1);
					point.getEnergy().put(m, point.getEnergy().get(m) + 1);
					point.getDuration().put(m, point.getDuration().get(m) + 1);
					point.getTemperature().put(m, point.getTemperature().get(m) + 1);
					point.getFlow().put(m, point.getFlow().get(m) + 1);
					break;
				case SUM:
					point.getVolume().put(m, point.getVolume().get(m) + volume);
					point.getEnergy().put(m, point.getEnergy().get(m) + energy);
					point.getDuration().put(m, point.getDuration().get(m) + duration);
					point.getTemperature().put(m, point.getTemperature().get(m) + temperature);
					point.getFlow().put(m, point.getFlow().get(m) + flow);
					break;
				case MIN:
					if (point.getVolume().get(m) > volume) {
						point.getVolume().put(m, volume);
					}
					if (point.getEnergy().get(m) > energy) {
						point.getEnergy().put(m, energy);
					}
					if (point.getDuration().get(m) > duration) {
						point.getDuration().put(m, duration);
					}
					if (point.getTemperature().get(m) > temperature) {
						point.getTemperature().put(m, temperature);
					}
					if (point.getFlow().get(m) > flow) {
						point.getFlow().put(m, flow);
					}
					break;
				case MAX:
					if (point.getVolume().get(m) < volume) {
						point.getVolume().put(m, volume);
					}
					if (point.getEnergy().get(m) < energy) {
						point.getEnergy().put(m, energy);
					}
					if (point.getDuration().get(m) < duration) {
						point.getDuration().put(m, duration);
					}
					if (point.getTemperature().get(m) < temperature) {
						point.getTemperature().put(m, temperature);
					}
					if (point.getFlow().get(m) < flow) {
						point.getFlow().put(m, flow);
					}
					break;
				case AVERAGE:
					avg = true;
				default:
					// Ignore
			}
		}
		if (avg) {
			double count = point.getVolume().get(EnumMetric.COUNT);
			if (count == 0) {
				point.getVolume().put(EnumMetric.AVERAGE, 0.0);
				point.getEnergy().put(EnumMetric.AVERAGE, 0.0);
				point.getDuration().put(EnumMetric.AVERAGE, 0.0);
				point.getTemperature().put(EnumMetric.AVERAGE, 0.0);
				point.getFlow().put(EnumMetric.AVERAGE, 0.0);
			} else {
				point.getVolume().put(EnumMetric.AVERAGE, point.getVolume().get(EnumMetric.SUM) / count);
				point.getEnergy().put(EnumMetric.AVERAGE, point.getEnergy().get(EnumMetric.SUM) / count);
				point.getDuration().put(EnumMetric.AVERAGE, point.getDuration().get(EnumMetric.SUM) / count);
				point.getTemperature().put(EnumMetric.AVERAGE, point.getTemperature().get(EnumMetric.SUM) / count);
				point.getFlow().put(EnumMetric.AVERAGE, point.getFlow().get(EnumMetric.SUM) / count);
			}
		}
	}

	private RankingDataPoint getRankingDataPoint(EnumTimeAggregation granularity, long timestamp, DateTimeZone timezone) {
		DateTime date = new DateTime(timestamp, timezone);

		switch (granularity) {
			case HOUR:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(),
								0, 0, timezone);
				break;
			case DAY:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0, 0, timezone);
				break;
			case WEEK:
				DateTime sunday = date.withDayOfWeek(DateTimeConstants.SUNDAY);

				date = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 0, 0, 0,
								timezone);
				break;
			case MONTH:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), 1, 0, 0, 0, timezone);
				break;
			case YEAR:
				date = new DateTime(date.getYear(), 1, 1, 0, 0, 0, timezone);
				break;
			case ALL:
				break;
			default:
				throw new IllegalArgumentException("Granularity level not supported.");
		}

		if (granularity == EnumTimeAggregation.ALL) {
			if (this.points.size() == 0) {
				this.points.add(new RankingDataPoint());
			}

			return (RankingDataPoint) this.points.get(0);
		} else {
			timestamp = date.getMillis();

			for (int i = 0, count = this.points.size(); i < count; i++) {
				if (timestamp == this.points.get(i).getTimestamp()) {
					return (RankingDataPoint) this.points.get(i);
				}
			}

			RankingDataPoint point = new RankingDataPoint(timestamp);
			this.points.add(point);

			return point;
		}
	}

	private UserDataPoint getUserDataPoint(EnumTimeAggregation granularity, UUID key, String label, long timestamp,
					List<EnumMetric> metrics, DataPoint.EnumDataPointType type, DateTimeZone timezone) {
		RankingDataPoint ranking = this.getRankingDataPoint(granularity, timestamp, timezone);

		for (UserDataPoint point : ranking.getUsers()) {
			if (point.getKey().equals(key)) {
				return point;
			}
		}

		switch (type) {
			case METER:
				MeterUserDataPoint meterPoint = new MeterUserDataPoint(key, label);
				ranking.getUsers().add(meterPoint);

				for (EnumMetric m : metrics) {
					if (m == EnumMetric.MIN) {
						meterPoint.getVolume().put(m, Double.MAX_VALUE);
					} else {
						meterPoint.getVolume().put(m, 0.0);
					}

				}

				return meterPoint;
			case AMPHIRO:
				AmphiroUserDataPoint amphiroPoint = new AmphiroUserDataPoint(key, label);
				ranking.getUsers().add(amphiroPoint);

				for (EnumMetric m : metrics) {
					if (m == EnumMetric.MIN) {
						amphiroPoint.getVolume().put(m, Double.MAX_VALUE);
						amphiroPoint.getEnergy().put(m, Double.MAX_VALUE);
						amphiroPoint.getTemperature().put(m, Double.MAX_VALUE);
						amphiroPoint.getDuration().put(m, Double.MAX_VALUE);
						amphiroPoint.getFlow().put(m, Double.MAX_VALUE);
					} else {
						amphiroPoint.getVolume().put(m, 0.0);
						amphiroPoint.getEnergy().put(m, 0.0);
						amphiroPoint.getTemperature().put(m, 0.0);
						amphiroPoint.getDuration().put(m, 0.0);
						amphiroPoint.getFlow().put(m, 0.0);
					}
				}

				return amphiroPoint;
			default:
				throw new IllegalArgumentException("Data point type is not supported.");
		}
	}

	public int getPopulation() {
		return population;
	}

	public void setPoints(ArrayList<DataPoint> points) {
		this.points = points;
	}

}
