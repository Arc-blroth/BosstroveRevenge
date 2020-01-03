package ai.arcblroth.boss.load;

import java.util.ArrayList;

import ai.arcblroth.boss.register.IRegistrable;
import ai.arcblroth.boss.resource.Resource;

public abstract class AbstractIRegisterableLoader<R extends IRegistrable<R>> {
	
	public abstract void registerAll(ArrayList<R> specifications);
	
}
