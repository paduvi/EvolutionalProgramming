package com.paduvi.app.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Timestamp {

	private long value;

	public Timestamp(long value) {
		this.value = value;
	}

	public Timestamp(String str) {
		try {
			this.value = new SimpleDateFormat("dd/MM/yyyy").parse(str).getTime() / TimeUnit.DAYS.toMillis(1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public long get() {
		return value;
	}

	@Override
	public String toString() {
		return new SimpleDateFormat("dd/MM/yyyy").format(new Date(value * TimeUnit.DAYS.toMillis(1)));
	}
}
