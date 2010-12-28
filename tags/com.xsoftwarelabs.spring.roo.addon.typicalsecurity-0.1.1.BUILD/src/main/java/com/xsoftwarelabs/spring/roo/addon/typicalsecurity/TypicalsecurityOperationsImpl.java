package com.xsoftwarelabs.spring.roo.addon.typicalsecurity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.shell.Shell;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of commands that are available via the Roo shell.
 * 
 * @since 1.1
 */
@Component
@Service
public class TypicalsecurityOperationsImpl implements TypicalsecurityOperations {
	private static Logger logger = Logger
			.getLogger(TypicalsecurityOperations.class.getName());
	@Reference private MetadataService metadataService;
	@Reference FileManager fileManager;
	@Reference PathResolver pathResolver;
    @Reference private Shell shell;
    @Reference private ClasspathOperations classpathOperations;


    private static char separator = File.separatorChar;

	public boolean isCommandAvailable() {
		return getPathResolver() != null
				&& classpathOperations.isPersistentClassAvailable();
	}

	public void setup(String entityPackage, String controllerPackage) {

		createUserRoleEntities(entityPackage);
		createControllers(entityPackage, controllerPackage);
		injectDatabasebasedSecurity(entityPackage);

	}

	/**
	 * Create All the entities required for User, Role and User Role
	 * @param entityPackage
	 */
	private void createUserRoleEntities(String entityPackage) {

		//-----------------------------------------------------------------------------------
		// Create User entity
		//-----------------------------------------------------------------------------------
		shell.executeCommand("entity --class " + entityPackage
				+ ".UserModel --testAutomatically");
		shell.executeCommand("field string --fieldName firstName --sizeMin 1 --notNull");
		shell.executeCommand("field string --fieldName lastName --sizeMin 1 --notNull");
		shell.executeCommand("field string --fieldName emailAddress --sizeMin 1 --notNull --unique");
		shell.executeCommand("field string --fieldName password --sizeMin 1 --notNull");

		//-----------------------------------------------------------------------------------
		// Create Role entity
		//-----------------------------------------------------------------------------------
		shell.executeCommand("entity --class " + entityPackage
				+ ".RoleModel --testAutomatically");
		shell.executeCommand("field string --fieldName roleName --sizeMin 1 --notNull --unique");
		shell.executeCommand("field string --fieldName roleDescription --sizeMin --sizeMax 200 --notNull");

		//-----------------------------------------------------------------------------------
		// Create User Role Mapping
		//-----------------------------------------------------------------------------------
		shell.executeCommand("entity --class " + entityPackage
				+ ".UserRoleModel --testAutomatically");
		shell.executeCommand("field ref --fieldName userEntry --type "
				+ entityPackage + ".UserModel --notNull");
		shell.executeCommand("field ref --fieldName roleEntry --type "
				+ entityPackage + ".RoleModel --notNull");


		//-----------------------------------------------------------------------------------
		// Create Finders for find user by email address and find user role by user 
		//-----------------------------------------------------------------------------------
		shell.executeCommand("finder add findUserModelsByEmailAddress --class ~.model.UserModel");
		shell.executeCommand("finder add findUserRoleModelsByUserEntry --class ~.model.UserRoleModel");

	}

	/**
	 * Create an Controller for User, Role and UserRole
	 * @param entityPackage
	 * @param controllerPackage
	 */
	private void createControllers(String entityPackage,
			String controllerPackage) {

		//-----------------------------------------------------------------------------------
		// Controller for User 
		//-----------------------------------------------------------------------------------
		shell.executeCommand("controller scaffold --class " + controllerPackage
				+ "UserModelController --entity " + entityPackage
				+ ".UserModel");

		//-----------------------------------------------------------------------------------
		// Controller for Role
		//-----------------------------------------------------------------------------------		
		shell.executeCommand("controller scaffold --class " + controllerPackage
				+ "RoleModelController --entity " + entityPackage
				+ ".RoleModel");
		
		//-----------------------------------------------------------------------------------
		// Controller for User Role
		//-----------------------------------------------------------------------------------		
		shell.executeCommand("controller scaffold --class " + controllerPackage
				+ "UserRoleModelController --entity " + entityPackage
				+ ".UserRoleModel");

	}

	/**
	 * Inject database based authentication provider in Spring Security
	 * @param entityPackage
	 */
	private void injectDatabasebasedSecurity(String entityPackage) {
		
		//----------------------------------------------------------------------
		// Run Security Setup Addon
		//----------------------------------------------------------------------
		shell.executeCommand("security setup");
		
		//----------------------------------------------------------------------
		// Copy DatabaseAuthenticationProvider from template 
		//----------------------------------------------------------------------
		createAuthenticationProvider(entityPackage);
		
		//----------------------------------------------------------------------
		// Inject database based authentication provider into applicationContext-security.xml
		//----------------------------------------------------------------------
		injectDatabasebasedAuthProviderInXml(entityPackage);
	}

