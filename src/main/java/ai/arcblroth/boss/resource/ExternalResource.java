package ai.arcblroth.boss.resource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

public class ExternalResource extends Resource {

	private String path;

	public ExternalResource(String... path) {
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
		return Paths.get(path).toFile().exists();
	}

	public URL resolve() throws NullPointerException {
		if (!exists()) {
			throw new NullPointerException("Resource at " + path + " doesn't exist!");
		}
		try {
			URL resolved = Paths.get(path).toFile().toURI().toURL();
			return resolved;
		} catch (MalformedURLException e) {
			throw new NullPointerException("Resource at " + path + " produced a malformed URI?");
		}
	}

	@Override
	public String toString() {
		return path;
	}

}
