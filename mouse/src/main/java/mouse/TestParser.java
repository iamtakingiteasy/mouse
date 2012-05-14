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
//    090803 Modified fror Mouse 1.1.
//   Version 1.2
//    100411 Specified invocation in comment to the class.
//    100411 Added general catch for all exceptions.
//   Version 1.3
//    101203 Convert Cache name to printable in statistics.
//    101206 Removed general catch. Added catch for exceptions that can
//           indicate user error. Declared other exceptions.
//    101208 Removed undocumented possibility to comment out files
//           from file list supplied with '-F'.
//   Version 1.5.1
//    120102 (Steve Owens) Removed unused import.
//           Removed unused local variable 'errors'.
//
//=========================================================================

package mouse;

import mouse.runtime.ParserTest.Cache;
import mouse.runtime.Source;
import mouse.runtime.SourceFile;
import mouse.runtime.SourceString;
import mouse.utility.CommandArgs;
import mouse.utility.Convert;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Vector;



//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  TestParser
//
//-------------------------------------------------------------------------
//
//  Run the instrumented parser (generated with option -T),
//  and print information about its operation.
//
//  Invocation
//
//    java mouse.TestParser <arguments>
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
//       <n> is a digit from 0 through 9 specifying the number of results
//       to be cached. Default is -m0.
//
//    -T <string>
//       Tracing switches. Optional.
//       The <string> is assigned to the 'trace' field in your semantics
//       object, where it can be used it to activate any trace
//       programmed there.
//       In addition, presence of certain letters in <string>
//       activates traces in the parser:
//       r - trace execution of parsing procedures for rules.
//       i - trace execution of parsing procedures for inner expressions.
//       e - trace error information.
//
//    -d Show detailed statistics for backtracking - rescan - reuse. Optional.
//
//    -D Show detailed statistics for all invoked procedures. Optional.
//
//  If you do not specify -f or -F,  the parser is executed interactively,
//  prompting for input by printing '>'.
//  It is invoked separately for each input line after you press 'Enter'.
//  You terminate the session by pressing 'Enter' directly at the prompt.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

