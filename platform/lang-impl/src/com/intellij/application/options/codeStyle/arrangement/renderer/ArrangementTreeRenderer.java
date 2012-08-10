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
package com.intellij.application.options.codeStyle.arrangement.renderer;

import com.intellij.psi.codeStyle.arrangement.model.HierarchicalArrangementSettingsNode;
import com.intellij.psi.codeStyle.arrangement.settings.ArrangementStandardSettingsAware;
import gnu.trove.TIntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * // TODO den add doc
 * 
 * @author Denis Zhdanov
 * @since 8/8/12 12:21 PM
 */
public class ArrangementTreeRenderer implements TreeCellRenderer, MouseListener, MouseMotionListener {

  @NotNull private final TIntObjectHashMap<Component> myRowRenderers = new TIntObjectHashMap<Component>();
  @NotNull private final ArrangementNodeRenderingContext myContext;
  @Nullable JComponent myComponentUnderMouse;

  public ArrangementTreeRenderer(@NotNull ArrangementStandardSettingsAware filter) {
    myContext = new ArrangementNodeRenderingContext(filter);
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree,
                                                Object value,
                                                boolean selected,
                                                boolean expanded,
                                                boolean leaf,
                                                int row,
                                                boolean hasFocus)
  {
    Component result = myRowRenderers.get(row);
    if (result == null) {
      HierarchicalArrangementSettingsNode node = (HierarchicalArrangementSettingsNode)((DefaultMutableTreeNode)value).getUserObject();
      result = myContext.getRenderer(node.getCurrent()).getRendererComponent(node.getCurrent());
      myRowRenderers.put(row, result);
    }
    return result;
  }

  public void onTreeRepaintStart() {
    myRowRenderers.clear();
    myContext.reset();
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mouseDragged(MouseEvent e) {
  }

  @Override
  public void mouseMoved(MouseEvent e) {
  }
}