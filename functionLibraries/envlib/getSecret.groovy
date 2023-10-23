// Gets a secret from AWS Secrets Manager using AWS CLI (using the container's task execution role)
// If a key is provided, then the key/value JSON is unpacked
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;
import groovy.json.*;

final boolean LOG_CALLS = false;

if(!(secretName ==~ AWS_SECRET_PATTERN)){
	throw new Exception("Invalid secret name: $secretName");
}

String secretString;
def jsonSlurper = new JsonSlurper();

ProtectedStringType cachedSecret = envlib.execute("getCachedSecret", ["secretName": secretName]);
if(cachedSecret && !bypassCache){
	secretString = basic.decrypt(cachedSecret);
}else{
	def command = "aws secretsmanager get-secret-value --secret-id $secretName";
	def process = command.execute();
	
	def outputStream = new StringBuffer();
	def errorStream = new StringBuffer();
	process.waitForProcessOutput(outputStream, errorStream);
	
	def error = errorStream.toString();
	if(error != ""){
		throw new Exception("Error getting secret from AWS secrets manager: $error");
	}
	
	def json = jsonSlurper.parseText(outputStream.toString());
	if(json.SecretString == null){
		throw new Exception("Error parsing JSON object for secret $secretName");
	}
	
	envlib.execute("setCachedSecret", ["secretName": secretName, "secretValue": basic.encrypt(json.SecretString)]);
	
	secretString = json.SecretString;
}

if(key){
	def secretJson = jsonSlurper.parseText(secretString);
	
	if(secretJson[key] == null){
		throw new Exception("Error finding key $key in JSON object for secret $secretName")
	}else{
		if(LOG_CALLS){
			log.info("envlib.getSecret was called for $secretName:$key by $actor");
		}
		
		return basic.encrypt(secretJson[key]);
	}
}

if(LOG_CALLS){
	log.info("envlib.getSecret was called for $secretName by $actor");
}

return basic.encrypt(secretString);