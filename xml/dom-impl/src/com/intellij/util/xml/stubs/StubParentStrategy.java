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
package com.intellij.util.xml.stubs;

import com.intellij.psi.xml.XmlElement;
import com.intellij.util.xml.impl.DomInvocationHandler;
import com.intellij.util.xml.impl.DomParentStrategy;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 *         Date: 8/9/12
 */
public class StubParentStrategy implements DomParentStrategy {

  private final DomStub myStub;

  public StubParentStrategy(DomStub stub) {
    myStub = stub;
  }

  @Override
  public DomInvocationHandler getParentHandler() {
    DomStub parentStub = myStub.getParentStub();
    return parentStub == null ? null : parentStub.getHandler();
  }

  @Override
  public XmlElement getXmlElement() {

    return null;
  }

  @NotNull
  @Override
  public DomParentStrategy refreshStrategy(DomInvocationHandler handler) {
    return this;
  }

  @NotNull
  @Override
  public DomParentStrategy setXmlElement(@NotNull XmlElement element) {
    return this;
  }

  @NotNull
  @Override
  public DomParentStrategy clearXmlElement() {
    return this;
  }

  @Override
  public String checkValidity() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}