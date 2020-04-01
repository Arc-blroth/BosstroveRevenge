package ai.arcblroth.boss.util;

import java.util.ArrayList;
import java.util.List;

public class PadUtils {

	// Because this should never be an external dependency
	// cough cough `npm install left-pad` cough cough
	public static String leftPad(String line, int length, char padder) {
		if (length <= 0)
			return line;
		else if (line.length() < length) {
			StringBuilder leftpad = new StringBuilder();
			for (int i = 0; i < length - line.length(); i++) {
				leftpad.append(padder);
			}
			leftpad.append(line);
			return leftpad.toString();
		} else if (line.length() > length) {
			return line.substring(line.length() - length);
		} else
			return line;
	}

	public static String leftPad(String line, int length) {
		return leftPad(line, length, ' ');
	}

	public static <T> List<T> leftPad(List<T> array, int length, T padder) {
		// Clone the array since these methods should not modify original
		array = deepCloneList(array);
		if (length <= 0)
			return array;
		else if (array.size() < length) {
			int size = array.size(); // Because the size will change
			for (int i = 0; i < length - size; i++) {
				array.add(0, padder);
			}
			return array;
		} else if (array.size() > length) {
			return new ArrayList<T>(array.subList(array.size() - length, array.size()));
		} else
			return array;
	}

	public static String rightPad(String line, int length, char padder) {
		if (length <= 0)
			return line;
		else if (line.length() < length) {
			StringBuilder rightPad = new StringBuilder();
			rightPad.append(line);
			for (int i = 0; i < length - line.length(); i++) {
				rightPad.append(padder);
			}
			return rightPad.toString();
		} else if (line.length() > length) {
			return line.substring(0, length);
		} else
			return line;
	}

	public static <T> List<T> rightPad(List<T> array, int length, T padder) {
		// Clone the array since these methods should not modify original
		array = deepCloneList(array);
		if (length <= 0)
			return array;
		else if (array.size() < length) {
			int size = array.size(); // Because the size will change
			for (int i = 0; i < length - size; i++) {
				array.add(padder);
			}
			return array;
		} else if (array.size() > length) {
			return new ArrayList<T>(array.subList(0, length));
		} else
			return array;
	}

	public static String rightPad(String line, int length) {
		return rightPad(line, length, ' ');
	}

	/**
	 * centerPad is biased to more chars at the left.
	 */
	public static String centerPad(String line, int length, char padder) {
		if (length <= 0)
			return line;
		int toLeft = (int) Math.ceil(((float) length - (float) line.length()) / 2F);
		int toRight = (int) Math.floor(((float) length - (float) line.length()) / 2F);
		return rightPad(leftPad(line, line.length() + toLeft, padder), line.length() + toLeft + toRight, padder);
	}
	
	public static <T> List<T> centerPad(List<T> array, int length, T padder)  {
		// Clone the array since these methods should not modify original
		array = deepCloneList(array);
		if (length <= 0)
			return array;
		else if (array.size() < length) {
			int toLeft = (int) Math.ceil(((float) length - (float) array.size()) / 2F);
			int toRight = (int) Math.floor(((float) length - (float) array.size()) / 2F);
			int size = array.size(); // Because the size will change
			return rightPad(leftPad(array, size + toLeft, padder), size + toLeft + toRight, padder);
		} else if (array.size() > length) {
			return new ArrayList<T>(array.subList(0, length));
		} else
			return array;
	}

	public static String centerPad(String line, int length) {
		return centerPad(line, length, ' ');
	}

	public static <T> List<T> deepCloneList(List<T> array) {
		ArrayList<T> newArray = new ArrayList<T>();
		for (T t : array) {
			if (t instanceof Cloneable) {
				newArray.add(t);
			} else {
				newArray.add(t);
			}
		}
		return newArray;
	}

	public static String stringTimes(String in, int times) {
		StringBuilder b = new StringBuilder("");
		for (int yeet = 0; yeet < times; yeet++) {
			b.append(in);
		}
		return b.toString();
	}
	
	public static ArrayList<Character> stringToArrayList(String in) {
		ArrayList<Character> array = new ArrayList<Character>(in.length());
		for(char c : in.toCharArray()) {
			array.add(c);
		}
		return array;
	}

}