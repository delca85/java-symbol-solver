package me.tomassetti.examples;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;
import me.tomassetti.symbolsolver.model.resolution.TypeSolver;
import me.tomassetti.symbolsolver.model.typesystem.ReferenceTypeUsage;
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
    static class MixedCalculatorVisitor extends VoidVisitorAdapter<JavaParserFacade> {
       @Override
        public void visit(ReturnStmt n, JavaParserFacade javaParserFacade) {
            super.visit(n, javaParserFacade);
            //System.out.println(n.getExpr().toString() + " has type " + javaParserFacade.getType(n.getExpr()));
        }

       @Override
        public void visit(ExpressionStmt stmt, JavaParserFacade javaParserFacade) {
            super.visit(stmt, javaParserFacade);
            System.out.println(stmt.toString() + " has type " + javaParserFacade.getType(stmt).describe());
            if (javaParserFacade.getType(stmt).isReferenceType()) {
                for (ReferenceTypeUsage ancestor : javaParserFacade.getType(stmt).asReferenceTypeUsage().getAllAncestors()) {
                    //System.out.println("Ancestor " + ancestor.describe());
                }
            }
        }

        @Override
        public void visit(IfStmt ifStmt, JavaParserFacade javaParserFacade) {
            super.visit(ifStmt, javaParserFacade);
            Expression condition = ifStmt.getCondition();
            Statement thenBranch, elseBranch;
            thenBranch = ifStmt.getThenStmt();
            elseBranch = ifStmt.getElseStmt();
            System.out.println(ifStmt.toString() + " has type " + javaParserFacade.getType(ifStmt).describe());
            System.out.println(thenBranch.toStringWithoutComments());
            if (elseBranch != null)
                System.out.println(thenBranch.toStringWithoutComments());
        }
    }

    public static void main(String[] args) throws FileNotFoundException, ParseException {
        TypeSolver typeSolver = new CombinedTypeSolver(new JreTypeSolver(), new JavaParserTypeSolver(new File("java-symbol-solver-examples/src/main/resources/someproject")));

        CompilationUnit agendaCu = JavaParser.parse(new FileInputStream(new File("java-symbol-solver-examples/src/main/resources/someproject/me/tomassetti/MainTest.java")));

        agendaCu.accept(new MixedCalculatorVisitor(), JavaParserFacade.get(typeSolver));

    }

}
