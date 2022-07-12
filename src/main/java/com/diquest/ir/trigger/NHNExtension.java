package com.diquest.ir.trigger;

import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.core.index.trigger.FilterDataProvider;
import com.diquest.ir.core.index.trigger.TriggerFieldSet;
import com.diquest.ir.core.schema.field.IntegerField;
import com.diquest.ir.core.schema.field.LongField;
import com.diquest.ir.core.service.index.data.BulkEntry;
import com.diquest.ir.util.common.TimeUtil;

public class NHNExtension implements TriggerExtension {

	public static final String CREATED_TIME = "CREATED_TIME";
	public static final String WITHIN_24_DISCOUNT_PERCENT = "WITHIN24_DISCOUNT_PERCENT";
	public static final String WITHIN_24_PERCENT = "WITHIN24_PERCENT";
	public static final String WITHIN_24_DISCOUNT_START_TIME = "WITHIN24_DISCOUNT_START_TIME";
	public static final String WITHIN_24_DISCOUNT_END_TIME = "WITHIN24_DISCOUNT_END_TIME";
	public static final String DISCOUNT_START_TIME = "DISCOUNT_START_TIME";
	public static final String DISCOUNT_END_TIME = "DISCOUNT_END_TIME";
	public static final String DISCOUNT_PERCENT = "DISCOUNT_PERCENT";
	public static final String WITHIN_24_PRICE = "WITHIN24_PRICE";
	public static final String WITHIN_24_DISCOUNT_PRICE = "WITHIN24_DISCOUNT_PRICE";
	public static final String PRICE = "PRICE";
	public static final String DISCOUNT_PRICE = "DISCOUNT_PRICE";
	private static IntegerField integerField;
	private static LongField longField;

	static {
		try {
			integerField = new IntegerField("", 9);
			longField = new LongField("", 18);
		} catch (IRException e) {
			e.printStackTrace();
		}
	}

	public void load() {
	}

	public float[] getTriggerValue(TriggerFieldSet tfSet, FilterDataProvider filterDataProvider, BulkEntry entry, long searchOccured) throws IRException {
		if (!tfSet.getParam().contains(",")) {
			throw new IRException("trigger parameter : " + tfSet.getParam() + " does not contains \",\"");
		}
		float[] result = new float[100];
		String field = tfSet.getParam().split(",")[1];
		if (field.equalsIgnoreCase("sale_price")) {
			fillSalePrice(filterDataProvider, entry, searchOccured, result);
		} else if (field.equalsIgnoreCase("sale_price_within24")) {
			fillSalePriceWithin24(filterDataProvider, entry, searchOccured, result);
		} else if (field.equalsIgnoreCase("sale_percent")) {
			fillSalePercent(filterDataProvider, entry, searchOccured, result);
		} else if (field.equalsIgnoreCase("sale_percent_within24")) {
			fillSalePercentWithin24(filterDataProvider, entry, searchOccured, result);
		} else if (field.equalsIgnoreCase("is_recently_reg_20days")) {
			fillIsRegister20Days(filterDataProvider, entry, searchOccured, result);
		} else{
			throw new IRException("unknown trigger parameter : " + tfSet.getParam());
		}
		return result;
	}

	private void fillIsRegister20Days(FilterDataProvider fdProvider, BulkEntry entry, long searchOccured, float[] result) throws IRException {
		byte[] createdTimeBulk = fdProvider.getFilterData(CREATED_TIME, entry);

		int readLongPos = 0;
		for (int i = entry.getFrom(); i < entry.getTo(); i++) {
			long createdTime = longField.fromByteToLong(createdTimeBulk, readLongPos, 8);
			readLongPos += 8;
			if (isWithin24Hour(createdTime, searchOccured)) {
				result[i] = 1;
			} else {
				result[i] = 0;
			}
		}
	}

