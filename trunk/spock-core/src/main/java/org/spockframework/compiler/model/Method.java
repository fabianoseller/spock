/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.compiler.model;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.Statement;

import org.spockframework.compiler.AstUtil;

/**
 * AST node representing a Speck method (one of fixture method, feature method, helper method).
 * @author Peter Niederwieser
 */
public abstract class Method extends Node<Speck, MethodNode> {
  private Block firstBlock;
  private Block lastBlock;

  public Method(Speck parent, MethodNode code) {
    super(parent, code);
    setName(code.getName());
  }

  // Class members

  public Block getFirstBlock() {
    return firstBlock;
  }

  public Block getLastBlock() {
    return lastBlock;
  }

  public List<Statement> getStatements() {
    return AstUtil.getStatements(getAst());
  }

  public Iterable<Block> getBlocks() {
    return new BlockIterable(firstBlock);
  }

  @Override
  public void accept(ISpeckVisitor visitor) throws Exception {
    visitor.visitMethod(this);
    for (Block b: getBlocks()) b.accept(visitor);
    visitor.visitMethodAgain(this);
  }

  public Block addBlock(Block block) {
    if (firstBlock == null)
      firstBlock = lastBlock = block;
    else {
      lastBlock.setNext(block);
      block.setPrevious(lastBlock);
      lastBlock = block;
    }
    return block;
  }
}

class BlockIterable implements Iterable<Block> {
  private final Block first;

  BlockIterable(Block first) {
    this.first = first;
  }

  public Iterator<Block> iterator() {
    return new Iterator<Block>() {
      Block block = first;

      public boolean hasNext() {
        return block != null;
      }

      public Block next() {
        if (hasNext()) {
          Block result = block;
          block = block.getNext();
          return result;
        } else
          throw new NoSuchElementException();
      }

      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    };
  }
}
