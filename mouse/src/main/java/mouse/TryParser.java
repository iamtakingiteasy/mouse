//=========================================================================
//
//  Part of PEG parser generator Mouse.
//
//  Copyright (C) 2009, 2010, 2012
//  by Roman R. Redziejowski (www.romanredz.se).
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
//    090701 License changed by the author to Apache v.2.
//    090714 Write ok/failed message to System.out instead of err.
//    090810 Modified for Mouse 1.1.
//   Version 1.2
//    100411 Specified invocation in comment to the class.
//    100411 Added general catch for all exceptions.
//    100411 Changed structure to the same as TestParser.
//   Version 1.3
//    101206 Removed general catch. Added catch for exceptions that can
//           indicate user error. Declared other exceptions.
//    101208 Removed undocumented possibility to comment out files
//           from file list supplied with '-F'.
//   Version 1.5.1
//    120102 (Steve Owens) Removed unused import.
//
//=========================================================================


package mouse;

import mouse.runtime.Source;
import mouse.runtime.SourceFile;
import mouse.runtime.SourceString;
import mouse.utility.CommandArgs;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Vector;



//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  TryParser
//
//-------------------------------------------------------------------------
//
//  Run the generated parser.
//
//  Invocation
//
//    java mouse.TryParser <arguments>
//
//  The <arguments> are specified as options according to POSIX syntax:
//
//    -P <parser>
//       Identifies the parser. Mandatory.
//       <parser> is the class name, fully qualified with package name,
//       if applicable. The class must reside in a directory corresponding
//       to the package.
//
//    -f <file>
//       Apply the parser to file <file>. Optional.
//       The <file> should include any extension.
//       Need not be a complete path, just enough to identify the file
//       in the current environment.
//
//    -F <list>
//       Apply the parser separately to each file in a list of files. Optional.
//       The <list> identifies a text file containing one fully qualified
//       file name per line.
//       The <list> itself need not be a complete path, just enough
//       to identify the file in the current environment.
//
//    -m <n>
//       Amount of memoization. Optional.
//       Applicable only to a parser generated with option -M.
//       <n> is a digit from 1 through 9 specifying the number of results
//       to be cached. Default is -m1.
//
//    -T <string>
//       Tracing switches. Optional.
//       The <string> is assigned to the 'trace' field in your semantics
//       object, where it can be used it to activate any trace
//       programmed there.
//
//  If you do not specify -f or -F,  the parser is executed interactively,
//  prompting for input by printing '>'.
//  It is invoked separately for each input line after you press 'Enter'.
//  You terminate the session by pressing 'Enter' directly at the prompt.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

class TryParser
{
  //=====================================================================
  //
  //  Data
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Parser class under test.
  //-------------------------------------------------------------------
  Class<?> parserClass;
  Method settrace; // Set amount of memo
  Method setmemo;  // Set amount of memo
  Method parse;    // Run parser

  //-------------------------------------------------------------------
  //  Instantiated paser.
  //-------------------------------------------------------------------
  Object parser;

  //-------------------------------------------------------------------
  //  List of files to apply (file names).
  //-------------------------------------------------------------------
  Vector<String> files = null;

  //-------------------------------------------------------------------
  //  Amount of memo.
  //-------------------------------------------------------------------
  int m = 1;

  //-------------------------------------------------------------------
  //  Trace switches.
  //-------------------------------------------------------------------
  String trace;


  //=====================================================================
  //
  //  Invocation
  //
  //=====================================================================

  public static void main(String argv[])
    throws IOException,IllegalAccessException,InvocationTargetException,
           InstantiationException,ClassNotFoundException,
           NoSuchMethodException
    {
      TryParser test = new TryParser();

      if (!test.init(argv)) return;

      if (test.files==null)
        test.interact();

      else
        for (String name: test.files)
          test.run(name);
    }


  //=====================================================================
  //
  //  Set up information for testing.
  //
  //=====================================================================

