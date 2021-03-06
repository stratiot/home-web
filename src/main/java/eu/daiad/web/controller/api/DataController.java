package eu.daiad.web.controller.api;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.DeviceMeasurementCollection;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IAmphiroTimeOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;
import eu.daiad.web.service.IDataService;

/**
 * 
 * Provides actions for storing Amphiro B1 data to the server and querying stored data.
 *
 */
@RestController("RestDataController")
public class DataController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(DataController.class);

	@Value("${tmp.folder}")
	private String temporaryPath;

	@Autowired
	private IAmphiroTimeOrderedRepository amphiroTimeOrderedRepository;

	@Autowired
	private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

	@Autowired
	private IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IDataService dataService;

	/**
	 * General purpose method for querying data using a set of filtering criteria. Depending on the given
	 * criteria, more than one data series may be returned.
	 * 
	 * @param data the query.
	 * @return the data series.
	 */
	@RequestMapping(value = "/api/v1/data/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse query(@RequestBody DataQueryRequest data) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(data.getCredentials(), EnumRole.ROLE_ADMIN);

			// Set defaults if needed
			DataQuery query = data.getQuery();
			if (query != null) {
				// Initialize time zone
				if (StringUtils.isBlank(query.getTimezone())) {
					query.setTimezone(user.getTimezone());
				}
			}

			return dataService.execute(query);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Stores Amphiro B1 session and measurement data. Sessions are index by time.
	 * 
	 * @param data the data to store.
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/api/v1/data/store", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse storeUsingAmphiroTimeOrdering(@RequestBody DeviceMeasurementCollection data) {
		RestResponse response = new RestResponse();

		AuthenticatedUser authenticatedUser = null;
		Device device = null;

		boolean success = true;

		try {
			switch (data.getType()) {
				case AMPHIRO:
					if (data instanceof AmphiroMeasurementCollection) {
						authenticatedUser = this.authenticate(data.getCredentials(), EnumRole.ROLE_USER);

						device = this.deviceRepository.getUserDeviceByKey(authenticatedUser.getKey(),
										data.getDeviceKey());

						if (device == null) {
							throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key",
											data.getDeviceKey().toString());
						}

						if (!device.getType().equals(EnumDeviceType.AMPHIRO)) {
							throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type",
											data.getType().toString());
						}

						amphiroTimeOrderedRepository.storeData(((AuthenticatedUser) authenticatedUser).getKey(),
										(AmphiroMeasurementCollection) data);
					}
					break;
				case METER:
					if (data instanceof WaterMeterMeasurementCollection) {
						authenticatedUser = this.authenticate(data.getCredentials(), EnumRole.ROLE_ADMIN);

						device = this.deviceRepository.getDeviceByKey(data.getDeviceKey());

						if (device == null) {
							throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key",
											data.getDeviceKey().toString());
						}

						if (!device.getType().equals(EnumDeviceType.METER)) {
							throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type",
											data.getType().toString());
						}

						waterMeterMeasurementRepository.storeData(((WaterMeterDevice) device).getSerial(),
										(WaterMeterMeasurementCollection) data);
					}
					break;
				default:
					break;
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));

			success = false;
		} finally {
			logDataUploadSession(authenticatedUser, device, success);
		}

		return response;
	}

	/**
	 * Stores Amphiro B1 session and measurement data. Sessions are index by id.
	 * 
	 * @param data the data to store
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/api/v2/data/store", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse storeUsingAmphiroIndexOredering(@RequestBody DeviceMeasurementCollection data) {
		RestResponse response = new RestResponse();

		AuthenticatedUser authenticatedUser = null;
		Device device = null;

		boolean success = true;

		try {
			switch (data.getType()) {
				case AMPHIRO:
					if (data instanceof AmphiroMeasurementCollection) {
						authenticatedUser = this.authenticate(data.getCredentials(), EnumRole.ROLE_USER);

						device = this.deviceRepository.getUserDeviceByKey(authenticatedUser.getKey(),
										data.getDeviceKey());

						if (device == null) {
							throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key",
											data.getDeviceKey().toString());
						}

						if (!device.getType().equals(EnumDeviceType.AMPHIRO)) {
							throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type",
											data.getType().toString());
						}
						response = amphiroIndexOrderedRepository.storeData(
										((AuthenticatedUser) authenticatedUser).getKey(),
										(AmphiroMeasurementCollection) data);
					}
					break;
				case METER:
					if (data instanceof WaterMeterMeasurementCollection) {
						authenticatedUser = this.authenticate(data.getCredentials(), EnumRole.ROLE_ADMIN);

						device = this.deviceRepository.getDeviceByKey(data.getDeviceKey());

						if (device == null) {
							throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key",
											data.getDeviceKey().toString());
						}

						if (!device.getType().equals(EnumDeviceType.METER)) {
							throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type",
											data.getType().toString());
						}

						waterMeterMeasurementRepository.storeData(((WaterMeterDevice) device).getSerial(),
										(WaterMeterMeasurementCollection) data);
					}
					break;
				default:
					break;
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));

			success = false;
		} finally {
			logDataUploadSession(authenticatedUser, device, success);
		}

		return response;
	}

	private void logDataUploadSession(AuthenticatedUser user, Device device, boolean success) {
		try {
			if ((user == null) || (device == null)) {
				return;
			}
			deviceRepository.setLastDataUploadDate(user.getKey(), device.getKey(), new DateTime(), success);
		} catch (Exception ex) {
			// Ignore exceptions
		}

	}
}
