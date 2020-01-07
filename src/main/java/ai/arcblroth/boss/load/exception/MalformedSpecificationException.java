package ai.arcblroth.boss.load.exception;

import ai.arcblroth.boss.resource.Resource;

public class MalformedSpecificationException extends Exception {

	private static final long serialVersionUID = -3413808997044129741L;

	public MalformedSpecificationException(String field, Resource specification) {
		super("The field " + field + " in " + specification.toString() + " does not refer to an allowed key.");
	}
	
}
