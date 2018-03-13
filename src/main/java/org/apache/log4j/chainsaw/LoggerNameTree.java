/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 */
package org.apache.log4j.chainsaw;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;


/**
 * LoggerNameTree is used to display a TreeModel of LoggerNames.
 *
 * @author Paul Smith &lt;psmith@apache.org&gt;
 */
public class LoggerNameTree extends JTree {
    LoggerNameTree(TreeModel model) {
        super(model);


        //    ============================================
        //    TODO remove this WIP node once we're statisfied
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

        DefaultMutableTreeNode node =
            new DefaultMutableTreeNode("Work in Progress...");
        node.setAllowsChildren(false);

        root.add(node);


    }


}
