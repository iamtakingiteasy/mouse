package se.romanredz.mouse.mousemavenplugin;

import mouse.TestPEGWrapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies PEG syntax
 *
 * @goal check
 * @requiresProject false
 */
public class TestPEG extends AbstractMojo {

    /**
     * Identifies the file that contains your grammar. Mandatory.
     * The filename should include any extension.
     * Need not be a complete path, just enough to identify the file in your environment.
     *
     * @required
     * @parameter expression="${mouse.grammar}" alias="grammar"
     */
    private File mouseGrammar;


    /**
     * Display the grammar. Optional.
     * Shows the rules and subexpressions together with their attributes according to Ford.
     * (0=may consume empty string, 1=may consume nonempty string, f= may fail, !WF=not well-formed.)
     *
     * @parameter expression="${mouse.displayGrammar}" alias="displayGrammar"
     */
    private boolean mouseDisplayGrammar;

    /**
     * Display the grammar in compact form: without duplicate subexpressions. Optional.
     *
     * @parameter expression="${mouse.displayGrammarCompact}" alias="displayGrammarCompact"
     */
    private boolean mouseDisplayGrammarCompact;

    /**
     * Display only the rules. Optional
     *
     * @parameter expression="${mouse.displayOnlyRules}" alias="displayOnlyRules"
     */
    private boolean mouseDisplayOnlyRules;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        TestPEGWrapper wrapper = new TestPEGWrapper();
        List<String> args = new ArrayList<String>();

        if (!mouseGrammar.exists() || !mouseGrammar.isFile()) {
            getLog().error("mouseGrammar file is not exists or not a regular file");
            return;
        }

        if ((mouseDisplayGrammar && mouseDisplayGrammarCompact) || (mouseDisplayGrammar && mouseDisplayOnlyRules) || mouseDisplayGrammarCompact && mouseDisplayOnlyRules) {
            getLog().error("Options displayGrammar, displayGrammarCompact and displayOnlyRules are mutualy exclusive");
            return;
        }

        args.add("-G");
        args.add(mouseGrammar.getAbsolutePath());

        if (mouseDisplayGrammar) {
            args.add("-D");
        } else if (mouseDisplayGrammarCompact) {
            args.add("-C");
        } else if (mouseDisplayOnlyRules) {
            args.add("-R");
        }

        wrapper.run(args.toArray(new String[args.size()]));

    }
}
