/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
package com.jetbrains.python.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.psi.PyForPart;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.PyWithStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Try to share logic with AssignmentToForLoopParameterInspection

/**
 * Checks for cases like
 * <pre>
 *   for i in range(1, 10):
 *    i = "new value"
 * </pre>
 * and
 * <pre>
 * with open("file") as f:
 *  f.read()
 *  f = open("another file")
 * </pre>
 *
 * @author link
 */
public class PyAssignmentToLoopOrWithParameterInspection extends PyInspection {

  private static final String MESSAGE = PyBundle.message("INSP.NAME.assignment.to.loop.or.with.parameter.display.name");

  @NotNull
  @Override
  public String getDisplayName() {
    return MESSAGE;
  }


  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder,
                                        boolean isOnTheFly,
                                        @NotNull final LocalInspectionToolSession session) {
    return new Visitor(holder, session);
  }

  private static class Visitor extends PyInspectionVisitor {
    private Visitor(@Nullable ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
      super(holder, session);
    }

    @Override
    public void visitPyTargetExpression(PyTargetExpression node) {
      PsiElement variableDeclaration = node.getReference().resolve();
      if (variableDeclaration == null) {
        return;
      }
      PsiElement variableFirstTimeDeclaration = variableDeclaration.getParent();

      if (variableFirstTimeDeclaration.equals(node.getParent())) {
        return; //We are checking first time declaration
      }

      //Check if variable declared in "for" or "with" statement
      if (PsiTreeUtil.getNonStrictParentOfType(variableFirstTimeDeclaration, PyForPart.class, PyWithStatement.class) != null) {
        registerProblem(node, MESSAGE);
      }
    }
  }
}
