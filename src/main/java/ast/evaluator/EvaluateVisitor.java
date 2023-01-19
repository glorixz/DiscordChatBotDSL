package ast.evaluator;

import ast.*;
import ast.Comparator;
import bot.BotConstants;

import java.util.*;


public class EvaluateVisitor implements DSLBotVisitor<StringBuilder, StringBuilder, Void> {
    private List<String> triggerPhrases = new ArrayList<>();
    private Map<String, String> variableNames = new HashMap<>();
    private Set<String> conversationNames = new HashSet<>();
    private int currentIfStatementCount = 0;

    // one of: global, triggerPhrase$triggerStr, convo$convoName, while$hashcode, if$hashcode, else$hashcode
    Stack<String> scopeStack = new Stack<>();

    @Override
    public Void visit(Program program, StringBuilder output, StringBuilder errors) {
        scopeStack.push("global");
        output.append(BotConstants.INIT_BOT);
        output.append(BotConstants.NEWLINE);

        for (Statement s : program.getStatements()) {
            s.accept(this, output, errors);
            if(!errors.isEmpty()) {
                return null; // terminate evaluation if error occurred while processing statement
            }
        }

        // Append listeners at end
        output.append(BotConstants.NEWLINE);
        String addListeners = BotConstants.createMessageCreateBody(triggerPhrases);
        output.append(addListeners);
        output.append("});");

        scopeStack.pop();
        return null;
    }

    @Override
    public Void visit(TriggerPhrase triggerPhrase, StringBuilder output, StringBuilder errors) {
        output.append(BotConstants.createTriggerPhraseStart(triggerPhrases.size() + 1));

        StringBuilder phrase = new StringBuilder();
        triggerPhrase.getPhrase().accept(this, phrase, errors);
        if(!errors.isEmpty()) return null;

        // remove string concat syntax and beginning/ending quotes
        String triggerStr = phrase.toString().replaceAll("\\\" *\\+ *\\\"", "");
        triggerStr = triggerStr.substring(1, triggerStr.length()-1);
        if (triggerPhrases.contains(triggerStr)) {
            errors.append("Error in triggerPhrase: duplicate trigger string " + triggerStr);
            return null;
        } else {
            triggerPhrases.add(triggerStr);
        }
        scopeStack.push("triggerPhrase$" + triggerStr);

        // Evaluate body of trigger phrase
        List<FnStatement> statements = triggerPhrase.getBody().getStatements();
        if (statements.isEmpty()) {
            errors.append("Error in triggerPhrase " + triggerStr + ": function body is empty.");
            return null;
        }
        for (FnStatement statement : statements) {
            output.append(BotConstants.TAB);
            statement.accept(this, output, errors);
            if(!errors.isEmpty()) return null;
            output.append(BotConstants.NEWLINE);
        }

        output.append(BotConstants.TRIGGER_PHRASE_END);

        scopeStack.pop();
        return null;
    }

    @Override
    public Void visit(InitCounterVar initCounterVar, StringBuilder output, StringBuilder errors) {
        String varName = initCounterVar.getCounterName();
        int initValue = initCounterVar.getValue();

        // Cannot define a counter var if there exists a variable in a higher scope with the same name.
        if (variableNames.containsKey(varName) && scopeStack.contains(variableNames.get(varName))) {
            errors.append(String.format("Attempted to initialize duplicate COUNTER variable %s", varName));
            return null;
        }

        variableNames.put(varName, scopeStack.peek());

        String initStatement = BotConstants.createInitCounter(varName, initValue);
        output.append(initStatement);
        output.append(BotConstants.NEWLINE);

        return null;
    }

    @Override
    public Void visit(Conversation conversation, StringBuilder output, StringBuilder errors) {
        String conversationName = conversation.getName();
        scopeStack.push("convo$" + conversationName);

        if (conversationNames.contains(conversationName)) {
            errors.append("Error in conversation: duplicate conversation name " + conversationName);
            return null;
        }

        conversationNames.add(conversationName);

        output.append(BotConstants.createConversationStart(conversationName));
        output.append(BotConstants.getUserName());
        output.append(BotConstants.NEWLINE);

        output.append(BotConstants.getUserRoles());
        output.append(BotConstants.NEWLINE);

        output.append(BotConstants.getUserId());
        output.append(BotConstants.NEWLINE);

        conversation.getFnBody().accept(this, output, errors);
        output.append(BotConstants.NEWLINE);
        output.append(BotConstants.CLOSING_BRACKET);
        output.append(BotConstants.NEWLINE);

        scopeStack.pop();
        return null;
    }

