package com.xsoftwarelabs.spring.roo.addon.typicalsecurity;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Sample of a command class. The command class is registered by the Roo shell following an
 * automatic classpath scan. You can provide simple user presentation-related logic in this
 * class. You can return any objects from each method, or use the logger directly if you'd
 * like to emit messages of different severity (and therefore different colours on 
 * non-Windows systems).
 * 
 * @since 1.1
 */
@Component
@Service
public class TypicalsecurityCommands implements CommandMarker {
	private static Logger logger = Logger.getLogger(TypicalsecurityCommands.class.getName());
	@Reference private TypicalsecurityOperations operations;
	
	@CliAvailabilityIndicator({"Typicalsecurity setup", "Typicalsecurity add"})
	public boolean isPropertyAvailable() {
		return true;  // it's safe to always see the properties we expose
	}
	
	@CliCommand(value="Typicalsecurity add", help="Some helpful description")
	public void add(@CliOption(key="type", mandatory=true, help="The java type to apply this annotation to") JavaType target) {
		operations.annotateType(target);
	}
	
	@CliCommand(value="Typicalsecurity setup", help="Setup Typicalsecurity addon")
	public void setup() {
		operations.setup();
	}
}