package se.romanredz.mouse.mousemavenplugin;

import mouse.MakeRuntimeWrapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes PEG-generated parser
 *
 * @goal makeruntime
 * @requiresProject false
 * @deprecated does not work correctly
 */
public class MakeRuntime extends AbstractMojo {
    /**
     * Generate files with package name runtime-package. Mandatory.
     *
     * @required
     * @parameter expression="${mouse.runtimePackage}" alias="runtimePackage"
     */
    private String mouseRuntimePackage;

    /**
     * Identifies target directory to receive the files.
     * Optional. If omitted, files are written to the current work directory.
     * The directory need not be a complete path, just enough to identify the directory
     * in current environment. The directory must exist.
     * The directory name need not correspond to the package name.
     *
     * @parameter expression="${mouse.directory}" alias="directory"
     */
    private File mouseDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MakeRuntimeWrapper wrapper = new MakeRuntimeWrapper();
        List<String> args = new ArrayList<String>();

        args.add("-r");
        args.add(mouseRuntimePackage);

        if (mouseDirectory != null) {
            if (!mouseDirectory.exists() || !mouseDirectory.isDirectory()) {
                getLog().error("directory path is not a directory");
                return;
            }
            args.add("-D");
            args.add(mouseDirectory.getAbsolutePath());
        }

        try {
            wrapper.run(args.toArray(new String[args.size()]));
        } catch (Exception e) {
            getLog().error(e);
        }
    }
}
