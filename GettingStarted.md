# Getting Started - Start with example roo script #
Create a setup.roo file using following contents
## Setup ##
### Uninstall previous version ###
```
addon remove --bundleSymbolicName com.xsoftwarelabs.spring.roo.addon.typicalsecurity
```
### Install the Typical Security Addon ###
**If you are using a version of Roo that is older than 0.1.2**
```
osgi start --url http://spring-roo-addon-typical-security.googlecode.com/files/com.xsoftwarelabs.spring.roo.addon.typicalsecurity-0.1.4.BUILD-SNAPSHOT.jar
```
**If you are using Roo version 0.1.2 or newer.**
```
osgi start --url http://spring-roo-addon-typical-security.googlecode.com/files/com.xsoftwarelabs.spring.roo.addon.typicalsecurity-0.1.5.BUILD-SNAPSHOT.jar
```

## Create a Sample Project ##
```
project --topLevelPackage com.testproject2 --projectName TestProject2
```
//Typical Security Addon needs persistence, other wise it doesn't show itself
```
persistence setup --provider HIBERNATE --database HYPERSONIC_IN_MEMORY
```
## Usage ##
//Run the TypicalSecurity setup addon in default mode
```
typicalsecurity setup
```


//After creating project using this addon, update the email.properties file with your email id and password to avoid spamming. location:`//<projectname>/target/classes/META-INF/spring`

//Run tomcat to see the effects
```
mvn tomcat:run
```

# Tutorial #

Youtube Video - http://www.youtube.com/watch?v=Y-kuYj8vsYU

<a href='http://www.youtube.com/watch?feature=player_embedded&v=Y-kuYj8vsYU' target='_blank'><img src='http://img.youtube.com/vi/Y-kuYj8vsYU/0.jpg' width='425' height=344 /></a>


# Installation #

Run following code on Roo Shell
```
$roo >osgi start --url http://spring-roo-addon-typical-security.googlecode.com/files/com.xsoftwarelabs.spring.roo.addon.typicalsecurity-0.1.4.BUILD-SNAPSHOT.jar


$roo >osgi ps
```
You will see a line as follows, which indicates addon has been installed

`[  72] [Active     ] [    1] spring-roo-addon-typical-security (0.1.4.BUILD-SNAPSHOT)`

# Uninstallation #
```
$roo > addon remove --bundleSymbolicName com.xsoftwarelabs.spring.roo.addon.typicalsecurity
```
# Usage #
```
$roo > project --topLevelPackage com.test --projectName `TestProject`

$roo > persistence setup --database ... --provider ....

$roo > typicalsecurity setup
```

Alternatively, you can provide options to typicalsecurity setup
```
$roo > typicalsecurity setup --entityPackage ~.model --controllerPackage ~.web
```
Once you have injected Typical Security
```
$> mvn tomcat:run
```
and go to http://localhost:8080/TestProject

This will take you to login page, login using "admin" and "admin". You will see Controllers for `UserModel`.

Create `UserModel` with a email address and password.

Now logout and login using newly created `UserModel`'s email address and password.


# Effect #

The addon does following

1. Creates Entity classes namely `UserModel`, `RoleModel`, `UserRoleModel`

2. Create Controllers for `UserModel`, `RoleModel`, `UserRoleModel`

3. Run "security setup" addon

4. Inject Database based Authentication Provider into applicationContext-security.xml

5. Project "/" url with access `IsAuthenticated()`


# Code Changes #

In version 0.1.4, the following changes have been added.
  1. Password encryption
  1. Fixed SimpleMailMessage Autowire bug in SignUpController.java and ForgotPasswordController.java
  1. Fixed activationDate
  1. Fixed the finders
  1. Renamed UserModel to User and RoleModel to Role.
  1. Renamed the Typicalsecurity command to typicalsecurity
  1. Set the default entity package to ~.domain (to align with how the rest of roo works).