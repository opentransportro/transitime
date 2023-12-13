package org.transitclock.core.dataCache;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden
 * Factory that will provide cache to hold dwell time model class instances for each stop.
 *
 */
public class DwellTimeModelCacheFactory {
	private static StringConfigValue className = 
			new StringConfigValue("transitclock.core.cache.dwellTimeModelCache", 
					null,
					"Specifies the class used to cache RLS data for a stop.");
	
	private static DwellTimeModelCacheInterface singleton = null;
	
	public static DwellTimeModelCacheInterface getInstance() {
		
		if (singleton == null) {
			if(className!=null && className.getValue()!=null && className.getValue().length()>0)
			{
				singleton = ClassInstantiator.instantiate(className.getValue(), 
						DwellTimeModelCacheInterface.class);
			}
		}
		
		return singleton;
	}
}
