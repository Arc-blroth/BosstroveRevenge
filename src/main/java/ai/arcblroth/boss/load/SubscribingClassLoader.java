package ai.arcblroth.boss.load;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

import ai.arcblroth.boss.event.AutoSubscribeClass;
import ai.arcblroth.boss.event.EventBus;

public class SubscribingClassLoader extends ClassLoader {

	private static final String SPEC_VENDER = "Arc'blroth";
	private static final List<String> PROHIBITED_PACKAGE_NAMES = Arrays.asList("java", "javax");
	private static final Logger subClassLoaderLogger = Logger.getLogger("SubscribingClassLoader");
	private ArrayList<Consumer<Class<?>>> hooks = new ArrayList<Consumer<Class<?>>>();

	public SubscribingClassLoader(ClassLoader parent) {
		super(parent);
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
		Class<?> clazz = defineClass(name, classData, 0, classData.length);
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
