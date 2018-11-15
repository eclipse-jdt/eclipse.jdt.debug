## Updating ASM library version

1. Change `org.ow2.asm:asm` version in `pom.xml` file. This defines version of ASM used by the javaagent at runtime and during Maven build on command line.
2. Change `org.objectweb.asm` bundle-version in `META-INF/MANIFEST.MF`. This defines version of ASM used to compile the javaagent in PDE. **This has no effect on command line Maven build or runtime!**.
	* Make sure the new version of ASM is part of PDE target platform. You may need to copy ASM jar to the target platform manually as PDE does not download project dependencies automatically.
3. If adding support for new Java classfile version
   - Update `ClassfileTransformer#ASM_API` to indicate ASM API version used by the javaagent. This defines what bytecode instructions ASM is able to interpret and process. Classfiles that use unsupported instructions will fail instrumentation and the javaagent will print `Could not instrument class ...` error message to stderr.
   - Update `ClassfileTransformer#MAX_CLASS_MAJOR` to match maximum java classfile version. The javaagent silently skips instrumentation of classfiles with newer version.
   - Update `StratumTests#testAvailableStrata` to indicate Java version(s) that are not supported by the javaagent. Typically this is N+1 compared to `ClassfileTransformer#MAX_CLASS_MAJOR`.
4. Build the javaagent jar file by running `mvn clean package` command from `org.eclipse.jdt.launching.javaagent/` directory. This creates  `org.eclipse.jdt.launching.javaagent/target/javaagent-shaded.jar` jar file, which includes the javaagent and ASM classes. 
   * Note that ASM classes are _relocated_ to `org.eclipse.jdt.launching.internal.org.objectweb.asm` package to avoid possible conflicts with applicates being debugged.
5. Copy `javaagent-shaded.jar` to `org.eclipse.jdt.launching/lib` folder. This is the javaagent jar used at runtime.
6. Run the tests, ideally using all supported java versions.
7. Commit all changed files to git and submit the changes to Gerrit for review.
