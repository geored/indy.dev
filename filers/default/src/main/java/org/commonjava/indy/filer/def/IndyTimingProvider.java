/**
 * Copyright (C) 2011-2020 Red Hat, Inc. (https://github.com/Commonjava/indy)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.filer.def;

import org.commonjava.indy.metrics.IndyMetricsManager;
import org.commonjava.maven.galley.spi.metrics.TimingProvider;

public class IndyTimingProvider
        implements TimingProvider
{
    private final String name;

    private final IndyMetricsManager metricsManager;

    public IndyTimingProvider( final String name, final IndyMetricsManager metricsManager )
    {
        this.name = name;
        this.metricsManager = metricsManager;
    }

    @Override
    public void start( final String name )
    {
        metricsManager.startTimer( name );
    }

    @Override
    public long stop()
    {
        return metricsManager.stopTimer( name );
    }
}