class TestParser
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
  Method setmemo;     // Set amount of memo
  Method settrace;    // Set trace
  Method parse;       // Run parser
  Method caches;      // Get list of Cache objects

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
  int m = 0;

  //-------------------------------------------------------------------
  //  Test options.
  //-------------------------------------------------------------------
  boolean allDetails;
  boolean brDetails;
  String  trace;


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
      TestParser test = new TestParser();

      if (!test.init(argv)) return;

      if (test.files==null)
        test.interact();

      else
        for (String name: test.files)
          test.run(name);
    }


  //=====================================================================
  //
  //  Set up information for testing
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
              "Dd",      // options
              "PFfmT",   // options with argument
               0,0);     // no positional arguments
      if (cmd.nErrors()>0) return false;

      //---------------------------------------------------------------
      //  Get parser name.
      //---------------------------------------------------------------
      String parsName    = cmd.optArg('P');

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
        {throw new Error("Class 'mouse.runtime.Source' not found."); }

      settrace = parserClass.getMethod("setTrace",Class.forName("java.lang.String"));

      //---------------------------------------------------------------
      //  Find the 'setMemo' and 'caches' methods of the parser.
      //  They are both present only in test version of the parser.
      //---------------------------------------------------------------
      try
      {
        setmemo = parserClass.getMethod("setMemo",int.class);
        caches  = parserClass.getMethod("caches");
      }
      catch (NoSuchMethodException e)
      {
          System.out.println(parsName + " is not a test version");
          return false;
      }

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

        if (m<0)
        {
          System.err.println("-m is outside the range 0-9.");
          return false;
        }
      }

      //---------------------------------------------------------------
      //  Get trace and options.
      //---------------------------------------------------------------

      allDetails = cmd.opt('D');
      brDetails  = cmd.opt('d');
      trace      = cmd.optArg('T');

      if (trace==null) trace = "";

      //---------------------------------------------------------------
      //  Instantiate the parser, set trace and memo.
      //---------------------------------------------------------------
      parser = parserClass.newInstance();
      settrace.invoke(parser,trace);
      setmemo.invoke(parser,m);

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
  //  Run test on file 'name'
  //
  //=====================================================================

  boolean run(final String name)
    throws IllegalAccessException,InvocationTargetException
    {
      //---------------------------------------------------------------
      //  Run the parser.
      //---------------------------------------------------------------
      Source src = new SourceFile(name);
      if (!src.created()) return false;

      boolean result = (Boolean)(parse.invoke(parser,src));

      if (!result)
      {
        System.out.println("\n" + name + " failed");
        return false;
      }

      showstat(parser,src,name);
      return true;
   }


  //=====================================================================
  //
  //  Run test interactively
  //
  //=====================================================================

  void interact()
    throws IllegalAccessException,InvocationTargetException
    {
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String input;
      int n = 1;
      while (true)
      {
        System.out.print("> ");
        try
        { input = in.readLine(); }
        catch (IOException e)
        {
          System.out.println(e.toString());
          return;
        }
        if (input.length()==0) return;

        SourceString src = new SourceString(input);
        boolean result = (Boolean)(parse.invoke(parser,src));

        String name = "line " + n;
        if (result)
          showstat(parser,src,name);
        else
          System.out.println("\n" + name + " failed\n");

        n++;
      }
    }


  //=====================================================================
  //
  //  Show statistics
  //
  //=====================================================================

  void showstat(Object parser, Source src, String name)
    throws IllegalAccessException,InvocationTargetException
    {
      //---------------------------------------------------------------
      //  Get list of expression objects.
      //---------------------------------------------------------------
      Cache cacheList[] = (Cache[])caches.invoke(parser);

      //-------------------------------------------------------------
      //  Collect totals
      //-------------------------------------------------------------
      int calls   = 0;
      int succ    = 0;
      int fail    = 0;
      int back    = 0;
      int reuse   = 0;
      int rescan  = 0;
      int totback = 0;
      int maxback = 0;

      for (Cache s: cacheList)
      {
        calls   += s.calls;
        succ    += s.succ;
        fail    += s.fail;
        back    += s.back;
        reuse   += s.reuse;
        rescan  += s.rescan;
        totback += s.totback;
        if (s.maxback>maxback) maxback = s.maxback;
      }

      //-------------------------------------------------------------
      // Show statistics
      //-------------------------------------------------------------
      Locale loc = new Locale("US");
      int size = src.end();
      System.out.printf("%n%s: %d bytes.%n",name,size);
      System.out.printf
        ("%d calls: %d ok, %d failed, %d backtracked.%n",
         calls, succ, fail, back);
      System.out.printf("%d rescanned", rescan);
      if (m==0)
        System.out.print(".\n");
      else
        System.out.printf(", %d reused.%n",reuse);
      if (back>0)
        System.out.printf
          (loc,"backtrack length: max %d, average %.1f.%n",
           maxback, (float)totback/back);
      System.out.println("");

      //-------------------------------------------------------------
      //  If requested, show details
      //-------------------------------------------------------------
      if (allDetails | brDetails)
      {
        if (brDetails) System.out.println("Backtracking, rescan, reuse:\n");
        System.out.printf
          ("%-13s %5s %5s %5s %5s %5s %5s %5s %-15s%n",
           "procedure", "ok", "fail", "back", "resc", "reuse", "totbk", "maxbk", "at");
        System.out.printf
          ("%-13s %5s %5s %5s %5s %5s %5s %5s %-15s%n",
           "-------------", "-----", "-----", "-----", "-----", "-----", "-----", "-----", "--");
        for (Cache s: cacheList)
        {
          if (allDetails || s.back!=0 || s.reuse!=0 || s.rescan!=0)
          {
            String desc = Convert.toPrint(s.name);
            if (desc.length()>13)
              desc = desc.substring(0,11) + "..";
            System.out.printf
              ("%-13s %5d %5d %5d %5d %5d",
               desc, s.succ, s.fail, s.back, s.rescan, s.reuse);
            if (s.back==0)
              System.out.printf
                (" %5d %5d%n",0,0);
            else
              System.out.printf
                (" %5d %5d %-15s%n",s.totback, s.maxback, src.where(s.maxbpos));
          }
        }

        System.out.println("");
      }
    }
}