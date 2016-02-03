package eu.daiad.web.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import eu.daiad.web.domain.DeviceAmphiro;
import eu.daiad.web.domain.DeviceAmphiroConfigurationProperty;
import eu.daiad.web.domain.DeviceProperty;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceConfiguration;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.error.UserErrorCode;

@Primary
@Repository()
@Transactional()
@Scope("prototype")
public class JpaDeviceRepository implements IDeviceRepository {

	@Autowired
	EntityManager entityManager;

	@Override
	public UUID createAmphiroDevice(UUID userKey, String name, String macAddress, String aesKey,
					ArrayList<KeyValuePair> properties) throws ApplicationException {
		UUID deviceKey = null;

		try {
			TypedQuery<eu.daiad.web.domain.Account> query = entityManager
							.createQuery("select a from account a where a.key = :key",
											eu.daiad.web.domain.Account.class).setFirstResult(0).setMaxResults(1);
			query.setParameter("key", userKey);

			eu.daiad.web.domain.Account account = query.getSingleResult();

			eu.daiad.web.domain.DeviceAmphiro amphiro = new eu.daiad.web.domain.DeviceAmphiro();
			amphiro.setName(name);
			amphiro.setMacAddress(macAddress);
			amphiro.setAesKey(aesKey);

			for (KeyValuePair p : properties) {
				amphiro.getProperties().add(new DeviceProperty(p.getKey(), p.getValue()));
			}

			account.getDevices().add(amphiro);

			this.entityManager.persist(account);

			deviceKey = amphiro.getKey();
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}

		return deviceKey;
	}

	@Override
	public UUID createMeterDevice(UUID userKey, String serial, ArrayList<KeyValuePair> properties)
					throws ApplicationException {
		UUID deviceKey = null;

		try {
			TypedQuery<eu.daiad.web.domain.Account> query = entityManager
							.createQuery("select a from account a where a.key = :key",
											eu.daiad.web.domain.Account.class).setFirstResult(0).setMaxResults(1);
			query.setParameter("key", userKey);

			eu.daiad.web.domain.Account account = query.getSingleResult();

			eu.daiad.web.domain.DeviceMeter meter = new eu.daiad.web.domain.DeviceMeter();
			meter.setSerial(serial);

			for (KeyValuePair p : properties) {
				meter.getProperties().add(new DeviceProperty(p.getKey(), p.getValue()));
			}

			account.getDevices().add(meter);

			this.entityManager.persist(account);

			deviceKey = meter.getKey();
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}

		return deviceKey;
	}

