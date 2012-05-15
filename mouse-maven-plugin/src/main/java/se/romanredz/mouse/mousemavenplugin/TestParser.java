package se.romanredz.mouse.mousemavenplugin;

import mouse.TestParserWrapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Testes PEG-generated parser
 *
 * @goal test
 * @requiresProject false
 */
public class TestParser extends AbstractMojo {
    /**
     * Identifies the parser. Mandatory.
     * parser is the class name, fully qualified with package name, if applicable. The class must reside in a
     * directory corresponding to the package.
     *
     * @required
     * @parameter expression="${mouse.parser}" alias="parser"
     */
    private String mouseParser;

    /**
     * Apply the parser to file filename. Optional.
     * The filename should include any extension.
     * Need not be a complete path, just enough to identify the file in your environment.
     *
     * @parameter expression="${mouse.fileName}" alias="fileName"
     */
    private File mouseFilename;

    /**
     * Apply the parser separately to each file in a list of files. Optional.
     * The list identifies a text file containing one fully qualified file name per line. The list itself need not
     * be a complete path, just enough to identify the file in your environment.
     *
     *  @parameter expression="${mouse.fileList}" alias="fileList"
     */
    private File mouseFileList;


    /**
     * Amount of memoization. Optional.
     * n is a digit from 0 through 9 specifying the number of results to be retained. Default is -m0.
     *
     *  @parameter expression="${mouse.memoLevel}" alias="memoLevel" default-value=0
     */
    private int mouseMemoLevel;

    /**
     * Tracing switches. Optional,
     * The string is assigned to the trace field in your semantics object, where you can use it to trigger any
     * trace you have programmed there. In addition, presence of certain letters in string activates traces in
     * the parser:
     * r – trace execution of parsing procedures for rules.
     * i – trace execution of parsing procedures for inner expressions.
     * e – trace error information.
     *
     * @parameter expression="${mouse.trace}" alias="mousetrace"
     */
    private String mouseTrace;

    /**
     * Show detailed statistics for procedures involved in backtracking - rescan - reuse. Optional.
     *
     * @parameter expression="${mouse.showDetailedBacktracking}" alias="showDetailedBacktracking"
     */
    private boolean mouseShowDetailedBacktracking;

    /**
     * Show detailed statistics for all invoked procedures. Optional.
     *
     * @parameter expression="${mouse.showDetailedProcedure}" alias="showDetailedProcedure"
     */
    private boolean mouseShowDetailedProcedure;



    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        TestParserWrapper wrapper = new TestParserWrapper();
        List<String> args = new ArrayList<String>();

        args.add("-P");
        args.add(mouseParser);

        if (mouseFilename != null) {
            if (!mouseFilename.exists() || !mouseFilename.isFile()) {
                getLog().error("mouseFilename is not exists or not a file");
                return;
            }
            args.add("-f");
            args.add(mouseFilename.getAbsolutePath());
        }

        if (mouseFileList != null) {
            if (!mouseFileList.exists() || !mouseFileList.isFile()) {
                getLog().error("mouseFileList is not exists or not a file");
                return;
            }
            args.add("-F");
            args.add(mouseFileList.getAbsolutePath());
        }

        args.add("-m");
        args.add(String.valueOf(mouseMemoLevel));

        if (mouseTrace != null) {
            args.add("-T");
            args.add(mouseTrace);
        }

        if (mouseShowDetailedBacktracking && mouseShowDetailedProcedure) {
            getLog().error("showDetailedBacktracking and showDetailedProcedure cannot be declared simultaneously");
            return;
        }

        if (mouseShowDetailedBacktracking) {
            args.add("-d");
        } else if (mouseShowDetailedProcedure) {
            args.add("-D");
        }

        try {
            wrapper.run(args.toArray(new String[args.size()]));
        } catch (InvocationTargetException e) {
            getLog().error(e);
        } catch (IllegalAccessException e) {
            getLog().error(e);
        } catch (ClassNotFoundException e) {
            getLog().error(e);
        } catch (IOException e) {
            getLog().error(e);
        } catch (NoSuchMethodException e) {
            getLog().error(e);
        } catch (InstantiationException e) {
            getLog().error(e);
        }
    }
}
