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
//    170203 Created.
//    170901 Version 1.9.1
//      Clarified meaning of 'end' and removed 'NUL'.
//      Renamed 'first' to 'firstTerms' and added 'firstTailTerms'.
//      Replaced 'tails' by 'tail' for the new version of Tail.
//      Corrected binding strength for End and SigmaStar.
//
//=========================================================================

package mouse.explorer;

import java.util.BitSet;
import java.util.Vector;
import mouse.utility.Convert;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Class Expr
//
//  Objects of class Expr represent parsing expressions.
//  Expr is an abstract class, with concrete subclasses representing
//  different kinds of expressions:
//
//  - Expr.Choice - two or more expressions separated by '/'.
//  - Expr.Sequence - sequence of two or more expressions.
//  - Expr.And - expression preceded by '&'.
//  - Expr.Not - expression preceded by '!'.
//  - Expr.Plus - expression followed by '+'.
//  - Expr.Star - expression followed by '*'.
//  - Expr.Query - expression followed by '?'.
//  - Expr.Ref - reference to Rule.
//  - Expr.StringLit - string literal.
//  - Expr.CharClass - character class.
//  - Expr.Range - character from range.
//  - Expr.Any - any character.
//  - Expr.End - end of input.
//  - Expr.SigmaStar - represents set of all strings.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public abstract class Expr
{
  //=====================================================================
  //
  //  Common data.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Name.
  //-------------------------------------------------------------------
  public String name;

  //-------------------------------------------------------------------
  //  Position in stucture.
  //-------------------------------------------------------------------
  public int index;              //  Index in PEG.index
  public boolean isRule = false; //  Is this a Rule?
  public Expr inRule;            //  Containing Rule

  //-------------------------------------------------------------------
  //  Reconstructed source text in 'true' form:
  //  with all literals converted to charaters they represent.
  //-------------------------------------------------------------------
  public String asString;

  //-------------------------------------------------------------------
  //  Extended Ford's attributes.
  //-------------------------------------------------------------------
  public boolean nul = false; // May succeed consuming empty string
  public boolean adv = false; // May succeed consuming non-empty string
  public boolean fal = false; // May fail
  public boolean end = false; // Success means end of input

  //  An expression with nul=true and end=false is referred in comments
  //  as 'transparent': after its success at position p, an expression
  //  called after it may consume input starting from position p.
  //  An expression not having this property is 'opaque'.

  //-------------------------------------------------------------------
  //  Tail in the context of full grammar.
  //-------------------------------------------------------------------
  public Tail tail = new Tail(this);

  //-------------------------------------------------------------------
  //  Indices of first terminals.
  //-------------------------------------------------------------------
  public BitSet firstTerms = new BitSet();      // Of the expression
  public BitSet firstTailTerms = new BitSet();  // Of possible tails

  //-------------------------------------------------------------------
  //  Shortcuts replacing 'instanceof'.
  //-------------------------------------------------------------------
  public boolean isTerm = false;
  public boolean isRef  = false;


  //=====================================================================
  //
  //  Common methods.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  As string.
  //-------------------------------------------------------------------
  public String asString()
    { return asString; }

  //-------------------------------------------------------------------
  //  As string with name.
  //-------------------------------------------------------------------
  public String named()
    { return name + " = " + asString; }

  //-------------------------------------------------------------------
  //  As name for Rule, as string otherwise.
  //-------------------------------------------------------------------
  public String simple()
    { return isRule? name : asString; }

  //-------------------------------------------------------------------
  //  Accept visitor.
  //-------------------------------------------------------------------
  public abstract void accept(Visitor v);

  //-------------------------------------------------------------------
  //  Binding strength.
  //-------------------------------------------------------------------
  public int bind() { return 5; }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.Choice
  //
  //  Represents expression 'arg-1 / arg-2 / ... / arg-n' where n>1.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class Choice extends Expr
  {
    //-----------------------------------------------------------------
    //  Data
    //-----------------------------------------------------------------
    public final Expr[] args;  // The 'arg's

    //-----------------------------------------------------------------
    //  Create object with specified 'arg's.
    //-----------------------------------------------------------------
    public Choice(final Expr ... args)
      { this.args = args; }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }

    //-------------------------------------------------------------------
    //  Binding strength.
    //-------------------------------------------------------------------
    public int bind() { return 0; }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.Sequence
  //
  //  Represents expression "arg-1 arg-2  ... arg-n" where n>1.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class Sequence extends Expr
  {
    //-----------------------------------------------------------------
    //  Data
    //-----------------------------------------------------------------
    public Expr[] args;  // The 'arg's

    //-----------------------------------------------------------------
    //  Create object with specified 'arg's.
    //-----------------------------------------------------------------
    public Sequence(final Expr ... args)
      { this.args = args; }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }

    //-------------------------------------------------------------------
    //  Binding strength.
    //-------------------------------------------------------------------
    public int bind() { return 1; }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.And
  //
  //  Represents expression '&arg'.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class And extends Expr
  {
    //-----------------------------------------------------------------
    //  Data
    //-----------------------------------------------------------------
    public Expr arg;

    //-----------------------------------------------------------------
    //  Create object with specified 'arg'.
    //-----------------------------------------------------------------
    public And(final Expr arg)
      { this.arg = arg; }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }

    //-------------------------------------------------------------------
    //  Binding strength.
    //-------------------------------------------------------------------
    public int bind() { return 3; }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.Not
  //
  //  Represents expression '!arg'.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class Not extends Expr
  {
    //-----------------------------------------------------------------
    //  Data
    //-----------------------------------------------------------------
    public Expr arg;

    //-----------------------------------------------------------------
    //  Create object with specified 'arg'.
    //-----------------------------------------------------------------
    public Not(final Expr arg)
      { this.arg = arg; }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }

    //-------------------------------------------------------------------
    //  Binding strength.
    //-------------------------------------------------------------------
    public int bind() { return 3; }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.Plus
  //
  //  Represents expression 'arg+'.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class Plus extends Expr
  {
    //-----------------------------------------------------------------
    //  Data
    //-----------------------------------------------------------------
    public Expr arg;

    //-----------------------------------------------------------------
    //  Create object with specified 'arg'.
    //-----------------------------------------------------------------
    public Plus(final Expr arg)
      { this.arg = arg; }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }

    //-------------------------------------------------------------------
    //  Binding strength.
    //-------------------------------------------------------------------
    public int bind() { return 4; }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.Star
  //
  //  Represents expression 'arg*'.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class Star extends Expr
  {
    //-----------------------------------------------------------------
    //  Data
    //-----------------------------------------------------------------
    public Expr arg;

    //-----------------------------------------------------------------
    //  Create object with specified 'arg'.
    //-----------------------------------------------------------------
    public Star(final Expr arg)
      { this.arg = arg; }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }

    //-------------------------------------------------------------------
    //  Binding strength.
    //-------------------------------------------------------------------
    public int bind() { return 4; }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.Query
  //
  //  Represents expression 'arg?'.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class Query extends Expr
  {
    //-----------------------------------------------------------------
    //  Data
    //-----------------------------------------------------------------
    public Expr arg;

    //-----------------------------------------------------------------
    //  Create object with specified 'arg'.
    //-----------------------------------------------------------------
    public Query(final Expr arg)
      { this.arg = arg; }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }

    //-------------------------------------------------------------------
    //  Binding strength.
    //-------------------------------------------------------------------
    public int bind() { return 4; }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.Ref
  //
  //  Represents reference to the Rule 'rule' with name 'target'.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class Ref extends Expr
  {
    public Expr rule;
    public String target;

    //-----------------------------------------------------------------
    //  Create the object with specified target.
    //-----------------------------------------------------------------
    public Ref(final String target)
      {
        this.target = target;
        asString = target;
        isRef = true;
      }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.StringLit
  //
  //  Represents string literal "s".
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class StringLit extends Expr
  {
    //-----------------------------------------------------------------
    //  Data
    //-----------------------------------------------------------------
    public final String s; // The string in true form.

    //-----------------------------------------------------------------
    //  Create the object with specified string.
    //-----------------------------------------------------------------
    public StringLit(final String s)
      {
        this.s = s;
        adv = true;
        fal = true;
        isTerm = true;
        asString = Convert.toPrint("\"" + s + "\"");
      }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.Range
  //
  //  Represents range [a-z].
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class Range extends Expr
  {
    //-----------------------------------------------------------------
    //  Data
    //-----------------------------------------------------------------
    public char a;      // Range limits in true form.
    public char z;

    //-----------------------------------------------------------------
    //  Create the object with limits a-z.
    //-----------------------------------------------------------------
    public Range(char a, char z)
      {
        this.a = a;
        this.z = z;
        adv = true;
        fal = true;
        isTerm = true;
        asString = Convert.toPrint("[" + a + "-" + z + "]");
      }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.CharClass
  //
  //  Represents character class [s] or ^[s].
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class CharClass extends Expr
  {
    //-----------------------------------------------------------------
    //  Data
    //-----------------------------------------------------------------
    public final String s; // The string in true form.
    public boolean hat;    // '^' present?

    //-----------------------------------------------------------------
    //  Create object with specified string and 'not'.
    //-----------------------------------------------------------------
    public CharClass(final String s, boolean hat)
      {
        this.s = s;
        this.hat = hat;
        adv = true;
        fal = true;
        isTerm = true;
        asString = Convert.toPrint((hat?"^[":"[") + s + "]");
      }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.Any
  //
  //  Represents 'any character'.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class Any extends Expr
  {
    //-----------------------------------------------------------------
    //  Create.
    //-----------------------------------------------------------------
    public Any()
      {
        fal = true;
        adv = true;
        isTerm = true;
        asString = "_";
      }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.End
  //
  //  Represents end of input.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class End extends Expr
  {
    //-----------------------------------------------------------------
    //  Create.
    //-----------------------------------------------------------------
    public End()
      {
        nul = true;
        fal = true;
        adv = true;
        end = true;
        isTerm = true;
        asString = "!_";
      }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }

    //-------------------------------------------------------------------
    //  Binding strength.
    //-------------------------------------------------------------------
    public int bind() { return 3; }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Expr.SigmaStar
  //
  //  Represents set of all strings up to end of input.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class SigmaStar extends Expr
  {
    //-----------------------------------------------------------------
    //  Create.
    //-----------------------------------------------------------------
    public SigmaStar()
      {
        nul = true;
        fal = true;
        adv = true;
        end = true;
        isTerm = true;
        asString = "_*";
        name = "Any string";
      }

    //-----------------------------------------------------------------
    //  Accept visitor.
    //-----------------------------------------------------------------
    public void accept(Visitor v)
      { v.visit(this); }

    //-------------------------------------------------------------------
    //  Binding strength.
    //-------------------------------------------------------------------
    public int bind() { return 3; }
  }
}


