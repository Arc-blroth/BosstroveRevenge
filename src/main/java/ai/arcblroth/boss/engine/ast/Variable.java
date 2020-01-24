package ai.arcblroth.boss.engine.ast;

import java.math.BigDecimal;

public class Variable {
	
	private BigDecimal internalNumber;
	
	public Variable(BigDecimal bd) {
		internalNumber = bd;
	}
	
	public Variable(int i) {
		internalNumber = new BigDecimal(i);
	}
	
	public Variable(long l) {
		internalNumber = new BigDecimal(l);
	}
	
	public Variable(double d) {
		internalNumber = new BigDecimal(d);
	}
	
	public Variable(float f) {
		internalNumber = new BigDecimal(f);
	}
	
	public Variable(boolean b) {
		internalNumber = new BigDecimal(b ? 1 : 0);
	}
	
	public int getAsInt() {
		return internalNumber.intValue();
	}
	
	public long getAsLong() {
		return internalNumber.longValue();
	}
	
	public double getAsDouble() {
		return internalNumber.doubleValue();
	}
	
	public double getAsFloat() {
		return internalNumber.floatValue();
	}
	
	public boolean getAsBoolean() {
		return internalNumber.intValue() == 1 ? true : false;
	}
	
	public Variable add(Variable other) {
		return new Variable(internalNumber.add(other.internalNumber));
	}
	
	public Variable subtract(Variable other) {
		return new Variable(internalNumber.subtract(other.internalNumber));
	}
	
	public Variable multiply(Variable other) {
		return new Variable(internalNumber.multiply(other.internalNumber));
	}
	
	public Variable divide(Variable other) {
		return new Variable(internalNumber.divide(other.internalNumber));
	}
}
