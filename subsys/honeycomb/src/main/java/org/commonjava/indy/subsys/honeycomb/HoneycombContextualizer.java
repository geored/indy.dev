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
package org.commonjava.indy.subsys.honeycomb;

import io.honeycomb.beeline.tracing.Beeline;
import io.honeycomb.beeline.tracing.Span;
import org.commonjava.cdi.util.weft.ThreadContextualizer;
import org.commonjava.indy.subsys.honeycomb.config.HoneycombConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@ApplicationScoped
@Named
public class HoneycombContextualizer
        implements ThreadContextualizer
{
    private static final String THREAD_NAME = "thread.name";

    private static final String THREAD_GROUP_NAME = "thread.group.name";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private static ThreadLocal<Span> SPAN = new ThreadLocal<>();

    @Inject
    private HoneycombManager honeycombManager;

    @Inject
    private HoneycombConfiguration configuration;

    @Inject
    private IndyTracingContext tracingContext;

    @Override
    public String getId()
    {
        return "honeycomb.threadpool.spanner";
    }

    @Override
    public Object extractCurrentContext()
    {
        if ( configuration.isEnabled() )
        {
            Beeline beeline = honeycombManager.getBeeline();
            SpanContext ctx = new SpanContext( beeline.getActiveSpan() );
            logger.trace( "Extracting parent-thread context: {}", ctx );
            return ctx;
        }
        return null;
    }

    @Override
    public void setChildContext( final Object parentContext )
    {
        if ( configuration.isEnabled() )
        {
            tracingContext.reinitThreadSpans();

            logger.trace( "Creating thread-level root span using parent-thread context: {}", parentContext );
            SPAN.set( honeycombManager.startRootTracer( "thread." + Thread.currentThread().getThreadGroup().getName(), (SpanContext) parentContext ) );
        }
    }

    @Override
    public void clearContext()
    {
        if ( configuration.isEnabled() )
        {
            Span span = SPAN.get();
            if ( span != null )
            {
                logger.trace( "Closing thread-level root span: {}", span );
                honeycombManager.addFields( span );
                span.addField( THREAD_NAME, Thread.currentThread().getName() );
                span.addField( THREAD_GROUP_NAME, Thread.currentThread().getThreadGroup().getName() );

                span.close();

                honeycombManager.endTrace();
            }

            SPAN.remove();

            tracingContext.clearThreadSpans();
        }
    }
}
