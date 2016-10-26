/*
 * Copyright 2016 Federico Tomassetti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javaparser.symbolsolver.javassistmodel;

import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.model.declarations.*;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.model.methods.MethodUsage;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.symbolsolver.model.typesystem.Type;
import com.github.javaparser.symbolsolver.model.typesystem.TypeVariable;
import com.github.javaparser.symbolsolver.model.typesystem.Wildcard;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.SignatureAttribute;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavassistMethodDeclaration implements MethodDeclaration {
    private CtMethod ctMethod;
    private TypeSolver typeSolver;

    public JavassistMethodDeclaration(CtMethod ctMethod, TypeSolver typeSolver) {
        this.ctMethod = ctMethod;
        this.typeSolver = typeSolver;
    }

    @Override
    public boolean isDefaultMethod() {
        return ctMethod.getDeclaringClass().isInterface() && !isAbstract();
    }

    @Override
    public String toString() {
        return "JavassistMethodDeclaration{" +
                "ctMethod=" + ctMethod +
                '}';
    }

    @Override
    public String getName() {
        return ctMethod.getName();
    }

    @Override
    public boolean isField() {
        return false;
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public boolean isType() {
        return false;
    }

    @Override
    public TypeDeclaration declaringType() {
        if (ctMethod.getDeclaringClass().isInterface()) {
            return new JavassistInterfaceDeclaration(ctMethod.getDeclaringClass(), typeSolver);
        } else {
            return new JavassistClassDeclaration(ctMethod.getDeclaringClass(), typeSolver);
        }
    }

    @Override
    public Type getReturnType() {
        try {
            return JavassistFactory.typeUsageFor(ctMethod.getReturnType(), typeSolver);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public int getNumberOfParams() {
        try {
            return ctMethod.getParameterTypes().length;
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ParameterDeclaration getParam(int i) {
        try {
            boolean variadic = false;
            if ((ctMethod.getModifiers() & javassist.Modifier.VARARGS) > 0) {
                variadic = i == (ctMethod.getParameterTypes().length - 1);
            }
            if (ctMethod.getGenericSignature() != null) {
                SignatureAttribute.MethodSignature methodSignature = SignatureAttribute.toMethodSignature(ctMethod.getGenericSignature());
                SignatureAttribute.Type signatureType = methodSignature.getParameterTypes()[i];
                return new JavassistParameterDeclaration(signatureTypeToType(signatureType), typeSolver, variadic);
            } else {
                return new JavassistParameterDeclaration(ctMethod.getParameterTypes()[0], typeSolver, variadic);
            }
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (BadBytecode badBytecode) {
            throw new RuntimeException(badBytecode);
        }
    }

    private Type signatureTypeToType(SignatureAttribute.Type signatureType){
        if (signatureType instanceof SignatureAttribute.ClassType) {
            SignatureAttribute.ClassType classType = (SignatureAttribute.ClassType)signatureType;
            List<Type> typeParameters = Arrays.stream(classType.getTypeArguments()).map(ta -> typeArgumentToType(ta)).collect(Collectors.toList());
            TypeDeclaration typeDeclaration = typeSolver.solveType(classType.getName());
            return new ReferenceTypeImpl(typeDeclaration, typeParameters, typeSolver);
        } else {
            throw new RuntimeException(signatureType.getClass().getCanonicalName());
        }
    }

    private Type objectTypeArgumentToType(SignatureAttribute.ObjectType typeArgument){
        String typeName = typeArgument.jvmTypeName();
        Optional<Type> type = getGenericParameterByName(typeName);
        if (type.isPresent()) {
            return type.get();
        } else {
            throw new UnsupportedOperationException(typeName);
        }
    }

    private Optional<Type> getGenericParameterByName(String typeName) {
        Optional<TypeParameterDeclaration> tp = findTypeParameter(typeName);
        return tp.map(it -> new TypeVariable(it));
    }

    private Type typeArgumentToType(SignatureAttribute.TypeArgument typeArgument){
        if (typeArgument.isWildcard()) {
            if (typeArgument.getType() == null) {
                return Wildcard.UNBOUNDED;
            } else if (typeArgument.getKind() == '+'){
                return Wildcard.extendsBound(objectTypeArgumentToType(typeArgument.getType()));
            } else if (typeArgument.getKind() == '-'){
                return Wildcard.superBound(objectTypeArgumentToType(typeArgument.getType()));
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public MethodUsage getUsage(Node node) {
        throw new UnsupportedOperationException();
    }

    public MethodUsage resolveTypeVariables(Context context, List<Type> parameterTypes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(ctMethod.getModifiers());
    }

    @Override
    public List<TypeParameterDeclaration> getTypeParameters() {
        try {
            if (ctMethod.getGenericSignature() == null) {
                return Collections.emptyList();
            }
            SignatureAttribute.MethodSignature methodSignature = SignatureAttribute.toMethodSignature(ctMethod.getGenericSignature());
            String qualifier = this.getQualifiedName();
            return Arrays.stream(methodSignature.getTypeParameters()).map((jasTp) -> new JavassistTypeParameter(jasTp, false, qualifier, typeSolver)).collect(Collectors.toList());
        } catch (BadBytecode badBytecode) {
            throw new RuntimeException(badBytecode);
        }
    }

    @Override
    public AccessLevel accessLevel() {
        throw new UnsupportedOperationException();
    }
}