	/**
	 * Inject database based authentication provider into applicationContext-security.xml
	 * @param entityPackage
	 */
	private void injectDatabasebasedAuthProviderInXml(String entityPackage){
		String springSecurity = pathResolver.getIdentifier(
				Path.SRC_MAIN_RESOURCES,
				"META-INF/spring/applicationContext-security.xml");

		MutableFile mutableConfigXml = null;
		Document webConfigDoc;
		try {
			if (fileManager.exists(springSecurity)) {
				mutableConfigXml = fileManager.updateFile(springSecurity);
				webConfigDoc = XmlUtils.getDocumentBuilder().parse(
						mutableConfigXml.getInputStream());
			} else {
				throw new IllegalStateException("Could not acquire "
						+ springSecurity);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		Element firstInterceptUrl = XmlUtils.findFirstElementByName(
				"intercept-url", webConfigDoc.getDocumentElement());
		Assert.notNull(firstInterceptUrl, "Could not find intercept-url in "
				+ springSecurity);

		firstInterceptUrl.getParentNode().insertBefore(
				new XmlElementBuilder("intercept-url", webConfigDoc)
						.addAttribute("pattern", "/")
						.addAttribute("access", "isAuthenticated()").build(),
				firstInterceptUrl);

		JavaPackage topLevelPackage = getProjectMetadata().getTopLevelPackage();

		String authenticationProviderClass = topLevelPackage
				.getFullyQualifiedPackageName()
				+ ".provider.DatabaseAuthenticationProvider";


		Element databaseAuthenticationProviderBean = new XmlElementBuilder(
				"beans:bean", webConfigDoc)
				.addAttribute("id", "databaseAuthenticationProvider")
				.addAttribute("class", authenticationProviderClass)
				.addChild(
						new XmlElementBuilder("beans:property", webConfigDoc)
								.addAttribute("name", "adminUser")
								.addAttribute("value", "admin").build())
				.addChild(
						new XmlElementBuilder("beans:property", webConfigDoc)
								.addAttribute("name", "adminPassword")
								.addAttribute("value", "admin").build())
				.build();

		Element authenticationManager = XmlUtils.findFirstElementByName(
				"authentication-manager", webConfigDoc.getDocumentElement());

		authenticationManager.getParentNode().insertBefore(
				databaseAuthenticationProviderBean, authenticationManager);

		Element oldAuthProvider = XmlUtils.findFirstElementByName(
				"authentication-provider", webConfigDoc.getDocumentElement());

		// <authentication-provider ref="databaseAuthenticationProvider" />

		Element newAuthProvider = new XmlElementBuilder(
				"authentication-provider", webConfigDoc).addAttribute("ref",
				"databaseAuthenticationProvider").build();
		authenticationManager.replaceChild(newAuthProvider, oldAuthProvider);

		XmlUtils.writeXml(mutableConfigXml.getOutputStream(), webConfigDoc);

	}
	
	/**
	 * Copy DatabaseAuthenticationProvider from template 
	 * @param entityPackage
	 */
	private void createAuthenticationProvider(String entityPackage) {
		JavaPackage topLevelPackage = getProjectMetadata().getTopLevelPackage();

		String packagePath = topLevelPackage.getFullyQualifiedPackageName()
				.replace('.', separator);
		String destinationFile = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
				packagePath + separator + "provider" + separator
						+ "DatabaseAuthenticationProvider.java");
		String finalEntityPackage = entityPackage.replace("~",
				topLevelPackage.getFullyQualifiedPackageName());

		try {
			InputStream in = TemplateUtils.getTemplate(getClass(),
					"DatabaseAuthenticationProvider.java-template");
			InputStreamReader reader = new InputStreamReader(in);
			String input = FileCopyUtils.copyToString(reader);

			input = input.replaceAll("__TOP_LEVEL_PACKAGE__",
					topLevelPackage.getFullyQualifiedPackageName());
			input = input.replaceAll("__ENTITY_LEVEL_PACKAGE__",
					finalEntityPackage);

			MutableFile mutableFile = fileManager.createFile(destinationFile);
			FileCopyUtils.copy(input.getBytes(), mutableFile.getOutputStream());

		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to create '"
					+ destinationFile + "'", ioe);
		}

	}

	/**
	 * @return the path resolver or null if there is no user project
	 */
	private PathResolver getPathResolver() {
		ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
				.get(ProjectMetadata.getProjectIdentifier());
		if (projectMetadata == null) {
			return null;
		}
		return projectMetadata.getPathResolver();
	}

	/**
	 * 
	 * @return the project metadata
	 */
	private ProjectMetadata getProjectMetadata() {
		return (ProjectMetadata) metadataService.get(ProjectMetadata
				.getProjectIdentifier());
	}

}