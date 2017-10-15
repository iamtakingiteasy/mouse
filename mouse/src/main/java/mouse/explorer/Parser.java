//=========================================================================
//
//  This file was generated by Mouse 1.9 at 2017-09-01 12:40:38 GMT
//  from grammar 'C:\Users\Giraf\Mouse\mouse\explorer\..\peg\grammar.peg'.
//
//=========================================================================

package mouse.explorer;

import mouse.runtime.Source;

public class Parser extends mouse.runtime.ParserBase
{
  final Semantics sem;
  
  //=======================================================================
  //
  //  Initialization
  //
  //=======================================================================
  //-------------------------------------------------------------------
  //  Constructor
  //-------------------------------------------------------------------
  public Parser()
    {
      sem = new Semantics();
      sem.rule = this;
      super.sem = sem;
    }
  
  //-------------------------------------------------------------------
  //  Run the parser
  //-------------------------------------------------------------------
  public boolean parse(Source src)
    {
      super.init(src);
      sem.init();
      boolean result = Grammar();
      closeParser(result);
      return result;
    }
  
  //-------------------------------------------------------------------
  //  Get semantics
  //-------------------------------------------------------------------
  public Semantics semantics()
    { return sem; }
  
  //=======================================================================
  //
  //  Parsing procedures
  //
  //=======================================================================
  //=====================================================================
  //  Grammar = Space (Rule / Skip)*+ EOT {Grammar} ;
  //=====================================================================
  private boolean Grammar()
    {
      begin("Grammar");
      Space();
      while (!EOT())
        if (!Grammar_0()) return reject();
      sem.Grammar();
      return accept();
    }
  
  //-------------------------------------------------------------------
  //  Grammar_0 = Rule / Skip
  //-------------------------------------------------------------------
  private boolean Grammar_0()
    {
      begin("");
      if (Rule()) return acceptInner();
      if (Skip()) return acceptInner();
      return rejectInner();
    }
  
  //=====================================================================
  //  Rule = Name EQUAL RuleRhs DiagName? SEMI {Rule} ~{Error} ;
  //=====================================================================
  private boolean Rule()
    {
      begin("Rule");
      if (Rule_0())
      { sem.Rule(); return accept(); }
      sem.Error();
      return reject();
    }
  
  //-------------------------------------------------------------------
  //  Rule_0 = Name EQUAL RuleRhs DiagName? SEMI
  //-------------------------------------------------------------------
  private boolean Rule_0()
    {
      begin("");
      if (!Name()) return rejectInner();
      if (!EQUAL()) return rejectInner();
      if (!RuleRhs()) return rejectInner();
      DiagName();
      if (!SEMI()) return rejectInner();
      return acceptInner();
    }
  
  //=====================================================================
  //  Skip = SEMI / _++ (SEMI / EOT) ;
  //=====================================================================
  private boolean Skip()
    {
      begin("Skip");
      if (SEMI()) return accept();
      if (Skip_0()) return accept();
      return reject();
    }
  
  //-------------------------------------------------------------------
  //  Skip_0 = _++ (SEMI / EOT)
  //-------------------------------------------------------------------
  private boolean Skip_0()
    {
      begin("");
      if (Skip_1()) return rejectInner();
      do if (!next()) return rejectInner();
        while (!Skip_1());
      return acceptInner();
    }
  
  //-------------------------------------------------------------------
  //  Skip_1 = SEMI / EOT
  //-------------------------------------------------------------------
  private boolean Skip_1()
    {
      begin("");
      if (SEMI()) return acceptInner();
      if (EOT()) return acceptInner();
      return rejectInner();
    }
  
  //=====================================================================
  //  RuleRhs = Sequence Actions (SLASH Sequence Actions)* {RuleRhs}
  //    <right-hand side> ;
  //=====================================================================
  private boolean RuleRhs()
    {
      begin("RuleRhs","right-hand side");
      if (!Sequence()) return reject();
      Actions();
      while (RuleRhs_0());
      sem.RuleRhs();
      return accept();
    }
  
  //-------------------------------------------------------------------
  //  RuleRhs_0 = SLASH Sequence Actions
  //-------------------------------------------------------------------
  private boolean RuleRhs_0()
    {
      begin("");
      if (!SLASH()) return rejectInner();
      if (!Sequence()) return rejectInner();
      Actions();
      return acceptInner();
    }
  
  //=====================================================================
  //  Choice = Sequence (SLASH Sequence)* {Choice} ;
  //=====================================================================
  private boolean Choice()
    {
      begin("Choice");
      if (!Sequence()) return reject();
      while (Choice_0());
      sem.Choice();
      return accept();
    }
  
