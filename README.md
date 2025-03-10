# BCS1600 Spring 2025 Project Skeleton

The files in this repository can be used as a basis for your implementation of the Routing
Engine portion of the project.

[Maven]: https://maven.apache.org/

## Building

Use [Maven][] to build, clean, test, and start your program. The command-line tool is called
`mvn`, and IDEs like IntelliJ IDEA and Visual Studio Code have built-in support for Maven
actions. Maven is configured with the [`pom.xml`](pom.xml) file in the root directory of your
repository.

 - `mvn clean`: removes the `target/` directory (but note that e.g. vscode will immediately put
   it back if vscode is open on the project!).

 - `mvn compile`: build the main project.

 - `mvn test`: run the test suite.

 - `mvn exec:java`: invoke the main class of the project (`exec.mainClass` property in
   `pom.xml`). Use the `-q` flag to silence Maven's own output: `mvn -q exec:java`.

## Files

 - `pom.xml`: [Maven][] configuration file.

 - `src/main/java/RoutingEngine.java`: Skeleton Routing Engine server implementation for you to
   build on.
