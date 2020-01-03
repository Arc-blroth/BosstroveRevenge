package ai.arcblroth.boss.resource;

import java.io.File;
import java.net.URL;

public class InternalResource extends Resource {

	private String path;

	public InternalResource(String... path) {
		if (path == null || path.length <= 0)
			throw new IllegalArgumentException("Must specify at least one path component.");
		StringBuilder pathBuilder = new StringBuilder();
		for (String string : path) {
			pathBuilder.append(string);
			pathBuilder.append(File.pathSeparatorChar);
		}
		pathBuilder.deleteCharAt(pathBuilder.length() - 1);
		this.path = pathBuilder.toString();
	}
	
	public boolean exists() throws NullPointerException {
		return Thread.currentThread().getContextClassLoader().getResource(this.path) != null;
	}

	public URL resolve() throws NullPointerException {
		URL resolved = Thread.currentThread().getContextClassLoader().getResource(this.path);
		if (resolved == null) {
			throw new NullPointerException("Resource at " + path + " doesn't exist!");
		}
		return resolved;
	}
	
	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return path;
	}

}
