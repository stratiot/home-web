package eu.daiad.web.controller.api;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.amphiro.AmphiroMeasurementIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroMeasurementTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQueryResult;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.meter.WaterMeterStatus;
import eu.daiad.web.model.meter.WaterMeterStatusQuery;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IAmphiroTimeOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;

/**
 * 
 * Provides actions for searching Amphiro B1 sessions and smart water meter readings.
 *
 */
@RestController("RestSearchController")
public class SearchController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(SearchController.class);

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IAmphiroTimeOrderedRepository amphiroTimeOrderedRepository;

	@Autowired
	private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

	@Autowired
	private IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

	/**
	 * Returns the status of one or more smart water meters.
	 * 
	 * @param query the query.
	 * @return the meter status.
	 */
	@RequestMapping(value = "/api/v1/meter/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse getWaterMeterStatus(@RequestBody WaterMeterStatusQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new WaterMeterStatusQueryResult();
			}

			String[] serials = this.checkMeterOwnership(user.getKey(), query.getDeviceKey());

			WaterMeterStatusQueryResult result = waterMeterMeasurementRepository.getStatus(serials);

			for (WaterMeterStatus status : result.getDevices()) {
				for (int i = 0, count = serials.length; i < count; i++) {
					if (status.getSerial().equals(serials[i])) {
						status.setDeviceKey(query.getDeviceKey()[i]);
						break;
					}
				}
			}

			return result;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Loads smart water meter readings on a given query.
	 * 
	 * @param query the query.
	 * @return the meter readings.
	 */
	@RequestMapping(value = "/api/v1/meter/history", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchWaterMeterMeasurements(@RequestBody WaterMeterMeasurementQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new WaterMeterMeasurementQueryResult();
			}

			String[] serials = this.checkMeterOwnership(user.getKey(), query.getDeviceKey());

			WaterMeterMeasurementQueryResult data = waterMeterMeasurementRepository.searchMeasurements(serials,
							DateTimeZone.forID(user.getTimezone()), query);

			return data;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Loads Amphiro B1 session measurements based on a given query. Amphiro B1 sessions are indexed by time.
	 * 
	 * @param query the query.
	 * @return the measurements.
	 */
	@RequestMapping(value = "/api/v1/device/measurement/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchAmphiroMeasurementsByTime(@RequestBody AmphiroMeasurementTimeIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new AmphiroMeasurementTimeIntervalQueryResult();
			}

			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			AmphiroMeasurementTimeIntervalQueryResult data = amphiroTimeOrderedRepository.searchMeasurements(
							DateTimeZone.forID(user.getTimezone()), query);

			return data;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Loads Amphiro B1 session measurements based on a given query. Amphiro B1 sessions are indexed by id.
	 * 
	 * @param query the query.
	 * @return the measurements.
	 */
	@RequestMapping(value = "/api/v2/device/measurement/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchAmphiroMeasurementsByIndex(@RequestBody AmphiroMeasurementIndexIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new AmphiroMeasurementIndexIntervalQueryResult();
			}

			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			AmphiroMeasurementIndexIntervalQueryResult data = amphiroIndexOrderedRepository.searchMeasurements(
							DateTimeZone.forID(user.getTimezone()), query);

			return data;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Loads Amphiro B1 sessions based on a given query. Amphiro B1 sessions are indexed by time.
	 * 
	 * @param query the query.
	 * @return the measurements.
	 */
	@RequestMapping(value = "/api/v1/device/session/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchAmphiroSessionsWithTimeOrdering(
					@RequestBody AmphiroSessionCollectionTimeIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new AmphiroSessionCollectionTimeIntervalQueryResult();
			}

			String[] names = this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			AmphiroSessionCollectionTimeIntervalQueryResult data = amphiroTimeOrderedRepository.searchSessions(names,
							DateTimeZone.forID(user.getTimezone()), query);

			return data;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Loads Amphiro B1 sessions based on a given query. Amphiro B1 sessions are indexed by id.
	 * 
	 * @param query the query.
	 * @return the measurements.
	 */
	@RequestMapping(value = "/api/v2/device/session/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchAmphiroSessionsWithIndexOrdering(
					@RequestBody AmphiroSessionCollectionIndexIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new AmphiroSessionCollectionIndexIntervalQueryResult();
			}

			String[] names = this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			AmphiroSessionCollectionIndexIntervalQueryResult data = amphiroIndexOrderedRepository.searchSessions(names,
							DateTimeZone.forID(user.getTimezone()), query);

			return data;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Loads an Amphiro B1 session based on a given query. Amphiro B1 session is indexed by time.
	 * 
	 * @param query the query
	 * @return the sessions
	 */
	@RequestMapping(value = "/api/v1/device/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse getAmphiroSessionByTime(@RequestBody AmphiroSessionTimeIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			if (query.getDeviceKey() == null) {
				return new AmphiroSessionTimeIntervalQueryResult();
			}

			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			AmphiroSessionTimeIntervalQueryResult data = amphiroTimeOrderedRepository.getSession(query);

			return data;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Loads an Amphiro B1 session based on a given query. Amphiro B1 session is indexed by id.
	 * 
	 * @param query the query
	 * @return the sessions
	 */
	@RequestMapping(value = "/api/v2/device/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse getAmphiroSessionByIndex(@RequestBody AmphiroSessionIndexIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			if (query.getDeviceKey() == null) {
				return new AmphiroSessionIndexIntervalQueryResult();
			}

			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			AmphiroSessionIndexIntervalQueryResult data = amphiroIndexOrderedRepository.getSession(query);

			return data;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	private String[] checkAmphiroOwnership(UUID userKey, UUID deviceKey) {
		if (deviceKey != null) {
			return this.checkAmphiroOwnership(userKey, new UUID[] { deviceKey });
		}

		return new String[] { null };
	}

	private String[] checkAmphiroOwnership(UUID userKey, UUID[] devices) {
		ArrayList<String> nameList = new ArrayList<String>();

		if (devices != null) {
			for (UUID deviceKey : devices) {
				Device device = this.deviceRepository.getUserDeviceByKey(userKey, deviceKey);

				if ((device == null) || (!device.getType().equals(EnumDeviceType.AMPHIRO))) {
					throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
				}

				nameList.add(((AmphiroDevice) device).getName());
			}
		}

		String[] nameArray = new String[nameList.size()];

		return nameList.toArray(nameArray);
	}

	private String[] checkMeterOwnership(UUID userKey, UUID[] devices) {
		ArrayList<String> serialList = new ArrayList<String>();

		if (devices != null) {
			for (UUID deviceKey : devices) {
				Device device = this.deviceRepository.getUserDeviceByKey(userKey, deviceKey);

				if ((device == null) || (!device.getType().equals(EnumDeviceType.METER))) {
					throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
				}

				serialList.add(((WaterMeterDevice) device).getSerial());
			}
		}

		String[] serialArray = new String[serialList.size()];

		return serialList.toArray(serialArray);
	}

}
