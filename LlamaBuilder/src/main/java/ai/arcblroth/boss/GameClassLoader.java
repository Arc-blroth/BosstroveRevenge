package ai.arcblroth.boss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

// Once upon a time there was a SubscribingClassLoader...
public class GameClassLoader extends ClassLoader {

	private static final String SPEC_VENDER = "Arc'blroth";
	private static final List<String> PROHIBITED_PACKAGE_NAMES = Arrays.asList("java", "javax", "sun");

	/**
	 * Ensure that the IOutputRenderer and CharacterInputEvent classes, as well
	 * as every class they load, are loaded by the parent class loader.
	 * This is needed to bridge the throwaway game instance with LlamaRenderer.
	 */
	public static final List<String> PASSTHROUGH_CLASSES = Arrays.asList(
			"ai.arcblroth.boss.render.Color",
			"ai.arcblroth.boss.io.IOutputRenderer",
			"ai.arcblroth.boss.render.PixelAndTextGrid",
			"ai.arcblroth.boss.render.PixelGrid",
			"ai.arcblroth.boss.util.Grid2D",
			"ai.arcblroth.boss.util.Pair",
			"ai.arcblroth.boss.util.TriConsumer",
			"ai.arcblroth.boss.key.CharacterInputEvent",
			"ai.arcblroth.boss.event.IEvent"
	);

	private static final Logger logger = Logger.getLogger("GameClassLoader");

	public GameClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			synchronized (getClassLoadingLock(name)) {
				if (PROHIBITED_PACKAGE_NAMES.contains(name.split("\\.")[0])) {
					return super.loadClass(name, resolve);
				}
				if(PASSTHROUGH_CLASSES.contains(name)) {
					return super.loadClass(name, resolve);
				}
				Class<?> loadedClazz = findLoadedClass(name);
				if (loadedClazz != null) {
					return loadedClazz;
				}
				if (name.equals(this.getClass().getName())) {
					return this.getClass();
				}

				logger.finest("Loading Class \"" + name + "\"");
				InputStream classInputStream = ClassLoader.getSystemResource(name.replace(".", "/").concat(".class")).openStream();
				byte[] classData = readBytes(classInputStream);
				classInputStream.close();
				Class<?> clazz = loadClass(classData, name, resolve);

				return clazz;
			}
		} catch (IOException e) {
			ClassNotFoundException cne = new ClassNotFoundException();
			cne.initCause(e);
			throw cne;
		}
	}

	private Class<?> loadClass(byte[] classData, String name, boolean resolve) {
		Class<?> clazz = defineClass(name, classData, 0, classData.length, this.getClass().getProtectionDomain());
		if (clazz != null) {
			if (clazz.getPackage() == null) {
				definePackage(name.replaceAll("\\.\\w+$", ""), null, null, SPEC_VENDER, null, null, null, null);
			}
			if (resolve)
				resolveClass(clazz);
		}
		return clazz;
	}

	private static byte[] readBytes(InputStream inputStream) throws IOException {
		byte[] b = new byte[1024];
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int c;
		while ((c = inputStream.read(b)) != -1) {
			os.write(b, 0, c);
		}
		return os.toByteArray();
	}


}
