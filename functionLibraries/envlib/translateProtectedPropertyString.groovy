// If the passed property is an AWS secret lookup string, get it from secrets manager and pass it to the caller
// Otherwise, the property passed is already the username, password, etc. the caller needs (e.g. we're running in the docker-compose environment)

if(property ==~ AWS_SECRET_LOOKUP_PATTERN){
	def matcher = property =~ AWS_SECRET_LOOKUP_PATTERN;
	
	matcher.find();
	
	def secretName = matcher.group(1);
	def secretKey = matcher.group(2);
	
	return envlib.execute("getSecret", ["secretName": secretName, "key": secretKey, "bypassCache": false]);
}else{
	return basic.encrypt(property);
}