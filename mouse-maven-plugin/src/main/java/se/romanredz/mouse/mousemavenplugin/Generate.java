package se.romanredz.mouse.mousemavenplugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import mouse.GenerateWrapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Generates a mouse peg .java file with parser for specified .peg config
 *
 * @goal generate
 * @phase generate-sources
 * @requiresProject true
 */
public class Generate extends AbstractMojo {

    /**
     * Identifies the file containing your grammar. Mandatory.
     * The filename need not be a complete path, just enough to identify the file
     * in current environment. It should include file extension, if any.
     *
     * @required
     * @parameter expression="${mouse.grammar}" alias="grammar"
     */
    private File mouseGrammar;

    /**
     * Identifies target directory to receive the generated file(s).
     * Optional. If omitted, files are generated in current work directory.
     * The directory need not be a complete path, just enough to identify the directory
     * in current environment. The directory must exist.
     *
     * @parameter expression="${mouse.directory}" alias="directory"
     */
    private File mouseDirectory;

    /**
     * Specifies name of the parser to be generated. Mandatory.
     * Must be an unqualified class name. The tool generates a file named ”parser .java” in the target
     * directory, The file contains definition of Java class parser. If target directory already contains a file
     * ”parser .java”, the file is replaced without a warning,
     *
     * @required
     * @parameter expression="${mouse.parser}" alias="parser"
     */
    private String mouseParser;


    /**
     * Indicates that semantic actions are methods in the Java class semantics.
     * Mandatory if your grammar specifies semantic actions. Must be an unqualified class name.
     *
     * @parameter expression="${mouse.semantics}" alias="semantics" default-value="mouse.runtime.SemanticsBase"
     */
    private String mouseSemantics;

    /**
     * Generate parser as member of package package.
     * The semantics class, if specified, is assumed to belong to the same package.
     * Optional. If not specified, both classes belong to unnamed package.
     * The specified package need not correspond to the target directory.
     *
     * @parameter expression="${mouse.package}" alias="package"
     */
    private String mousePackage;

    /**
     * Generate parser that will use runtime support package runtime-package.
     * Optional. If not specified, mouse.runtime is assumed.
     *
     * @parameter expression="${mouse.runtimePackage}" alias="runtimePackage"
     */
    private String mouseRuntimePackage;

    /**
     * The skeleton is generated as file ”semantics.java” in the target directory, where semantics is the
     * name specified by -S option. The option is ignored if -S is not specified. If target directory already
     * contains a file ”semantics.java”, the tool is not executed, to prevent accidental destruction of your
     * semantics class.
     *
     * @parameter expression="${mouse.skeleton}" alias="skeleton"
     */
    private boolean mouseSkeleton;

    /**
     * Generate memoizing version of the parser.
     *
     * @parameter expression="${mouse.memoizing}" alias="memoizing"
     */
    private boolean mouseMemoizing;

    /**
     * Generate instrumented test version of the parser.
     *
     * @parameter expression="${mouse.instrumented}" alias="instrumented"
     */
    private boolean mouseInstrumented;


    /**
     * @required
     * @readonly
     * @parameter expression="${project}"
     */
    private MavenProject project;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        GenerateWrapper wrapper = new GenerateWrapper();
        List<String> args = new ArrayList<String>();
        StringBuilder message = new StringBuilder();
        message.append("Generating Mouse PEG parser from ");

        if (!mouseGrammar.exists() || !mouseGrammar.isFile()) {
            getLog().error("mouseGrammar file is not exists or not a regular file");
            return;
        }

        if (mouseDirectory != null) {
            if (!mouseDirectory.exists() || !mouseDirectory.isDirectory()) {
                getLog().error("mouseDirectory is not exists or not a directory");
                return;
            }
        }

        if (mouseInstrumented && mouseMemoizing) {
            getLog().error("instrumented and memoized cannot be both specified at the same time");
            return;
        }

        args.add("-G");
        args.add(mouseGrammar.getAbsolutePath());
        message.append(mouseGrammar.getAbsolutePath());
        message.append(" into ");

        if (mouseDirectory != null) {
            args.add("-D");
            args.add(mouseDirectory.getAbsolutePath());
            message.append(mouseDirectory.getAbsolutePath());
            message.append("/");
            this.project.addCompileSourceRoot(mouseDirectory.getAbsolutePath());
        }

        args.add("-P");
        args.add(mouseParser);
        message.append(mouseParser);
        message.append(".java");

        if (mouseSemantics != null) {
            args.add("-S");
            args.add(mouseSemantics);
            message.append(" using semantics ");
            message.append(mouseSemantics);
        }

        if (mousePackage != null) {
            args.add("-p");
            args.add(mousePackage);
            message.append(" in package ");
            message.append(mousePackage);
        }

        if (mouseRuntimePackage != null) {
            args.add("-r");
            args.add(mouseRuntimePackage);
        }

        if (mouseMemoizing) {
            args.add("-M");
        }

        if (mouseInstrumented) {
            args.add("-T");
        }

        getLog().info(message.toString());
        String[] argsArr = args.toArray(new String[args.size()]);
        wrapper.run(argsArr);
    }
}
