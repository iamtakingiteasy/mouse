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
//      Restructured.
//      Changed for the redesigned Tail class.
//      Replaced #Start expression by tail to start expression.
//      Added computation of firstTailTerms,
//        thus eliminating the use of Follow matrix.
//      Removed use of 'NUL' attribute of Expr.
//      Changed name e.first -> e.firstTerms.
//      Calls Diagnose earlier and checks for error after the call.
//      Added 'notes'.
//
//=========================================================================

package mouse.explorer;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Vector;
import mouse.utility.BitMatrix;
import mouse.utility.Convert;
import mouse.runtime.Source;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Class PEG
//
//-------------------------------------------------------------------------
//
//  A static class to hold parsed grammar.
//  The parsed grammar is a structure of Expr objects in the form of
//  trees rooted in Expr objects representing Rules.
//  These objects are listed in the array 'rules'.
//  The objects representing terminals and nonterminals are listed
//  separately in 'terms' and 'nonterms'. Note that both terminals
//  and nonterminals may appear among rules.
//  All Expr objects are listed in array 'index', all terminals
//  appearing first. Each object holds its index position in field 'index'.
//  The class uses four visitor defined as internal static classes.
//  Two of them, 'ListVisitor' and 'SourceVisitor', descend recursively
//  the Expr trees, starting with the list of Rules.
//  The other two, 'AttrVisitor' and 'TailVisitor' proceed sequentially
//  using the 'index'.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public class PEG
{
  //=====================================================================
  //
  //  Data
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Lists of Expressions.
  //-------------------------------------------------------------------
  public static Expr rules[] = new Expr[0];
  public static Expr.Ref refs[] = new Expr.Ref[0];
  public static Expr nonterms[];
  public static Expr terms[];
  public static Expr index[];

  public static int N;  // Number of all Expressions.
  public static int T;  // Number of Terminals.

  //-------------------------------------------------------------------
  //  Start expression.
  //-------------------------------------------------------------------
  public static Expr start;

  //-------------------------------------------------------------------
  //  Error count.
  //-------------------------------------------------------------------
  public static int errors = 0;

  //-------------------------------------------------------------------
  //  Notes produced by Diagnose.
  //-------------------------------------------------------------------
  public static String notes = null;


  //=====================================================================
  //
  //  Constructor
  //
  //=====================================================================
  public static void parse(Source src)
    {
      //---------------------------------------------------------------
      //  Parse the grammar and quit if error found
      //---------------------------------------------------------------
      Parser parser = new Parser();
      parser.parse(src);

      Semantics sem = parser.semantics();
      if (sem.errcount>0) return;

      //---------------------------------------------------------------
      //  Get list of rules and refs.
      //---------------------------------------------------------------
      rules = sem.rules.toArray(rules);
      refs = sem.refs.toArray(refs);

      //---------------------------------------------------------------
      //  The first rule is starting expression.
      //---------------------------------------------------------------
      Expr start = rules[0];

      //---------------------------------------------------------------
      //  Resolve references and quit if error found.
      //---------------------------------------------------------------
      resolve();
      if (errors>0) return;

      //---------------------------------------------------------------
      //  Compute source for all expressions.
      //---------------------------------------------------------------
      SourceVisitor sourceVisitor = new SourceVisitor();
      for (Expr e: rules)
        e.accept(sourceVisitor);
      sourceVisitor = null;

      //---------------------------------------------------------------
      //  Give names to subexpressions, build lists and index.
      //---------------------------------------------------------------
      buildLists();

      //---------------------------------------------------------------
      //  Compute attributes.
      //---------------------------------------------------------------
      computeAttributes();

      //---------------------------------------------------------------
      //  Construct matrices.
      //---------------------------------------------------------------
      Relations.compute();

      //---------------------------------------------------------------
      //  Diagnose.
      //---------------------------------------------------------------
      Diagnose.apply();
      if (errors>0) return;

      //---------------------------------------------------------------
      //  Get first terminals.
      //---------------------------------------------------------------
      for (Expr e: index)
        e.firstTerms = Relations.First.row(e.index).get(0,T);

      //---------------------------------------------------------------
      //  Compute tails.
      //---------------------------------------------------------------
      TailVisitor tailVisitor = new TailVisitor();
      for (Expr e: index)
        e.accept(tailVisitor);
      tailVisitor = null;

      //---------------------------------------------------------------
      //  If start expression does not require termination of input,
      //  add SigmaStar as its tail.
      //---------------------------------------------------------------
      if (!start.end)
        start.tail.add(new Tail.Strand(start,null,new Expr.SigmaStar()));

      //---------------------------------------------------------------
      //  Compute first terminals of Tails.
      //---------------------------------------------------------------
      computeTailTerms();

      //---------------------------------------------------------------
      //  Find LL1 violations.
      //---------------------------------------------------------------
      Conflicts.find();
    }


  //=====================================================================
  //
  //  Resolve references.
  //
  //=====================================================================
  private static void resolve()
    {
      Hashtable<String,Expr> names = new Hashtable<String,Expr>();
      HashSet<String> referenced = new HashSet<String>();

      //---------------------------------------------------------------
      //  Dummy rule - replaces undefined to stop multiple messages.
      //---------------------------------------------------------------

      Expr dummy = new Expr.Any();

      //---------------------------------------------------------------
      //  Build table for finding Rules by name.
      //---------------------------------------------------------------
      for (Expr r: rules)
      {
        Expr prev = names.put(r.name,r);
        if (prev!=null)
        {
          System.out.println("Error: duplicate name '" + r.name + "'.");
          errors++;
        }
      }

      //---------------------------------------------------------------
      //  Start expression is assumed referenced.
      //---------------------------------------------------------------
      referenced.add(rules[0].name);

      //---------------------------------------------------------------
      //  Insert rule in Ref objects.
      //---------------------------------------------------------------
      for (Expr.Ref ref: refs)
      {
        Expr rule = names.get(ref.target);
        if (rule==null)
        {
          System.out.println("Error: undefined name '" + ref.target + "'.");
          errors++;
          names.put(ref.target,dummy);
          rule = dummy;
        }
        else
          referenced.add(ref.target);
        ref.rule = rule;
      }

      //---------------------------------------------------------------
      //  Detect unused rules.
      //---------------------------------------------------------------
      for (Expr r: rules)
      {
        if (!referenced.contains(r.name))
          System.out.println("Warning: rule '" + r.name + "' is not used.");
      }
    }

  //=====================================================================
  //
  //  Construct names for subexpressions and build lists of expressions.
  //
  //=====================================================================
  private static void buildLists()
    {
      ListVisitor listVisitor = new ListVisitor();

      for (Expr r: rules)
      {
        r.inRule = r;
        r. accept(listVisitor);
      }

      nonterms = listVisitor.myNonterms.toArray(new Expr[0]);
      terms = listVisitor.myTerms.toArray(new Expr[0]);

      listVisitor = null;

      //-----------------------------------------------------------------
      //  Build index of expressions.
      //-----------------------------------------------------------------
      T = terms.length;
      N = T + nonterms.length;
      index = new Expr[N];

      int i = 0;

      for (Expr e: terms)
      {
        e.index = i;
        index[i] = e;
        i++;
      }

      for (Expr e: nonterms)
      {
        e.index = i;
        index[i] = e;
        i++;
      }
    }

  //=====================================================================
  //
  //  Compute attributes by iteration to fixpoint.
  //
  //---------------------------------------------------------------------
  //
  //  Computes nul, adv, fal, and end attributes for all expressions.
  //  For terminals the attributes are preset by the constructor.
  //  For other expressions they are computed by iteration to a fixpoint.
  //  The AttrVisitor is used for the iteration step.
  //
  //=====================================================================
  private static void computeAttributes()
    {
      int trueAttrs; // Number of true attributes after last step
      int a = 0;     // Number of true attributes before last step
      int iter = 0;  // Number of steps
      AttrVisitor attrVisitor = new AttrVisitor();

      while(true)
      {
        //-------------------------------------------------------------
        //  Iteration step
        //-------------------------------------------------------------
        for (Expr e: index)
          e.accept(attrVisitor);

        //-------------------------------------------------------------
        //  Count true attributes
        //-------------------------------------------------------------
        trueAttrs = 0;
        for (Expr e: index)
          trueAttrs += (e.nul? 1:0) + (e.adv? 1:0)
                     + (e.fal? 1:0) + (e.end? 1:0);

        //-------------------------------------------------------------
        //  Break if fixpoint reached
        //-------------------------------------------------------------
        if (trueAttrs==a) break;

        //-------------------------------------------------------------
        //  To next step
        //-------------------------------------------------------------
        a = trueAttrs;
        iter++;
      }
      attrVisitor = null;
      // System.out.println(iter + " iterations for attributes");
    }

  //=====================================================================
  //
  //  Compute first terminals of Tails by iteration to fixpoint.
  //
  //=====================================================================
  private static void computeTailTerms()
    {
      int trueBits;  // Number of true bits after last step
      int b = 0;     // Number of true bits before last step
      int iter = 0;  // Number of steps

      while(true)
      {
        //-------------------------------------------------------------
        //  Iteration step.
        //-------------------------------------------------------------
        trueBits = 0;
        for (Expr e: index)
        {
          //-----------------------------------------------------------
          //  Look at tail of next expression.
          //-----------------------------------------------------------
          nexttail:
          for (Tail.Strand strand: e.tail)
          {
            //---------------------------------------------------------
            //  Look at next strand of the tail.
            //  Collect first terminals of expressions in 'seq'
            //  as long as they are 'transparent'.
            //---------------------------------------------------------
            for (Expr tailElem: strand.seq)
            {
              e.firstTailTerms.or(tailElem.firstTerms);
              if (!tailElem.nul | tailElem.end) continue nexttail;
            }

            //---------------------------------------------------------
            //  If 'seq' is 'transparent' or absent and the strand
            //  has tail, add first terminals of that tail.
            //---------------------------------------------------------
            if (strand.hasTail)
              e.firstTailTerms.or(strand.E.firstTailTerms);
          }
          trueBits += e.firstTailTerms.cardinality();
        }

        //-----------------------------------------------------------
        //  Break iteration if fixpoint reached.
        //-----------------------------------------------------------
        if (trueBits==b) break;

        //-----------------------------------------------------------
        //  To next step
        //-----------------------------------------------------------
        b = trueBits;
        iter++;
      }
      // System.out.println(iter + " iterations for first tail terminals");
    }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  ListVisitor
  //
  //-----------------------------------------------------------------------
  //
  //  Constructs names for subexpressions and builds lists of expressions.
  //  Each visit starts with adding the visited expression to either
  //  'myTerms' or 'myNonterms'.
  //  Then each argument that is not a Rule is first given a name
  //  - expression name followed by '.' followed by argument name -
  //  and then is visited.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  static class ListVisitor extends Visitor
  {
    //===================================================================
    //  Local data.
    //===================================================================
    private Vector<Expr> myNonterms  = new Vector<Expr>();
    private Vector<Expr> myTerms = new Vector<Expr>();

    //===================================================================
    //  Private methods.
    //===================================================================
    //-----------------------------------------------------------------
    //  Common for Choice and Sequence.
    //-----------------------------------------------------------------
    private void doCompound(Expr e, Expr args[])
      {
        myNonterms.add(e);
        for (int i=0;i<args.length;i++)
        {
          Expr arg = args[i];
          if (arg.isRule) continue;
          arg.name = e.name + "." + (i+1);
          arg.inRule = e.inRule;
          arg.accept(this);
        }
      }

    //-----------------------------------------------------------------
    //  Common for one-argument operations.
    //-----------------------------------------------------------------
    private void doUnary(Expr e, Expr arg)
      {
        myNonterms.add(e);
        if (arg.isRule) return;
        arg.name = e.name + ".1";
        arg.inRule = e.inRule;
        arg.accept(this);
      }

    //-----------------------------------------------------------------
    //  Common for terminals.
    //-----------------------------------------------------------------
    private void doTerm(Expr e, Expr arg)
      {
        myTerms.add(e);
        arg.inRule = e.inRule;
      }

    //===================================================================
    //  Visitor methods.
    //===================================================================

    public void visit(Expr.Choice e)
      { doCompound(e,e.args); }

    public void visit(Expr.Sequence e)
      { doCompound(e,e.args); }

    public void visit(Expr.And e)
      { doUnary(e,e.arg); }

    public void visit(Expr.Not e)
      { doUnary(e,e.arg); }

    public void visit(Expr.Plus e)
      { doUnary(e,e.arg); }

    public void visit(Expr.Star e)
      { doUnary(e,e.arg); }

    public void visit(Expr.Query e)
      { doUnary(e,e.arg); }

    public void visit(Expr.Ref e)
      { myNonterms.add(e); }

    public void visit(Expr.StringLit e)
      { myTerms.add(e); }

    public void visit(Expr.Range e)
      { myTerms.add(e); }

    public void visit(Expr.CharClass e)
      { myTerms.add(e); }

    public void visit(Expr.Any e)
      { myTerms.add(e); }

    public void visit(Expr.End e)
      { myTerms.add(e); }

    public void visit(Expr.SigmaStar e)
      { myTerms.add(e); }
  }


  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  SourceVisitor - Constructs source strings of expressions
  //
  //-----------------------------------------------------------------------
  //
  //  Each visit starts with visiting the subexpressions to construct
  //  their source strings. These strings are then used as building
  //  blocks to produce the final result. Procedure 'enclose'
  //  encloses the subexpression in parentheses if needed, depending
  //  on the binding strength of subexpression and containing expression.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class SourceVisitor extends Visitor
  {
    public void visit(Expr.Choice e)
      {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Expr arg: e.args)
        {
          sb.append(sep + asString(arg,0));
          sep = " / ";
        }
        e.asString = sb.toString();
      }

    public void visit(Expr.Sequence e)
      {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Expr arg: e.args)
        {
          sb.append(sep + asString(arg,1));
          sep = " ";
        }
        e.asString = sb.toString();
      }

    public void visit(Expr.And e)
      { e.asString = "&" + asString(e.arg,3); }

    public void visit(Expr.Not e)
      { e.asString = "!" + asString(e.arg,3); }

    public void visit(Expr.Plus e)
      { e.asString = asString(e.arg,4) + "+"; }

    public void visit(Expr.Star e)
      { e.asString = asString(e.arg,4) + "*"; }

    public void visit(Expr.Query e)
      { e.asString = asString(e.arg,4) + "?"; }


    //-----------------------------------------------------------------
    //  Get string of 'e', parenthesized if needed
    //-----------------------------------------------------------------
    private String asString(final Expr e, int myBind)
      {
        if (e.isRule) return e.name;
        if (e.asString==null)
          e.accept(this);
        boolean nest = e.bind()<=myBind;
        return (nest?"(":"") + e.asString + (nest?")":"");
     }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  AttrVisitor
  //
  //---------------------------------------------------------------------
  //  Each visit computes nul, adv, fal, and end attributes
  //  of an expression from those of subexpressions.
  //  Attributes for terminals are preset by their constructors.
  //  The visitor does not descend recursively the Expr trees.
  //  It is used in iteration to a fixpoint, which requires that
  //  the process is monotone, i.e. no value is changed from true to false.
  //  Moreover, the nul attribute is subsequently used to discover
  //  left-recursion, and must be correct even if there is left recursion.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  static class AttrVisitor extends Visitor
  {
    public void visit(Expr.Choice e)
      {
        boolean exNul = false; // Any arg has nul attribute?
        boolean exAdv = false; // Any arg has adv attribute?
        boolean allFal = true; // All args have fal attribute?
        boolean allEnd = true; // All args have end attribute?

        for (Expr arg: e.args)
        {
          exNul |= arg.nul;
          exAdv |= arg.adv;
          allFal &= arg.fal;
          allEnd &= arg.end;
        }

        e.nul = exNul;  // true if any arg has nul attribute
        e.adv = exAdv;  // true if any arg has adv attribute
        e.fal = allFal; // true if all args have nul attribute
        e.end = allEnd; // true if all args have end attribute
      }

    public void visit(Expr.Sequence e)
      {
        boolean allNul = true;  // All args have nul attribute?
        boolean exAdv  = false; // Any arg has adv attribute?
        boolean exFal  = false; // Any arg has fal attribute?
        boolean exEnd  = false; // Any arg has end attribute?

        for (Expr arg: e.args)
        {
          allNul &= arg.nul;
          exAdv |= arg.adv;
          exFal |= arg.fal;
          exEnd |= arg.end;
        }

        e.nul = allNul;   // true if all args have nul attribute
        e.adv = exAdv;    // true if any arg has adv attribute
        e.fal = exFal;    // true if any arg has fal attribute
        e.end = exEnd;    // true if any arg has end attribute
      }

    public void visit(Expr.And e)
      {
        // Predicate does not advance.
        Expr arg = e.arg;
        e.nul = arg.nul | arg.adv;
        e.fal = arg.fal;
      }

    public void visit(Expr.Not e)
      {
        // Predicate does not advance.
        Expr arg = e.arg;
        e.nul = arg.fal;
        e.fal = arg.nul | arg.adv;
      }

    public void visit(Expr.Plus e)
      {
        Expr a = e.arg;

        // Computed as for a a*

        // Attributes of a*
        boolean nul2 = a.fal | a.nul;
        boolean adv2 = a.adv;
        boolean fal2 = false;
        boolean end2 = false;

        // Attributes of a a*
        e.nul = a.nul & nul2;
        e.adv = a.adv | adv2;
        e.fal = a.fal;     // a.fal | fal2;
        e.end = a.end;     // a.end | end2;
      }

    public void visit(Expr.Star e)
      {
        // Always succeeds: e.fal = false, end = false.
        Expr arg = e.arg;
        e.nul = arg.fal | arg.nul;
        e.adv = arg.adv;
      }

    public void visit(Expr.Query e)
      {
        // Always succeeds: e.fal = false, end = false.
        Expr arg = e.arg;
        e.nul = arg.fal | arg.nul;
        e.adv = arg.adv;
      }

    public void visit(Expr.Ref e)
      {
        Expr rule = e.rule;
        e.nul = rule.nul;
        e.adv = rule.adv;
        e.fal = rule.fal;
        e.end = rule.end;
      }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  TailVisitor
  //
  //-----------------------------------------------------------------------
  //
  //  Creates Tail.Strand objects for expressions called by the visited
  //  expression 'e'. Each such object represents the fact that 'e'
  //  contains call to an expression 'to' followed by a (possibly empty)
  //  sequence 'seq' of expression calls. The Strand is added to the Tail
  //  object associated with 'to'.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  static class TailVisitor extends Visitor
  {
    //-----------------------------------------------------------------
    //  Choice.
    //  The called expressions are the 'args',
    //  each followed by empty 'seq'.
    //-----------------------------------------------------------------
    public void visit(Expr.Choice e)
      {
        for (Expr to: e.args)
          to.tail.add(new Tail.Strand(to,e));
      }

    //-----------------------------------------------------------------
    //  Sequence.
    //  The called expressions are the 'args',
    //  with each 'args[i]' followed by 'args[i+1] ... args[n-1]'
    //  where 'n' is the number of 'args'.
    //-----------------------------------------------------------------
    public void visit(Expr.Sequence e)
      {
        int n = e.args.length;
        for (int i=0;i<n;i++)
        {
          Expr to = e.args[i];
          Expr[] seq = Arrays.copyOfRange(e.args,i+1,n);
          to.tail.add(new Tail.Strand(to,e,seq));
        }
      }

    //-----------------------------------------------------------------
    //  Plus.
    //  We treat the expression as being shorthand for the Choice
    //  'arg arg+ / arg', which is the same as 'arg e / arg',
    //  and create Tail.Strand for each call to 'arg' appearing there.
    //-----------------------------------------------------------------
    public void visit(Expr.Plus e)
      {
        Expr to = e.arg;
        to.tail.add(new Tail.Strand(to,e,e));
        to.tail.add(new Tail.Strand(to,e));
      }

    //-----------------------------------------------------------------
    //  Star.
    //  We treat the expression as being shorthand for the Choice
    //  'arg arg* / empty', which is the same as 'arg e / empty',
    //  and create Tail.Strand for the call to 'arg' appearing there.
    //-----------------------------------------------------------------
    public void visit(Expr.Star e)
      {
        Expr to = e.arg;
        to.tail.add(new Tail.Strand(to,e,e));
      }

    //-----------------------------------------------------------------
    //  Query.
    //  The called expression is 'arg', followed by empty 'seq'.
    //-----------------------------------------------------------------
    public void visit(Expr.Query e)
      {
        Expr to = e.arg;
        to.tail.add(new Tail.Strand(to,e));
      }

   public void visit(Expr.Ref e)
     {
       Expr to = e.rule;
       to.tail.add(new Tail.Strand(to,e));
     }

  }
}

