package ai.arcblroth.boss.load.exception;

public class UnsupportedSpecificationVersionException extends Exception {

	private static final long serialVersionUID = -3413808997044129741L;

	public UnsupportedSpecificationVersionException(String version, String specificationType) {
		super("Version " + version + " of specification of type " + specificationType + " is not supported.");
	}
	
	public UnsupportedSpecificationVersionException(long version, String specificationType) {
		super("Version " + version + " of specification of type " + specificationType + " is not supported.");
	}
	
}
