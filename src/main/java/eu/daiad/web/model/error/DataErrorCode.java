package eu.daiad.web.model.error;

public enum DataErrorCode implements ErrorCode {
	TIME_GRANULARITY_NOT_SUPPORTED;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}