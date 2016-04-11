package eu.daiad.web.model.query;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class TimeFilter {

	@JsonDeserialize(using = EnumTemporalFilterType.Deserializer.class)
	private EnumTemporalFilterType type = EnumTemporalFilterType.ABSOLUTE;

	@JsonDeserialize(using = EnumTimeAggregation.Deserializer.class)
	private EnumTimeAggregation graunlarity = EnumTimeAggregation.ALL;

	private long start;

	private Long end;

	private Integer duration;

	@JsonDeserialize(using = EnumTimeUnit.Deserializer.class)
	private EnumTimeUnit durationTimeUnit = EnumTimeUnit.HOUR;

	public TimeFilter() {
		this.type = EnumTemporalFilterType.ABSOLUTE;
		this.graunlarity = EnumTimeAggregation.HOUR;

		this.start = (new DateTime()).getMillis();
		this.end = this.start;
	}

	public TimeFilter(long start, long end) {
		this.type = EnumTemporalFilterType.ABSOLUTE;
		this.graunlarity = EnumTimeAggregation.HOUR;

		this.start = start;
		this.end = end;
	}

	public TimeFilter(DateTime start, DateTime end) {
		this.type = EnumTemporalFilterType.ABSOLUTE;
		this.graunlarity = EnumTimeAggregation.HOUR;

		this.start = start.getMillis();
		this.end = end.getMillis();
	}

	public TimeFilter(long start, long end, EnumTimeAggregation graunlarity) {
		this.type = EnumTemporalFilterType.ABSOLUTE;
		this.graunlarity = graunlarity;

		this.start = start;
		this.end = end;
	}

	public TimeFilter(DateTime start, DateTime end, EnumTimeAggregation graunlarity) {
		this.type = EnumTemporalFilterType.ABSOLUTE;
		this.graunlarity = graunlarity;

		this.start = start.getMillis();
		this.end = end.getMillis();
	}

	public TimeFilter(long start, int duration) {
		this.type = EnumTemporalFilterType.SLIDING;
		this.graunlarity = EnumTimeAggregation.HOUR;

		this.start = start;
		this.duration = duration;
		this.durationTimeUnit = EnumTimeUnit.HOUR;
	}

	public TimeFilter(long start, int duration, EnumTimeUnit durationTimeUnit) {
		this.type = EnumTemporalFilterType.SLIDING;
		this.graunlarity = EnumTimeAggregation.HOUR;

		this.start = start;
		this.duration = duration;
		this.durationTimeUnit = durationTimeUnit;
	}

	public TimeFilter(long start, int duration, EnumTimeUnit durationTimeUnit, EnumTimeAggregation granularity) {
		this.type = EnumTemporalFilterType.SLIDING;
		this.graunlarity = granularity;

		this.start = start;
		this.duration = duration;
		this.durationTimeUnit = durationTimeUnit;
	}

	public TimeFilter(DateTime start, int duration) {
		this.type = EnumTemporalFilterType.SLIDING;
		this.graunlarity = EnumTimeAggregation.HOUR;

		this.start = start.getMillis();
		this.duration = duration;
		this.durationTimeUnit = EnumTimeUnit.HOUR;
	}

	public TimeFilter(DateTime start, int duration, EnumTimeUnit durationTimeUnit) {
		this.type = EnumTemporalFilterType.SLIDING;
		this.graunlarity = EnumTimeAggregation.HOUR;

		this.start = start.getMillis();
		this.duration = duration;
		this.durationTimeUnit = durationTimeUnit;
	}

	public TimeFilter(DateTime start, int duration, EnumTimeUnit durationTimeUnit, EnumTimeAggregation granularity) {
		this.type = EnumTemporalFilterType.SLIDING;
		this.graunlarity = granularity;

		this.start = start.getMillis();
		this.duration = duration;
		this.durationTimeUnit = durationTimeUnit;
	}

	public EnumTemporalFilterType getType() {
		return type;
	}

	public EnumTimeAggregation getGraunlarity() {
		return graunlarity;
	}

	public long getStart() {
		return start;
	}

	public Long getEnd() {
		return end;
	}

	public Integer getDuration() {
		return duration;
	}

	public EnumTimeUnit getDurationTimeUnit() {
		return durationTimeUnit;
	}

	@Override
	public String toString() {
		return "TimeFilter [type=" + type + ", graunlarity=" + graunlarity + ", start=" + start + ", end=" + end
						+ ", duration=" + duration + ", durationTimeUnit=" + durationTimeUnit + "]";
	}

}
