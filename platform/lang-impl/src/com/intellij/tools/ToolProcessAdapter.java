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
package com.intellij.tools;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.WindowManager;

/**
 * @author Eugene Zhuravlev
 * @since Mar 30, 2005
 */
class ToolProcessAdapter extends ProcessAdapter {
  private final Project myProject;
  private final boolean mySynchronizeAfterExecution;
  private final String myName;

  public ToolProcessAdapter(Project project, final boolean synchronizeAfterExecution, final String name) {
    myProject = project;
    mySynchronizeAfterExecution = synchronizeAfterExecution;
    myName = name;
  }

  public void processTerminated(ProcessEvent event) {
    final String message = ToolsBundle.message("tools.completed.message", myName, event.getExitCode());

    if (mySynchronizeAfterExecution) {
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        public void run() {
          VirtualFileManager.getInstance().asyncRefresh(new Runnable() {
            public void run() {
              if (ProjectManagerEx.getInstanceEx().isProjectOpened(myProject)) {
                WindowManager.getInstance().getStatusBar(myProject).setInfo(message);
              }
            }
          });
        }
      });
    }
    if (ProjectManagerEx.getInstanceEx().isProjectOpened(myProject)) {
      WindowManager.getInstance().getStatusBar(myProject).setInfo(message);
    }
  }
}
