/*
 * Copyright (C) 2013 Andrey Chaschev.
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

package bear.task;

import bear.core.SessionContext;

/**
* @author Andrey Chaschev chaschev@gmail.com
*/
public abstract class InstallationTask<TASK_DEF extends InstallationTaskDef> extends Task<Object, TaskResult<?>> {
    private boolean wasInsideInstallationBefore;

    public InstallationTask(Task<Object, TaskResult<?>> parent, TASK_DEF def, SessionContext $) {
        super(parent, def, $);
    }

    /**
     * Returns a dependency to check that this tool is installed.
     */
    public abstract Dependency asInstalledDependency();

    @Override
    protected void beforeExec() {
        wasInsideInstallationBefore = $.var(getBear().installationInProgress);
        if(!wasInsideInstallationBefore){
            $.put(getBear().installationInProgress, true);
        }
    }

    @Override
    protected void afterExec() {
        if(!wasInsideInstallationBefore){
            $.removeConst(getBear().installationInProgress);
        }
    }

    private static final InstallationTask<?> NOP_TASK = new InstallationTask<InstallationTaskDef>(null, null, null) {
        @Override
        public Dependency asInstalledDependency() {
            return Dependency.NONE;
        }

        @Override
        protected TaskResult<?> exec(SessionRunner runner) {
            return TaskResult.OK;
        }
    };

    public static InstallationTask nop() {
        return NOP_TASK;
    }

}
