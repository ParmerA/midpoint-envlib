import com.evolveum.midpoint.xml.ns._public.common.common_3.GenericObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;

secretName = basic.lc(secretName)
def secretNameNorm = "AWS-Secret:" + secretName;

def prismContext = basic.getPrismContext();

def secretObjectQuery = prismContext.queryFor(GenericObjectType.class).item(GenericObjectType.F_NAME).eq(secretNameNorm).and().item(GenericObjectType.F_SUBTYPE).eq(CACHE_OBJECT_SUBTYPE).build();
def secretObjectList = midpoint.searchObjects(GenericObjectType.class, secretObjectQuery);

if(secretObjectList.size() == 1){
	def secretObject = secretObjectList[0];
	
	return secretObject.getCredentials().getPassword().getValue();
}