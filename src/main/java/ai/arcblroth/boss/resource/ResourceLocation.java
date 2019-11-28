package ai.arcblroth.boss.resource;

import java.io.File;
import java.net.URL;

public class ResourceLocation {

	private String path;

	public ResourceLocation(String... path) {
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

	public URL resolve() throws NullPointerException {
		URL resolved = Thread.currentThread().getContextClassLoader().getResource(this.path);
		if (resolved == null) {
			throw new NullPointerException("ResourceLocation at " + path + " doesn't exist!");
		}
		return resolved;
	}

	@Override
	public String toString() {
		return path;
	}

}
