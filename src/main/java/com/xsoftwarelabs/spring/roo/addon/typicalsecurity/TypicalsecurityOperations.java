package com.xsoftwarelabs.spring.roo.addon.typicalsecurity;


/**
 * Interface of commands that are available via the Roo shell.
 *
 * @since 1.1
 */
public interface TypicalsecurityOperations {

	boolean isCommandAvailable();
	
	void setup(String entityPackage,String controllerPackage);
}