	private void fillSalePercentWithin24(FilterDataProvider fdProvider, BulkEntry entry, long searchOccured, float[] result) throws IRException {
		byte[] createdTimeBulk = fdProvider.getFilterData(CREATED_TIME, entry);
		byte[] discountPercentBulk = fdProvider.getFilterData(DISCOUNT_PERCENT, entry);
		byte[] discountStartTimeBulk = fdProvider.getFilterData(DISCOUNT_START_TIME, entry);
		byte[] discountEndTimeBulk = fdProvider.getFilterData(DISCOUNT_END_TIME, entry);
		byte[] percentWithin24Bulk = fdProvider.getFilterData(WITHIN_24_PERCENT, entry);
		byte[] discountPercentWithin24Bulk = fdProvider.getFilterData(WITHIN_24_DISCOUNT_PERCENT, entry);
		byte[] discountStartTimeWithin24Bulk = fdProvider.getFilterData(WITHIN_24_DISCOUNT_START_TIME, entry);
		byte[] discountEndTimeWithin24Bulk = fdProvider.getFilterData(WITHIN_24_DISCOUNT_END_TIME, entry);

		int readIntPos = 0;
		int readLongPos = 0;
		for (int i = entry.getFrom(); i < entry.getTo(); i++) {
			int discountPercent = integerField.fromByteToInt(discountPercentBulk, readIntPos, 4);
			int percentWithin24 = integerField.fromByteToInt(percentWithin24Bulk, readIntPos, 4);
			int discountPercentWithin24 = integerField.fromByteToInt(discountPercentWithin24Bulk, readIntPos, 4);

			long createdTime = longField.fromByteToLong(createdTimeBulk, readLongPos, 8);
			long discountStartTime = longField.fromByteToLong(discountStartTimeBulk, readLongPos, 8);
			long discountEndTime = longField.fromByteToLong(discountEndTimeBulk, readLongPos, 8);
			long discountStartTimeWithin24 = longField.fromByteToLong(discountStartTimeWithin24Bulk, readLongPos, 8);
			long discountEndTimeWithin24 = longField.fromByteToLong(discountEndTimeWithin24Bulk, readLongPos, 8);
			readIntPos += 4;
			readLongPos += 8;

			if (!isWithin24Hour(createdTime, searchOccured)) {
				result[i] = getSalePercent(discountStartTime, discountEndTime, discountPercent, searchOccured);
			} else if (discountPercentWithin24 == 0 || percentWithin24 == 0) {// 필드값이 없으면 sale_percent 로직 태움
				result[i] = getSalePercent(discountStartTime, discountEndTime, discountPercent, searchOccured);
			} else {
				result[i] = (searchOccured >= discountStartTimeWithin24 && searchOccured <= discountEndTimeWithin24) ? discountPercentWithin24 : percentWithin24;
			}
		}
	}

	private boolean isWithin24Hour(long createdTime, long searchOccured) {
		long prev20Day = searchOccured - (TimeUtil.DAY * 20) / 1000;
		return prev20Day <= createdTime;
	}

	private void fillSalePercent(FilterDataProvider fdProvider, BulkEntry entry, long searchOccured, float[] result) throws IRException {
		byte[] discountStartTimeBulk = fdProvider.getFilterData(DISCOUNT_START_TIME, entry);
		byte[] discountEndTimeBulk = fdProvider.getFilterData(DISCOUNT_END_TIME, entry);
		byte[] discountPercentBulk = fdProvider.getFilterData(DISCOUNT_PERCENT, entry);

		int readIntPos = 0;
		int readLongPos = 0;
		for (int i = entry.getFrom(); i < entry.getTo(); i++) {
			int discountPercent = integerField.fromByteToInt(discountPercentBulk, readIntPos, 4);

			long discountStartTime = longField.fromByteToLong(discountStartTimeBulk, readLongPos, 8);
			long discountEndTime = longField.fromByteToLong(discountEndTimeBulk, readLongPos, 8);
			readIntPos += 4;
			readLongPos += 8;

			result[i] = getSalePercent(discountStartTime, discountEndTime, discountPercent, searchOccured);
		}
	}

	private float getSalePercent(long discountStartTime, long discountEndTime, int discountPercent, long searchOccured) {
		return (searchOccured >= discountStartTime && searchOccured <= discountEndTime) ? discountPercent : 0;
	}

