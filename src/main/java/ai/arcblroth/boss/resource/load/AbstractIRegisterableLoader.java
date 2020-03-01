package ai.arcblroth.boss.resource.load;

import com.google.gson.Gson;

import ai.arcblroth.boss.resource.Resource;

public abstract class AbstractIRegisterableLoader {
	
	public abstract boolean accepts(Resource specification);
	
	public abstract void register(Gson loader, Resource specification);
	
}
