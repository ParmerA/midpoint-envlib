import com.evolveum.midpoint.xml.ns._public.common.common_3.GenericObjectType;

def prismContext = basic.getPrismContext();

def secretObjectQuery = prismContext.queryFor(GenericObjectType.class).item(GenericObjectType.F_SUBTYPE).eq(CACHE_OBJECT_SUBTYPE).build();
def secretObjectList = midpoint.searchObjects(GenericObjectType.class, secretObjectQuery);

for(secretObject in secretObjectList){
	try{
		if(shouldLog){
			log.info("Refreshing object for cached AWS secret ${secretObject.getDescription()}");
		}
		
		envlib.execute("getSecret", ["secretName": secretObject.getDescription(), "key": "", "bypassCache": true]);
	}catch(Exception e){
		if(shouldLog){
			log.error("Error refreshing cached AWS secret. Deleting midPoint object")
		}
		
		midpoint.deleteObject(GenericObjectType.class, secretObject.getOid());
	}
}