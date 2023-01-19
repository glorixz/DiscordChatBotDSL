package ast.evaluator;

import ast.*;

public interface DSLBotVisitor<T, E, U> {
    U visit (Program n, T t, E e);
    U visit (InitCounterVar n, T t, E e);
    U visit (Conversation n, T t, E e);
    U visit (TriggerPhrase n, T t, E e);
    U visit (Stop n, T t, E e);
    U visit (IncDec n, T t, E e);
    U visit (FnCall n, T t, E e);
    U visit (FnBody n, T t, E e);
    U visit (CondChain n, T t, E e);
    U visit (While n, T t, E e);
    U visit (IfStatement n, T t, E e);
    U visit (Message n, T t, E e);
    U visit (Form n, T t, E e);
    U visit (StringBuild n, T t, E e);
    U visit (Constant n, T t, E e);
    U visit (TextVar n, T t, E e);
    U visit (CounterVar n, T t, E e);
    U visit (Comparator n, T t, E e);
}
