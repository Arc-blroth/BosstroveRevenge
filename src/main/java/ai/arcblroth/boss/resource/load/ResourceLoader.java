package ai.arcblroth.boss.resource.load;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Stream;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelGrid;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.resource.ExternalResource;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.resource.ZipResource;
import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;

public class ResourceLoader {

	public static Texture loadPNG(Resource resource) throws NullPointerException {
		try {
			PngReader pngr = new PngReader(resource.resolve().openStream());
			PixelGrid pg = new PixelGrid(pngr.imgInfo.cols, pngr.imgInfo.rows);
			int channels = pngr.imgInfo.channels;
			for (int row = 0; row < pngr.imgInfo.rows; row++) {
				IImageLine ill = pngr.readRow();
				int[] scanline = ((ImageLineInt) ill).getScanline();
				for (int col = 0; col < pngr.imgInfo.cols; col++) {
					if(pngr.imgInfo.alpha) {
						pg.setPixel(col, row, new Color(
								scanline[col * channels],
								scanline[col * channels + 1],
								scanline[col * channels + 2],
								scanline[col * channels + 3]));
					} else {
						pg.setPixel(col, row, new Color(
								scanline[col * channels],
								scanline[col * channels + 1],
								scanline[col * channels + 2]));
					}
				}
			}
			pngr.end();
			return new Texture(pg);
		} catch (NullPointerException e) {
			throw e;
		}  catch (IOException e) {
			NullPointerException npe = new NullPointerException(
					"Could not read PNG resource " + resource.toString());
			npe.initCause(e);
			throw npe;
		}
	}
	
	public static String loadTextFile(Resource resource) throws IOException, NullPointerException {
		try(BufferedReader br = new BufferedReader(new InputStreamReader(resource.resolve().openStream()))) {
			StringBuilder sb = new StringBuilder();
			br.lines().forEachOrdered(line -> sb.append(line).append("\n"));
			return sb.toString();
		} catch(IOException e) {
			throw e;
		}
	}

	//Based off of https://stackoverflow.com/a/28057735
	public static Stream<Resource> getAllResourcesFromInternalFolder(InternalResource folder, boolean recursive)
			throws IOException, NullPointerException, URISyntaxException {
		URI internalFolder = folder.resolve().toURI();
	    if (internalFolder.getScheme().equals("jar")) {
	        FileSystem fileSystem = FileSystems.newFileSystem(internalFolder, Collections.<String, Object>emptyMap());
	        Path internalPath = fileSystem.getPath(folder.getPath());
		    return Files.walk(internalPath, recursive ? Integer.MAX_VALUE : 1)
		    		.filter((path) -> !path.toString().endsWith("/") && !path.toString().endsWith(File.separator))
		    		.map((path) -> new ZipResource(fileSystem, path));
	    } else {
	    	Path internalPath = Paths.get(internalFolder);
		    return Files.walk(internalPath, recursive ? Integer.MAX_VALUE : 1)
		    		.filter((path) -> !path.toFile().isDirectory())
		    		.map((path) -> new ExternalResource(path.toString()));
	    }
	}
	
}