    @Override
    public Void visit(Stop stop, StringBuilder output, StringBuilder errors) {
        output.append("break;");
        return null;
    }

    @Override
    public Void visit(IncDec incDec, StringBuilder output, StringBuilder errors) {
        String varName = incDec.getCounterName().getVarName();

        // Cannot increment/decrement a variable that does not exist or is out of scope.
        if (!variableNames.containsKey(varName) || !scopeStack.contains(variableNames.get(varName))) {
            errors.append(String.format("Cannot increment or decrement COUNTER %s as it's not initialized or is out of scope", varName));
            return null;
        }

        if (incDec.isInc()) {
            output.append(BotConstants.createIncCounter(varName));
        } else {
            output.append(BotConstants.createDecCounter(varName));
        }

        output.append(BotConstants.NEWLINE);

        return null;
    }

    @Override
    public Void visit(FnCall fnCall, StringBuilder output, StringBuilder errors) {
        String conversationName = fnCall.getFnName();

        if (!conversationNames.contains(conversationName)) {
            errors.append("Cannot call conversation " + conversationName + " as it has not been defined.");
            return null;
        }

        output.append(BotConstants.createFunctionalCall(conversationName));
        return null;
    }

    @Override
    public Void visit(FnBody fnBody, StringBuilder output, StringBuilder errors) {
        List<FnStatement> statements = fnBody.getStatements();

        for (FnStatement statement : statements) {
            statement.accept(this, output, errors);
            if(!errors.isEmpty()) {
                return null; // terminate evaluation if error occurred while processing statement
            }
            output.append(BotConstants.NEWLINE);
        }

        return null;
    }

    @Override
    public Void visit(CondChain condChain, StringBuilder output, StringBuilder errors) {
        currentIfStatementCount = 0;
        List<IfStatement> ifs = condChain.getIfs();
        for (IfStatement ifElement : ifs) {
            ifElement.accept(this, output, errors);
            if(!errors.isEmpty()) {
                return null; // terminate evaluation if error occurred
            }
            output.append(BotConstants.NEWLINE);
        }

        FnBody elseElement = condChain.getElseStatement();
        if (elseElement != null) {
            scopeStack.push("else$" + elseElement.hashCode());
            output.append("else {");
            elseElement.accept(this, output, errors);
            if(!errors.isEmpty()) {
                return null; // terminate evaluation if error occurred
            }
            output.append("}");
            scopeStack.pop();
            return null;
        }

        return null;
    }

    @Override
    public Void visit(While whileNode, StringBuilder output, StringBuilder errors) {
        scopeStack.push("while$" + whileNode.hashCode());
        // add code here
        output.append("while");
        Comparee condition = whileNode.getCondition();

        if (condition instanceof Constant) {
            output.append("(");
            condition.accept(this, output, errors);
            if (!errors.isEmpty()) return null;
            output.append(") {");
            output.append(BotConstants.NEWLINE);
        } else {
            condition.accept(this, output, errors);
            if (!errors.isEmpty()) return null;
            output.append(" {");
            output.append(BotConstants.NEWLINE);
        }

        FnBody ifFnBody = whileNode.getBody();
        ifFnBody.accept(this, output, errors);
        if (!errors.isEmpty()) return null;
        output.append("}");

        scopeStack.pop();
        return null;
    }

    @Override
    public Void visit(IfStatement ifStatement, StringBuilder output, StringBuilder errors) {
        scopeStack.push("if$" + ifStatement.hashCode());

        currentIfStatementCount++;
        if (currentIfStatementCount == 1) {
            output.append("if (");
        } else {
            output.append("else if (");
        }

        Comparee condition = ifStatement.getCondition();
        condition.accept(this, output, errors);
        if (!errors.isEmpty()) return null;
        output.append(") {");
        output.append(BotConstants.NEWLINE);

        FnBody ifFnBody = ifStatement.getBody();
        ifFnBody.accept(this, output, errors);
        if (!errors.isEmpty()) return null;
        output.append("}");

        scopeStack.pop();
        return null;
    }

