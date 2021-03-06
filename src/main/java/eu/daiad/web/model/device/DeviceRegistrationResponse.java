package eu.daiad.web.model.device;

import eu.daiad.web.model.RestResponse;

public class DeviceRegistrationResponse extends RestResponse {

	private String deviceKey;

	public DeviceRegistrationResponse() {
		super();
	}

	public DeviceRegistrationResponse(String code, String description) {
		super(code, description);
	}

	public String getDeviceKey() {
		return this.deviceKey;
	}

	public void setDeviceKey(String value) {
		this.deviceKey = value;
	}
}