  //-------------------------------------------------------------------
  //  Choice_0 = SLASH Sequence
  //-------------------------------------------------------------------
  private boolean Choice_0()
    {
      begin("");
      if (!SLASH()) return rejectInner();
      if (!Sequence()) return rejectInner();
      return acceptInner();
    }
  
  //=====================================================================
  //  Sequence = Prefixed+ {Sequence} ;
  //=====================================================================
  private boolean Sequence()
    {
      begin("Sequence");
      if (!Prefixed()) return reject();
      while (Prefixed());
      sem.Sequence();
      return accept();
    }
  
  //=====================================================================
  //  Prefixed = PREFIX? Suffixed {Prefixed} ;
  //=====================================================================
  private boolean Prefixed()
    {
      begin("Prefixed");
      PREFIX();
      if (!Suffixed()) return reject();
      sem.Prefixed();
      return accept();
    }
  
  //=====================================================================
  //  Suffixed = Primary (UNTIL Primary / SUFFIX)? {Suffixed} ;
  //=====================================================================
  private boolean Suffixed()
    {
      begin("Suffixed");
      if (!Primary()) return reject();
      Suffixed_0();
      sem.Suffixed();
      return accept();
    }
  
  //-------------------------------------------------------------------
  //  Suffixed_0 = UNTIL Primary / SUFFIX
  //-------------------------------------------------------------------
  private boolean Suffixed_0()
    {
      begin("");
      if (Suffixed_1()) return acceptInner();
      if (SUFFIX()) return acceptInner();
      return rejectInner();
    }
  
  //-------------------------------------------------------------------
  //  Suffixed_1 = UNTIL Primary
  //-------------------------------------------------------------------
  private boolean Suffixed_1()
    {
      begin("");
      if (!UNTIL()) return rejectInner();
      if (!Primary()) return rejectInner();
      return acceptInner();
    }
  
  //=====================================================================
  //  Primary = Name {Resolve} / LPAREN Choice RPAREN {Pass2} / ANY {Any}
  //    / StringLit {Pass} / Range {Pass} / CharClass {Pass} ;
  //=====================================================================
  private boolean Primary()
    {
      begin("Primary");
      if (Name())
      { sem.Resolve(); return accept(); }
      if (Primary_0())
      { sem.Pass2(); return accept(); }
      if (ANY())
      { sem.Any(); return accept(); }
      if (StringLit())
      { sem.Pass(); return accept(); }
      if (Range())
      { sem.Pass(); return accept(); }
      if (CharClass())
      { sem.Pass(); return accept(); }
      return reject();
    }
  
  //-------------------------------------------------------------------
  //  Primary_0 = LPAREN Choice RPAREN
  //-------------------------------------------------------------------
  private boolean Primary_0()
    {
      begin("");
      if (!LPAREN()) return rejectInner();
      if (!Choice()) return rejectInner();
      if (!RPAREN()) return rejectInner();
      return acceptInner();
    }
  
  //=====================================================================
  //  Actions = OnSucc OnFail {Actions} ;
  //=====================================================================
  private boolean Actions()
    {
      begin("Actions");
      OnSucc();
      OnFail();
      sem.Actions();
      return accept();
    }
  
  //=====================================================================
  //  OnSucc = (LWING AND? Name? RWING)? {OnSucc} ;
  //=====================================================================
  private boolean OnSucc()
    {
      begin("OnSucc");
      OnSucc_0();
      sem.OnSucc();
      return accept();
    }
  
  //-------------------------------------------------------------------
  //  OnSucc_0 = LWING AND? Name? RWING
  //-------------------------------------------------------------------
  private boolean OnSucc_0()
    {
      begin("");
      if (!LWING()) return rejectInner();
      AND();
      Name();
      if (!RWING()) return rejectInner();
      return acceptInner();
    }
  
  //=====================================================================
  //  OnFail = (TILDA LWING Name? RWING)? {OnFail} ;
  //=====================================================================
  private boolean OnFail()
    {
      begin("OnFail");
      OnFail_0();
      sem.OnFail();
      return accept();
    }
  
  //-------------------------------------------------------------------
  //  OnFail_0 = TILDA LWING Name? RWING
  //-------------------------------------------------------------------
  private boolean OnFail_0()
    {
      begin("");
      if (!TILDA()) return rejectInner();
      if (!LWING()) return rejectInner();
      Name();
      if (!RWING()) return rejectInner();
      return acceptInner();
    }
  