    @Override
    public Void visit(Message message, StringBuilder output, StringBuilder errors) {
        StringBuilder messageOutput = new StringBuilder();
        message.getOutput().accept(this, messageOutput, errors);
        if (!errors.isEmpty()) return null;

        String messageOutputStr = BotConstants.createSendMessage(messageOutput.toString());
        output.append(messageOutputStr);

        return null;
    }

    @Override
    public Void visit(Form form, StringBuilder output, StringBuilder errors) {
        String responseVarName = form.getResponseVar().getVarName();

        // check if variable has already been initialized in a higher or equal scope
        boolean firstInit = !(variableNames.containsKey(responseVarName) && scopeStack.contains(variableNames.get(responseVarName)));
        output.append(BotConstants.createResponseVarInit(responseVarName, firstInit));

        output.append(BotConstants.NEWLINE);

        output.append(BotConstants.createReceiveResponse(responseVarName));
        output.append(BotConstants.NEWLINE);

        return null;
    }

    @Override
    public Void visit(StringBuild stringBuild, StringBuilder output, StringBuilder errors) {
        List<Value> values = stringBuild.getValues();

        for (int i = 0; i < values.size(); i++) {
            Value value = values.get(i);
            value.accept(this, output, errors);
            if(!errors.isEmpty()) return null;

            if (i < values.size() - 1) {
                output.append(" + ");
            }
        }

        return null;
    }

    @Override
    public Void visit(Constant constant, StringBuilder output, StringBuilder errors) {
        if (constant.getType() == Type.TEXT) {
            output.append("\"");
            output.append(constant.getValue());
            output.append("\"");
        } else if (constant.getType() == Type.BOOLEAN) {
            output.append(constant.getValue().toLowerCase());
        } else {
            errors.append("Error: constant value of unknown type. Stacktrace: \n" + getStackTrace());
            return null;
        }

        return null;
    }

    @Override
    public Void visit(TextVar textVar, StringBuilder output, StringBuilder errors) {
        String varName = textVar.getVarName();
        variableNames.put(varName, scopeStack.peek());

        // Cannot reference a string variable that does not exist or is out of scope.
        if (!varName.contains("@User") &&
                (!variableNames.containsKey(varName) )) {
            errors.append(String.format("Cannot reference variable %s as it's not initialized or is out of scope", varName));
            return null;
        }

        if(varName.equals("@User")){
            output.append("userName$");
            return null;
        }

        if (varName.contains("@User")){
            if (varName.contains("id")){
                output.append("userId$");
                return null;
            } else if(varName.contains("role")) {
                output.append("userRole$");
                return  null;
            } else {
                errors.append("User variable " + varName + " not supported." +
                        " Use one of the following: @User, @User.id, @User.role");
            }
        }

        output.append(varName);
        return null;
    }

    @Override
    public Void visit(CounterVar counterVar, StringBuilder output, StringBuilder errors) {
        String varName = counterVar.getVarName();
        // Cannot reference a counter variable that does not exist or is out of scope.
        if (!variableNames.containsKey(varName) || !scopeStack.contains(variableNames.get(varName))) {
            errors.append(String.format("Cannot reference counter variable %s as it's not initialized or is out of scope", varName));
            return null;
        }

        output.append(varName);
        return null;
    }

    @Override
    public Void visit(Comparator comparator, StringBuilder output, StringBuilder errors) {
        Operator op = comparator.getOperator();
        Comparee lhs = comparator.getLhs();
        Comparee rhs = comparator.getRhs();
        // START
        output.append("(");
        // IF the operator is NOT we do not look at the LHS
        if (op == Operator.NOT) {
            output.append("!");
            rhs.accept(this, output, errors);
            if (!errors.isEmpty()) return null;
            output.append(")");
            return null;
        }

        // hacky but turns potential variables to string
        lhs.accept(this, output, errors);
        if (!errors.isEmpty()) return null;
        switch (op) {
            case IS -> output.append("==");
            case AND -> output.append("&&");
            case OR -> output.append("||");
        }

        // Append the RHS
        rhs.accept(this, output, errors);
        if (!errors.isEmpty()) return null;

        // End
        output.append(")");
        return null;
    }

    public String getStackTrace() {
        String ret = "";
        for (String s : scopeStack) {
            ret += s;
            ret += "\n";
        }
        return ret;
    }

}
