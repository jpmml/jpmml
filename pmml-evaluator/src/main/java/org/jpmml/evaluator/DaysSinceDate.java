/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.joda.time.*;

public class DaysSinceDate implements Comparable<DaysSinceDate> {

	private LocalDate epoch = null;

	private Days days = null;


	public DaysSinceDate(LocalDate epoch, LocalDate date){
		setEpoch(epoch);

		setDays(Days.daysBetween(epoch, date));
	}

	@Override
	public int compareTo(DaysSinceDate that){

		if(!(this.getEpoch()).equals(that.getEpoch())){
			throw new ClassCastException();
		}

		return (this.getDays()).compareTo(that.getDays());
	}

	@Override
	public int hashCode(){
		return 37 * getEpoch().hashCode() + getDays().hashCode();
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof DaysSinceDate){
			DaysSinceDate that = (DaysSinceDate)object;

			return (this.getEpoch()).equals(that.getEpoch()) && (this.getDays()).equals(that.getDays());
		}

		return false;
	}

	public int intValue(){
		return getDays().getDays();
	}

	public LocalDate getEpoch(){
		return this.epoch;
	}

	private void setEpoch(LocalDate epoch){
		this.epoch = epoch;
	}

	public Days getDays(){
		return this.days;
	}

	private void setDays(Days days){
		this.days = days;
	}
}