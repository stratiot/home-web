package eu.daiad.web.model.query;

import java.util.HashMap;
import java.util.Map;

public class AmphiroDataPoint extends DataPoint {

	private Map<EnumMetric, Double> volume = new HashMap<EnumMetric, Double>();

	private Map<EnumMetric, Double> duration = new HashMap<EnumMetric, Double>();

	private Map<EnumMetric, Double> temperature = new HashMap<EnumMetric, Double>();

	private Map<EnumMetric, Double> energy = new HashMap<EnumMetric, Double>();

	private Map<EnumMetric, Double> flow = new HashMap<EnumMetric, Double>();

	public AmphiroDataPoint() {
		this.type = EnumDataPointType.AMPHIRO;
	}

	public AmphiroDataPoint(long timestamp) {
		super(timestamp);
		this.type = EnumDataPointType.AMPHIRO;
	}

	public Map<EnumMetric, Double> getDuration() {
		return duration;
	}

	public Map<EnumMetric, Double> getTemperature() {
		return temperature;
	}

	public Map<EnumMetric, Double> getEnergy() {
		return energy;
	}

	public Map<EnumMetric, Double> getFlow() {
		return flow;
	}

	public Map<EnumMetric, Double> getVolume() {
		return volume;
	}

}
