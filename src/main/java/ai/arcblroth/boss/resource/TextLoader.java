package ai.arcblroth.boss.resource;

import java.io.*;

public class TextLoader {
	
	public static String loadTextFile(Resource resource) throws IOException, NullPointerException {
		try(BufferedReader br = new BufferedReader(new InputStreamReader(resource.resolve().openStream()))) {
			StringBuilder sb = new StringBuilder();
			br.lines().forEachOrdered(line -> sb.append(line).append("\n"));
			return sb.toString();
		} catch(IOException e) {
			throw e;
		}
	}
	
}
