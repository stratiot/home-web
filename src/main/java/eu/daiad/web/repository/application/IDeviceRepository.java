package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.domain.application.DeviceAmphiroConfigurationDefault;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceConfigurationCollection;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.error.ApplicationException;

public interface IDeviceRepository {

	public abstract void removeDevice(UUID deviceKey);

	public abstract UUID createAmphiroDevice(UUID userKey, String name, String macAddress, String aesKey,
					ArrayList<KeyValuePair> properties) throws ApplicationException;

	public List<DeviceAmphiroConfigurationDefault> getAmphiroDefaultConfigurations() throws ApplicationException;

	public abstract UUID createMeterDevice(String username, String serial, ArrayList<KeyValuePair> properties,
					Geometry location) throws ApplicationException;

	public abstract void updateMeterLocation(String username, String serial, Geometry location)
					throws ApplicationException;

	public abstract Device getUserDeviceByKey(UUID userKey, UUID deviceKey) throws ApplicationException;

	public abstract Device getDeviceByKey(UUID deviceKey) throws ApplicationException;

	public abstract Device getUserAmphiroDeviceByMacAddress(UUID userKey, String macAddress)
					throws ApplicationException;

	public abstract Device getUserWaterMeterDeviceBySerial(UUID userKey, String serial) throws ApplicationException;

	public abstract Device getWaterMeterDeviceBySerial(String serial) throws ApplicationException;

	public abstract ArrayList<Device> getUserDevices(UUID userKey, DeviceRegistrationQuery query)
					throws ApplicationException;

	public abstract void shareDevice(UUID ownerID, String assigneeUsername, UUID deviceKey, boolean shared)
					throws ApplicationException;

	public abstract ArrayList<DeviceConfigurationCollection> getConfiguration(UUID userKey, UUID deviceKeys[])
					throws ApplicationException;

	public abstract void notifyConfiguration(UUID userKey, UUID deviceKey, UUID version, DateTime updatedOn)
					throws ApplicationException;

	public abstract void setLastDataUploadDate(UUID userKey, UUID deviceKey, DateTime when, boolean success);

}