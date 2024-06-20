package sootupexport;

import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import sootup.core.inputlocation.*;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.*;
import sootup.core.model.*;
import sootup.core.signatures.*;
import sootup.core.types.*;
import sootup.java.bytecode.inputlocation.*;
import sootup.java.core.*;
import sootup.java.core.views.*;

public class Representation {

  private static final Pattern qPat = Pattern.compile("'");
  private static final Pattern dotPat = Pattern.compile("\\.");

  private final Map<JavaSootMethod, String> _methodSigRepr = new ConcurrentHashMap<>();

  public static String instructionId(String m, String kind, int index) {
    return m + "/" + kind + "/" + index;
  }

  public static String numberedInstructionId(String pre, String mid, SessionCounter c) {
    return instructionId(pre, mid, c.nextNumber(mid));
  }

  public static String getKind(Stmt unit) {
    if (unit instanceof JAssignStmt) {
      JAssignStmt assignStmt = (JAssignStmt) unit;
      Value rightOp = assignStmt.getRightOp();
      Value leftOp = assignStmt.getLeftOp();
      if (rightOp instanceof JCastExpr) return "assign-cast";
      else if (rightOp instanceof JFieldRef)
        return "read-field-" + ((JFieldRef) rightOp).getFieldSignature().getName();
      else if (leftOp instanceof JFieldRef)
        return "write-field-" + ((JFieldRef) leftOp).getFieldSignature().getName();
      else if (rightOp instanceof JArrayRef) return "read-array-idx";
      else if (leftOp instanceof JArrayRef) return "write-array-idx";
      else return "assign";
    } else if (unit instanceof JIdentityStmt) return "assign";
    else if (unit instanceof AbstractDefinitionStmt) return "definition";
    else if (unit instanceof JEnterMonitorStmt) return "enter-monitor";
    else if (unit instanceof JExitMonitorStmt) return "exit-monitor";
    else if (unit instanceof JGotoStmt) return "goto";
    else if (unit instanceof JIfStmt) return "if";
    else if (unit instanceof JInvokeStmt) return "invoke";
    else if (unit instanceof JRetStmt) return "ret";
    else if (unit instanceof JReturnVoidStmt) return "return-void";
    else if (unit instanceof JReturnStmt) return "return";
    else if (unit instanceof JSwitchStmt) {
      if (((JSwitchStmt) unit).isTableSwitch()) return "table-switch";
      else if (!((JSwitchStmt) unit).getValues().isEmpty()) return "lookup-switch";
      else return "switch";
    } else if (unit instanceof JThrowStmt) return "throw";
    return "unknown";
  }

  String signature(JavaSootMethod m) {
    String result = _methodSigRepr.get(m);
    if (result == null) {
      MethodSignature sig = m.getSignature();
      String raw = name(sig.getDeclClassType()) + "." + simpleName(m) + ":" + descriptor(m);
      result = stripQuotes(raw);
      _methodSigRepr.put(m, result);
    }

    return result;
  }

  public String simpleName(JavaSootMethod m) {
    return m.getSignature().getName();
  }

  public String descriptor(JavaSootMethod m) {
    MethodSignature sig = m.getSignature();
    return "("
        + (sig.getParameterTypes().stream().map(this::name).collect(Collectors.joining()))
        + ")"
        + name(sig.getType());
  }

  public String thisVar(String methodId) {
    return methodId + "/this";
  }

  public String param(String methodId, int i) {
    return methodId + "/@parameter" + i;
  }

  public String local(String methodId, Local l) {
    return methodId + "/" + l.getName();
  }

  public String name(Type t) {
    TypeNameVisitor v = new TypeNameVisitor();
    String result = new String();
    while (t instanceof ArrayType) {
      t.accept(v);
      result += v.getResult();
      t = ((ArrayType) t).getBaseType();
    }

    t.accept(v);
    result += v.getResult();
    return result;
  }

  public static String stripQuotes(CharSequence s) {
    return qPat.matcher(s).replaceAll("");
  }
}
