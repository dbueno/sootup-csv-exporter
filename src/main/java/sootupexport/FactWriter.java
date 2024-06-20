package sootupexport;

import static sootupexport.PredicateFile.*;

import sootup.core.inputlocation.*;
import sootup.core.jimple.basic.*;
import sootup.core.model.*;
import sootup.core.signatures.*;
import sootup.core.types.*;
import sootup.java.bytecode.inputlocation.*;
import sootup.java.core.*;
import sootup.java.core.views.*;

public class FactWriter {
  protected final Database _db;
  protected final Representation _rep;

  public FactWriter(Database db, Representation rep) {
    this._db = db;
    this._rep = rep;
  }

  public String writeMethod(JavaSootMethod m) {
    String methodRaw = _rep.signature(m);
    String methodId = methodSig(m, methodRaw);
    String arity = Integer.toString(m.getParameterCount());
    MethodSignature sig = m.getSignature();

    _db.add(
        METHOD,
        methodId,
        _rep.simpleName(m),
        _rep.name(sig.getDeclClassType()),
        _rep.descriptor(m),
        _rep.name(sig.getType()),
        arity);

    return methodId;
  }

  public void writeThisVar(String methodId, ClassType declaring) {
    String thisVar = _rep.thisVar(methodId);
    String type = _rep.name(declaring);
    _db.add(THIS, methodId, thisVar, type);
  }

  public void writeLocal(String methodId, Local l) {
    String var = _rep.local(methodId, l);
    Type t = l.getType();
    _db.add(VAR_SIMPLENAME, var, l.getName());
    _db.add(VAR_TYPE, var, _rep.name(t));
    writeVarDeclaringMethod(methodId, var);
  }

  public void writeVarDeclaringMethod(String methodId, String var) {
    _db.add(VAR_DECLARING_METHOD, var, methodId);
  }

  public void writeBodyPosition(String methodId, Position p) {
    _db.add(
        METHOD_BODY_POSITION,
        methodId,
        String.valueOf(p.getFirstLine()),
        String.valueOf(p.getLastLine()),
        String.valueOf(p.getFirstCol()),
        String.valueOf(p.getLastCol()));
  }

  public void writeFormalParam(String methodId, Type paramType, int index) {
    String var = _rep.param(methodId, index);
    _db.add(FORMAL, methodId, var, _rep.name(paramType), String.valueOf(index));
  }

  public void writeAssignLocal(InstrInfo ii, Local to, Local from) {
    String methodId = ii.methodId;
    writeAssignLocal(
        ii.insn, ii.index, _rep.local(methodId, from), _rep.local(ii.methodId, to), methodId);
  }

  public void writeAssignLocal(String insn, int index, String to, String from, String methodId) {
    _db.add(ASSIGN_LOCAL, insn, String.valueOf(index), from, to, methodId);
  }

  public String methodSig(JavaSootMethod m, String methodRaw) {
    if (methodRaw == null) methodRaw = _rep.signature(m);
    return methodRaw; // hashMethodNameIfLong(methodRaw);
  }

  public void writeClassOrInterfaceType(JavaSootClass cls) {}
}
