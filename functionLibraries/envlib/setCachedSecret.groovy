import com.evolveum.midpoint.model.api.ModelExecuteOptions;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConflictResolutionActionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConflictResolutionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.GenericObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;
import com.evolveum.midpoint.prism.delta.*;
import com.evolveum.midpoint.prism.impl.delta.*;

secretName = basic.lc(secretName)
def secretNameNorm = "AWS-Secret:" + secretName;

def prismContext = basic.getPrismContext();

def secretObjectQuery = prismContext.queryFor(GenericObjectType.class).item(GenericObjectType.F_NAME).eq(secretNameNorm).and().item(GenericObjectType.F_SUBTYPE).eq(CACHE_OBJECT_SUBTYPE).build();
def secretObjectList = midpoint.searchObjects(GenericObjectType.class, secretObjectQuery);

if(secretObjectList.size() > 0){
	def secretObject = secretObjectList[0];
	
	secretObject = secretObject.beginCredentials().beginPassword().value(secretValue).end().end();
	
	// Retry the modification action 2 times with a delay of 150 (+ random) ms
	def executeOptions = new ModelExecuteOptions().focusConflictResolution(new ConflictResolutionType().action(ConflictResolutionActionType.RESTART).maxAttempts(2).delayUnit(150));
	
	ContainerDeltaImpl containerDelta = ContainerDeltaImpl.createModificationReplace(GenericObjectType.F_CREDENTIALS, GenericObjectType.class, prismContext, secretObject.getCredentials().clone());
	ObjectDeltaImpl objectDelta = new ObjectDeltaImpl(GenericObjectType.class, ChangeType.MODIFY, prismContext);
	objectDelta.setOid(secretObject.getOid());
	objectDelta.swallow(containerDelta);
	midpoint.modifyObject(objectDelta, executeOptions);
}else{
	/* // For some reason, this doesn't work when a focus is added?
	// Ignore failed creations (when there is a race condition that we didn't catch)
	def executeOptions = ModelExecuteOptions.create().focusConflictResolution(new ConflictResolutionType().action(ConflictResolutionActionType.NONE)).force(true);
	*/
	
	def secretObject = new GenericObjectType().name(secretNameNorm).description(secretName).subtype(CACHE_OBJECT_SUBTYPE).beginCredentials().beginPassword().value(secretValue).end().end();
	try{
		midpoint.addObject(secretObject);
	}catch(ObjectAlreadyExistsException e){
		//log.info(e.toString());
	}
}