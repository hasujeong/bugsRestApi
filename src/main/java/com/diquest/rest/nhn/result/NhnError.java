package com.diquest.rest.nhn.result;

public class NhnError {
	Header header;

	public NhnError(Header header) {
		this.header = header;
	}

	public static NhnError makeError(boolean isSuccessful, int resultCode, String resultMessage, String errorMessage, String timezone) {
		return new NhnError(new Header(isSuccessful, resultCode, resultMessage, errorMessage, timezone));
	}

	public static class Header {
		boolean isSuccessful;
		int resultCode;
		String resultMessage;
		String errorMessage;
		String timezone;

		public Header(boolean isSuccessful, int resultCode, String resultMessage, String errorMessage, String timezone) {
			this.isSuccessful = isSuccessful;
			this.resultCode = resultCode;
			this.resultMessage = resultMessage;
			this.errorMessage = errorMessage;
			this.timezone = timezone;
		}

	}
}
