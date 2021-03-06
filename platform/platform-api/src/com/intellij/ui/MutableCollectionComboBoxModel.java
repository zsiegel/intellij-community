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
package com.intellij.ui;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author traff
 */
public class MutableCollectionComboBoxModel extends AbstractCollectionComboBoxModel {
  private List myItems;

  public MutableCollectionComboBoxModel(List items, Object selection) {
    super(selection);
    myItems = items;
  }

  public MutableCollectionComboBoxModel(List items) {
    super(items.isEmpty() ? null : items.get(0));
    myItems = items;
  }

  @NotNull
  @Override
  final protected List getItems() {
    return myItems;
  }

  public void update(List items) {
    myItems = items;
    super.update();
  }
}