  boolean init(String[] argv)
    throws IOException,IllegalAccessException,InvocationTargetException,
           InstantiationException,ClassNotFoundException,
           NoSuchMethodException
    {
      //---------------------------------------------------------------
      //  Get command arguments.
      //---------------------------------------------------------------
      CommandArgs cmd = new CommandArgs
             (argv,      // arguments to parse
              "",        // options without argument
              "PFfmT",   // options with argument
              0,0);      // no positional arguments
      if (cmd.nErrors()>0) return false;

      //---------------------------------------------------------------
      //  Get parser name.
      //---------------------------------------------------------------
      String parsName = cmd.optArg('P');

      if (parsName==null)
      {
        System.err.println("Specify -P parser name.");
        return false;
      }

      //---------------------------------------------------------------
      //  Find the parser.
      //---------------------------------------------------------------
      try{parserClass = Class.forName(parsName);}
      catch (ClassNotFoundException e)
      {
        System.err.println("Parser '" + parsName + "' not found.");
        return false;
      }

      //---------------------------------------------------------------
      //  Find the 'parse' and 'setTrace' methods of parser.
      //---------------------------------------------------------------
      try {parse = parserClass.getMethod("parse",Class.forName("mouse.runtime.Source"));}
      catch (ClassNotFoundException e)
      {
        System.err.println("Class 'mouse.runtime.Source' not found.");
        return false;
      }

      settrace = parserClass.getMethod("setTrace",Class.forName("java.lang.String"));

      //---------------------------------------------------------------
      //  Find the 'setMemo' method of the parser.
      //  It is present only in memoizing parser.
      //---------------------------------------------------------------
      setmemo = null;
      try {setmemo = parserClass.getMethod("setMemo",int.class);}
      catch (NoSuchMethodException e) {}

      //---------------------------------------------------------------
      //  Process the -m option.
      //---------------------------------------------------------------
      String memo = cmd.optArg('m');

      if (setmemo==null && memo!=null)
      {
        System.err.println(parsName + " is not a memo version.");
        return false;
      }

      if (memo!=null)
      {
        if (memo.length()!=1) m = -1;
        else m = "0123456789".indexOf(memo.charAt(0));

        if (m<1)
        {
          System.err.println("-m is outside the range 1-9.");
          return false;
        }
      }

      //---------------------------------------------------------------
      //  Get trace switches.
      //---------------------------------------------------------------
      trace = cmd.optArg('T');
      if (trace==null) trace = "";

      //---------------------------------------------------------------
      //  Instantiate the parser, set trace and (optionally) memo.
      //---------------------------------------------------------------
      parser = parserClass.newInstance();
      settrace.invoke(parser,trace);
      if (setmemo!=null) setmemo.invoke(parser,m);

      //---------------------------------------------------------------
      //  If no input files given, return to run parser interactively.
      //---------------------------------------------------------------
      if (!cmd.opt('f') && !cmd.opt('F'))
        return true;

      //---------------------------------------------------------------
      //  Get file name(s).
      //---------------------------------------------------------------
      files = cmd.optArgs('f');

      String listName = cmd.optArg('F');

      if (listName!=null)
      {
        BufferedReader reader;
        try {reader = new BufferedReader(new FileReader(listName));}
        catch (FileNotFoundException e)
        {
          System.err.println("File '" + listName + "' was not found");
          return false;
        }
        String line = reader.readLine();
        while (line!=null)
        {
          files.add(line);
          line = reader.readLine();
        }
      }

      if (files.size()==0)
      {
         System.err.println("No files to test.");
         return false;
      }

      return true;
    }


  //=====================================================================
  //
  //  Run parser on file 'name'
  //
  //=====================================================================

  boolean run(final String name)
    throws IllegalAccessException,InvocationTargetException
    {
      Source src = new SourceFile(name);
      if (!src.created()) return false;

      boolean result = (Boolean)(parse.invoke(parser,src));

      System.out.println("\n" + name + ": " + (result? "ok" : "failed"));
      if (!result) return false;
      return true;
    }


  //=====================================================================
  //
  //  Run parser interactively
  //
  //=====================================================================

  void interact()
    throws IOException,IllegalAccessException,InvocationTargetException
    {
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String input;
      while (true)
      {
        System.out.print("> ");
        input = in.readLine();
        if (input.length()==0) return;
        SourceString src = new SourceString(input);
        parse.invoke(parser,src);
      }
    }
}