	private void fillSalePriceWithin24(FilterDataProvider fdProvider, BulkEntry entry, long searchOccured, float[] result) throws IRException {
		byte[] createdTimeBulk = fdProvider.getFilterData(CREATED_TIME, entry);
		byte[] priceBulk = fdProvider.getFilterData(PRICE, entry);
		byte[] discountPriceBulk = fdProvider.getFilterData(DISCOUNT_PRICE, entry);
		byte[] discountStartTimeBulk = fdProvider.getFilterData(DISCOUNT_START_TIME, entry);
		byte[] discountEndTimeBulk = fdProvider.getFilterData(DISCOUNT_END_TIME, entry);
		byte[] priceWithin24Bulk = fdProvider.getFilterData(WITHIN_24_PRICE, entry);
		byte[] discountPriceWithin24Bulk = fdProvider.getFilterData(WITHIN_24_DISCOUNT_PRICE, entry);
		byte[] discountStartTimeWithin24Bulk = fdProvider.getFilterData(WITHIN_24_DISCOUNT_START_TIME, entry);
		byte[] discountEndTimeWithin24Bulk = fdProvider.getFilterData(WITHIN_24_DISCOUNT_END_TIME, entry);

		int readIntPos = 0;
		int readLongPos = 0;
		for (int i = entry.getFrom(); i < entry.getTo(); i++) {
			int price = integerField.fromByteToInt(priceBulk, readIntPos, 4);
			int discountPrice = integerField.fromByteToInt(discountPriceBulk, readIntPos, 4);
			int priceWithin24 = integerField.fromByteToInt(priceWithin24Bulk, readIntPos, 4);
			int discountPriceWithin24 = integerField.fromByteToInt(discountPriceWithin24Bulk, readIntPos, 4);

			long createdTime = longField.fromByteToLong(createdTimeBulk, readLongPos, 8);
			long discountStartTime = longField.fromByteToLong(discountStartTimeBulk, readLongPos, 8);
			long discountEndTime = longField.fromByteToLong(discountEndTimeBulk, readLongPos, 8);
			long discountStartTimeWithin24 = longField.fromByteToLong(discountStartTimeWithin24Bulk, readLongPos, 8);
			long discountEndTimeWithin24 = longField.fromByteToLong(discountEndTimeWithin24Bulk, readLongPos, 8);
			readIntPos += 4;
			readLongPos += 8;

			if (!isWithin24Hour(createdTime, searchOccured)) { // 24시간 이내 등록상품이 아니면 salePrice 로직을 태움
				result[i] = getSalePrice(price, discountPrice, discountStartTime, discountEndTime, searchOccured);
			} else if (priceWithin24 == 0 || discountPriceWithin24 == 0) { // 필드값이 없으면, salePrice 로직 태움
				result[i] = getSalePrice(price, discountPrice, discountStartTime, discountEndTime, searchOccured);
			} else {
				result[i] = (discountPriceWithin24 != 0 && searchOccured >= discountStartTimeWithin24 && searchOccured <= discountEndTimeWithin24) ? discountPriceWithin24 : priceWithin24;
			}
		}
	}

	private void fillSalePrice(FilterDataProvider fdProvider, BulkEntry entry, long searchOccured, float[] result) throws IRException {
		byte[] priceBulk = fdProvider.getFilterData(PRICE, entry);
		byte[] discountPriceBulk = fdProvider.getFilterData(DISCOUNT_PRICE, entry);
		byte[] discountStartTimeBulk = fdProvider.getFilterData(DISCOUNT_START_TIME, entry);
		byte[] discountEndTimeBulk = fdProvider.getFilterData(DISCOUNT_END_TIME, entry);

		int readIntPos = 0;
		int readLongPos = 0;
		for (int i = entry.getFrom(); i < entry.getTo(); i++) {
			int price = integerField.fromByteToInt(priceBulk, readIntPos, 4);
			int discountPrice = integerField.fromByteToInt(discountPriceBulk, readIntPos, 4);

			long discountStartTime = longField.fromByteToLong(discountStartTimeBulk, readLongPos, 8);
			long discountEndTime = longField.fromByteToLong(discountEndTimeBulk, readLongPos, 8);
			readIntPos += 4;
			readLongPos += 8;
			result[i] = getSalePrice(price, discountPrice, discountStartTime, discountEndTime, searchOccured);
		}
	}

	private float getSalePrice(int price, int discountPrice, long discountStartTime, long discountEndTime, long searchOccured) {
		return (discountPrice != 0 && searchOccured >= discountStartTime && searchOccured <= discountEndTime) ? discountPrice : price;
	}
}
