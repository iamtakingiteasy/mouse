package se.romanredz.mouse.mousemavenplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a mouse peg .java file with parser for specified .peg config
 *
 * @goal explore
 * @requiresProject false
 */
public class ExplorePEG extends AbstractMojo {
    /**
     * Identifies the file containing your grammar. Mandatory.
     * The filename need not be a complete path, just enough to identify the file
     * in current environment. It should include file extension, if any.
     *
     * @required
     * @parameter expression="${mouse.grammar}" alias="grammar"
     */
    private File mouseGrammar;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<String> args = new ArrayList<>();

        if (!mouseGrammar.exists() || !mouseGrammar.isFile()) {
            getLog().error("mouseGrammar file is not exists or not a regular file");
            return;
        }

        args.add("-G");
        args.add(mouseGrammar.getAbsolutePath());

        mouse.ExplorePEG.main(args.toArray(new String[args.size()]));

        final Frame frame = mouse.explorer.GUI.getFrames()[0];

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synchronized (frame) {
                    frame.setVisible(false);
                    frame.notify();
                }
            }
        });

        frame.setVisible(true);
        synchronized (frame) {
            while (frame.isVisible()) {
                try {
                    frame.wait();
                } catch (InterruptedException e) {
                    getLog().error(e);
                }
            }
        }
    }
}