  //=====================================================================
  //  Name = Letter (Letter / Digit)* Space {Name} ;
  //=====================================================================
  private boolean Name()
    {
      begin("Name");
      if (!Letter()) return reject();
      while (Name_0());
      Space();
      sem.Name();
      return accept();
    }
  
  //-------------------------------------------------------------------
  //  Name_0 = Letter / Digit
  //-------------------------------------------------------------------
  private boolean Name_0()
    {
      begin("");
      if (Letter()) return acceptInner();
      if (Digit()) return acceptInner();
      return rejectInner();
    }
  
  //=====================================================================
  //  DiagName = "<" Char++ ">" Space {DiagName} ;
  //=====================================================================
  private boolean DiagName()
    {
      begin("DiagName");
      if (!next('<')) return reject();
      if (next('>')) return reject();
      do if (!Char()) return reject();
        while (!next('>'));
      Space();
      sem.DiagName();
      return accept();
    }
  
  //=====================================================================
  //  StringLit = ["] Char++ ["] Space {StringLit} ;
  //=====================================================================
  private boolean StringLit()
    {
      begin("StringLit");
      if (!next('"')) return reject();
      if (next('"')) return reject();
      do if (!Char()) return reject();
        while (!next('"'));
      Space();
      sem.StringLit();
      return accept();
    }
  
  //=====================================================================
  //  CharClass = ("[" / "^[") Char++ "]" Space {CharClass} ;
  //=====================================================================
  private boolean CharClass()
    {
      begin("CharClass");
      if (!next('[')
       && !next("^[")
         ) return reject();
      if (next(']')) return reject();
      do if (!Char()) return reject();
        while (!next(']'));
      Space();
      sem.CharClass();
      return accept();
    }
  
  //=====================================================================
  //  Range = "[" Char "-" Char "]" Space {Range} ;
  //=====================================================================
  private boolean Range()
    {
      begin("Range");
      if (!next('[')) return reject();
      if (!Char()) return reject();
      if (!next('-')) return reject();
      if (!Char()) return reject();
      if (!next(']')) return reject();
      Space();
      sem.Range();
      return accept();
    }
  
  //=====================================================================
  //  Char = Escape {Pass} / ^[\r\n\] {Char} ;
  //=====================================================================
  private boolean Char()
    {
      begin("Char");
      if (Escape())
      { sem.Pass(); return accept(); }
      if (nextNotIn("\r\n\\"))
      { sem.Char(); return accept(); }
      return reject();
    }
  
  //=====================================================================
  //  Escape = "\ u" HexDigit HexDigit HexDigit HexDigit {Unicode} / "\t"
  //    {Tab} / "\n" {Newline} / "\r" {CarRet} / !"\ u" "\" _ {Escape} ;
  //=====================================================================
  private boolean Escape()
    {
      begin("Escape");
      if (Escape_0())
      { sem.Unicode(); return accept(); }
      if (next("\\t"))
      { sem.Tab(); return accept(); }
      if (next("\\n"))
      { sem.Newline(); return accept(); }
      if (next("\\r"))
      { sem.CarRet(); return accept(); }
      if (Escape_1())
      { sem.Escape(); return accept(); }
      return reject();
    }
  
  //-------------------------------------------------------------------
  //  Escape_0 = "\ u" HexDigit HexDigit HexDigit HexDigit
  //-------------------------------------------------------------------
  private boolean Escape_0()
    {
      begin("");
      if (!next("\\u")) return rejectInner();
      if (!HexDigit()) return rejectInner();
      if (!HexDigit()) return rejectInner();
      if (!HexDigit()) return rejectInner();
      if (!HexDigit()) return rejectInner();
      return acceptInner();
    }
  
  //-------------------------------------------------------------------
  //  Escape_1 = !"\ u" "\" _
  //-------------------------------------------------------------------
  private boolean Escape_1()
    {
      begin("");
      if (!aheadNot("\\u")) return rejectInner();
      if (!next('\\')) return rejectInner();
      if (!next()) return rejectInner();
      return acceptInner();
    }
  
  //=====================================================================
  //  Letter = [a-z] / [A-Z] ;
  //=====================================================================
  private boolean Letter()
    {
      begin("Letter");
      if (nextIn('a','z')) return accept();
      if (nextIn('A','Z')) return accept();
      return reject();
    }
  
  //=====================================================================
  //  Digit = [0-9] ;
  //=====================================================================
  private boolean Digit()
    {
      begin("Digit");
      if (!nextIn('0','9')) return reject();
      return accept();
    }
  
