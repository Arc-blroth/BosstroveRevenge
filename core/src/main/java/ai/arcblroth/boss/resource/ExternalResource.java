package ai.arcblroth.boss.resource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExternalResource extends Resource {

	private Path path;

	public ExternalResource(String... path) {
		if (path == null || path.length <= 0)
			throw new IllegalArgumentException("Must specify at least one path component.");
		StringBuilder pathBuilder = new StringBuilder();
		for (String string : path) {
			pathBuilder.append(string);
			pathBuilder.append(File.pathSeparatorChar);
		}
		pathBuilder.deleteCharAt(pathBuilder.length() - 1);
		this.path = Paths.get(pathBuilder.toString());
	}
	
	public ExternalResource(Path path) {
		this.path = path;
	}
	
	public boolean exists() throws NullPointerException {
		return Files.exists(path);
	}

	public URL resolve() throws NullPointerException {
		if (!exists()) {
			throw new NullPointerException("Resource at " + path + " doesn't exist!");
		}
		try {
			URL resolved = path.toUri().toURL();
			return resolved;
		} catch (MalformedURLException e) {
			throw new NullPointerException("Resource at " + path + " produced a malformed URI?");
		}
	}
	
	public String getPath() {
		return path.toString();
	}
	
	@Override
	public int hashCode() {
		return path.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExternalResource other = (ExternalResource) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public int compareTo(Resource other) {
		return getPath().compareTo(other.getPath());
	}

	@Override
	public String toString() {
		return path.toString();
	}

}
