package me.tomassetti.examples;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import me.tomassetti.Address;
import me.tomassetti.Agenda;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;
import me.tomassetti.symbolsolver.model.resolution.TypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.JreTypeSolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bibi on 17/10/16.
 */
public class IfCatch {

    static TypeSolver typeSolver = new CombinedTypeSolver(new JreTypeSolver(), new JavaParserTypeSolver(new File("java-symbol-solver-examples/src/main/resources/someproject")));
    static JavaParserFacade javaParserFacade = JavaParserFacade.get(typeSolver);
    static List<Class> classesIfCondition = new ArrayList();
    static class IfCalculatorVisitor extends VoidVisitorAdapter<JavaParserFacade>{

        @Override
        public void visit(IfStmt n, JavaParserFacade arg) {
            super.visit(n, arg);
            Expression condition = n.getCondition();
            if (condition instanceof  MethodCallExpr){
                MethodCallExpr methodCallExprCondition = (MethodCallExpr) condition;
                System.out.println("[IfStmt]Scope: " + javaParserFacade.getType(methodCallExprCondition.getScope()));
                System.out.println("[IfStmt]NameExpr: " + methodCallExprCondition.getNameExpr());
                System.out.println("[IfStmt]Solve simple: " + javaParserFacade.solve(methodCallExprCondition));
                System.out.println("[IfStmt]Solve: " + javaParserFacade.solveMethodAsUsage(methodCallExprCondition).getDeclaration().declaringType().getQualifiedName().equals((List.class).getName()));
            }
            Statement thenBranch = n.getThenStmt();
        /*    for (Node child : thenBranch.getChildrenNodes()) {
                if (child instanceof ExpressionStmt){
                    Expression expr = ((ExpressionStmt) child).getExpression();
                    if (expr instanceof MethodCallExpr) {
                        MethodCallExpr methodChild = (MethodCallExpr) expr;
                        System.out.println("method: " + methodChild.getName() + " scope: " );
                        if (methodChild.getScope().getClass().equals(Address.class))
                            System.out.println(methodChild.getScope() + " => To be annotated!");
                    }
                }
                else
                 System.out.println(child.getClass() + " " + child.toString() + " " + child.getRange());
            }
*/
        }

        /*@Override
        public void visit(MethodCallExpr n, JavaParserFacade arg) {
            super.visit(n, arg);
            System.out.println("[MethodCallExpr]" + n.getName() + " args: " + n.getArgs());
        }*/

        /*@Override
        public void visit(VariableDeclarator n, JavaParserFacade arg) {
            super.visit(n, arg);
            //    System.out.println("[VariableDeclarationExpr]" + varDec + " " + n.getType());
                System.out.println(javaParserFacade.getType(n, false));
        }*/

        /*@Override
        public void visit(ExpressionStmt n, JavaParserFacade arg) {
            super.visit(n, arg);
        }*/

    }

    public static void main(String[] args) throws FileNotFoundException, ParseException {
        classesIfCondition.add(Agenda.class);
        classesIfCondition.add(List.class);
        CompilationUnit agendaCu = JavaParser.parse(new FileInputStream(new File("java-symbol-solver-examples/src/main/resources/someproject/me/tomassetti/MainTest.java")));
        agendaCu.accept(new IfCalculatorVisitor(), JavaParserFacade.get(typeSolver));

    }
}