  //=====================================================================
  //  HexDigit = [0-9] / [a-f] / [A-F] ;
  //=====================================================================
  private boolean HexDigit()
    {
      begin("HexDigit");
      if (nextIn('0','9')) return accept();
      if (nextIn('a','f')) return accept();
      if (nextIn('A','F')) return accept();
      return reject();
    }
  
  //=====================================================================
  //  PREFIX = [&!] Space <& or !> ;
  //=====================================================================
  private boolean PREFIX()
    {
      begin("PREFIX","& or !");
      if (!nextIn("&!")) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  SUFFIX = [?*+] Space <? or * or +> ;
  //=====================================================================
  private boolean SUFFIX()
    {
      begin("SUFFIX","? or * or +");
      if (!nextIn("?*+")) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  UNTIL = ("*+" / "++") Space <*+ or ++> ;
  //=====================================================================
  private boolean UNTIL()
    {
      begin("UNTIL","*+ or ++");
      if (!next("*+")
       && !next("++")
         ) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  EQUAL = "=" Space <=> ;
  //=====================================================================
  private boolean EQUAL()
    {
      begin("EQUAL","=");
      if (!next('=')) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  SEMI = ";" Space <;> ;
  //=====================================================================
  private boolean SEMI()
    {
      begin("SEMI",";");
      if (!next(';')) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  SLASH = "/" Space </> ;
  //=====================================================================
  private boolean SLASH()
    {
      begin("SLASH","/");
      if (!next('/')) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  AND = "&" Space <&> ;
  //=====================================================================
  private boolean AND()
    {
      begin("AND","&");
      if (!next('&')) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  LPAREN = "(" Space <(> ;
  //=====================================================================
  private boolean LPAREN()
    {
      begin("LPAREN","(");
      if (!next('(')) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  RPAREN = ")" Space <)> ;
  //=====================================================================
  private boolean RPAREN()
    {
      begin("RPAREN",")");
      if (!next(')')) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  LWING = "{" Space <{> ;
  //=====================================================================
  private boolean LWING()
    {
      begin("LWING","{");
      if (!next('{')) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  RWING = "}" Space <}> ;
  //=====================================================================
  private boolean RWING()
    {
      begin("RWING","}");
      if (!next('}')) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  TILDA = "~" Space <~> ;
  //=====================================================================
  private boolean TILDA()
    {
      begin("TILDA","~");
      if (!next('~')) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  ANY = "_" Space <_> ;
  //=====================================================================
  private boolean ANY()
    {
      begin("ANY","_");
      if (!next('_')) return reject();
      Space();
      return accept();
    }
  
  //=====================================================================
  //  Space = ([ \r\n\t] / Comment)* {Space} ;
  //=====================================================================
  private boolean Space()
    {
      begin("Space");
      while (Space_0());
      sem.Space();
      return accept();
    }
  
  //-------------------------------------------------------------------
  //  Space_0 = [ \r\n\t] / Comment
  //-------------------------------------------------------------------
  private boolean Space_0()
    {
      begin("");
      if (nextIn(" \r\n\t")) return acceptInner();
      if (Comment()) return acceptInner();
      return rejectInner();
    }
  
  //=====================================================================
  //  Comment = "//" _*+ EOL ;
  //=====================================================================
  private boolean Comment()
    {
      begin("Comment");
      if (!next("//")) return reject();
      while (!EOL())
        if (!next()) return reject();
      return accept();
    }
  
  //=====================================================================
  //  EOL = [\r]? [\n] / !_ <end of line> ;
  //=====================================================================
  private boolean EOL()
    {
      begin("EOL","end of line");
      if (EOL_0()) return accept();
      if (EOL_1()) return accept();
      return reject();
    }
  
  //-------------------------------------------------------------------
  //  EOL_0 = [\r]? [\n]
  //-------------------------------------------------------------------
  private boolean EOL_0()
    {
      begin("");
      next('\r');
      if (!next('\n')) return rejectInner();
      return acceptInner();
    }
  
  //-------------------------------------------------------------------
  //  EOL_1 = !_
  //-------------------------------------------------------------------
  private boolean EOL_1()
    {
      begin("","end of text");
      if (next()) return rejectPred();
      return acceptPred();
    }
  
  //=====================================================================
  //  EOT = !_ <end of text> ;
  //=====================================================================
  private boolean EOT()
    {
      begin("EOT","end of text");
      if (!aheadNot()) return reject();
      return accept();
    }
  
}
