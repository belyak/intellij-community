/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.intellij.openapi.vcs.changes.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diff.DiffRequest;
import com.intellij.openapi.diff.SimpleDiffRequest;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/14/12
 * Time: 3:48 PM
 */
public class MultipleDiffRequestPresentable implements DiffRequestPresentable {
  private final Project myProject;
  private final Change myChange;

  public MultipleDiffRequestPresentable(Project project, Change change) {
    myProject = project;
    myChange = change;
  }

  @Override
  public MyResult step(DiffChainContext context) {
    final ChangeForDiffConvertor convertor = new ChangeForDiffConvertor(myProject, false);
    final List<Pair<String,DiffRequestPresentable>> list = new ArrayList<Pair<String,DiffRequestPresentable>>();
    final DiffRequestPresentable requestPresentable = convertor.convert(myChange, false);
    if (requestPresentable != null) {
      list.add(new Pair<String, DiffRequestPresentable>("", requestPresentable));
    }
    final Map<String,Change> layers = myChange.getOtherLayers();
    for (Map.Entry<String, Change> entry : layers.entrySet()) {
      final String key = entry.getKey();
      final Change value = entry.getValue();
      final DiffRequestPresentable additional = convertor.convert(value, true);
      if (additional != null) {
        list.add(new Pair<String, DiffRequestPresentable>(key, additional));
      }
    }

    if (list.isEmpty()) return new MyResult(new SimpleDiffRequest(myProject, ""), DiffPresentationReturnValue.removeFromList);

    DiffRequest request = null;
    for (Pair<String, DiffRequestPresentable> pair : list) {
      final MyResult step = pair.getSecond().step(context);
      if (step == null) continue;
      final DiffPresentationReturnValue returnValue = step.getReturnValue();
      if (DiffPresentationReturnValue.quit.equals(returnValue)) {
        return new MyResult(new SimpleDiffRequest(myProject, ""), DiffPresentationReturnValue.quit);
      } else if (! DiffPresentationReturnValue.removeFromList.equals(returnValue)) {
        // use contents
        if (request == null) {
          request = step.getRequest();
          if (! StringUtil.isEmptyOrSpaces(pair.getFirst())) {
            request.setWindowTitle(pair.getFirst() + " " + request.getWindowTitle());
          }
        } else {
          request.addOtherLayer(pair.getFirst(), step.getRequest());
        }
      }
    }
    if (request == null) {
      return new MyResult(new SimpleDiffRequest(myProject, ""), DiffPresentationReturnValue.removeFromList);
    }
    return new MyResult(request, DiffPresentationReturnValue.useRequest);
  }

  @Override
  public void haveStuff() throws VcsException {
  }

  @Override
  public List<? extends AnAction> createActions(DiffExtendUIFactory uiFactory) {
    return Collections.emptyList();
  }

  @Override
  public String getPathPresentation() {
    return ChangesUtil.getFilePath(myChange).getPath();
  }
}
