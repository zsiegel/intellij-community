/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package com.intellij.execution.junit;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.execution.configurations.ConfigurationUtil;
import com.intellij.execution.testframework.SourceScope;
import com.intellij.ide.util.ClassFilter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TestClassFilter implements ClassFilter.ClassFilterWithScope {
  private final PsiClass myBase;
  private final Project myProject;
  private final GlobalSearchScope myScope;

  private TestClassFilter(@NotNull PsiClass base, final GlobalSearchScope scope) {
    myBase = base;
    myProject = base.getProject();
    myScope = scope;
  }

  public PsiManager getPsiManager() { return PsiManager.getInstance(myProject); }

  public Project getProject() { return myProject; }

  public boolean isAccepted(final PsiClass aClass) {
    return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
      @Override
      public Boolean compute() {
        return aClass.getQualifiedName() != null &&
               ConfigurationUtil.PUBLIC_INSTANTIATABLE_CLASS.value(aClass) &&
               (aClass.isInheritor(myBase, true) || JUnitUtil.isTestClass(aClass))
               && !CompilerConfiguration.getInstance(getProject()).isExcludedFromCompilation(PsiUtilCore.getVirtualFile(aClass)); 
      }
    });
  }

  public TestClassFilter intersectionWith(final GlobalSearchScope scope) {
    return new TestClassFilter(myBase, myScope.intersectWith(scope));
  }

  public static TestClassFilter create(final SourceScope sourceScope, Module module) throws JUnitUtil.NoJUnitException {
    if (sourceScope == null) throw new JUnitUtil.NoJUnitException();
    PsiClass testCase = module == null ? JUnitUtil.getTestCaseClass(sourceScope) : JUnitUtil.getTestCaseClass(module);
    return new TestClassFilter(testCase, sourceScope.getGlobalSearchScope());
  }

  public static TestClassFilter create(final SourceScope sourceScope, Module module, final String pattern) throws JUnitUtil.NoJUnitException {
    if (sourceScope == null) throw new JUnitUtil.NoJUnitException();
    PsiClass testCase = module == null ? JUnitUtil.getTestCaseClass(sourceScope) : JUnitUtil.getTestCaseClass(module);
    final String[] patterns = pattern.split("\\|\\|");
    final List<Pattern> compilePatterns = new ArrayList<Pattern>();
    for (String p : patterns) {
      final Pattern compilePattern = getCompilePattern(p);
      if (compilePattern != null) {
        compilePatterns.add(compilePattern);
      }
    }
    return new TestClassFilter(testCase, sourceScope.getGlobalSearchScope()){
      @Override
      public boolean isAccepted(final PsiClass aClass) {
        if (super.isAccepted(aClass)) {
          final String qualifiedName = ApplicationManager.getApplication().runReadAction(new Computable<String>() {
            @Override
            public String compute() {
              return aClass.getQualifiedName();
            }
          });
          for (Pattern compilePattern : compilePatterns) {
            if (compilePattern.matcher(qualifiedName).matches()) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }

  private static Pattern getCompilePattern(String pattern) {
    Pattern compilePattern;
    try {
      compilePattern = Pattern.compile(pattern.trim());
    }
    catch (PatternSyntaxException e) {
      compilePattern = null;
    }
    return compilePattern;
  }

  public GlobalSearchScope getScope() { return myScope; }
  public PsiClass getBase() { return myBase; }
}
