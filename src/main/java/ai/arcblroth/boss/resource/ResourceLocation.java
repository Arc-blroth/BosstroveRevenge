package ai.arcblroth.boss.resource;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	public File toFile() throws NullPointerException, URISyntaxException {
		URL resolved = resolve();
		return Paths.get(resolved.toURI()).toFile();
	}

	@Override
	public String toString() {
		return path;
	}

}
