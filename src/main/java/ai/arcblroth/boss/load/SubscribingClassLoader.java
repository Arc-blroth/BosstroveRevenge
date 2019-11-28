package ai.arcblroth.boss.load;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SubscribingClassLoader extends ClassLoader {

	private static final String SPEC_VENDER = "Arc'blroth";
	private static final List<String> PROHIBITED_PACKAGE_NAMES = Arrays.asList("java", "javax", "sun");
	private static final Logger subClassLoaderLogger = Logger.getLogger("SubscribingClassLoader");
	private ArrayList<Consumer<Class<?>>> hooks = new ArrayList<Consumer<Class<?>>>();

	public SubscribingClassLoader(ClassLoader parent) {
		super(parent);
		// [00:00:00][Logger/LEVEL]: Message
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT][%3$s/%4$s]: %5$s %6$s%n");
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

		try {
			synchronized (getClassLoadingLock(name)) {
				if (PROHIBITED_PACKAGE_NAMES.contains(name.split("\\.")[0])) {
					return super.loadClass(name, resolve);
				}
				Class<?> loadedClazz = findLoadedClass(name);
				if (loadedClazz != null) {
					return loadedClazz;
				}
				if (name.equals(this.getClass().getName())) {
					return this.getClass();
				}

				subClassLoaderLogger.finest("Loading Class \"" + name + "\"");
				InputStream classInputStream = ClassLoader.getSystemResource(name.replace(".", "/").concat(".class"))
						.openStream();
				byte[] classData = readBytes(classInputStream);
				classInputStream.close();
				Class<?> clazz = loadClass(classData, name, resolve);
				subscribeClass(clazz);

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

	private void subscribeClass(Class<?> clazz) {
		hooks.forEach((hook) -> hook.accept(clazz));
	}

	public void addHook(Consumer<Class<?>> r) {
		this.hooks.add(r);
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
