/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.intellij.psi.resolve;

import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.infos.CandidateInfo;
import com.intellij.psi.infos.MethodCandidateInfo;
import com.intellij.psi.scope.PsiConflictResolver;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.scope.conflictResolvers.JavaMethodsConflictResolver;
import com.intellij.psi.scope.conflictResolvers.DuplicateConflictResolver;
import com.intellij.psi.scope.processor.MethodCandidatesProcessor;
import com.intellij.psi.scope.processor.MethodResolverProcessor;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * @author peter
 */
public class JavaMethodResolveHelper {
  private final Set<MethodSignature> myDuplicates = new THashSet<MethodSignature>();

  private final MethodCandidatesProcessor myProcessor;
  @Nullable private final PsiType[] myArgumentTypes;

  public JavaMethodResolveHelper(final PsiElement argumentList, @Nullable final PsiType[] argumentTypes) {
    myArgumentTypes = argumentTypes;
    final LanguageLevel languageLevel = PsiUtil.getLanguageLevel(argumentList);
    final PsiConflictResolver resolver = argumentTypes == null ? DuplicateConflictResolver.INSTANCE : new JavaMethodsConflictResolver(argumentList, argumentTypes);
    myProcessor = new MethodResolverProcessor(argumentList, new PsiConflictResolver[]{resolver}) {
      @Override
      protected MethodCandidateInfo createCandidateInfo(final PsiMethod method, final PsiSubstitutor substitutor,
                                                        final boolean staticProblem,
                                                        final boolean accessible) {
        return JavaMethodResolveHelper.this
          .createCandidateInfo(method, substitutor, staticProblem, myCurrentFileContext, !accessible, argumentList, argumentTypes,
                               languageLevel);
      }

      @Override
      protected boolean isAccepted(final PsiMethod candidate) {
        return !candidate.isConstructor();
      }
    };
  }

  protected MethodCandidateInfo createCandidateInfo(PsiMethod method,
                                                    PsiSubstitutor substitutor,
                                                    boolean staticProblem,
                                                    PsiElement currentFileContext,
                                                    boolean accessProblem,
                                                    PsiElement argumentList,
                                                    PsiType[] argumentTypes, LanguageLevel languageLevel) {
    return new MethodCandidateInfo(method, substitutor, accessProblem, staticProblem, argumentList, currentFileContext, argumentTypes,
                                   PsiType.EMPTY_ARRAY, languageLevel);
  }

  public void addMethod(PsiMethod method, PsiSubstitutor substitutor, boolean staticError) {
    if (myDuplicates.add(method.getSignature(substitutor))) {
      myProcessor.addMethod(method, substitutor, staticError);
    }
  }

  @NotNull
  public ErrorType getResolveError() {
    final CandidateInfo[] candidates = myProcessor.getCandidates();
    if (candidates.length != 1) return ErrorType.RESOLVE;

    if (!candidates[0].isStaticsScopeCorrect()) return ErrorType.STATIC;

    return getResolveError((MethodCandidateInfo)candidates[0]);
  }

  protected ErrorType getResolveError(MethodCandidateInfo info) {
    if (myArgumentTypes == null) return ErrorType.NONE;

    if (!info.isApplicable()) {
      boolean hasNulls = false;
      //noinspection ConstantConditions
      final PsiParameter[] parameters = info.getElement().getParameterList().getParameters();
      if (myArgumentTypes.length == parameters.length) {
        for (int i = 0; i < myArgumentTypes.length; i++) {
          PsiType type = myArgumentTypes[i];
          if (type == null) {
            hasNulls = true;
          }
          else if (!parameters[i].getType().isAssignableFrom(type)) {
            return ErrorType.RESOLVE;
          }
        }
      }
      return hasNulls ? ErrorType.NONE : ErrorType.RESOLVE;
    }
    return ErrorType.NONE;
  }

  public void handleEvent(final PsiScopeProcessor.Event event, final Object associated) {
    myProcessor.handleEvent(event, associated);
  }

  public enum ErrorType {
    NONE, STATIC, RESOLVE
  }

  public Collection<JavaMethodCandidateInfo> getMethods() {
    return ContainerUtil.mapNotNull(myProcessor.getResult(), new Function<JavaResolveResult, JavaMethodCandidateInfo>() {
      @Override
      public JavaMethodCandidateInfo fun(final JavaResolveResult javaResolveResult) {
        return new JavaMethodCandidateInfo((PsiMethod)javaResolveResult.getElement(), javaResolveResult.getSubstitutor());
      }
    });
  }
}
