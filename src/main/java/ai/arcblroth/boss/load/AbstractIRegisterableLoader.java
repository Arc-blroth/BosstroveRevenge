package ai.arcblroth.boss.load;

import ai.arcblroth.boss.resource.Resource;

public abstract class AbstractIRegisterableLoader {
	
	public abstract boolean accepts(Resource specification);
	
	public abstract void register(Resource specification);
	
}
