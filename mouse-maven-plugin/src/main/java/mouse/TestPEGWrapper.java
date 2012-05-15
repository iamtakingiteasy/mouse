package mouse;

import mouse.peg.PEG;
import mouse.runtime.SourceFile;
import mouse.utility.CommandArgs;

/**
 * Alexander <itakingiteasy> Tumin
 * Created: 2012-05-15 21:03
 */
public class TestPEGWrapper extends TestPEG {
    public void run(String[] args) {
        //---------------------------------------------------------------
        //  Parse arguments.
        //---------------------------------------------------------------
        CommandArgs cmd = new CommandArgs
                (args,      // arguments to parse
                        "CDR",     // options without argument
                        "G",       // options with argument
                        0,0);     // no positional arguments
        if (cmd.nErrors()>0) return;

        String gramName = cmd.optArg('G');
        if (gramName==null)
        {
            System.err.println("Specify -G grammar file.");
            return;
        }

        SourceFile src = new SourceFile(gramName);
        if (!src.created()) return;

        //---------------------------------------------------------------
        //  Create PEG object from source file.
        //---------------------------------------------------------------
        PEG peg = new PEG(src);
        if (peg.errors>0) return;

        // System.out.println(peg.iterAt + " iterations for attributes.");
        // System.out.println(peg.iterWF + " iterations for well-formed.");

        if (peg.notWF==0)
            System.out.println("The grammar is well-formed.");

        //---------------------------------------------------------------
        //  Display as requested.
        //---------------------------------------------------------------
        if (cmd.opt('C'))
        {
            peg.compact();
            peg.showAll();
        }
        else if (cmd.opt('D')) peg.showAll();
        else if (cmd.opt('R')) peg.showRules();
    }
}