	@Override
	public Device getUserDeviceByKey(UUID userKey, UUID deviceKey) throws ApplicationException {
		try {
			TypedQuery<eu.daiad.web.domain.Device> query = entityManager
							.createQuery("select d from device d where d.key = :device_key and d.account.key = :user_key",
											eu.daiad.web.domain.Device.class).setFirstResult(0).setMaxResults(1);
			query.setParameter("user_key", userKey);
			query.setParameter("device_key", deviceKey);

			List<eu.daiad.web.domain.Device> result = query.getResultList();

			if (result.size() == 1) {
				eu.daiad.web.domain.Device entity = result.get(0);

				switch (entity.getType()) {
				case AMPHIRO:
					eu.daiad.web.domain.DeviceAmphiro amphiroEntiry = (eu.daiad.web.domain.DeviceAmphiro) entity;

					AmphiroDevice amphiro = new AmphiroDevice(amphiroEntiry.getKey(), amphiroEntiry.getName(),
									amphiroEntiry.getMacAddress(), amphiroEntiry.getAesKey());

					for (eu.daiad.web.domain.DeviceProperty p : amphiroEntiry.getProperties()) {
						amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
					}

					return amphiro;
				case METER:
					eu.daiad.web.domain.DeviceMeter meterEntiry = (eu.daiad.web.domain.DeviceMeter) entity;

					WaterMeterDevice meter = new WaterMeterDevice(meterEntiry.getKey(), meterEntiry.getSerial());

					for (eu.daiad.web.domain.DeviceProperty p : meterEntiry.getProperties()) {
						meter.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
					}

					return meter;
				default:
					break;
				}

			}

			return null;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public ArrayList<Device> getUserDevices(UUID userKey, DeviceRegistrationQuery query) throws ApplicationException {
		ArrayList<Device> devices = new ArrayList<Device>();

		try {
			TypedQuery<eu.daiad.web.domain.Device> typedQuery = entityManager.createQuery(
							"select d from device d where d.account.key = :user_key", eu.daiad.web.domain.Device.class)
							.setFirstResult(0);
			typedQuery.setParameter("user_key", userKey);

			List<eu.daiad.web.domain.Device> result = typedQuery.getResultList();

			for (eu.daiad.web.domain.Device entity : result) {
				switch (entity.getType()) {
				case AMPHIRO:
					if ((query.getType() == EnumDeviceType.UNDEFINED) || (query.getType() == entity.getType())) {
						eu.daiad.web.domain.DeviceAmphiro amphiroEntiry = (eu.daiad.web.domain.DeviceAmphiro) entity;

						AmphiroDevice amphiro = new AmphiroDevice(amphiroEntiry.getKey(), amphiroEntiry.getName(),
										amphiroEntiry.getMacAddress(), amphiroEntiry.getAesKey());

						for (eu.daiad.web.domain.DeviceProperty p : amphiroEntiry.getProperties()) {
							amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
						}

						devices.add(amphiro);
					}
					break;
				case METER:
					if ((query.getType() == EnumDeviceType.UNDEFINED) || (query.getType() == entity.getType())) {
						eu.daiad.web.domain.DeviceMeter meterEntiry = (eu.daiad.web.domain.DeviceMeter) entity;

						WaterMeterDevice meter = new WaterMeterDevice(meterEntiry.getKey(), meterEntiry.getSerial());

						for (eu.daiad.web.domain.DeviceProperty p : meterEntiry.getProperties()) {
							meter.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
						}

						devices.add(meter);
					}
					break;
				default:
					break;
				}

			}
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}

		return devices;
	}

	@Override
	public Device getUserAmphiroDeviceByMacAddress(UUID userKey, String macAddress) throws ApplicationException {
		try {
			TypedQuery<eu.daiad.web.domain.DeviceAmphiro> query = entityManager
							.createQuery("select d from device_amphiro d where d.macAddress = :macAddress",
											eu.daiad.web.domain.DeviceAmphiro.class).setFirstResult(0).setMaxResults(1);
			query.setParameter("macAddress", macAddress);

			List<eu.daiad.web.domain.DeviceAmphiro> result = query.getResultList();

			if (result.size() == 1) {
				eu.daiad.web.domain.DeviceAmphiro entity = result.get(0);

				AmphiroDevice amphiro = new AmphiroDevice(entity.getKey(), entity.getName(), entity.getMacAddress(),
								entity.getAesKey());

				for (eu.daiad.web.domain.DeviceProperty p : entity.getProperties()) {
					amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
				}

				return amphiro;
			}

			return null;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public Device getUserWaterMeterDeviceBySerial(UUID userKey, String serial) throws ApplicationException {
		try {
			TypedQuery<eu.daiad.web.domain.DeviceMeter> query = entityManager
							.createQuery("select d from device_meter d where d.serial = :serial",
											eu.daiad.web.domain.DeviceMeter.class).setFirstResult(0).setMaxResults(1);
			query.setParameter("serial", serial);

			List<eu.daiad.web.domain.DeviceMeter> result = query.getResultList();

			if (result.size() == 1) {
				eu.daiad.web.domain.DeviceMeter entity = result.get(0);

				WaterMeterDevice meter = new WaterMeterDevice(entity.getKey(), entity.getSerial());

				for (eu.daiad.web.domain.DeviceProperty p : entity.getProperties()) {
					meter.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
				}

				return meter;

			}

			return null;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public void shareDevice(UUID ownerID, String assigneeUsername, UUID deviceKey, boolean shared)
					throws ApplicationException {
		try {
			// Get device
			TypedQuery<eu.daiad.web.domain.DeviceAmphiro> deviceQuery = entityManager
							.createQuery("select d from device_amphiro d where d.key = :key",
											eu.daiad.web.domain.DeviceAmphiro.class).setFirstResult(0).setMaxResults(1);
			deviceQuery.setParameter("key", deviceKey);

			List<eu.daiad.web.domain.DeviceAmphiro> devices = deviceQuery.getResultList();

			if (devices.size() != 1) {
				throw new ApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
			}

			// Check owner
			eu.daiad.web.domain.DeviceAmphiro device = devices.get(0);

			if (!device.getAccount().getKey().equals(ownerID)) {
				throw new ApplicationException(SharedErrorCode.AUTHORIZATION);
			}

			// Get assignee
			TypedQuery<eu.daiad.web.domain.Account> userQuery = entityManager
							.createQuery("select a from account a where a.username = :username and a.utility.id = :utility_id",
											eu.daiad.web.domain.Account.class).setFirstResult(0).setMaxResults(1);
			userQuery.setParameter("username", assigneeUsername);
			userQuery.setParameter("utility_id", device.getAccount().getUtility().getId());

			List<eu.daiad.web.domain.Account> users = userQuery.getResultList();

			if (users.size() == 0) {
				throw new ApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", assigneeUsername);
			}

			eu.daiad.web.domain.Account assignee = users.get(0);

			if (assignee.getId() == device.getAccount().getId()) {
				return;
			}

			eu.daiad.web.domain.DeviceAmphiroPermission permission = null;

			TypedQuery<eu.daiad.web.domain.DeviceAmphiroPermission> permissionQuery = entityManager
							.createQuery("select p from device_amphiro_permission p where p.device.id = :deviceId and p.owner.id = :ownerId and p.assignee.id = :assigneeId",
											eu.daiad.web.domain.DeviceAmphiroPermission.class).setFirstResult(0)
							.setMaxResults(1);
			permissionQuery.setParameter("deviceId", device.getId());
			permissionQuery.setParameter("ownerId", device.getAccount().getId());
			permissionQuery.setParameter("assigneeId", assignee.getId());

			List<eu.daiad.web.domain.DeviceAmphiroPermission> permissions = permissionQuery.getResultList();
			if (permissions.size() == 1) {
				permission = permissions.get(0);
			}

			if (shared) {
				if (permission == null) {
					permission = new eu.daiad.web.domain.DeviceAmphiroPermission();
					permission.setDevice(device);
					permission.setOwner(device.getAccount());
					permission.setAssignee(assignee);
					permission.setAssignedOn(DateTime.now());

					this.entityManager.persist(permission);
				}
			} else {
				if (permission != null) {
					this.entityManager.remove(permission);
				}
			}

		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	public ArrayList<DeviceConfiguration> getConfiguration(UUID userKey, UUID deviceKeys[]) throws ApplicationException {
		ArrayList<DeviceConfiguration> configuration = new ArrayList<DeviceConfiguration>();
		try {
			for (UUID deviceKey : deviceKeys) {
				TypedQuery<eu.daiad.web.domain.DeviceAmphiro> deviceQuery = entityManager
								.createQuery("select d from device_amphiro d where d.key = :deviceKey and d.account.key = :userKey",
												eu.daiad.web.domain.DeviceAmphiro.class).setFirstResult(0)
								.setMaxResults(1);
				deviceQuery.setParameter("deviceKey", deviceKey);
				deviceQuery.setParameter("userKey", userKey);

				List<eu.daiad.web.domain.DeviceAmphiro> devices = deviceQuery.getResultList();

				if (devices.size() != 1) {
					throw new ApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
				}

				DeviceAmphiro device = devices.get(0);

				DeviceConfiguration deviceConfiguration = new DeviceConfiguration();

				deviceConfiguration.setKey(device.getKey());
				deviceConfiguration.setMacAddress(device.getMacAddress());

				for (DeviceAmphiroConfigurationProperty p : device.getConfigurationProperties()) {
					deviceConfiguration.add(p.getKey(), p.getValue());
				}

				configuration.add(deviceConfiguration);
			}
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}

		return configuration;
	}
}