/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
package com.intellij.ide.favoritesTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.AbstractUrl;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.util.TreeItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class FavoritesListNode extends AbstractTreeNode<String> {
  private final Project myProject;
  private final String myListName;
  private final boolean myAllowsTree;

  public FavoritesListNode(Project project, String listName, boolean tree) {
    super(project, listName);
    myProject = project;
    myListName = listName;
    myAllowsTree = tree;
  }

  public boolean isAllowsTree() {
    return myAllowsTree;
  }

  @NotNull
  @Override
  public Collection<? extends AbstractTreeNode> getChildren() {
    return getFavoritesRoots(myProject, myListName, this);
  }

  @Override
  protected void update(PresentationData presentation) {
    presentation.setIcon(AllIcons.Toolwindows.ToolWindowFavorites);
    presentation.setPresentableText(myListName);
  }
  
  @NotNull public static Collection<AbstractTreeNode> getFavoritesRoots(Project project, String listName, final FavoritesListNode listNode) {
    final Collection<TreeItem<Pair<AbstractUrl,String>>> pairs = FavoritesManager.getInstance(project).getFavoritesListRootUrls(listName);
    if (pairs == null) return Collections.emptyList();
    return createFavoriteRoots(project, pairs, listNode);
  }
  
  @NotNull 
  private static Collection<AbstractTreeNode> createFavoriteRoots(Project project, @NotNull Collection<TreeItem<Pair<AbstractUrl, String>>> urls,
                                                                  final AbstractTreeNode me) {
    Collection<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>();
    processUrls(project, urls, result, me);
    return result;
  }

  private static void processUrls(Project project,
                                  Collection<TreeItem<Pair<AbstractUrl, String>>> urls,
                                  Collection<AbstractTreeNode> result, final AbstractTreeNode me) {
    for (TreeItem<Pair<AbstractUrl, String>> pair : urls) {
      AbstractUrl abstractUrl = pair.getData().getFirst();
      final Object[] path = abstractUrl.createPath(project);
      if (path == null || path.length < 1 || path[0] == null) {
        continue;
      }
      try {
        final String className = pair.getData().getSecond();

        @SuppressWarnings("unchecked")
        final Class<? extends AbstractTreeNode> nodeClass = (Class<? extends AbstractTreeNode>)Class.forName(className);

        final AbstractTreeNode node = ProjectViewNode
          .createTreeNode(nodeClass, project, path[path.length - 1], FavoritesManager.getInstance(project).getViewSettings());
        node.setParent(me);
        node.setIndex(result.size());
        result.add(node);

        if (node instanceof ProjectViewNodeWithChildrenList) {
          final List<TreeItem<Pair<AbstractUrl, String>>> children = pair.getChildren();
          if (children != null && ! children.isEmpty()) {
            Collection<AbstractTreeNode> childList = new ArrayList<AbstractTreeNode>();
            processUrls(project, children, childList, node);
            for (AbstractTreeNode treeNode : childList) {
              ((ProjectViewNodeWithChildrenList)node).addChild(treeNode);
            }
          }
        }
      } catch (Exception ignored) {
      }
    }
  }
} 
