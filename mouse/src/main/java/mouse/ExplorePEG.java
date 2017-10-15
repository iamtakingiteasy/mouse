//=========================================================================
//
//  Part of PEG parser generator Mouse.
//
//  Copyright (C) 2017 by Roman R. Redziejowski (www.romanredz.se).
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//-------------------------------------------------------------------------
//
//  Change log
//    170301 Created.
//    170901 Version 1.9.1
//      Display diagnostic notes in PEG_Info window.
//
//=========================================================================

package mouse;

import mouse.utility.CommandArgs;
import mouse.runtime.SourceFile;
import mouse.explorer.*;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  ExplorePEG
//
//-------------------------------------------------------------------------
//
//  Explore the grammar.
//
//  Invocation
//
//    java mouse.ExplorePEG <arguments>
//
//  The <arguments> are specified as options according to POSIX syntax:
//
//    -G <filename>
//       Identifies the file containing the grammar. Mandatory.
//       The <filename> need not be a complete path,
//       just enough to identify the file in current environment.
//       Should include file extension, if any.
//
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public class ExplorePEG
{
  //=====================================================================
  //
  //  Main
  //
  //=====================================================================

  public static void main(String argv[])
    {
      //---------------------------------------------------------------
      //  Parse arguments.
      //---------------------------------------------------------------
      CommandArgs cmd = new CommandArgs
             (argv,      // arguments to parse
              "",        // options without argument
              "G",       // options with argument
               0,0);     // no positional arguments
      if (cmd.nErrors()>0) return;

      //---------------------------------------------------------------
      //  Get grammar name.
      //---------------------------------------------------------------
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
      PEG.parse(src);
      if (PEG.errors>0) return;

      //---------------------------------------------------------------
      //  Display grammar window.
      //---------------------------------------------------------------
      GUI_Grammar.display();

      //---------------------------------------------------------------
      //  Display any diagnostic notes.
      //---------------------------------------------------------------
      if (PEG.notes!=null)
        GUI_Info.display(PEG.notes,"Notes");
   }
}
