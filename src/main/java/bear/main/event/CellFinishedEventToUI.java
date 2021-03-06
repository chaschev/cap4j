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

package bear.main.event;

import bear.task.TaskResult;

/**
 * @author Andrey Chaschev chaschev@gmail.com
 */
public class CellFinishedEventToUI extends ConsoleEventToUI {
    public TaskResult<?> result;
    public long duration;

    public CellFinishedEventToUI(TaskResult<?> result, long duration, String console) {
        super(console, "cellFinished");
        this.result = result;
        this.duration = duration;
    }

    @Override
    public String getFormattedMessage() {
        return "cellFinished, result=" + result + ", duration: " + String.format("%.2fs", duration *1.0 / 1000);
    }
}
