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
	
	public boolean exists() {
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
	public int hashCode() {
		return path.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InternalResource other = (InternalResource) obj;
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
		return path;
	}

}
