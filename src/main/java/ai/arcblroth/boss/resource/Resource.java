package ai.arcblroth.boss.resource;

import java.io.File;
import java.net.URL;

public abstract class Resource {
	
	public Resource(String... path) {}
	
	public abstract boolean exists() throws NullPointerException;

	public abstract URL resolve() throws NullPointerException;
	
	public abstract String getPath();

	@Override
	public abstract String toString();

}
