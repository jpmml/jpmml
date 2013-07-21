/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.joda.time.*;

public class SecondsSinceDate implements Comparable<SecondsSinceDate> {

	private LocalDate epoch = null;

	private Seconds seconds = null;


	public SecondsSinceDate(LocalDate epoch, LocalDateTime dateTime){
		setEpoch(epoch);

		// Have to have the same set of fields
		LocalDateTime epochDateTime = new LocalDateTime(epoch.getYear(), epoch.getMonthOfYear(), epoch.getDayOfMonth(), 0, 0, 0);

		setSeconds(Seconds.secondsBetween(epochDateTime, dateTime));
	}

	@Override
	public int compareTo(SecondsSinceDate that){

		if(!(this.getEpoch()).equals(that.getEpoch())){
			throw new ClassCastException();
		}

		return (this.getSeconds()).compareTo(that.getSeconds());
	}

	@Override
	public int hashCode(){
		return 37 * getEpoch().hashCode() + getSeconds().hashCode();
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof SecondsSinceDate){
			SecondsSinceDate that = (SecondsSinceDate)object;

			return (this.getEpoch()).equals(that.getEpoch()) && (this.getSeconds()).equals(that.getSeconds());
		}

		return false;
	}

	public int intValue(){
		return getSeconds().getSeconds();
	}

	public LocalDate getEpoch(){
		return this.epoch;
	}

	private void setEpoch(LocalDate epoch){
		this.epoch = epoch;
	}

	public Seconds getSeconds(){
		return this.seconds;
	}

	private void setSeconds(Seconds seconds){
		this.seconds = seconds;
	}
}