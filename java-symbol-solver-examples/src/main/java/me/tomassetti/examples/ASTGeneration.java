package me.tomassetti.examples;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;
import me.tomassetti.symbolsolver.model.resolution.TypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.JreTypeSolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by bibi on 14/10/16.
 */
public class ASTGeneration {
    static TypeSolver typeSolver = new CombinedTypeSolver(new JreTypeSolver(), new JavaParserTypeSolver(new File("java-symbol-solver-examples/src/main/resources/someproject")));

    static class MixedCalculatorVisitor extends VoidVisitorAdapter<JavaParserFacade> {
/*       @Override
        public void visit(ReturnStmt n, JavaParserFacade javaParserFacade) {
            super.visit(n, javaParserFacade);
            System.out.println("[ReturnStmt]"+ n.getExpr().toString() + " has type " + javaParserFacade.getType(n.getExpr()));
        }

       @Override
        public void visit(ExpressionStmt stmt, JavaParserFacade javaParserFacade) {
            super.visit(stmt, javaParserFacade);
            System.out.println(stmt.toString() + " range: " + stmt.getRange().toString());
        }*/


        @Override
        public void visit(AssignExpr n, JavaParserFacade arg) {
            super.visit(n, arg);
            System.out.println("[AssignExpr]target: " + n.getTarget() + " value: " + n.getValue());
        }

        @Override
        public void visit(VariableDeclarator n, JavaParserFacade arg) {
            super.visit(n, arg);
            String id = n.getId().getName();
            Expression init = n.getInit();
            System.out.print("[VariableDeclarator]id: " + id);
            if (init == null)
                System.out.println();
            if (init instanceof ObjectCreationExpr)
                this.visit((ObjectCreationExpr) init, arg);
            if (init instanceof MethodCallExpr)
                this.visit((MethodCallExpr) init, arg);
        }

        @Override
        public void visit(ClassExpr n, JavaParserFacade arg) {
            super.visit(n, arg);
            System.out.println("[ClassExpr]" + n.getType().toString());
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, JavaParserFacade arg) {
            super.visit(n, arg);
            System.out.println("[ClassOrInterfaceDeclaration]" + n.getName());

        }

        @Override
        public void visit(MethodCallExpr n, JavaParserFacade arg) {
            super.visit(n, arg);
            System.out.println("[MethodCallExpr]" + n.getName() + " args: " + n.getArgs());
        }

        @Override
        public void visit(ObjectCreationExpr n, JavaParserFacade arg) {
            super.visit(n, arg);
            System.out.println("[ObjectCreationExpr]" + n.getType() + " " + n.getArgs());
        }

        @Override
        public void visit(BinaryExpr n, JavaParserFacade arg) {
            super.visit(n, arg);
            System.out.println("[BinaryExpr]" + n.getLeft() + " " + n.getOperator() + " " + n.getRight());
        }

        @Override
        public void visit(IfStmt n, JavaParserFacade arg) {
            super.visit(n, arg);
            Expression condition = n.getCondition();
            Statement thenBranch, elseBranch;
            thenBranch = n.getThenStmt();
            elseBranch = n.getElseStmt();
            System.out.println("[IfStmt]condition: "+ condition.toString());
            if (condition instanceof BinaryExpr)
                this.visit((BinaryExpr) condition, arg);
            if (condition instanceof MethodCallExpr)
                this.visit((MethodCallExpr) condition, arg);
            System.out.println("[IfStmt]then branch: " + thenBranch.toStringWithoutComments());
            if (elseBranch != null)
                System.out.println("[IfStmt]else branch: " + elseBranch.toStringWithoutComments());
        }

    }

    public static void main(String[] args) throws FileNotFoundException, ParseException {
//        TypeSolver typeSolver = new CombinedTypeSolver(new JreTypeSolver(), new JavaParserTypeSolver(new File("java-symbol-solver-examples/src/main/resources/someproject")));

        CompilationUnit agendaCu = JavaParser.parse(new FileInputStream(new File("java-symbol-solver-examples/src/main/resources/someproject/me/tomassetti/MainTest.java")));

        agendaCu.accept(new MixedCalculatorVisitor(), JavaParserFacade.get(typeSolver));

    }

}
