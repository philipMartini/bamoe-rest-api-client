# Contribute to this project

To contribute to the project you need to be flagged as one of the project developer. Write an email to one of the Authors in README.md and request to be enabled as one of the developers.

Then just clone the project and start typing your code, being bug fixes or new features. Once you are done just push your feature branch and open a merge request to one of the mantainers.

## Coding Style

Use your preferred code style if you want, we will not refuse a merge request for that reason alone. However please keep in mind the following:

* Name Interfaces using C# (.net) notation, i.e. starting with capital I and then following with standard class naming convention *i.e. IMyCustomInterface*
* Provide Javadoc for **all** public methods and fields. If you are overriding a method you *can* annotate it with 

```
/**
 * {@inheritDoc} 
 */
```
* If possible provide Javadoc for protected, package and private methods and fields. This will help later developer with understanding your code
* Use english when commenting, writing Javadoc, writing commit messages and naming variables and methods.
* **Always** write unit test for your code. The project already includes [JUnit5](https://junit.org/junit5/) and [Mockito](https://site.mockito.org/) as testing framework
* When updating dependencies or adding new one please include the **latest stable** release (no beta no alpha no Mx or nightly build)
* Please write your code so that it is clear and contains no smells. The project should be validated using a plugin for your IDE such as SonarLint